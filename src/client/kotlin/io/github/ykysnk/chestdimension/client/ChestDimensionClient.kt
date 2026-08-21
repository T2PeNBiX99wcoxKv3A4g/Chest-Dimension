package io.github.ykysnk.chestdimension.client

import io.github.ykysnk.chestdimension.Constants.ForceInitialize
import io.github.ykysnk.chestdimension.block.Blocks
import io.github.ykysnk.chestdimension.block.entity.BlockEntityTypes
import io.github.ykysnk.chestdimension.block.entity.ChestDimensionBlockEntity
import io.github.ykysnk.chestdimension.item.Items
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.client.renderer.blockentity.ChestRenderer
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BlockItem

object ChestDimensionClient : ClientModInitializer {
    val itemChestDimensionBlockEntity by lazy {
        ChestDimensionBlockEntity(
            BlockPos.ZERO,
            Blocks.CHEST_DIMENSION.defaultBlockState()
        )
    }

    override fun onInitializeClient() {
        ForceInitialize

        // TODO: Create new ChestRenderer
        BlockEntityRenderers.register(BlockEntityTypes.CHEST_DIMENSION, ::ChestRenderer)
        BuiltinItemRendererRegistry.INSTANCE.register(Items.CHEST_DIMENSION) { stack, mode, matrices, vertexConsumers, light, overlay ->
            val dispatcher = Minecraft.getInstance().blockEntityRenderDispatcher
            val item = stack.item
            if (item !is BlockItem) return@register
            val block = item.block
            val blockState = block.defaultBlockState()
            if (!blockState.`is`(Blocks.CHEST_DIMENSION)) return@register
            dispatcher.renderItem(itemChestDimensionBlockEntity, matrices, vertexConsumers, light, overlay)
        }
    }
}