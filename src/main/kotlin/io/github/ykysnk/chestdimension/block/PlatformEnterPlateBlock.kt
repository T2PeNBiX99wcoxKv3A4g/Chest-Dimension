package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.block.entity.PlatformEnterPlateBlockEntity
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import io.github.ykysnk.chestdimension.level.UUIDManager
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class PlatformEnterPlateBlock(properties: Properties) : Block(properties.sound(SoundType.WOOL)), EntityBlock {
    companion object {
        private val AABB: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        AABB

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

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        PlatformEnterPlateBlockEntity(pos, state)

    @Deprecated("Deprecated in Java")
    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        if (!state.`is`(newState.block)) {
            ChestLevelManager.removeSpawnPos(level, pos)
            UUIDManager.save()
        }

        super.onRemove(state, level, pos, newState, movedByPiston)
    }
}