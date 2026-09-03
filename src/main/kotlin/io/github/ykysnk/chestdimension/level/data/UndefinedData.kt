package io.github.ykysnk.chestdimension.level.data

import io.github.ykysnk.chestdimension.data.DeepCopy
import io.github.ykysnk.chestdimension.data.NbtLoad
import io.github.ykysnk.chestdimension.data.NbtSave
import net.minecraft.nbt.CompoundTag

data class UndefinedData(val seed: Long, val created: Boolean = false) : NbtSave, DeepCopy<UndefinedData> {
    override fun deepCopy(): UndefinedData = copy(seed = seed, created = created)

    override fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.putLong("seed", seed)
        tag.putBoolean("created", created)
        return tag
    }

    companion object : NbtLoad<UndefinedData> {
        override fun load(tag: CompoundTag): UndefinedData {
            val seed = tag.getLong("seed")
            val created = tag.getBoolean("created")
            return UndefinedData(seed, created)
        }
    }
}
