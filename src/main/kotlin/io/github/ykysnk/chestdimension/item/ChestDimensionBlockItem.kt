package io.github.ykysnk.chestdimension.item

import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.level.ChestLevelManager
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import java.util.*

class ChestDimensionBlockItem : BlockItem(Blocks.CHEST_DIMENSION, Properties()) {
    private fun getUUID(stack: ItemStack): UUID? = stack.getTagElement("BlockEntityTag")?.getUUID("UUID")

    override fun place(context: BlockPlaceContext): InteractionResult {
        val itemUUID = getUUID(context.itemInHand)
        val uuid = ChestLevelManager.findUUIDByLevel(context.level)
        if (itemUUID == uuid) {
            context.player?.displayClientMessage(
                Component.literal("Can't place same UUID chest inside this own chest"),
                true
            )
            return InteractionResult.FAIL
        }
        return super.place(context)
    }
}