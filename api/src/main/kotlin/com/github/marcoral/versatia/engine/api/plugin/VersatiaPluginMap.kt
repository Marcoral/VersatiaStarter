package com.github.marcoral.versatia.engine.api.plugin

interface VersatiaPluginMap: Map<String, VersatiaPlugin> {
    override fun get(key: String): VersatiaPlugin
}