package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.block.entity.TeleportPressurePlateBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.PressurePlateBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty

class TeleportPressurePlateBlock(properties: Properties, type: BlockSetType) :
    BaseTeleportPressurePlateBlock(properties, type) {
    companion object {
        val POWERED: BooleanProperty = BlockStateProperties.POWERED
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(POWERED, false))
    }

    override fun getSignalForState(state: BlockState): Boolean = state.getValue(POWERED)

    override fun setSignalForState(state: BlockState, signal: Boolean): BlockState = state.setValue(POWERED, signal)

    override fun getSignalStrength(level: Level, pos: BlockPos): Boolean =
        getEntityCount(level, TOUCH_AABB.move(pos), Entity::class.java) > 0

    override fun getEntities(level: Level, pos: BlockPos): List<Entity> =
        level.getEntitiesOfClass(Entity::class.java, TOUCH_AABB.move(pos), EntitySelector.NO_SPECTATORS)

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(*arrayOf(PressurePlateBlock.POWERED))
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        TeleportPressurePlateBlockEntity(pos, state)
}