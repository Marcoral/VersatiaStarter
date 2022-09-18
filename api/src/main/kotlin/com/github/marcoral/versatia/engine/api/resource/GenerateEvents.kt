package com.github.marcoral.versatia.engine.api.resource

//TODO issues/2: Add javadoc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class GenerateEvents(val packageName: String = "", val overrideClassName: String = "")