package com.github.marcoral.versatia.engine.api

import com.github.marcoral.versatia.engine.api.plugin.VersatiaPlugins
import org.bukkit.plugin.java.JavaPlugin

//TODO issues/2: Add javadoc
open class VersatiaPluginInitializer: JavaPlugin() {
    /**
     * {@inheritDoc}
     */
    final override fun onEnable() = VersatiaPlugins.build(this)

    /**
     * {@inheritDoc}
     */
    final override fun onDisable() = VersatiaPlugins.invalidate(this)
}
