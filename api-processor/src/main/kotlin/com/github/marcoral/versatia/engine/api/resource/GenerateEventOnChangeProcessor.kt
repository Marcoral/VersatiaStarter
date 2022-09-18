package com.github.marcoral.versatia.engine.api.resource

import com.github.marcoral.versatia.engine.api.VersatiaEngineDevtools
import com.github.marcoral.versatia.engine.api.util.findNotEmptyString
import com.github.marcoral.versatia.engine.api.util.findValueOf
import com.github.marcoral.versatia.engine.api.util.getAnnotationsDeclarationsByType
import com.github.marcoral.versatia.engine.api.util.processAnnotated
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class GenerateEventOnChangeProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = GenerateEventOnChangeProcessor(environment.logger, environment.codeGenerator)
}

class GenerateEventOnChangeProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> = resolver.processAnnotated(InvokeGeneratedEventOnChange::class) {
        forEach { it.accept(Visitor(), Unit) }
    }

    private inner class Visitor: KSVisitorVoid() {
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            if(!handleValidateProperty(property))
                return

            val generateEventsAnnotation = property.parentDeclaration!!.getAnnotationsDeclarationsByType(GenerateEvents::class).first()

            val eventClassName = getNameForEvent(generateEventsAnnotation, property)
            val cancellable = property.getAnnotationsDeclarationsByType(InvokeGeneratedEventOnChange::class).first()
                .findValueOf("cancellable") as Boolean

            val propertyTypeName = property.type.toTypeName()
            val eventTypeSpec = GenerateEventOnChangeGeneratedModel_EventClass(eventClassName, propertyTypeName, cancellable).asTypeSpec

            val creatorPackageName = property.packageName.asString()
            val packageName = generateEventsAnnotation?.findNotEmptyString("packageName")
                ?: creatorPackageName

            //Event file
            FileSpec.builder(packageName, eventClassName)
                .addType(eventTypeSpec).build()
                .writeTo(codeGenerator, false)

            //Event creator file
            val eventCreatorClassName = VersatiaEngineDevtools.getNameForEventCreator(eventClassName)
            FileSpec.builder(creatorPackageName, eventCreatorClassName)
                .addType(GenerateEventOnChangeGeneratedModel_EventCreatorClass(eventCreatorClassName, propertyTypeName, ClassName(packageName, eventTypeSpec.name!!)).asTypeSpec).build()
                .writeTo(codeGenerator, false)
        }

        private fun getNameForEvent(generateEventsAnnotation: KSAnnotation?, property: KSPropertyDeclaration): String {
            //TODO issues/11: Check it on class, not every single property
            val classNameSimple = property.parentDeclaration!!.simpleName.asString()

            val className = generateEventsAnnotation
                ?.findNotEmptyString("overrideClassName")
                ?: classNameSimple

            val fieldName = property.getAnnotationsDeclarationsByType(InvokeGeneratedEventOnChange::class).first()
                .findNotEmptyString("overrideFieldName")
                ?: property.simpleName.asString()

            return VersatiaEngineDevtools.getNameForEvent(className, fieldName)
        }

        private fun handleValidateProperty(property: KSPropertyDeclaration): Boolean {
            var isValid = true
            if(!property.isOpen()) {
                logger.error("Property $property of class ${property.closestClassDeclaration()} must be open!")
                isValid = false
            }
            if(!property.isMutable) {
                logger.error("Property $property of class ${property.closestClassDeclaration()} must be mutable!")
                isValid = false
            }
            return isValid
        }
    }

}
