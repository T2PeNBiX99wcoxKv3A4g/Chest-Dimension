package io.github.ykysnk.chestdimension.client

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.Constants.ForceInitialize
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.block.entity.ChestDimensionBlockEntity
import io.github.ykysnk.chestdimension.client.iris.IrisCompat
import io.github.ykysnk.chestdimension.client.renderer.blockentity.TeleportDoorRenderer
import io.github.ykysnk.chestdimension.client.world.inventory.WeatherTimeControllerScreen
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.world.inventory.MenuTypes
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.client.renderer.blockentity.ChestRenderer
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BlockItem

object ChestDimensionClient : ClientModInitializer {
    private val itemChestDimensionBlockEntity by lazy {
        ChestDimensionBlockEntity(
            BlockPos.ZERO,
            Blocks.CHEST_DIMENSION.defaultBlockState()
        )
    }

    override fun onInitializeClient() {
        ForceInitialize

        Constants.LOGGER.debug("{}", IrisCompat)

        // TODO: Create new ChestRenderer
        BlockEntityRenderers.register(BlockEntityTypes.CHEST_DIMENSION, ::ChestRenderer)
        BlockEntityRenderers.register(BlockEntityTypes.TELEPORT_DOOR, ::TeleportDoorRenderer)
        BuiltinItemRendererRegistry.INSTANCE.register(Items.CHEST_DIMENSION) { stack, mode, matrices, vertexConsumers, light, overlay ->
            val dispatcher = Minecraft.getInstance().blockEntityRenderDispatcher
            val item = stack.item
            if (item !is BlockItem) return@register
            val block = item.block
            val blockState = block.defaultBlockState()
            if (!blockState.`is`(Blocks.CHEST_DIMENSION)) return@register
            dispatcher.renderItem(itemChestDimensionBlockEntity, matrices, vertexConsumers, light, overlay)
        }
        MenuScreens.register(MenuTypes.WEATHER_TIME_CONTROLLER, ::WeatherTimeControllerScreen)
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.TELEPORT_DOOR, RenderType.cutout())
    }
}