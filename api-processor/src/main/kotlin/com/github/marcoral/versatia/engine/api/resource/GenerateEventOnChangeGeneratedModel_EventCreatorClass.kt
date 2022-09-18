package com.github.marcoral.versatia.engine.api.resource

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class GenerateEventOnChangeGeneratedModel_EventCreatorClass(
    className: String,
    propertyTypeName: TypeName,
    eventClassNameObj: ClassName
) {

    val create = FunSpec.builder("create")
        .addParameter("oldValue", propertyTypeName)
        .addParameter("newValue", propertyTypeName)
        .addStatement("return %T(oldValue, newValue)", eventClassNameObj)
        .addModifiers(KModifier.OVERRIDE)
        .build()

    val asTypeSpec = TypeSpec.classBuilder(className)
        .addSuperinterface(VersatiaPropertyChangeEventCreator::class.asTypeName().parameterizedBy(
            propertyTypeName,
            eventClassNameObj
        ))
        .addFunction(create)
        .build()
}
