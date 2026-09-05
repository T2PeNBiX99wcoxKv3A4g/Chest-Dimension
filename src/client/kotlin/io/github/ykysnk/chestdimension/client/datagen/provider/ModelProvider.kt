@file:Suppress("SameParameterValue")

package io.github.ykysnk.chestdimension.client.datagen.provider

import com.google.gson.JsonObject
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.id
import io.github.ykysnk.chestdimension.item.Items
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.ItemModelGenerators
import net.minecraft.data.models.blockstates.MultiVariantGenerator
import net.minecraft.data.models.blockstates.Variant
import net.minecraft.data.models.blockstates.VariantProperties
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items as MCItems
import net.minecraft.world.level.block.Blocks as MCBlocks

class ModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(blockStateModelGenerator: BlockModelGenerators) {
        blockStateModelGenerator.family(Blocks.CHEST_PLATFORM).wall(Blocks.CHEST_PLATFORM_WALL)
            .fence(Blocks.CHEST_PLATFORM_FENCE).pressurePlate(Blocks.TELEPORT_PRESSURE_PLATE)
        blockStateModelGenerator.blockEntityModels(
            ModelLocationUtils.getModelLocation(Blocks.CHEST_DIMENSION),
            MCBlocks.OAK_PLANKS
        ).createWithoutBlockItem(Blocks.CHEST_DIMENSION)
        blockStateModelGenerator.createGlassBlocks(Blocks.BLAST_RESISTANT_GLASS, Blocks.BLAST_RESISTANT_GLASS_PANE)

        val chestPlatformEnterPlateTextureMapping = TextureMapping()
        chestPlatformEnterPlateTextureMapping.put(TextureSlot.TEXTURE, id("block/chest_platform_enter_plate"))

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
        blockStateModelGenerator.createDoor(Blocks.TELEPORT_DOOR)
        blockStateModelGenerator.blockEntityModels(
            ModelLocationUtils.getModelLocation(Blocks.DEATH_BODY),
            MCBlocks.WHITE_WOOL
        ).createWithoutBlockItem(Blocks.DEATH_BODY)
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerators) {
        addChestBlockItem(itemModelGenerator, Items.CHEST_DIMENSION)
        addChestBlockItem(itemModelGenerator, Items.DEATH_BODY)
        itemModelGenerator.generateFlatItem(Items.TELEPORT_DOOR_LINKER, ModelTemplates.FLAT_ITEM)
    }

    @Suppress("unused")
    private fun addSimpleBlockItem(itemModelGenerator: ItemModelGenerators, item: Item) {
        val resourceLocation = BuiltInRegistries.ITEM.getKey(item)
        itemModelGenerator.output.accept(resourceLocation.withPrefix("item/")) {
            JsonObject().apply {
                addProperty("parent", resourceLocation.withPrefix("block/").toString())
            }
        }
    }

    private fun addChestBlockItem(itemModelGenerator: ItemModelGenerators, item: Item) {
        val resourceLocation = BuiltInRegistries.ITEM.getKey(item)
        val chestResourceLocation = BuiltInRegistries.ITEM.getKey(MCItems.CHEST)
        itemModelGenerator.output.accept(resourceLocation.withPrefix("item/")) {
            JsonObject().apply {
                addProperty("parent", chestResourceLocation.withPrefix("item/").toString())
            }
        }
    }
}