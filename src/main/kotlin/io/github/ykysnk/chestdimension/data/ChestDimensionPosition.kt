package io.github.ykysnk.chestdimension.data

import io.github.ykysnk.chestdimension.utils.NBTHelper
import io.github.ykysnk.chestdimension.utils.save
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

data class ChestDimensionPosition(
    val dimension: ResourceKey<Level>? = null,
    val blockPos: BlockPos? = null
) : NbtSave {
    override fun save(): CompoundTag {
        val tag = CompoundTag()
        dimension?.let { tag.putString("dimension", it.location().toString()) }
        blockPos?.let { tag.put("blockPos", it.save()) }
        return tag
    }

    companion object : NbtLoad<ChestDimensionPosition> {
        override fun load(tag: CompoundTag): ChestDimensionPosition {
            val dimension = ResourceLocation.tryParse(tag.getString("dimension"))
                ?.let { ResourceKey.create(Registries.DIMENSION, it) } ?: Level.OVERWORLD
            val blockPos = tag.getCompound("blockPos").let { NBTHelper.getBlockPos(it) }
            return ChestDimensionPosition(dimension, blockPos)
        }
    }
}
