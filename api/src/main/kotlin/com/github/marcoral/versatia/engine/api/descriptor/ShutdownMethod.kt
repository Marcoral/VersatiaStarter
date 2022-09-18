package com.github.marcoral.versatia.engine.api.descriptor

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@VersatiaEnhancement("com.github.marcoral.versatia.engine.ShutdownMethodEnhancementProcessor")
annotation class ShutdownMethod