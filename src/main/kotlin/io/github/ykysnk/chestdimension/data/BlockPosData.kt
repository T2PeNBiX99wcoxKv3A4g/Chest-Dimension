package io.github.ykysnk.chestdimension.data

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos

@Serializable
data class BlockPosData(val x: Int, val y: Int, val z: Int) {
    companion object {
        fun BlockPos.toData(): BlockPosData = BlockPosData(x, y, z)
    }

    val blockPos: BlockPos
        get() = BlockPos(x, y, z)
}
