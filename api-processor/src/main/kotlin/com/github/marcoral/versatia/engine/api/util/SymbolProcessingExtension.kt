package com.github.marcoral.versatia.engine.api.util

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

fun <T: KSNode> process(nodes: Collection<T>, action: List<T>.() -> Unit): List<T> {
    nodes.partition {
        it.validate()
    }.also {
        action.invoke(it.first)
    }.let {
        return it.second
    }
}

//This method is an attempt to hack the getSymbolsWithAnnotation method for all modules (not just sourceset)
fun Resolver.getAnnotationsSubtypesFromAnyModule(annotatedByClass: KClass<*>) = getAnnotationsAnnotated(annotatedByClass)
    .map { it.toDeclaration() }
    .distinct()

//This method is an attempt to hack the getSymbolsWithAnnotation method for all modules (not just sourceset)
private val annotationsAnnotatedCache = mutableMapOf<KClass<*>, List<KSAnnotation>>()
fun Resolver.getAnnotationsAnnotated(annotatedByClass: KClass<*>) =
    annotationsAnnotatedCache.compute(annotatedByClass) { _, alreadyFoundSymbols ->
        val newSymbols = CollectAnnotatedSymbolsVisitor().also { collector ->
            getNewFiles().forEach {
                it.accept(collector, Unit)
            }
        }.symbols
            .flatMap { it.annotations }
            .filter { it.isAnnotatedBy(annotatedByClass) }

        (alreadyFoundSymbols ?: emptyList()).plus(newSymbols)
    }!!

fun KSAnnotation.isAnnotatedBy(annotationClass: KClass<*>) = toDeclaration().annotations.any { it.isAnnotationOfClass(annotationClass) }
fun KSAnnotation.toDeclaration() = (annotationType.resolve().declaration as KSClassDeclaration)

//Taken from com.google.devtools.ksp.visitor.CollectAnnotatedSymbolsVisitor
class CollectAnnotatedSymbolsVisitor(private val inDepth: Boolean = true) : KSVisitorVoid() {
    val symbols = arrayListOf<KSAnnotated>()

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        if (annotated.annotations.any())
            symbols.add(annotated)
    }

    override fun visitFile(file: KSFile, data: Unit) {
        visitAnnotated(file, data)
        file.declarations.forEach { it.accept(this, data) }
    }

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit) {
        if (typeAlias.annotations.any())
            symbols.add(typeAlias)
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        visitAnnotated(classDeclaration, data)
        classDeclaration.typeParameters.forEach { it.accept(this, data) }
        classDeclaration.declarations.forEach { it.accept(this, data) }
    }

    override fun visitPropertyGetter(getter: KSPropertyGetter, data: Unit) {
        visitAnnotated(getter, data)
    }

    override fun visitPropertySetter(setter: KSPropertySetter, data: Unit) {
        setter.parameter.accept(this, data)
        visitAnnotated(setter, data)
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        visitAnnotated(function, data)
        function.typeParameters.forEach { it.accept(this, data) }
        function.parameters.forEach { it.accept(this, data) }
        if (inDepth) {
            function.declarations.forEach { it.accept(this, data) }
        }
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        visitAnnotated(property, data)
        property.typeParameters.forEach { it.accept(this, data) }
        property.getter?.accept(this, data)
        property.setter?.accept(this, data)
    }

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit) {
        visitAnnotated(typeParameter, data)
        super.visitTypeParameter(typeParameter, data)
    }

    override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit) {
        visitAnnotated(valueParameter, data)
    }
}

fun Resolver.processAnnotated(annotationClass: KClass<*>, action: List<KSAnnotated>.() -> Unit) =
    process(getSymbolsWithAnnotation(annotationClass.java.canonicalName).toList(), action)
fun Resolver.processAnnotated(annotationClassQualifiedName: String, action: List<KSAnnotated>.() -> Unit) =
    process(getSymbolsWithAnnotation(annotationClassQualifiedName).toList(), action)

fun Resolver.getAllSubclassesOf(canonicalName: String) = getAllFiles().map { it.declarations }
    //TODO issues/10: Process also inner declarations
    .flatMap { it }
    .filterIsInstance(KSClassDeclaration::class.java)
    .filter {
        it.getAllSuperTypes().any {
            //TODO issues/9: Simplify it if possible
            it.declaration.qualifiedName!!.asString() == canonicalName
        }
    }.toList()
fun Resolver.getAllSubclassesOf(clazz: Class<*>) = getAllSubclassesOf(clazz.canonicalName)
fun KSAnnotated.getAnnotationsDeclarationsByType(annotationClass: KClass<*>) = annotations.filter {
    it.isAnnotationOfClass(annotationClass)
}

fun KSAnnotation.isAnnotationOfClass(annotationClass: KClass<*>) = shortName.getShortName() == annotationClass.simpleName && annotationType.resolve().declaration
    .qualifiedName?.asString() == annotationClass.qualifiedName

fun KSAnnotation.isAnnotationOfClass(ksClassDeclaration: KSClassDeclaration) = shortName.getShortName() == ksClassDeclaration.simpleName.getShortName() && annotationType.resolve().declaration
    .qualifiedName == ksClassDeclaration.qualifiedName

fun KSAnnotation.findValueOf(fieldName: String) = arguments.first { arg -> arg.name?.asString() == fieldName }.value

fun KSAnnotation.findNotEmptyString(fieldName: String) = findValueOf(fieldName)
    ?.let { it as String }
    ?.takeIf { it.isNotEmpty() }

fun List<KSType>.toCodeBlock() = map { CodeBlock.of("%T::class", it.toClassName()) }
    .joinToCode()
