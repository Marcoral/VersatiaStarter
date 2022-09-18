package com.github.marcoral.versatia.engine.api.plugin

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

//TODO issues/2: Add javadoc
class VersatiaPluginInvalidatedEvent(val plugin: VersatiaPlugin) : Event() {
    override fun getHandlers() = Companion.handlers

    companion object {
        private val handlers: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = handlers
    }
}