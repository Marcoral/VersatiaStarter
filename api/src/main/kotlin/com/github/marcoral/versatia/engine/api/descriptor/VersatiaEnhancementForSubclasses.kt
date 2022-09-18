package com.github.marcoral.versatia.engine.api.descriptor

import kotlin.reflect.KClass

//TODO issues/2: Add javadoc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Repeatable
annotation class VersatiaEnhancementForSubclasses(val enhancementProcessorQualifiedName: String, val baseClassToEnhance: KClass<*>)