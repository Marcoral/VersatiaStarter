package com.github.marcoral.versatia.engine.api.service

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import javax.lang.model.SourceVersion

data class RegisterVersatiaServiceAnnotationData(val name: String,
                                                  val proxiedClass: KSType,
                                                  val proxierClass: KSType,
                                                  val injectTo: KSType,
                                                  val dependsOn: List<KSType>)

object RegisterVersatiaServiceUtil {
    fun handleRegisteringService(
        data: RegisterVersatiaServiceAnnotationData,
        annotation: KSAnnotation,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        if(handleValidateClassForRegisteringService(data, annotation, logger))
            codeGenerator.generateFilesForRegisteringService(data)
    }

    private fun handleValidateClassForRegisteringService(
        data: RegisterVersatiaServiceAnnotationData,
        annotation: KSAnnotation,
        logger: KSPLogger
    ): Boolean {
        /** Prerequisites:
         * [injectTo] does not have another service named [name] */

        fun validateOrError(condition: Boolean, errorMessage: () -> String): Boolean {
            if(condition)
                return true
            logger.error(errorMessage.invoke(), annotation)
            return false
        }

        fun nameIsOK() = validateOrError(
            SourceVersion.isName(data.name)     //TODO issues/12: Use a Kotlin equivalent, if one is ever created
        ) {
            "${data.name} is not a valid field name for a service"
        }

        val proxiedClass = data.proxiedClass
        val proxierClass = data.proxierClass
        val proxiedClassDeclaration = proxiedClass.declaration
        val proxierClassDeclaration = proxierClass.declaration
        val proxiedClassName = proxiedClassDeclaration.simpleName.asString()
        val proxierClassName = proxierClassDeclaration.simpleName.asString()
        fun proxiedClassIsPublic() = validateOrError(
            proxiedClassDeclaration.isPublic()
        ) {
            "$proxiedClassName must be public"
        }

        fun proxierClassHasValidConstructor() = validateOrError(
            proxierClassDeclaration.closestClassDeclaration()?.classKind == ClassKind.OBJECT
        ) {
            "$proxierClassName must be a kotlin object! (i.e. contain \"object\" keyword in its definition)"
        }

        fun proxierClassIsAssignableToProxiedClass() = validateOrError(
            proxiedClass.isAssignableFrom(proxierClass)
        ) {
            "$proxierClassName must be assignable to $proxiedClassName"
        }

        fun injectToDoesNotContainAnotherSuchNamedService() = validateOrError(true) {
            TODO()  //TODO issues/13: Currently no idea how to handle it. It should scan for all the Plugins
        }

        return nameIsOK()
            && proxiedClassIsPublic()
            && proxierClassHasValidConstructor()
            && proxierClassIsAssignableToProxiedClass()
            && injectToDoesNotContainAnotherSuchNamedService()
    }

    private fun CodeGenerator.generateFilesForRegisteringService(data: RegisterVersatiaServiceAnnotationData) {
        val injectToClassName = data.injectTo.toClassName()

        val baseName = "VersatiaServiceRegistrar_${injectToClassName.simpleName}_${data.name}"
        val apiClassProxyName = getApiClassProxyName(baseName)
        val apiPartModel = RegisterVersatiaServiceGeneratedModel_ApiPart(data, apiClassProxyName)
        val apiPartProxy = apiPartModel.RegisterVersatiaServiceGeneratedModel_ApiPart_Proxy()

        val proxiedClassPackageName = data.proxiedClass.toClassName().packageName
        val apiPartFileSpecBuilder = FileSpec.builder(
            proxiedClassPackageName,
            baseName
        )
        apiPartFileSpecBuilder.addProperty(apiPartModel.PluginFunction)
        apiPartFileSpecBuilder.addType(apiPartProxy.asTypeSpec)
        apiPartFileSpecBuilder.build().writeTo(this, false)

        val instanceInjectorName = getInstanceInjectorName(baseName)
        val implPartModel = RegisterVersatiaServiceGeneratedModel_ImplPart(
            data,
            instanceInjectorName,
            ClassName(proxiedClassPackageName, apiClassProxyName)
        )
        val implPartFileSpecBuilder = FileSpec.builder(
            data.proxierClass.toClassName().packageName,
            instanceInjectorName
        )
        implPartFileSpecBuilder.addType(implPartModel.asTypeSpec)
        implPartFileSpecBuilder.build().writeTo(this, false)
    }

    fun getApiClassProxyName(baseName: String) = baseName + "_Proxy"
    fun getInstanceInjectorName(baseName: String) = getApiClassProxyName(baseName) + "_InstanceInjector"
}
