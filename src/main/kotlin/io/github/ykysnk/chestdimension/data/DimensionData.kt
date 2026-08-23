package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

@Serializable
data class DimensionData(private val key: String) {
    companion object {
        fun ResourceKey<Level>.toData(): DimensionData = DimensionData(location().toString())
    }

    val resourceKey: ResourceKey<Level>
        get() {
            ResourceLocation.tryParse(key)?.let { return ResourceKey.create(Registries.DIMENSION, it) }
            return Level.OVERWORLD
        }
}
