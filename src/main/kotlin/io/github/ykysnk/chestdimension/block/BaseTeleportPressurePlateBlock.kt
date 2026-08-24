package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.block.entity.TeleportPressurePlateBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

abstract class BaseTeleportPressurePlateBlock(properties: Properties, private val type: BlockSetType) :
    Block(properties.sound(type.soundType())), EntityBlock {
    companion object {
        protected val PRESSED_AABB: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0)
        protected val AABB: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0)

        @JvmStatic
        protected val TOUCH_AABB: AABB = AABB(0.0625, 0.0, 0.0625, 0.9375, 0.25, 0.9375)
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "if (getSignalForState(state)) PRESSED_AABB else AABB",
            "io.github.ykysnk.chestdimension.block.BaseTeleportPressurePlateBlock.Companion.PRESSED_AABB",
            "io.github.ykysnk.chestdimension.block.BaseTeleportPressurePlateBlock.Companion.AABB"
        )
    )
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        if (getSignalForState(state)) PRESSED_AABB else AABB

    protected open fun getPressedTime(): Int {
        return 20
    }

    override fun isPossibleToRespawnInThis(state: BlockState): Boolean {
        return true
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "if (direction == Direction.DOWN && !state.canSurvive( level, pos ) ) Blocks.AIR.defaultBlockState() else super.updateShape(state, direction, neighborState, level, pos, neighborPos)",
            "net.minecraft.core.Direction",
            "net.minecraft.world.level.block.Blocks",
            "net.minecraft.world.level.block.Block"
        )
    )
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState =
        if (direction == Direction.DOWN && !state.canSurvive(
                level,
                pos
            )
        ) Blocks.AIR.defaultBlockState() else super.updateShape(
            state,
            direction,
            neighborState,
            level,
            pos,
            neighborPos
        )

    @Deprecated("Deprecated in Java")
    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        val blockPos = pos.below()
        return canSupportRigidBlock(level, blockPos) || canSupportCenter(level, blockPos, Direction.UP)
    }

    @Deprecated("Deprecated in Java")
    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val currentSignal = getSignalForState(state)
        if (currentSignal) {
            val entities = getEntities(level, pos)
            if (entities.isNotEmpty()) (level.getBlockEntity(pos) as? TeleportPressurePlateBlockEntity)?.teleport(
                entities
            )
            checkPressed(null, level, pos, state, true)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (level.isClientSide) return
        val currentSignal = getSignalForState(state)
        if (currentSignal) return
        checkPressed(entity, level, pos, state, false)
    }

    private fun checkPressed(entity: Entity?, level: Level, pos: BlockPos, state: BlockState, currentSignal: Boolean) {
        val signalStrength = getSignalStrength(level, pos)
        if (currentSignal != signalStrength) {
            val blockState = setSignalForState(state, signalStrength)
            level.setBlock(pos, blockState, 2)
            updateNeighbours(level, pos)
            level.setBlocksDirty(pos, state, blockState)
        }

        if (!signalStrength && currentSignal) {
            level.playSound(null, pos, type.pressurePlateClickOff(), SoundSource.BLOCKS)
            level.gameEvent(entity, GameEvent.BLOCK_DEACTIVATE, pos)
        } else if (signalStrength && !currentSignal) {
            level.playSound(null, pos, type.pressurePlateClickOn(), SoundSource.BLOCKS)
            level.gameEvent(entity, GameEvent.BLOCK_ACTIVATE, pos)
        }

        if (signalStrength) {
            level.scheduleTick(BlockPos(pos), this, getPressedTime())
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(
        state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean
    ) {
        if (!movedByPiston && !state.`is`(newState.block)) {
            if (getSignalForState(state)) {
                updateNeighbours(level, pos)
            }

            super.onRemove(state, level, pos, newState, false)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun updateNeighbours(level: Level, pos: BlockPos) {
        level.updateNeighborsAt(pos, this)
        level.updateNeighborsAt(pos.below(), this)
    }

    protected fun getEntityCount(level: Level, box: AABB, entityClass: Class<out Entity>): Int {
        return level.getEntitiesOfClass(
            entityClass,
            box,
            EntitySelector.NO_SPECTATORS
        ).size
    }

    @Deprecated("Deprecated in Java")
    override fun triggerEvent(state: BlockState, level: Level, pos: BlockPos, id: Int, param: Int): Boolean {
        super.triggerEvent(state, level, pos, id, param)
        val blockEntity = level.getBlockEntity(pos)
        return blockEntity?.triggerEvent(id, param) ?: false
    }

    @Deprecated("Deprecated in Java")
    override fun getMenuProvider(state: BlockState, level: Level, pos: BlockPos): MenuProvider? {
        val blockEntity = level.getBlockEntity(pos)
        return blockEntity as? MenuProvider
    }

    protected abstract fun getSignalStrength(level: Level, pos: BlockPos): Boolean

    protected abstract fun getSignalForState(state: BlockState): Boolean

    protected abstract fun setSignalForState(state: BlockState, signal: Boolean): BlockState

    protected abstract fun getEntities(level: Level, pos: BlockPos): List<Entity>
}