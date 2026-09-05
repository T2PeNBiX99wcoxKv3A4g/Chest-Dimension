package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.block.Blocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.world.item.Items

class BlockLootTableProvider(output: FabricDataOutput) : FabricBlockLootTableProvider(output) {
    override fun generate() {
        dropSelf(Blocks.CHEST_PLATFORM)
        dropSelf(Blocks.CHEST_PLATFORM_WALL)
        dropSelf(Blocks.CHEST_PLATFORM_FENCE)
        dropSelf(Blocks.CHEST_PLATFORM_ENTER_PLATE)
        dropSelf(Blocks.BLAST_RESISTANT_GLASS)
        dropSelf(Blocks.BLAST_RESISTANT_GLASS_PANE)
        dropSelf(Blocks.TELEPORT_PRESSURE_PLATE)
        dropSelf(Blocks.CHEST_DIMENSION)
        dropSelf(Blocks.WEATHER_TIME_CONTROLLER)
        dropOther(Blocks.DEATH_BODY, Items.BONE)
        add(Blocks.TELEPORT_DOOR, createDoorTable(Blocks.TELEPORT_DOOR))
    }
}