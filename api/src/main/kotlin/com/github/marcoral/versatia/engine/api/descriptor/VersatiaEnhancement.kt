package com.github.marcoral.versatia.engine.api.descriptor

//TODO issues/2: Add javadoc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Repeatable
annotation class VersatiaEnhancement(val enhancementProcessorQualifiedName: String)