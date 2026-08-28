package io.github.ykysnk.chestdimension.client.datagen.provider

import com.google.gson.JsonObject
import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.block.Blocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.blockstates.MultiVariantGenerator
import net.minecraft.data.models.blockstates.Variant
import net.minecraft.data.models.blockstates.VariantProperties
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.world.level.block.Blocks as MCBlocks

class ModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators) {
        blockStateModelGenerator.family(Blocks.CHEST_PLATFORM).wall(Blocks.CHEST_PLATFORM_WALL)
            .fence(Blocks.CHEST_PLATFORM_FENCE).pressurePlate(Blocks.TELEPORT_PRESSURE_PLATE)
        blockStateModelGenerator.blockEntityModels(
            ModelLocationUtils.getModelLocation(Blocks.CHEST_DIMENSION),
            MCBlocks.OAK_PLANKS
        ).createWithoutBlockItem(Blocks.CHEST_DIMENSION)

        val chestPlatformEnterPlateTextureMapping = TextureMapping()
        chestPlatformEnterPlateTextureMapping.put(TextureSlot.TEXTURE, Constants.id("block/chest_platform_enter_plate"))

        val chestPlatformEnterPlateModel = ModelTemplates.PRESSURE_PLATE_UP.create(
            Blocks.CHEST_PLATFORM_ENTER_PLATE,
            chestPlatformEnterPlateTextureMapping,
            blockStateModelGenerator.modelOutput
        )

        val chestPlatformEnterPlateVariant =
            Variant.variant().with(VariantProperties.MODEL, chestPlatformEnterPlateModel)
        blockStateModelGenerator.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(
                Blocks.CHEST_PLATFORM_ENTER_PLATE,
                chestPlatformEnterPlateVariant
            )
        )

        blockStateModelGenerator.createTrivialCube(Blocks.WEATHER_TIME_CONTROLLER)
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerators) {
        itemModelGenerator.output.accept(Constants.id("item/chest_dimension")) {
            JsonObject().apply {
                addProperty("parent", "minecraft:item/chest")
            }
        }
    }
}