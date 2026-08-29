package io.github.ykysnk.chestdimension.level.saveddata

import io.github.ykysnk.chestdimension.level.storage.ChestServerLevelData
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData

class ChestSavedData(val data: ChestServerLevelData) : SavedData() {
    init {
        data.chestSavedData = this
    }

    override fun save(tag: CompoundTag): CompoundTag = data.save(tag)
}