package io.github.ykysnk.chestdimension.level.data

import io.github.ykysnk.chestdimension.data.NbtLoad
import io.github.ykysnk.chestdimension.data.NbtSave
import io.github.ykysnk.chestdimension.utils.NBTHelper
import io.github.ykysnk.chestdimension.utils.save
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

data class DimensionPosition(
    val dimension: ResourceKey<Level>,
    val blockPos: BlockPos
) : NbtSave {
    override fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.putString("dimension", dimension.location().toString())
        tag.put("blockPos", blockPos.save())
        return tag
    }

    companion object : NbtLoad<DimensionPosition> {
        override fun load(tag: CompoundTag): DimensionPosition {
            val dimension = ResourceLocation.tryParse(tag.getString("dimension"))
                ?.let { ResourceKey.create(Registries.DIMENSION, it) } ?: Level.OVERWORLD
            val blockPos = tag.getCompound("blockPos").let { NBTHelper.getBlockPos(it) }
            return DimensionPosition(dimension, blockPos)
        }
    }
}
