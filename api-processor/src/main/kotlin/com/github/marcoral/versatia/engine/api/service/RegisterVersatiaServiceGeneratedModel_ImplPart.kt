package com.github.marcoral.versatia.engine.api.service

import com.github.marcoral.versatia.engine.api.util.toCodeBlock
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName

internal class RegisterVersatiaServiceGeneratedModel_ImplPart(
    data: RegisterVersatiaServiceAnnotationData,
    className: String,
    apiPartClassName: ClassName,
) {
    val asTypeSpec: TypeSpec

    private val annotation = AnnotationSpec.builder(InjectInstance::class)
        .addMember("classToInstantiate = %T::class", data.proxierClass.toTypeName())
        .addMember("resolves = %T::class", data.proxiedClass.toTypeName())
        .addMember("injectTo = %T::class", apiPartClassName)
        .addMember("dependsOn = [%L]", data.dependsOn.toCodeBlock())
        .build()

    init {
        asTypeSpec = TypeSpec.objectBuilder(className)
            .addAnnotation(annotation)
            .build()
    }
}
