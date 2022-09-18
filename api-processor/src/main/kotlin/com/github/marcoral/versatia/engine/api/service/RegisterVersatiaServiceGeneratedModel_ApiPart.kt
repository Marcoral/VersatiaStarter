package com.github.marcoral.versatia.engine.api.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.LATEINIT
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ksp.toTypeName

val ProxyType = ClassName("com.github.marcoral.versatia.engine.api.plugin", "Proxy")

internal class RegisterVersatiaServiceGeneratedModel_ApiPart(
    private val data: RegisterVersatiaServiceAnnotationData,
    private val proxyClassName: String,
) {

    val PluginFunction = PropertySpec.builder(data.name, data.proxiedClass.toTypeName())
        .receiver(data.injectTo.toTypeName())
        .getter(FunSpec.getterBuilder()
            .addStatement("return %N.%N", RegisterVersatiaServiceGeneratedModel_ApiPart_Proxy().asTypeSpec, data.name)
            .build())
        .build()

    internal inner class RegisterVersatiaServiceGeneratedModel_ApiPart_Proxy {
        val asTypeSpec: TypeSpec

        private val proxy = PropertySpec.builder("proxy", data.proxiedClass.toTypeName(), PRIVATE, LATEINIT)
            .mutable()
            .addAnnotation(ProxyType)
            .build()

        private val getter = PropertySpec.builder(data.name, data.proxiedClass.toTypeName())
            .getter(FunSpec.getterBuilder()
                .addStatement("return %N", proxy)
                .build()
            )
            .build()

        init {
            asTypeSpec = TypeSpec.objectBuilder(proxyClassName)
                .addProperty(proxy)
                .addProperty(getter)
                .build()
        }
    }
}
