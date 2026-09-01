package io.github.ykysnk.chestdimension.level.data

import io.github.ykysnk.chestdimension.data.NbtLoad
import io.github.ykysnk.chestdimension.data.NbtSave
import net.minecraft.nbt.CompoundTag

data class TeleportInfo(val dimensionPosition: DimensionPosition) : NbtSave {
    override fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.put("dimensionPosition", dimensionPosition.save())
        return tag
    }

    companion object : NbtLoad<TeleportInfo> {
        override fun load(tag: CompoundTag): TeleportInfo {
            val dimensionPosition = DimensionPosition.load(tag.getCompound("dimensionPosition"))
            return TeleportInfo(dimensionPosition)
        }
    }
}
