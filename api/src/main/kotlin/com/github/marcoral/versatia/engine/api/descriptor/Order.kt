package com.github.marcoral.versatia.engine.api.descriptor

//TODO issues/2: Add javadoc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Order(val value: Int = 1)
