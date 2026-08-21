package io.github.ykysnk.chestdimension.item

import io.github.ykysnk.chestdimension.block.Blocks
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items as MCItems

@Suppress("unused")
object Items {
    val CHEST_PLATFORM: Item = MCItems.registerBlock(Blocks.CHEST_PLATFORM)
    val CHEST_PLATFORM_WALL: Item = MCItems.registerBlock(Blocks.CHEST_PLATFORM_WALL)
    val CHEST_PLATFORM_FENCE: Item = MCItems.registerBlock(Blocks.CHEST_PLATFORM_FENCE)
    val CHEST_DIMENSION: Item = MCItems.registerBlock(Blocks.CHEST_DIMENSION)
}