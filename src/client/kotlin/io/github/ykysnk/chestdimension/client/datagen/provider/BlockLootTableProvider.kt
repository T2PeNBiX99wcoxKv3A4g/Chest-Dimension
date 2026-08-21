package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.block.Blocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider

class BlockLootTableProvider(output: FabricDataOutput) : FabricBlockLootTableProvider(output) {
    override fun generate() {
        dropSelf(Blocks.CHEST_PLATFORM)
        dropSelf(Blocks.CHEST_PLATFORM_WALL)
        dropSelf(Blocks.CHEST_PLATFORM_FENCE)
        dropSelf(Blocks.CHEST_DIMENSION)
    }
}