package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.level.TeleportManager
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import java.util.*

class TeleportDoorBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.TELEPORT_DOOR, pos, blockState) {
    var uuid: UUID = TeleportManager.randomUUID()
        private set

    override fun setLevel(level: Level) {
        super.setLevel(level)
        (level as? ServerLevel)?.apply {
            if (blockState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
                val pos = blockPos.below()
                val belowEntity = getBlockEntity(pos)
                (belowEntity as? TeleportDoorBlockEntity)?.let { uuid = it.uuid }
                return@apply
            }
            TeleportManager[uuid, this] = blockPos
            TeleportManager.save()
        }
    }

    override fun load(tag: CompoundTag) {
        if (blockState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) return
        if (tag.hasUUID("UUID"))
            uuid = tag.getUUID("UUID")
    }

    override fun saveAdditional(tag: CompoundTag) {
        if (blockState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) return
        tag.putUUID("UUID", uuid)
    }
}