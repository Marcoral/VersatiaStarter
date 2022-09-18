package com.github.marcoral.versatia.engine.api.service

import com.github.marcoral.versatia.engine.api.service.RegisterVersatiaServiceUtil.handleRegisteringService
import com.github.marcoral.versatia.engine.api.util.findValueOf
import com.github.marcoral.versatia.engine.api.util.getAnnotationsDeclarationsByType
import com.github.marcoral.versatia.engine.api.util.processAnnotated
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid

class RegisterVersatiaServiceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = RegisterVersatiaServiceProcessor(environment.logger, environment.codeGenerator)
}

class RegisterVersatiaServiceProcessor(private val logger: KSPLogger, private val codeGenerator: CodeGenerator): SymbolProcessor {
    override fun process(resolver: Resolver) = resolver.processAnnotated(RegisterVersatiaService::class) {
        forEach { it.accept(Visitor(), Unit) }
    }

    private inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val annotation = classDeclaration.getAnnotationsDeclarationsByType(RegisterVersatiaService::class).first()
            val data = RegisterVersatiaServiceAnnotationData(
                annotation.findValueOf("name") as String,
                annotation.findValueOf("proxiedClass") as KSType,
                annotation.findValueOf("proxierClass") as KSType,
                annotation.findValueOf("injectTo") as KSType,
                annotation.findValueOf("dependsOn") as List<KSType>
            )
            handleRegisteringService(data, annotation, codeGenerator, logger)
        }
    }
}
