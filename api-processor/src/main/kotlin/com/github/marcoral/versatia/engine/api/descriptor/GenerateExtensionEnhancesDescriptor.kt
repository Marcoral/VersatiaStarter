package com.github.marcoral.versatia.engine.api.descriptor

import com.github.marcoral.versatia.engine.api.util.*
import com.google.common.base.Objects
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass

//TODO issues/6: Add validations (e.g. whether element is visible i.e. it is not private)
class GeneratePluginEnhancementsDescriptorProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = GeneratePluginEnhancementsDescriptor(environment.logger, environment.codeGenerator)
}

private const val CURRENT_VERSION = 1L
class GeneratePluginEnhancementsDescriptor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private lateinit var pluginClassDeclaration: KSClassDeclaration

    private val annotatedElements = mutableMapOf<String, Set<VersatiaEnhancementInstance>>()
    data class VersatiaEnhancementInstance(val annotatedNode: KSNode, val origin: KSAnnotation) {
        override fun equals(other: Any?) =
            if(other !is VersatiaEnhancementInstance)
                false
            else
                annotatedNode.location == other.annotatedNode.location
                        && origin.location == other.origin.location

        override fun hashCode() = Objects.hashCode(annotatedNode.location, origin.location)
    }

    //TODO issues/1: Refactor code in GeneratePluginEnhancementsDescriptorProcessorProvider
    override fun process(resolver: Resolver): List<KSAnnotated> {
        fun addAnnotatedElements(enhancementAnnotationClass: KSClassDeclaration, enhancerClass: KClass<*>, enhancements: Collection<VersatiaEnhancementInstance>) {
            enhancementAnnotationClass.getAnnotationsDeclarationsByType(enhancerClass).map {
                it.findNotEmptyString("enhancementProcessorQualifiedName")!!
            }.forEach {
                annotatedElements.compute(it) { _, value ->
                    value?.plus(enhancements) ?: enhancements.toSet()
                }
            }
        }

        fun processAndReturnEnhancementsInstances(enhancementAnnotationClass: KSClassDeclaration): List<KSAnnotated> {
            fun getEnhancementInstancesOfAnnotatedElement(enhancementAnnotationClass: KSClassDeclaration, annotated: KSAnnotated)
                = annotated.annotations
                    .filter { it.isAnnotationOfClass(enhancementAnnotationClass) }
                    .map {
                        VersatiaEnhancementInstance(annotated, it)
                    }

            return resolver.processAnnotated(enhancementAnnotationClass.qualifiedName!!.asString()) {
                addAnnotatedElements(
                    enhancementAnnotationClass,
                    VersatiaEnhancement::class,
                    this.flatMap { getEnhancementInstancesOfAnnotatedElement(enhancementAnnotationClass, it) }
                )
            }
        }

        fun processSubclassesToEnhanceByAnnotation(subclassesEnhancementAnnotation: KSAnnotation)
            = subclassesEnhancementAnnotation.toDeclaration()
                .getAnnotationsDeclarationsByType(VersatiaEnhancementForSubclasses::class).map {
                    val baseClass = it.findValueOf("baseClassToEnhance") as KSType
                    process(resolver.getAllSubclassesOf(baseClass.declaration.qualifiedName!!.asString())) {
                        addAnnotatedElements(
                            subclassesEnhancementAnnotation.toDeclaration(),
                            VersatiaEnhancementForSubclasses::class,
                            this.map { VersatiaEnhancementInstance(it, subclassesEnhancementAnnotation) }
                        )
                    }
                }.flatten().toList()

        //TODO issues/7: Rewrite it so that it pulls the version from plugins.yml - it would be far better
        fun Resolver.getPluginClassDeclaration(): KSClassDeclaration {
            with(getAllSubclassesOf(JavaPlugin::class.java)) {
                when(size) {
                    0 -> logger.error("No class that extends JavaPlugin have been found!")
                    1 -> return first()
                    else -> logger.error("Multiple classes that extends JavaPlugin have been found! It is not allowed at the moment :(")
                }
            }
            throw IllegalStateException()
        }

        pluginClassDeclaration = resolver.getPluginClassDeclaration()

        return resolver.getAnnotationsSubtypesFromAnyModule(VersatiaEnhancement::class).asSequence()
            .map { enhancementAnnotationClass -> processAndReturnEnhancementsInstances(enhancementAnnotationClass) }
            .plus(
                resolver.getAnnotationsAnnotated(VersatiaEnhancementForSubclasses::class).map {
                    processSubclassesToEnhanceByAnnotation(it)
                }
            ).flatten()
            .distinct()
            .toList()
    }

    override fun finish() {
        fun buildDescriptorClassName(PluginClassSimpleName: String) = "${PluginClassSimpleName}PluginEnhancementsDescriptor"
        fun buildVersionDescriptorMethod() = FunSpec.builder("version")
            .addStatement("return %LL", CURRENT_VERSION)
            .build()
        fun buildEnhancedElementsFunction(): FunSpec {
            fun getBuilderForEnhancedElementsFunction(): FunSpec.Builder {
                val annotatedElementDataType = AnnotatedElementData::class.asTypeName().parameterizedBy(ANY)
                val collectionValueType = COLLECTION.parameterizedBy(annotatedElementDataType)
                val returnType = MAP.parameterizedBy(STRING, collectionValueType)

                val builder = FunSpec.builder("enhancedElements")
                builder.returns(returnType)
                return builder
            }

            fun buildEnhancedElementsList(enhancedElements: Collection<VersatiaEnhancementInstance>): CodeBlock {
                fun getClassesFunction(classDeclaration: KSClassDeclaration) = CodeBlock.of("%T::class.java,\n", classDeclaration.toClassName())
                fun getMethodsFunction(methodDeclaration: KSFunctionDeclaration) = CodeBlock.of(
                    "%T::class.java.getDeclaredMethod(\"${methodDeclaration.simpleName.asString()}\"),\n",
                    methodDeclaration.closestClassDeclaration()!!.toClassName()
                )
                fun getFieldsFunction(propertyDeclaration: KSPropertyDeclaration) = CodeBlock.of(
                    "%T::class.java.getDeclaredField(\"${propertyDeclaration.simpleName.asString()}\"),\n",
                    propertyDeclaration.closestClassDeclaration()!!.toClassName()
                )
                fun codeForEnhancedElement(element: Any) =
                    when(element) {
                        is KSPropertyDeclaration -> getFieldsFunction(element)
                        is KSFunctionDeclaration -> getMethodsFunction(element)
                        is KSClassDeclaration -> getClassesFunction(element)
                        else -> throw IllegalArgumentException()
                    }

                fun CodeBlock.Builder.addAnnotationArgumentsMap(arguments: Collection<KSValueArgument>) {
                    fun mapTypeValueArgument(argValue: KSType)
                            = when ((argValue.declaration as KSClassDeclaration).classKind) {
                        ClassKind.ENUM_ENTRY -> CodeBlock.of("%T", argValue.toTypeName())
                        else -> CodeBlock.of("%T::class", argValue.toTypeName())
                    }

                    fun parseArg(argValue: Any?): CodeBlock {
                        return when (argValue) {
                            is Collection<*> -> KotlinPoetUtils.buildCodeBlockListOfElements(
                                ANY,
                                argValue.map { parseArg(it) },
                                "%L"
                            )
                            is KSType -> mapTypeValueArgument(argValue)
                            is String -> CodeBlock.of("%S", argValue)
                            else -> CodeBlock.of("%L", argValue)
                        }
                    }

                    add(KotlinPoetUtils.buildCodeBlockMapOf(
                        STRING,
                        ANY,
                        arguments.map { it.name!!.asString() to parseArg(it.value) },
                        "%S",
                        "%L"
                    ))
                }

                return KotlinPoetUtils.buildCodeBlockListOf(
                    AnnotatedElementData::class.asTypeName().parameterizedBy(ANY),
                    enhancedElements.map {
                        KotlinPoetUtils.buildObject(AnnotatedElementData::class.asTypeName()) {
                            add(codeForEnhancedElement(it.annotatedNode))
                            add("%T::class.java,\n", it.origin.toDeclaration().toClassName())
                            addAnnotationArgumentsMap(it.origin.arguments)
                        }
                    }
                )
            }

            val builder = getBuilderForEnhancedElementsFunction()
            val elements = annotatedElements.map {
                it.key to buildEnhancedElementsList(it.value)
            }

            val valueType = COLLECTION.parameterizedBy(AnnotatedElementData::class.asTypeName().parameterizedBy(ANY))
            builder.addCode("return %L", KotlinPoetUtils.buildCodeBlockMapOf(STRING, valueType, elements, "%S", "%L"))
            return builder.build()
        }

        val packageName = pluginClassDeclaration.packageName.asString()
        val descriptorClassName = buildDescriptorClassName(pluginClassDeclaration.simpleName.asString())
        val descriptor = TypeSpec.objectBuilder(descriptorClassName)
            .addFunction(buildVersionDescriptorMethod())
            .addFunction(buildEnhancedElementsFunction())
            .build()

        FileSpec.get(packageName, descriptor).writeTo(codeGenerator, true)
    }
}