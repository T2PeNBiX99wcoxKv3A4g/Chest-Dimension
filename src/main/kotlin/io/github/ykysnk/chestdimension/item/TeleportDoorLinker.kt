package io.github.ykysnk.chestdimension.item

import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.entity.TeleportDoorBlockEntity
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf

class TeleportDoorLinker : Item(Properties().durability(1)) {
    override fun useOn(context: UseOnContext): InteractionResult {
        val stack = context.itemInHand
        var pos = context.clickedPos
        val level = context.level
        val player = context.player
        val state = level.getBlockState(pos)
        if (!state.`is`(Blocks.TELEPORT_DOOR)) return InteractionResult.FAIL
        if (state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) pos = pos.below()

        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is TeleportDoorBlockEntity) {
            if (blockEntity.linkUUID != null) {
                player?.displayClientMessage(Component.literal("This door is already linked!"), true)
                return InteractionResult.FAIL
            }
            val tag = stack.orCreateTag
            tag.putUUID("linkUUID", blockEntity.uuid)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.FAIL
    }
}