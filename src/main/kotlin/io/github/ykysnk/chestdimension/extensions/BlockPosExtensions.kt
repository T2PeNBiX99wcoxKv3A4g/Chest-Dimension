@file:Suppress("unused")

package io.github.ykysnk.chestdimension.extensions

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity

fun BlockPos.isBlockNearBy(level: ServerLevel, block: Block): Boolean {
    val neighbors = listOf(north(), south(), east(), west())
    return neighbors.any { level.getBlockState(it).`is`(block) }
}

inline fun <reified T : BlockEntity> BlockPos.getBlockEntityNearBy(level: ServerLevel): T? {
    val neighbors = listOf(north(), south(), east(), west())
    return neighbors.firstNotNullOfOrNull { level.getBlockEntity(it) as? T }
}