package com.github.marcoral.versatia.engine.api.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.buildCodeBlock

object KotlinPoetUtils {
    fun buildCodeBlockMapOf(keyType: TypeName, valueType: TypeName, elements: Collection<Pair<Any, Any>>, keyPattern: String, valuePattern: String)
        = if(elements.isEmpty())
            CodeBlock.of("%T()", ClassName("kotlin.collections", "emptyMap").parameterizedBy(keyType, valueType))
        else buildObject(ClassName("kotlin.collections", "mapOf")) {
            val args = mutableListOf<Any>()
            elements.forEach {
                args.add(it.first)
                args.add(it.second)
            }

            add(
                CodeBlock.of(
                List(elements.size) { "$keyPattern to $valuePattern" }.joinToString(",\n", postfix = "\n"),
                *args.toTypedArray()
            ))
        }

    fun buildCodeBlockListOf(type: TypeName, elements: Collection<CodeBlock>) = buildCodeBlockListOfElements(type, elements, "%L")
    fun buildCodeBlockListOfElements(type: TypeName, elements: Collection<*>, elementPattern: String = "%T")
            = if(elements.isEmpty())
        CodeBlock.of("%T()", ClassName("kotlin.collections", "emptyList").parameterizedBy(type))
    else buildObject(ClassName("kotlin.collections", "listOf")) {
        add(buildCodeBlockContainerInitializer(elements, elementPattern))
    }

    fun buildObject(objectType: TypeName, builderAction: CodeBlock.Builder.() -> Unit) = buildCodeBlock {
        add("%T(\n⇥", objectType)
        builderAction.invoke(this)
        add("⇤)")
    }

    private fun buildCodeBlockContainerInitializer(elements: Collection<*>, elementPattern: String = "%T") = CodeBlock.of(
        List(elements.size) { elementPattern }.joinToString(",\n", postfix = "\n"),
        *elements.toTypedArray()
    )
}