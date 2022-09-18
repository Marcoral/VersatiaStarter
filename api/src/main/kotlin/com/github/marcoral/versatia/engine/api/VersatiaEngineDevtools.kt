package com.github.marcoral.versatia.engine.api

import com.github.marcoral.versatia.engine.api.plugin.VersatiaPluginMap
import com.github.marcoral.versatia.engine.api.plugin.Proxy
import com.github.marcoral.versatia.engine.api.util.processFileFromEngineDirectoryInternal
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection

//TODO issues/2: Add javadoc
object VersatiaEngineDevtools {
    interface ApiProxy {
        fun <T: Any> injectProxy(containingClass: Class<T>, proxyInstance: Any, key: String)
        fun <T: Any> injectProxyToInstance(containingClass: Class<T>, instance: T, proxyInstance: Any, key: String)
        fun <T> asVersatiaObject(objectConstructorReference: () -> T, initializationAction: (T) -> Unit): T

        val plugins: VersatiaPluginMap
        fun isPluginPresent(PluginName: String): Boolean
    }

    @Proxy
    private lateinit var implementation: ApiProxy

    fun <T: Any> Class<T>.injectProxy(proxyInstance: Any, key: String = Proxy.DEFAULT_VALUE) = implementation.injectProxy(this, proxyInstance, key)
    fun <T: Any> Class<T>.injectProxyToInstance(instance: T, proxyInstance: Any, key: String = Proxy.DEFAULT_VALUE) = implementation.injectProxyToInstance(this, instance, proxyInstance, key)
    fun <T: Any> asVersatiaObject(objectConstructorReference: () -> T, initializationAction: (T) -> Unit) = implementation.asVersatiaObject(objectConstructorReference, initializationAction)

    val engineInstance by lazy { plugins["VersatiaEngine"] } //Since the engine is often used, its instance is cached
    val plugins get() = implementation.plugins
    fun isPluginPresent(PluginName: String) = implementation.isPluginPresent(PluginName)

    //Following methods are JvmStatic to allow processing at the KSP level
    @JvmStatic
    fun getNameForEventCreator(eventClassName: String) = eventClassName + "Creator"

    @JvmStatic
    fun getNameForEvent(effectiveClassName: String, effectiveFieldName: String) = effectiveClassName + effectiveFieldName.capitalize() + "ChangedEvent"
    fun processFileFromEngineDirectory(fileName: String, throwIfNotExists: Boolean = false, action: (ConfigurationSection) -> Unit) = processFileFromEngineDirectoryInternal(fileName, throwIfNotExists, action)
}

fun String.asColored() = ChatColor.translateAlternateColorCodes('&', this)
fun <T: Any> VersatiaObject(objectConstructorReference: () -> T, initializationAction: (T) -> Unit) = VersatiaEngineDevtools.asVersatiaObject(objectConstructorReference, initializationAction)