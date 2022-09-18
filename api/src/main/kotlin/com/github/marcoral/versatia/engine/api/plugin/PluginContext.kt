package com.github.marcoral.versatia.engine.api.plugin

import com.github.marcoral.versatia.engine.api.descriptor.VersatiaEnhancement

//TODO issues/2: Add javadoc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@VersatiaEnhancement("com.github.marcoral.versatia.engine.plugin.PluginContextEnhancementProcessor")
annotation class PluginContext