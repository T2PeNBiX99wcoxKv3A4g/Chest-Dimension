package io.github.ykysnk.chestdimension.client

import io.github.ykysnk.chestdimension.Constants
import io.github.ykysnk.chestdimension.Constants.ForceInitialize
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.TeleportDoorBlock
import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.block.entity.ChestDimensionBlockEntity
import io.github.ykysnk.chestdimension.block.entity.TeleportDoorBlockEntity
import io.github.ykysnk.chestdimension.client.iris.IrisCompat
import io.github.ykysnk.chestdimension.client.renderer.blockentity.TeleportDoorRenderer
import io.github.ykysnk.chestdimension.client.world.inventory.WeatherTimeControllerScreen
import io.github.ykysnk.chestdimension.item.Items
import io.github.ykysnk.chestdimension.world.inventory.MenuTypes
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.LevelRenderer
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

        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            WorldRenderEvents.AFTER_ENTITIES.register { context ->
                val poseStack = context.matrixStack() ?: return@register
                val bufferSource = context.consumers() ?: return@register
                val buffer = bufferSource.getBuffer(RenderType.lines())
                val camera = context.camera()
                val cameraPos = camera.position

                for (pos in TeleportDoorBlockEntity.blockPosList) {
                    val level = Minecraft.getInstance().level ?: continue
                    val state = level.getBlockState(pos)

                    if (!state.`is`(Blocks.TELEPORT_DOOR)) continue

                    val box = TeleportDoorBlock.getTouchAABB(state, pos)
                    val renderBox = box.move(
                        -cameraPos.x,
                        -cameraPos.y,
                        -cameraPos.z
                    )

                    LevelRenderer.renderLineBox(
                        poseStack,
                        buffer,
                        renderBox,
                        1.0f,
                        1.0f,
                        0.0f,
                        1.0f
                    )
                }
            }
        }
    }
}