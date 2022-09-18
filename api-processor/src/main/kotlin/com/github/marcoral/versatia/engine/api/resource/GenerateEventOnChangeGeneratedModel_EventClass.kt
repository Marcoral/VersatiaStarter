package com.github.marcoral.versatia.engine.api.resource

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

internal class GenerateEventOnChangeGeneratedModel_EventClass(
    className: String,
    propertyTypeName: TypeName,
    cancellable: Boolean) {

    val asTypeSpec: TypeSpec

    val oldValue = PropertySpec.builder("oldValue", propertyTypeName)
        .initializer("oldValue")
        .build()
    val newValue = PropertySpec.builder("newValue", propertyTypeName)
        .initializer("newValue")
        .build()

    val primaryConstructor = FunSpec.constructorBuilder()
        .addParameter("oldValue", propertyTypeName)
        .addParameter("newValue", propertyTypeName)
        .build()

    val getHandlers = FunSpec.builder("getHandlers")
        .addStatement("return Companion.%N", handlers)
        .addModifiers(OVERRIDE)
        .build()


    val cancelled: PropertySpec?
    val isCancelled: FunSpec?
    val setCancelled: FunSpec?

    init {
        val baseBuilder = baseBuilder(className)

        if(!cancellable) {
            cancelled = null
            isCancelled = null
            setCancelled = null
        } else {
            cancelled = PropertySpec.builder("cancelled", Boolean::class.asTypeName(), PRIVATE)
                .mutable()
                .initializer("false")
                .build()
            isCancelled = FunSpec.builder("isCancelled")
                .addStatement("return %N", cancelled)
                .addModifiers(OVERRIDE)
                .build()
            setCancelled = FunSpec.builder("setCancelled")
                .addParameter("isCancelled", Boolean::class)
                .addModifiers(OVERRIDE)
                .addStatement("%N = %N", cancelled, "isCancelled")
                .build()
            baseBuilder.addProperty(cancelled)
            baseBuilder.addSuperinterface(Cancellable::class.asTypeName())
            baseBuilder.addFunction(isCancelled)
            baseBuilder.addFunction(setCancelled)
        }

        baseBuilder.primaryConstructor(primaryConstructor)
        asTypeSpec = baseBuilder.build()
    }

    private fun baseBuilder(className: String): TypeSpec.Builder {
        val builder = TypeSpec.classBuilder(className)
        builder.superclass(Event::class.asTypeName())
        builder.addProperty(oldValue)
        builder.addProperty(newValue)
        builder.addFunction(getHandlers)
        builder.addType(Companion.asTypeSpec)
        return builder
    }

    companion object {
        val asTypeSpec: TypeSpec

        val handlers = PropertySpec.builder(
            "handlers", HandlerList::class.asTypeName(), PRIVATE)
            .initializer("%T()", HandlerList::class)
            .build()

        val getHandlersList = FunSpec.builder("getHandlerList")
            .addAnnotation(JvmStatic::class)
            .addStatement("return %N", handlers)
            .build()

        init {
            asTypeSpec = buildType()
        }

        private fun buildType(): TypeSpec {
            val toReturn = TypeSpec.companionObjectBuilder()
            toReturn.addProperty(handlers)
            toReturn.addFunction(getHandlersList)
            return toReturn.build()
        }
    }
}
