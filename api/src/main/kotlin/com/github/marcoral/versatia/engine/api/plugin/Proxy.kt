package com.github.marcoral.versatia.engine.api.plugin

//TODO issues/2: Add javadoc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Proxy(val value: String = DEFAULT_VALUE) {
    companion object {
        const val DEFAULT_VALUE = "default"
    }
}
