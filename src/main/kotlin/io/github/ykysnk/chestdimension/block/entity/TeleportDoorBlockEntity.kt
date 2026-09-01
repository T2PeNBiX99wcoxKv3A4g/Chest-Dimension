package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.level.TeleportManager
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class TeleportDoorBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.TELEPORT_DOOR, pos, blockState) {
    var uuid: UUID = TeleportManager.randomUUID()
        private set
}