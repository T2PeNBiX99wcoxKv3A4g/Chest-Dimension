package io.github.ykysnk.chestdimension.interfaces

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState

interface PlayerInsideBlock {
    fun playerInside(state: BlockState, level: ServerLevel, pos: BlockPos, player: ServerPlayer): Boolean
}