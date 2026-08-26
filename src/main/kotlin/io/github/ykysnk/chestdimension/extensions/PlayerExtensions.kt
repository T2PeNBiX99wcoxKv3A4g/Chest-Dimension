package io.github.ykysnk.chestdimension.extensions

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.accessor.ChestDimensionData
import net.minecraft.server.level.ServerPlayer

var ServerPlayer.openedChestDimension
    get() = (this as? ChestDimensionData)?.let { Constants.Server.getLevel(it.`chestDimension$getOpenedChestDimension`()) }
    set(value) {
        value?.let { level -> (this as? ChestDimensionData)?.`chestDimension$setOpenedChestDimension`(level.dimension()) }
    }

var ServerPlayer.openedChestPos
    get() = (this as? ChestDimensionData)?.`chestDimension$getOpenedChestPos`()
    set(value) {
        (this as? ChestDimensionData)?.`chestDimension$setOpenedChestPos`(value)
    }