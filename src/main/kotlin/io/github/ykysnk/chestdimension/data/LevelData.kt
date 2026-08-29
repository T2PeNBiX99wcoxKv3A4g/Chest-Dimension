package io.github.ykysnk.chestdimension.data

import io.github.ykysnk.chestdimension.utils.NBTHelper
import io.github.ykysnk.chestdimension.utils.save
import net.minecraft.nbt.CompoundTag

data class LevelData(
    val seed: Long,
    val chestDimensionPositions: HashMap<String, ChestDimensionPosition> = hashMapOf(),
) : NbtSave {
    override fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.putLong("seed", seed)
        tag.put("chestDimensionPositions", chestDimensionPositions.save())
        return tag
    }

    companion object : NbtLoad<LevelData> {
        override fun load(tag: CompoundTag): LevelData {
            val seed = tag.getLong("seed")
            val chestDimensionPositions = tag.getCompound("chestDimensionPositions")
                .let { NBTHelper.getHashMap(it, { key, tag -> tag.getCompound(key) }, ChestDimensionPosition::load) }
            return LevelData(seed, chestDimensionPositions)
        }
    }
}
