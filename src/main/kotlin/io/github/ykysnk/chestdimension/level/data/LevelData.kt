package io.github.ykysnk.chestdimension.level.data

import io.github.ykysnk.chestdimension.data.NbtLoad
import io.github.ykysnk.chestdimension.data.NbtSave
import io.github.ykysnk.chestdimension.utils.NBTHelper
import io.github.ykysnk.chestdimension.utils.save
import net.minecraft.nbt.CompoundTag

data class LevelData(
    val seed: Long,
    val dimensionPositions: HashMap<String, DimensionPosition> = hashMapOf(),
) : NbtSave {
    override fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.putLong("seed", seed)
        tag.put("dimensionPositions", dimensionPositions.save())
        return tag
    }

    companion object : NbtLoad<LevelData> {
        override fun load(tag: CompoundTag): LevelData {
            val seed = tag.getLong("seed")
            val dimensionPositions = tag.getCompound("dimensionPositions")
                .let { NBTHelper.getHashMap(it, { key, tag -> tag.getCompound(key) }, DimensionPosition::load) }
            return LevelData(seed, dimensionPositions)
        }
    }
}
