package com.github.marcoral.versatia.engine.api.descriptor

import org.bukkit.event.Listener

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@VersatiaEnhancementForSubclasses("com.github.marcoral.versatia.engine.RegisterListenersEnhancementProcessor", Listener::class)
annotation class VersatiaEnginePlugin