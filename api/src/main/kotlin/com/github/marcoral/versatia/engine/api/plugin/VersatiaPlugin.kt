package com.github.marcoral.versatia.engine.api.plugin

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin

//TODO issues/2: Add javadoc
interface VersatiaPlugin {
    val bukkitPlugin: JavaPlugin

    fun regenerateConfiguration()
    fun overwriteConfiguration()

    /** Throws [NoSuchFileException] if file at specified path doesn't exist. */
    fun getConfig(path: String): ConfigurationSection
}