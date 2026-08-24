package io.github.ykysnk.chestdimension.block.entity

import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class PlatformEnterPlateBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.CHEST_PLATFORM_ENTER_PLATE, pos, blockState) {
    override fun setLevel(level: Level) {
        super.setLevel(level)
        ChestLevelManager.addSpawnPos(level, blockPos)
        UUIDManager.save()
    }
}