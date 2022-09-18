package com.github.marcoral.versatia.engine.api.service

import com.github.marcoral.versatia.api.VersatiaEngine
import kotlin.reflect.KClass

/**
 * When the plugin is started, an instance of [proxierClass] is to be created
 *   and wired in [injectTo] class to the field of type [proxiedClass] named [name].
 * Classes marked as [dependsOn] will be resolved first.
 * If no [injectTo] is specified, service will be injected to [VersatiaEngine]
 *
 * Prerequisites:
 * * [name] is the valid name of the Java Programming Language variable
 * * [proxiedClass] is public
 * * [proxierClass] is assignable to the [proxiedClass] type
 * * [proxierClass] has a have a parameterless, non-abstract constructor (it will be used to create an instance of the object)
 * * [injectTo] does not have another service named [name] */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class RegisterVersatiaService(val name: String, val proxiedClass: KClass<*>, val proxierClass: KClass<*>, val injectTo: KClass<*> = VersatiaEngine::class, val dependsOn: Array<KClass<*>> = [])
