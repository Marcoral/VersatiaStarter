package com.github.marcoral.versatia.api

//TODO issues/2: Add javadoc
class AsComparatorDependencyGraphSolver<K, V>(throwOnUnknownDependencies: Boolean = false)
    : AbstractDependencyGraphSolver<K, V>(throwOnUnknownDependencies) {

    private val valueToNodeMap: MutableMap<V, NodeWithTransitiveDependencies> = mutableMapOf()

    override fun createNode(referenceKey: K, value: V) = NodeWithTransitiveDependencies(referenceKey, value)

    override fun onNodeUpdated(node: Node, justCreated: Boolean) {
        super.onNodeUpdated(node, justCreated)
        require(node is NodeWithTransitiveDependencies)

        if(justCreated)
            valueToNodeMap[node.value] = node
        node.dependenciesDeep.addAll(node.dependencies)
    }

    override fun accessNode(key: K): NodeWithTransitiveDependencies? {
        return super.accessNode(key)
            ?.let { it as NodeWithTransitiveDependencies }
    }

    fun solve(): Comparator<V> {
        nodes.values.forEach {
            it.resolveRecurrent {
                require(it is NodeWithTransitiveDependencies)
                it.calculateAndStoreTransitiveDependencies()
            }
        }

        return compareBy(nullsFirst { node1, node2 ->
            compare(node1, node2)
        }, valueToNodeMap::get)
    }

    private fun NodeWithTransitiveDependencies.calculateAndStoreTransitiveDependencies() {
        val transitiveDependencies = dependencies.map {
            accessNode(it)?.dependenciesDeep ?: emptySet()
        }.flatten()
        this.dependenciesDeep.addAll(transitiveDependencies)
    }

    private fun compare(node1: NodeWithTransitiveDependencies, node2: NodeWithTransitiveDependencies) = if(node2.referenceKey in node1.dependenciesDeep)
            1
        else if(node1.referenceKey in node2.dependenciesDeep)
            -1
        else
            0

    inner class NodeWithTransitiveDependencies(referenceKey: K, value: V) : Node(referenceKey, value) {
        val dependenciesDeep = mutableSetOf<K>()
    }
}
