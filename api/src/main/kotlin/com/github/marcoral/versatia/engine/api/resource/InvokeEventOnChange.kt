package com.github.marcoral.versatia.engine.api.resource

import org.bukkit.event.Event
import kotlin.reflect.KClass

//TODO issues/2: Add javadoc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class InvokeEventOnChange(val eventCreator: KClass<out VersatiaPropertyChangeEventCreator<*, *>>)

interface VersatiaPropertyChangeEventCreator<V, T: Event> {
    fun create(oldValue: V, newValue: V): T
}