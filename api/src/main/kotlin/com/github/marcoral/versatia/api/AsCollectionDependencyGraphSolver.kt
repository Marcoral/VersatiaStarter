package com.github.marcoral.versatia.api

//TODO issues/2: Add javadoc

class AsCollectionDependencyGraphSolver<K, V>(throwOnUnknownDependencies: Boolean = false)
    : AbstractDependencyGraphSolver<K, V>(throwOnUnknownDependencies) {

    fun solve(): Collection<Pair<K, V>> {
        val result = mutableSetOf<Pair<K, V>>()
        nodes.values.forEach {
            it.resolveRecurrent {
                val resolvedDependencyAsPair = Pair(referenceKey, value)
                result.add(resolvedDependencyAsPair)
            }
        }
        return result
    }
}