package io.github.ykysnk.chestdimension.level.data

import io.github.ykysnk.chestdimension.data.DeepCopy
import io.github.ykysnk.chestdimension.data.NbtLoad
import io.github.ykysnk.chestdimension.data.NbtSave
import io.github.ykysnk.chestdimension.utils.NBTHelper
import io.github.ykysnk.chestdimension.utils.save
import net.minecraft.nbt.CompoundTag

data class TeleportPoints(val points: HashMap<String, HashMap<String, TeleportInfo>> = hashMapOf()) : NbtSave,
    DeepCopy<TeleportPoints> {
    override fun deepCopy(): TeleportPoints =
        TeleportPoints(HashMap(points.mapValues { HashMap(it.value.mapValues { info -> info.value.copy() }) }))

    override fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.put("points", points.save())
        return tag
    }

    companion object : NbtLoad<TeleportPoints> {
        override fun load(tag: CompoundTag): TeleportPoints {
            val points = tag.getCompound("points")
                .let {
                    NBTHelper.getHashMap(it, { key, tag -> tag.getCompound(key) }) { value ->
                        NBTHelper.getHashMap(value, { key, tag -> tag.getCompound(key) }, TeleportInfo::load)
                    }
                }
            return TeleportPoints(points)
        }
    }
}
