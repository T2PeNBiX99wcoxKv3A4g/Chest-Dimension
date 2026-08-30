package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.block.entity.TeleportDoorBlockEntity
import io.github.ykysnk.chestdimension.extensions.teleportToLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

class TeleportDoorBlock(properties: Properties, type: BlockSetType) : DoorBlock(properties, type), EntityBlock {
    companion object {
        private const val DOOR_THICKNESS = 3.0 / 16.0
        private const val TOUCH_THICKNESS = 0.1 / 16.0
        private const val TOUCH_OFFSET = DOOR_THICKNESS - TOUCH_THICKNESS
        private val SOUTH_TOUCH_AABB = AABB(0.0, 0.0, TOUCH_OFFSET, 1.0, 1.0, TOUCH_OFFSET + TOUCH_THICKNESS)
        private val NORTH_TOUCH_AABB =
            AABB(0.0, 0.0, 1.0 - TOUCH_OFFSET - TOUCH_THICKNESS, 1.0, 1.0, 1.0 - TOUCH_OFFSET)
        private val WEST_TOUCH_AABB = AABB(1.0 - TOUCH_OFFSET - TOUCH_THICKNESS, 0.0, 0.0, 1.0 - TOUCH_OFFSET, 1.0, 1.0)
        private val EAST_TOUCH_AABB = AABB(TOUCH_OFFSET, 0.0, 0.0, TOUCH_OFFSET + TOUCH_THICKNESS, 1.0, 1.0)

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

        fun getTouchPlane(state: BlockState): Double {
            val centerOffset = TOUCH_OFFSET + TOUCH_THICKNESS / 2.0

            return when (state.getValue(FACING)) {
                Direction.EAST -> centerOffset
                Direction.SOUTH -> centerOffset
                Direction.WEST -> 1.0 - centerOffset
                Direction.NORTH -> 1.0 - centerOffset
                else -> centerOffset
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (level.isClientSide) return
        (level as? ServerLevel)?.apply {
            val box = getTouchAABB(state, pos)
            val list = getEntitiesOfClass(Entity::class.java, box, EntitySelector.NO_SPECTATORS)
            list.forEach {
                val oldPos = it.position()
                it.teleportToLevel(this, Vec3(oldPos.x, oldPos.y + 20, oldPos.z))
            }
        }
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = TeleportDoorBlockEntity(pos, state)
}