package com.github.marcoral.versatia.engine.api.util

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

//TODO issues/2: Add javadoc
object ReflectionUtil {
    fun Field.forceSetValue(instance: Any?, value: Any) {
        try {
            isAccessible = true
            set(instance, value)
        } finally {
            isAccessible = false
        }
    }

    fun Method.forceInvoke(subject: Any?, vararg args: Any?): Any? {
        try {
            isAccessible = true
            return invoke(subject, *args)
        } finally {
            isAccessible = false
        }
    }

    fun Method.forceInvokeOnKotlinObject(vararg args: Any?): Any? {
        try {
            isAccessible = true
            return invoke(declaringClass.kotlin.getObjectInstanceOrThrow(), *args)
        } finally {
            isAccessible = false
        }
    }

    fun <T: Any> KClass<T>.getObjectInstanceOrThrow(): T {
        objectInstance?.let { return it }
        throw IllegalStateException("${java.canonicalName} is not declared as a kotlin object! Does it have an \"object\" keyword in its definition?")
    }
}