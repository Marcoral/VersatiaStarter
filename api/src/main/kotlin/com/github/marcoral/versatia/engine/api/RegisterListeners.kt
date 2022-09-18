package com.github.marcoral.versatia.engine.api

import com.github.marcoral.versatia.engine.api.descriptor.VersatiaEnhancementForSubclasses
import org.bukkit.event.Listener

//TODO issues/2: Add javadoc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@VersatiaEnhancementForSubclasses("com.github.marcoral.versatia.engine.RegisterListenersEnhancementProcessor", Listener::class)
annotation class RegisterListeners