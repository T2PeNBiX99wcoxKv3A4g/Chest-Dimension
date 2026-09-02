package io.github.ykysnk.chestdimension.block

import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.block.entity.TeleportDoorBlockEntity
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.level.TeleportManager
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult

class TeleportDoorBlock(properties: Properties, type: BlockSetType) : DoorBlock(properties, type), EntityBlock {
    companion object {
        private const val DOOR_THICKNESS = 3.0 / 16.0
        private const val TOUCH_THICKNESS = 0.5 / 16.0
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

    private fun getLowerEntity(state: BlockState, level: Level, pos: BlockPos): TeleportDoorBlockEntity? {
        var getPos = pos
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) getPos = getPos.below()
        return level.getBlockEntity(getPos) as? TeleportDoorBlockEntity
    }

    @Deprecated("Deprecated in Java")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (level.isClientSide) return
        (level as? ServerLevel)?.apply ServerLevel@{
            val box = getTouchAABB(state, pos)
            val list = getEntitiesOfClass(Entity::class.java, box, EntitySelector.NO_SPECTATORS)
            getLowerEntity(state, this, pos)?.apply {
                list.forEach { enterDoor(it) }
                level.scheduleTick(pos, this@TeleportDoorBlock, 2)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val blockEntity = getLowerEntity(state, level, pos) ?: return
        blockEntity.startTeleport()
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = TeleportDoorBlockEntity(pos, state)

    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).`is`(Items.TELEPORT_DOOR_LINKER) || player.getItemInHand(
                InteractionHand.OFF_HAND
            ).`is`(Items.TELEPORT_DOOR_LINKER)
        ) return InteractionResult.PASS
        return super.use(state, level, pos, player, hand, hit)
    }

    @Deprecated("Deprecated in Java")
    override fun getDrops(state: BlockState, params: LootParams.Builder): List<ItemStack> {
        val drops = super.getDrops(state, params)
        val blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY)

        if (blockEntity is TeleportDoorBlockEntity) {
            for (stack in drops) {
                if (stack.item != asItem()) continue
                val tag = CompoundTag()
                tag.putUUID("UUID", blockEntity.uuid)
                BlockItem.setBlockEntityData(stack, BlockEntityTypes.TELEPORT_DOOR, tag)
            }
        }

        return drops
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
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is TeleportDoorBlockEntity && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                TeleportManager.removePosition(blockEntity.uuid)
                TeleportManager.save()
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston)
    }
}