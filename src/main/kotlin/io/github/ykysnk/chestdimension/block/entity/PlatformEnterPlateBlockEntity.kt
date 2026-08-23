package io.github.ykysnk.chestdimension.block.entity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class PlatformEnterPlateBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityTypes.CHEST_PLATFORM_ENTER_PLATE, pos, blockState) {
    override fun load(tag: CompoundTag) {

    }
}