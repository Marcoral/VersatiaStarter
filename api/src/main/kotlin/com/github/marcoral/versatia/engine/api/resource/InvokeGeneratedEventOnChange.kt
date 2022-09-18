package com.github.marcoral.versatia.engine.api.resource

import kotlin.annotation.AnnotationTarget.FIELD

//TODO issues/2: Add javadoc
//TODO issues/4: Add support for intercepting methods (e.g. adding elements to collections)
@Retention(AnnotationRetention.RUNTIME)
@Target(FIELD) //TODO issues/5: Add "CLASS" support - generate event on change of any changeable fields
annotation class InvokeGeneratedEventOnChange(val overrideFieldName: String = "", val cancellable: Boolean = true)