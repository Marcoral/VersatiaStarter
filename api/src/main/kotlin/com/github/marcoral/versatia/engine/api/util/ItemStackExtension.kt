package com.github.marcoral.versatia.engine.api.util

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun <T: ItemStack> T.modifyMeta(action: (ItemMeta) -> Unit) {
    val metaClone = itemMeta?.apply {
        action.invoke(this)
    }
    this.itemMeta = metaClone
}

fun <T: ItemStack> T.alsoModifyMeta(action: (ItemMeta) -> Unit): T {
    modifyMeta(action)
    return this
}