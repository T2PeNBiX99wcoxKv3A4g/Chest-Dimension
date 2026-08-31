package io.github.ykysnk.chestdimension.level.data

import io.github.ykysnk.chestdimension.data.NbtLoad
import io.github.ykysnk.chestdimension.data.NbtSave
import io.github.ykysnk.chestdimension.utils.NBTHelper
import io.github.ykysnk.chestdimension.utils.save
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

data class Levels(
    var levels: HashMap<String, LevelData> = hashMapOf(),
    var inactiveLevels: HashMap<String, HashSet<String>> = hashMapOf()
) : NbtSave {
    fun deepCopy(): Levels =
        Levels(HashMap(levels), HashMap(inactiveLevels.mapValues { (_, value) -> value.toHashSet() }))

    override fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.put("levels", levels.save())
        tag.put("inactiveLevels", inactiveLevels.save())
        return tag
    }

    companion object : NbtLoad<Levels> {
        override fun load(tag: CompoundTag): Levels {
            val levels = tag.getCompound("levels")
                .let { NBTHelper.getHashMap(it, { key, tag -> tag.getCompound(key) }, LevelData::load) }
            val inactiveLevels = tag.getCompound("inactiveLevels").let {
                NBTHelper.getHashMap(it, { key, tag -> tag.getCompound(key) }) { key, value ->
                    val list = value.getList(key, Tag.TAG_STRING.toInt())
                    NBTHelper.getHashSet(list, { key, tag -> tag.getString(key) }) { value2 -> value2 }
                }
            }
            return Levels(levels, inactiveLevels)
        }
    }
}
