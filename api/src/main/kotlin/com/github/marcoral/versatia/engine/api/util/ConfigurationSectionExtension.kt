package com.github.marcoral.versatia.engine.api.util

import com.github.marcoral.versatia.api.AsCollectionDependencyGraphSolver
import com.github.marcoral.versatia.engine.api.asColored
import org.bukkit.configuration.ConfigurationSection

fun ConfigurationSection.getColoredString(key: String) = if(contains(key)) getString(key)!!.asColored() else null
fun ConfigurationSection.getColoredStringList(key: String) = if(contains(key)) getStringList(key).map { it.asColored() } else null

fun ConfigurationSection.getBooleanOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getBoolean)
fun ConfigurationSection.getIntOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getInt)
fun ConfigurationSection.getDoubleOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getDouble)
fun ConfigurationSection.getLongOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getLong)
fun ConfigurationSection.getStringOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getString)!!
fun ConfigurationSection.getColoredStringOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getColoredString)!!
fun ConfigurationSection.getBooleanListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getBooleanList)
fun ConfigurationSection.getByteListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getByteList)
fun ConfigurationSection.getCharacterListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getCharacterList)
fun ConfigurationSection.getIntegerListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getIntegerList)
fun ConfigurationSection.getLongListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getLongList)
fun ConfigurationSection.getFloatListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getFloatList)
fun ConfigurationSection.getDoubleListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getDoubleList)
fun ConfigurationSection.getStringListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getStringList)
fun ConfigurationSection.getColoredStringListOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getColoredStringList)
fun ConfigurationSection.getConfigurationSectionOrThrow(key: String, exceptionMessage: String? = null) = getOrThrow(key, exceptionMessage, ::getConfigurationSection)!!

fun ConfigurationSection.ifBooleanPresent(key: String, action: (Boolean) -> Unit) = ifElementPresent(key, action, ::isBoolean, ::getBoolean)
fun ConfigurationSection.ifIntPresent(key: String, action: (Int) -> Unit) = ifElementPresent(key, action, ::isInt, ::getInt)
fun ConfigurationSection.ifLongPresent(key: String, action: (Long) -> Unit) = ifElementPresent(key, action, ::isLong, ::getLong)
fun ConfigurationSection.ifDoublePresent(key: String, action: (Double) -> Unit) = ifElementPresent(key, action, ::isDouble, ::getDouble)
fun ConfigurationSection.ifStringPresent(key: String, action: (String) -> Unit) = ifElementPresent(key, action, ::isString) { getString(it)!! }
fun ConfigurationSection.ifColoredStringPresent(key: String, action: (String) -> Unit) = ifElementPresent(key, action, ::isString) { getColoredString(it)!! }
fun ConfigurationSection.ifBooleanListPresent(key: String, action: (List<Boolean>) -> Unit) = ifElementPresent(key, action, ::isList, ::getBooleanList)
fun ConfigurationSection.ifByteListPresent(key: String, action: (List<Byte>) -> Unit) = ifElementPresent(key, action, ::isList, ::getByteList)
fun ConfigurationSection.ifCharacterListPresent(key: String, action: (List<Char>) -> Unit) = ifElementPresent(key, action, ::isList, ::getCharacterList)
fun ConfigurationSection.ifIntegerListPresent(key: String, action: (List<Int>) -> Unit) = ifElementPresent(key, action, ::isList, ::getIntegerList)
fun ConfigurationSection.ifLongListPresent(key: String, action: (List<Long>) -> Unit) = ifElementPresent(key, action, ::isList, ::getLongList)
fun ConfigurationSection.ifFloatListPresent(key: String, action: (List<Float>) -> Unit) = ifElementPresent(key, action, ::isList, ::getFloatList)
fun ConfigurationSection.ifDoubleListPresent(key: String, action: (List<Double>) -> Unit) = ifElementPresent(key, action, ::isList, ::getDoubleList)
fun ConfigurationSection.ifStringListPresent(key: String, action: (List<String>) -> Unit) = ifElementPresent(key, action, ::isList, ::getStringList)
fun ConfigurationSection.ifColoredStringListPresent(key: String, action: (List<String>) -> Unit) = ifElementPresent(key, action, ::isList) { getColoredStringList(it)!! }
fun ConfigurationSection.ifSectionPresent(key: String, action: (ConfigurationSection) -> Unit) = ifElementPresent(key, action, ::isConfigurationSection) { getConfigurationSection(it)!! }

const val PARENTS_KEY = "Parents"
fun ConfigurationSection.asOrderedEntriesWithParents(parentsKey: String = PARENTS_KEY): Map<String, Pair<ConfigurationSection, List<String>>> {
    val graphSolver = AsCollectionDependencyGraphSolver<String, Pair<ConfigurationSection, List<String>>>()
    getValues(false).forEach { element ->
        val elementKey = element.key
        val elementParents = (element.value as ConfigurationSection).getStringList(parentsKey)
        val elementAsSection = element.value as ConfigurationSection to elementParents
        graphSolver.newEntry(elementKey, elementAsSection, elementParents.toTypedArray())
    }
    return graphSolver.solve().toMap()
}
fun <T, R> ConfigurationSection.mapEntriesWithParents(mappingAction: (ConfigurationSection) -> T, mergeAction: (T, List<R>) -> R, parentsKey: String = PARENTS_KEY) : Map<String, R> {
    val data = asOrderedEntriesWithParents(parentsKey)
    val elements = mutableMapOf<String, R>()
    data.forEach { entry ->
        val entrySection = entry.value.first
        val parents = entry.value.second.map { elements[it]!! }
        val mappedElement = mappingAction.invoke(entrySection)
        elements[entry.key] = mergeAction.invoke(mappedElement, parents)
    }
    return elements
}

private fun <T> ConfigurationSection.getOrThrow(key: String, exceptionMessage: String?, fetchAction: (String) -> T): T
    = if(contains(key)) fetchAction.invoke(key)!! else throw NoSuchElementException(exceptionMessage ?: "No value present at key: ${getPathToKey(key)}")

private fun ConfigurationSection.getPathToKey(key: String) = if(currentPath.isNullOrEmpty()) key else "$currentPath.$key"

private fun <T> ConfigurationSection.ifElementPresent(key: String, action: (T) -> Unit, predicate: (String) -> Boolean, fetchAction: (String) -> T) {
    if (predicate.invoke(key)) action.invoke(fetchAction.invoke(key))
}