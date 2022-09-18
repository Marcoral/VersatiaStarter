package com.github.marcoral.versatia.engine.api.service

import com.github.marcoral.versatia.engine.api.VersatiaEngineInternals
import kotlin.reflect.KClass

/**
 * For such annotated class, a class annotated with [RegisterVersatiaService] will be generated underneath,
 * where ```proxierClass``` denotes this class and ```injectTo``` denotes [VersatiaEngineInternals] (other parameters remain unchanged). */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
//TODO issues/8: Delete injectTo parameter and hardcode VersatiaEngineInternals::class on its place
annotation class VersatiaInternalService(val name: String, val type: KClass<*> = DEFAULT::class, val injectTo: KClass<*> = VersatiaEngineInternals::class, val dependsOn: Array<KClass<*>> = [])
