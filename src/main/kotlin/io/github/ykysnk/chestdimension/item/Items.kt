package io.github.ykysnk.chestdimension.item

import io.github.ykysnk.chestdimension.NameSpaces
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.utils.RegistryHelper
import net.minecraft.world.item.DoubleHighBlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items as MCItems

@Suppress("unused")
object Items : RegistryHelper<Item>() {
    @JvmField
    val CHEST_PLATFORM: Item = register(MCItems.registerBlock(Blocks.CHEST_PLATFORM))

    @JvmField
    val CHEST_PLATFORM_WALL: Item = register(MCItems.registerBlock(Blocks.CHEST_PLATFORM_WALL))

    @JvmField
    val CHEST_PLATFORM_FENCE: Item = register(MCItems.registerBlock(Blocks.CHEST_PLATFORM_FENCE))

    @JvmField
    val CHEST_PLATFORM_ENTER_PLATE: Item = register(MCItems.registerBlock(Blocks.CHEST_PLATFORM_ENTER_PLATE))

    @JvmField
    val BLAST_RESISTANT_GLASS: Item = register(MCItems.registerBlock(Blocks.BLAST_RESISTANT_GLASS))

    @JvmField
    val BLAST_RESISTANT_GLASS_PANE: Item = register(MCItems.registerBlock(Blocks.BLAST_RESISTANT_GLASS_PANE))

    @JvmField
    val TELEPORT_PRESSURE_PLATE: Item = register(MCItems.registerBlock(Blocks.TELEPORT_PRESSURE_PLATE))

    @JvmField
    val CHEST_DIMENSION: Item = register(MCItems.registerBlock(ChestDimensionBlockItem()))

    @JvmField
    val WEATHER_TIME_CONTROLLER: Item = register(MCItems.registerBlock(Blocks.WEATHER_TIME_CONTROLLER))

    @JvmField
    val TELEPORT_DOOR: Item =
        register(MCItems.registerBlock(DoubleHighBlockItem(Blocks.TELEPORT_DOOR, Item.Properties())))

    @JvmField
    val TELEPORT_DOOR_LINKER: Item =
        register(MCItems.registerItem(NameSpaces.MOD("teleport_door_linker"), TeleportDoorLinker()))

    @JvmField
    val DEATH_BODY: Item = register(MCItems.registerBlock(Blocks.DEATH_BODY))

    @JvmField
    val NULL: Item = register(MCItems.registerBlock(Blocks.NULL))
}