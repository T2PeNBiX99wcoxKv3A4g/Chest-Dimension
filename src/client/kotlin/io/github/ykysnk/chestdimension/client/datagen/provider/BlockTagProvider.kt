package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.tags.BlockTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture
import net.minecraft.tags.BlockTags as MCBlockTags

class BlockTagProvider(output: FabricDataOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    FabricTagProvider.BlockTagProvider(output, registries) {
    override fun addTags(provider: HolderLookup.Provider) {
        getOrCreateTagBuilder(MCBlockTags.WALLS).add(Blocks.CHEST_PLATFORM_WALL)
        getOrCreateTagBuilder(MCBlockTags.FENCES).add(Blocks.CHEST_PLATFORM_FENCE)
        getOrCreateTagBuilder(MCBlockTags.PRESSURE_PLATES).add(Blocks.TELEPORT_PRESSURE_PLATE)
        getOrCreateTagBuilder(MCBlockTags.WOODEN_PRESSURE_PLATES).add(Blocks.TELEPORT_PRESSURE_PLATE)
        getOrCreateTagBuilder(MCBlockTags.WALL_POST_OVERRIDE).add(Blocks.TELEPORT_PRESSURE_PLATE)
            .add(Blocks.CHEST_PLATFORM_ENTER_PLATE)
        getOrCreateTagBuilder(MCBlockTags.MINEABLE_WITH_AXE)
            .add(Blocks.CHEST_PLATFORM)
            .add(Blocks.CHEST_PLATFORM_WALL)
            .add(Blocks.CHEST_PLATFORM_FENCE)
            .add(Blocks.CHEST_PLATFORM_ENTER_PLATE)
            .add(Blocks.TELEPORT_PRESSURE_PLATE)
            .add(Blocks.CHEST_DIMENSION)
            .add(Blocks.WEATHER_TIME_CONTROLLER)
            .add(Blocks.TELEPORT_DOOR)
        getOrCreateTagBuilder(MCBlockTags.MINEABLE_WITH_PICKAXE).add(Blocks.DEATH_BODY)
        getOrCreateTagBuilder(BlockTags.GLASS_PANES).add(Blocks.BLAST_RESISTANT_GLASS_PANE)
    }
}