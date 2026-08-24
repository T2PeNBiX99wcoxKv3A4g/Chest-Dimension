package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.extensions.getBlockEntityNearBy
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class TeleportPressurePlateBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.TELEPORT_PRESSURE_PLATE, pos, blockState) {
    fun teleport(entities: List<Entity>) {
        (level as? ServerLevel)?.let { currentLevel ->
            val copyCache = entities.toList()
            if (!ChestLevelManager.teleportEntitiesToExit(currentLevel, copyCache)) {
                val chestEntity = blockPos.getBlockEntityNearBy<ChestDimensionBlockEntity>(currentLevel)
                chestEntity?.let {
                    val world = ChestLevelManager.getOrCreate(Constants.Server, it.uuid)
                    ChestLevelManager.teleportEntitiesToEnter(world, copyCache)
                }
            }
        }
    }
}