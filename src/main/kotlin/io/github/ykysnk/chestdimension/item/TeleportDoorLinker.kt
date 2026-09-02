package io.github.ykysnk.chestdimension.item

import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.entity.TeleportDoorBlockEntity
import io.github.ykysnk.chestdimension.level.TeleportManager
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
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
        if (blockEntity !is TeleportDoorBlockEntity) return InteractionResult.FAIL

        (level as? ServerLevel)?.apply {
            val tag = stack.orCreateTag
            if (tag.contains("linkUUID")) {
                (player as? ServerPlayer)?.apply {
                    val linkUUID = tag.getUUID("linkUUID")
                    val isLinked = TeleportManager.linkDoors(linkUUID, blockEntity.uuid)
                    if (!isLinked) {
                        displayClientMessage(Component.literal("Invalid UUID"), true)
                        return InteractionResult.FAIL
                    }
                    stack.hurtAndBreak(1, this) { it.broadcastBreakEvent(EquipmentSlot.MAINHAND) }
                }
                return InteractionResult.sidedSuccess(isClientSide)
            }
            tag.putUUID("linkUUID", blockEntity.uuid)
            if (FabricLoader.getInstance().isDevelopmentEnvironment)
                player?.displayClientMessage(Component.literal(blockEntity.uuid.toString()), true)
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun isFoil(stack: ItemStack) = stack.tag?.contains("linkUUID") == true || super.isFoil(stack)
}