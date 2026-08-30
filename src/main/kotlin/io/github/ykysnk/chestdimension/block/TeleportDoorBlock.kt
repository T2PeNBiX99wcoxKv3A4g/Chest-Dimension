package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.extensions.teleportToLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

class TeleportDoorBlock(properties: Properties, type: BlockSetType) : DoorBlock(properties, type) {
    companion object {
        private const val DOOR_THICKNESS = 3.0 / 16.0
        private const val TOUCH_THICKNESS = 0.1 / 16.0
        private const val TOUCH_OFFSET = DOOR_THICKNESS - TOUCH_THICKNESS
        val SOUTH_TOUCH_AABB = AABB(0.0, 0.0, TOUCH_OFFSET, 1.0, 1.0, TOUCH_OFFSET + TOUCH_THICKNESS)
        val NORTH_TOUCH_AABB = AABB(0.0, 0.0, 1.0 - TOUCH_OFFSET - TOUCH_THICKNESS, 1.0, 1.0, 1.0 - TOUCH_OFFSET)
        val WEST_TOUCH_AABB = AABB(1.0 - TOUCH_OFFSET - TOUCH_THICKNESS, 0.0, 0.0, 1.0 - TOUCH_OFFSET, 1.0, 1.0)
        val EAST_TOUCH_AABB = AABB(TOUCH_OFFSET, 0.0, 0.0, TOUCH_OFFSET + TOUCH_THICKNESS, 1.0, 1.0)

        fun getTouchAABB(state: BlockState, pos: BlockPos): AABB {
            val local = when (state.getValue(FACING)) {
                Direction.EAST -> EAST_TOUCH_AABB
                Direction.SOUTH -> SOUTH_TOUCH_AABB
                Direction.WEST -> WEST_TOUCH_AABB
                Direction.NORTH -> NORTH_TOUCH_AABB
                else -> EAST_TOUCH_AABB
            }

            return local.move(pos)
        }

        val blockPosList = mutableListOf<BlockPos>()
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "blockPosList.add(pos)",
            "io.github.ykysnk.chestdimension.block.TeleportDoorBlock.Companion.blockPosList"
        )
    )
    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, movedByPiston: Boolean) {
        blockPosList.add(pos)
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        if (!state.`is`(newState.block)) {
            blockPosList.remove(pos)
        }

        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (level.isClientSide) return
        (level as? ServerLevel)?.apply {
            val box = getTouchAABB(state, pos)
            val list = level.getEntitiesOfClass(Entity::class.java, box.move(pos), EntitySelector.NO_SPECTATORS)
            list.forEach {
                val oldPos = it.position()
                it.teleportToLevel(level, Vec3(oldPos.x, oldPos.y + 20, oldPos.z))
                Constants.LOGGER.info("Entities inside teleport door: {}", it)
            }
            Constants.LOGGER.info("Entities inside teleport door: {}", list.size)
        }
    }
}