package com.github.marcoral.versatia.engine.api.util

fun <K, V> MutableMap<K, V>.getOrThrow(key: K, exceptionGenerator: () -> Throwable = { IllegalStateException("No value assigned to key \"$key\"") }): V & Any {
    if(key !in this)
        throw exceptionGenerator.invoke()
    return get(key)!!
}

fun <K, V> MutableMap<K, V>.putOrThrow(key: K, value: V, exceptionGenerator: (V) -> Throwable = { IllegalStateException("Value of key \"$key\" already exists ($it)") }) {
    get(key)?.let { throw exceptionGenerator.invoke(it) }
    put(key, value)
}

fun <T> MutableSet<T>.addOrThrow(element: T, exceptionGenerator: () -> Throwable = { IllegalStateException("Element equal to \"$element\" already in set") }) {
    if (element in this) { throw exceptionGenerator.invoke() }
    add(element)
}