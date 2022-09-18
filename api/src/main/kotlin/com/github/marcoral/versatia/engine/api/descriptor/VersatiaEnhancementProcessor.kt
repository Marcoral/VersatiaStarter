package com.github.marcoral.versatia.engine.api.descriptor

import com.github.marcoral.versatia.engine.api.plugin.VersatiaPlugin
import java.lang.reflect.Field
import java.lang.reflect.Method

//TODO issues/2: Add javadoc
interface VersatiaEnhancementProcessor {
    //May be field, method or class
    fun processElements(processedElementsData: Collection<AnnotatedElementData<out Any>>, pluginInstance: VersatiaPlugin) {}

    fun processFields(processedElementsData: Collection<AnnotatedElementData<Field>>, pluginInstance: VersatiaPlugin) {}
    fun processMethods(processedElementsData: Collection<AnnotatedElementData<Method>>, pluginInstance: VersatiaPlugin) {}
    fun processClasses(processedElementsData: Collection<AnnotatedElementData<Class<*>>>, pluginInstance: VersatiaPlugin) {}
}

//TODO issues/2: Add javadoc
data class AnnotatedElementData <T: Any> (
    val processedElement: T,
    val annotationClass: Class<*>,
    val annotationArguments: Map<String, Any>
)