package com.github.marcoral.versatia.engine.api.plugin

import org.bukkit.plugin.java.JavaPlugin

object VersatiaPlugins {
    interface ApiProxy {
        fun build(plugin: JavaPlugin)
        fun invalidate(plugin: JavaPlugin)
    }

    @Proxy
    private lateinit var implementation: ApiProxy

    /**
     * Registers specified plugin as Versatia Plugin and regenerates its configuration files.
     * @param plugin Plugin that you want to register as Versatia Plugin
     */
    fun build(plugin: JavaPlugin) = implementation.build(plugin)


    /**
     * Invalidates the Plugin. Always when mode is turned off this method should be invoked.
     * @param plugin Plugin to invalidate
     */
    fun invalidate(plugin: JavaPlugin) = implementation.invalidate(plugin)

}