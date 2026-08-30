package io.github.ykysnk.chestdimension.compat

import net.fabricmc.loader.api.FabricLoader

@Suppress("unused")
open class ModCompat(private val modId: String) {
    val isModLoaded by lazy { FabricLoader.getInstance().isModLoaded(modId) }
    fun whenLoaded(action: () -> Unit) {
        if (!isModLoaded) return
        action()
    }
}