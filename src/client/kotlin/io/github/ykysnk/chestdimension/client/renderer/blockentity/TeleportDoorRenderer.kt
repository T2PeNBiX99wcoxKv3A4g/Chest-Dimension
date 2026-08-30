package io.github.ykysnk.chestdimension.client.renderer.blockentity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import io.github.ykysnk.chestdimension.block.TeleportDoorBlock
import io.github.ykysnk.chestdimension.block.entity.TeleportDoorBlockEntity
import io.github.ykysnk.chestdimension.client.iris.IrisCompat
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer
import net.minecraft.core.Direction
import net.minecraft.world.level.block.DoorBlock
import org.joml.Matrix3f
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
        val normal = poseStack.last().normal()
        val renderType = if (IrisCompat.isModLoaded && IrisCompat.getCurrentPack().isPresent) RenderType.entitySolid(
            TheEndPortalRenderer.END_PORTAL_LOCATION
        ) else RenderType.endPortal()
        val progress =
            if (IrisCompat.isModLoaded && IrisCompat.getCurrentPack().isPresent) IrisCompat.frameTimeCounter * 0.01f % 1.0f else 0.0f
        renderPortal(blockEntity, matrix4f, normal, buffer.getBuffer(renderType), progress, packedLight, packedOverlay)
    }

    private fun renderPortal(
        blockEntity: TeleportDoorBlockEntity,
        pose: Matrix4f,
        normal: Matrix3f,
        consumer: VertexConsumer,
        progress: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val state = blockEntity.blockState
        val facing = state.getValue(DoorBlock.FACING)
        val plane = TeleportDoorBlock.getTouchPlane(state).toFloat()

        when (facing) {
            Direction.SOUTH,
            Direction.NORTH -> renderHorizontalPlane(
                pose,
                normal,
                consumer,
                plane,
                facing,
                progress,
                packedLight,
                packedOverlay
            )

            Direction.EAST,
            Direction.WEST -> renderVerticalPlane(
                pose,
                normal,
                consumer,
                plane,
                facing,
                progress,
                packedLight,
                packedOverlay
            )

            else -> Unit
        }
    }

    private fun renderHorizontalPlane(
        pose: Matrix4f,
        normal: Matrix3f,
        consumer: VertexConsumer,
        z: Float,
        direction: Direction,
        progress: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        vertex(consumer, pose, normal, 0f, 0f, z, 0f, 0f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, 1f, 0f, z, 0f, 0.2f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, 1f, 1f, z, 0.2f, 0.2f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, 0f, 1f, z, 0.2f, 0f, direction, progress, packedLight, packedOverlay)

        vertex(consumer, pose, normal, 0f, 1f, z, 0.2f, 0f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, 1f, 1f, z, 0.2f, 0.2f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, 1f, 0f, z, 0f, 0.2f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, 0f, 0f, z, 0f, 0f, direction, progress, packedLight, packedOverlay)
    }

    private fun renderVerticalPlane(
        pose: Matrix4f,
        normal: Matrix3f,
        consumer: VertexConsumer,
        x: Float,
        direction: Direction,
        progress: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        vertex(consumer, pose, normal, x, 0f, 0f, 0f, 0f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, x, 0f, 1f, 0f, 0.2f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, x, 1f, 1f, 0.2f, 0.2f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, x, 1f, 0f, 0.2f, 0f, direction, progress, packedLight, packedOverlay)

        vertex(consumer, pose, normal, x, 1f, 0f, 0.2f, 0f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, x, 1f, 1f, 0.2f, 0.2f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, x, 0f, 1f, 0f, 0.2f, direction, progress, packedLight, packedOverlay)
        vertex(consumer, pose, normal, x, 0f, 0f, 0f, 0f, direction, progress, packedLight, packedOverlay)
    }

    private fun vertex(
        consumer: VertexConsumer,
        pose: Matrix4f,
        normal: Matrix3f,
        x: Float,
        y: Float,
        z: Float,
        u: Float,
        v: Float,
        direction: Direction,
        progress: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        consumer.vertex(pose, x, y, z)
            .color(0.075f, 0.15f, 0.2f, 1.0f)
            .uv(u + progress, v + progress)
            .overlayCoords(packedOverlay)
            .uv2(packedLight)
            .normal(
                normal,
                direction.stepX.toFloat(),
                direction.stepY.toFloat(),
                direction.stepZ.toFloat()
            )
            .endVertex()
    }
}