package io.github.ykysnk.chestdimension.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.FenceBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class GlassFenceBlock(properties: Properties) : FenceBlock(properties) {
    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "adjacentState.`is`(this) || super.skipRendering(state, adjacentState, direction)",
            "net.minecraft.world.level.block.WallBlock"
        )
    )
    override fun skipRendering(state: BlockState, adjacentState: BlockState, direction: Direction): Boolean {
        return adjacentState.`is`(this) || super.skipRendering(state, adjacentState, direction)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("Shapes.empty()", "net.minecraft.world.phys.shapes.Shapes"))
    override fun getVisualShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return Shapes.empty()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("1.0f"))
    override fun getShadeBrightness(state: BlockState, level: BlockGetter, pos: BlockPos): Float {
        return 1.0f
    }

    override fun propagatesSkylightDown(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return true
    }
}