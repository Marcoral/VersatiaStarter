package com.github.marcoral.versatia.engine.api.service

import com.github.marcoral.versatia.api.VersatiaEngine
import kotlin.reflect.KClass

/**
 * For such annotated class, a class annotated with [RegisterVersatiaService] will be generated underneath,
 * where ```proxierClass``` denotes this class (other parameters remain unchanged). */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class VersatiaService(val name: String, val type: KClass<*> = DEFAULT::class, val injectTo: KClass<*> = VersatiaEngine::class, val dependsOn: Array<KClass<*>> = [])
