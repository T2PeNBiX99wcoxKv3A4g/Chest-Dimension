package io.github.ykysnk.chestdimension.item

import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items as MCItems

@Suppress("unused")
object Items : RegistryHelper<Item>() {
    val CHEST_PLATFORM: Item = register { MCItems.registerBlock(Blocks.CHEST_PLATFORM) }
    val CHEST_PLATFORM_WALL: Item = register { MCItems.registerBlock(Blocks.CHEST_PLATFORM_WALL) }
    val CHEST_PLATFORM_FENCE: Item = register { MCItems.registerBlock(Blocks.CHEST_PLATFORM_FENCE) }
    val CHEST_PLATFORM_ENTER_PLATE: Item = register { MCItems.registerBlock(Blocks.CHEST_PLATFORM_ENTER_PLATE) }
    val TELEPORT_PRESSURE_PLATE: Item = register { MCItems.registerBlock(Blocks.TELEPORT_PRESSURE_PLATE) }
    val CHEST_DIMENSION: Item = register { MCItems.registerBlock(Blocks.CHEST_DIMENSION) }
}