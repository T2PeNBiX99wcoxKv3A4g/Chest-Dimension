package io.github.ykysnk.chestdimension.client.datagen.provider

import io.github.ykysnk.chestdimension.block.Blocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.world.level.block.Blocks as MCBlocks

class ModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators) {
        blockStateModelGenerator.family(Blocks.CHEST_PLATFORM).wall(Blocks.CHEST_PLATFORM_WALL)
        blockStateModelGenerator.blockEntityModels(
            ModelLocationUtils.getModelLocation(Blocks.CHEST_DIMENSION),
            MCBlocks.OAK_PLANKS
        ).createWithoutBlockItem(Blocks.CHEST_DIMENSION)
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerators) {
    }
}