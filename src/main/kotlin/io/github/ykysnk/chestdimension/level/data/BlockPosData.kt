package io.github.ykysnk.chestdimension.level.data

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos

@Serializable
data class BlockPosData(private val x: Int, private val y: Int, private val z: Int) {
    companion object {
        fun BlockPos.toData(): BlockPosData = BlockPosData(x, y, z)
    }

    val blockPos: BlockPos
        get() = BlockPos(x, y, z)
}
