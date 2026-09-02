package io.github.ykysnk.chestdimension.level.data

import io.github.ykysnk.chestdimension.data.IsEmpty
import io.github.ykysnk.chestdimension.data.NbtLoad
import io.github.ykysnk.chestdimension.data.NbtSave
import io.github.ykysnk.chestdimension.utils.getCompoundOrNull
import io.github.ykysnk.chestdimension.utils.getUUIDOrNull
import net.minecraft.nbt.CompoundTag
import java.util.*

data class TeleportInfo(val linkUUID: UUID? = null, val dimensionPosition: DimensionPosition? = null) : NbtSave,
    IsEmpty {
    override fun save(): CompoundTag {
        val tag = CompoundTag()
        linkUUID?.let { tag.putUUID("linkUUID", it) }
        dimensionPosition?.let { tag.put("dimensionPosition", it.save()) }
        return tag
    }

    override fun isEmpty() = linkUUID == null && dimensionPosition == null

    companion object : NbtLoad<TeleportInfo> {
        override fun load(tag: CompoundTag): TeleportInfo {
            val linkUUID = tag.getUUIDOrNull("linkUUID")
            val dimensionPosition = tag.getCompoundOrNull("dimensionPosition")?.let { DimensionPosition.load(it) }
            return TeleportInfo(linkUUID, dimensionPosition)
        }
    }
}
