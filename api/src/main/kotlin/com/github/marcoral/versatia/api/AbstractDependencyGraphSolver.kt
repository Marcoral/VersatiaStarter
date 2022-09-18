package com.github.marcoral.versatia.api

//TODO issues/2: Add javadoc
abstract class AbstractDependencyGraphSolver<K, V>(private val throwOnUnknownDependencies: Boolean) {
    fun newEntry(referenceKey: K, value: V, dependencies: Array<K>) {
        val notExistingBefore = referenceKey !in nodes
        if(notExistingBefore) {
            val node = createNode(referenceKey, value)
            nodes[referenceKey] = node
        }

        val node = accessNode(referenceKey)!!
        require(node.value == value) {
            "Found multiple values for reference key \"$referenceKey\": ${node.value} and $value"
        }

        node.dependencies.addAll(dependencies)
        onNodeUpdated(node, notExistingBefore)
    }

    protected open fun createNode(referenceKey: K, value: V) = Node(referenceKey, value)
    protected open fun onNodeUpdated(node: Node, justCreated: Boolean) {}

    protected val nodes: MutableMap<K, Node> = mutableMapOf()


    protected open fun accessNode(key: K): Node? = nodes[key]
        ?: if(throwOnUnknownDependencies)
            throw NoSuchElementException("Unknown dependency: $key")
        else null


    open inner class Node(val referenceKey: K, val value: V) {
        val dependencies = mutableSetOf<K>()
        var referenced = false
        var resolved = false

        fun resolveRecurrent(solvedCallback: Node.() -> Unit) {
            if(resolved)
                return
            referenced = true
            dependencies.forEach {
                val dependency = accessNode(it) ?: return@forEach
                if (!dependency.resolved)
                    if (dependency.referenced)
                        throw RuntimeException("Circular dependency found for referenceKeys: $referenceKey and ${dependency.referenceKey}")
                    else dependency.resolveRecurrent(solvedCallback)
            }
            resolved = true
            solvedCallback.invoke(this)
        }
    }
}