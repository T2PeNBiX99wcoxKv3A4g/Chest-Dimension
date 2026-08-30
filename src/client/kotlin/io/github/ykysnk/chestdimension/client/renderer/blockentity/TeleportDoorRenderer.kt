package io.github.ykysnk.chestdimension.client.renderer.blockentity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import io.github.ykysnk.chestdimension.block.TeleportDoorBlock
import io.github.ykysnk.chestdimension.block.entity.TeleportDoorBlockEntity
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.world.level.block.DoorBlock
import org.joml.Matrix4f

class TeleportDoorRenderer(context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<TeleportDoorBlockEntity> {
    override fun render(
        blockEntity: TeleportDoorBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val matrix4f = poseStack.last().pose()
        renderPortal(blockEntity, matrix4f, buffer.getBuffer(RenderType.endPortal()))
    }

    private fun renderPortal(blockEntity: TeleportDoorBlockEntity, pose: Matrix4f, consumer: VertexConsumer) {
        val state = blockEntity.blockState
        val facing = state.getValue(DoorBlock.FACING)
        val plane = TeleportDoorBlock.getTouchPlane(state).toFloat()

        when (facing) {
            Direction.SOUTH,
            Direction.NORTH -> renderHorizontalPlane(pose, consumer, plane)

            Direction.EAST,
            Direction.WEST -> renderVerticalPlane(pose, consumer, plane)

            else -> Unit
        }
    }

    private fun renderHorizontalPlane(
        pose: Matrix4f,
        consumer: VertexConsumer,
        z: Float
    ) {
        consumer.vertex(pose, 0f, 0f, z).endVertex()
        consumer.vertex(pose, 1f, 0f, z).endVertex()
        consumer.vertex(pose, 1f, 1f, z).endVertex()
        consumer.vertex(pose, 0f, 1f, z).endVertex()

        consumer.vertex(pose, 0f, 1f, z).endVertex()
        consumer.vertex(pose, 1f, 1f, z).endVertex()
        consumer.vertex(pose, 1f, 0f, z).endVertex()
        consumer.vertex(pose, 0f, 0f, z).endVertex()
    }

    private fun renderVerticalPlane(
        pose: Matrix4f,
        consumer: VertexConsumer,
        x: Float
    ) {
        consumer.vertex(pose, x, 0f, 0f).endVertex()
        consumer.vertex(pose, x, 0f, 1f).endVertex()
        consumer.vertex(pose, x, 1f, 1f).endVertex()
        consumer.vertex(pose, x, 1f, 0f).endVertex()

        consumer.vertex(pose, x, 1f, 0f).endVertex()
        consumer.vertex(pose, x, 1f, 1f).endVertex()
        consumer.vertex(pose, x, 0f, 1f).endVertex()
        consumer.vertex(pose, x, 0f, 0f).endVertex()
    }
}