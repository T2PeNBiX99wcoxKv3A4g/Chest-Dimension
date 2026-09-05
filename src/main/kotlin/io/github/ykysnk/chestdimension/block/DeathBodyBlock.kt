package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.block.entity.DeathBodyBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class DeathBodyBlock(properties: Properties) : BaseEntityBlock(properties) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = DeathBodyBlockEntity(pos, state)
}