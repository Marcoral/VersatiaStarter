package com.github.marcoral.versatia.engine.api.service

import com.github.marcoral.versatia.engine.api.descriptor.VersatiaEnhancement
import kotlin.reflect.KClass

/**
 * For classes marked with this annotation that are detected during services registering, the fields will be scanned.
 * If exactly one of them is annotated with ```Proxy```, an instance of [classToInstantiate] will be injected there.
 * This annotation is used by Versatia's internal mechanisms and is not intended to be used as part of the API. You use it at your own risk. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@VersatiaEnhancement("com.github.marcoral.versatia.engine.service.InjectInstanceEnhancementProcessor")
annotation class InjectInstance(val classToInstantiate: KClass<*>, val resolves: KClass<*>, val injectTo: KClass<*>, val dependsOn: Array<KClass<*>> = [])
