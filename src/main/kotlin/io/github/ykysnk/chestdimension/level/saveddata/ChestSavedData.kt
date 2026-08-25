package io.github.ykysnk.chestdimension.level.saveddata

import io.github.ykysnk.chestdimension.level.storage.ChestServerLevelData
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.storage.ServerLevelData

class ChestSavedData(val data: ServerLevelData) : SavedData() {
    init {
        (data as? ChestServerLevelData)?.let {
            it.chestSavedData = this
        }
    }

    override fun save(tag: CompoundTag): CompoundTag = (data as? ChestServerLevelData)?.save(tag) ?: tag
}