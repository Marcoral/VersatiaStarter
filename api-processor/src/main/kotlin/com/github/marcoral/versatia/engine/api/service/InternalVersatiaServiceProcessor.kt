package com.github.marcoral.versatia.engine.api.service

import com.github.marcoral.versatia.engine.api.service.RegisterVersatiaServiceUtil.handleRegisteringService
import com.github.marcoral.versatia.engine.api.util.findValueOf
import com.github.marcoral.versatia.engine.api.util.getAnnotationsDeclarationsByType
import com.github.marcoral.versatia.engine.api.util.processAnnotated
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

class InternalVersatiaServiceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = InternalVersatiaServiceProcessor(environment.logger, environment.codeGenerator)
}

class InternalVersatiaServiceProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator): SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> = resolver.processAnnotated(VersatiaInternalService::class) {
        forEach { it.accept(Visitor(), Unit) }
    }

    private inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val annotation = classDeclaration.getAnnotationsDeclarationsByType(VersatiaInternalService::class).first()
            val data = RegisterVersatiaServiceAnnotationData(
                annotation.findValueOf("name") as String,
                getProxiedClass(annotation, classDeclaration.asStarProjectedType()),
                classDeclaration.asStarProjectedType(),
                //TODO issues/8: Delete injectTo parameter and hardcode VersatiaEngineInternals::class on its place
                annotation.findValueOf("injectTo") as KSType,
                annotation.findValueOf("dependsOn") as List<KSType>
            )
            handleRegisteringService(data, annotation, codeGenerator, logger)
        }

        private fun getProxiedClass(annotation: KSAnnotation, fallback: KSType) = annotation.findValueOf("type")
            .let { it as KSType }
            .takeIf { it.toClassName() != DEFAULT::class.asClassName() }
            ?: fallback
    }
}
