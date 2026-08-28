package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.block.Blocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags
import java.util.concurrent.CompletableFuture

class BlockTagProvider(output: FabricDataOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    FabricTagProvider.BlockTagProvider(output, registries) {
    override fun addTags(provider: HolderLookup.Provider) {
        getOrCreateTagBuilder(BlockTags.WALLS).add(Blocks.CHEST_PLATFORM_WALL)
        getOrCreateTagBuilder(BlockTags.FENCES).add(Blocks.CHEST_PLATFORM_FENCE)
        getOrCreateTagBuilder(BlockTags.PRESSURE_PLATES).add(Blocks.TELEPORT_PRESSURE_PLATE)
        getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES).add(Blocks.TELEPORT_PRESSURE_PLATE)
        getOrCreateTagBuilder(BlockTags.WALL_POST_OVERRIDE).add(Blocks.TELEPORT_PRESSURE_PLATE)
            .add(Blocks.CHEST_PLATFORM_ENTER_PLATE)
        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE)
            .add(Blocks.CHEST_PLATFORM)
            .add(Blocks.CHEST_PLATFORM_WALL)
            .add(Blocks.CHEST_PLATFORM_FENCE)
            .add(Blocks.CHEST_PLATFORM_ENTER_PLATE)
            .add(Blocks.TELEPORT_PRESSURE_PLATE)
            .add(Blocks.CHEST_DIMENSION)
            .add(Blocks.WEATHER_TIME_CONTROLLER)
    }
}