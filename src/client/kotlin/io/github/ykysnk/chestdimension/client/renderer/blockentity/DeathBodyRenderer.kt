package io.github.ykysnk.chestdimension.client.renderer.blockentity

import com.mojang.blaze3d.vertex.PoseStack
import io.github.ykysnk.chestdimension.block.entity.DeathBodyBlockEntity
import net.minecraft.client.model.SkeletonModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.monster.Skeleton

class DeathBodyRenderer(
    context: BlockEntityRendererProvider.Context
) : BlockEntityRenderer<DeathBodyBlockEntity> {

    private val model = SkeletonModel<Skeleton>(context.bakeLayer(ModelLayers.SKELETON))

    override fun render(
        blockEntity: DeathBodyBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        poseStack.pushPose()

        poseStack.translate(0.5, 2.3, 0.9)
        poseStack.scale(-1.0f, -1.0f, 1.0f)
        poseStack.scale(2.0f, 2.0f, 2.0f)

        model.head.xScale = 0.7f
        model.head.yScale = 0.7f
        model.head.zScale = 0.7f

        model.head.xRot = Math.toRadians(42.0).toFloat()
        model.body.xRot = Math.toRadians(-10.0).toFloat()

        model.rightArm.xRot = Math.toRadians(-20.0).toFloat()
        model.leftArm.xRot = Math.toRadians(-20.0).toFloat()

        model.rightLeg.xRot = Math.toRadians(-90.0).toFloat()
        model.rightLeg.yRot = Math.toRadians(20.0).toFloat()
        model.leftLeg.xRot = Math.toRadians(-90.0).toFloat()
        model.leftLeg.yRot = Math.toRadians(-20.0).toFloat()

        model.renderToBuffer(
            poseStack,
            buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
            packedLight,
            packedOverlay,
            1f,
            1f,
            1f,
            1f
        )

        poseStack.popPose()
    }

    companion object {
        private val TEXTURE = ResourceLocation("minecraft", "textures/entity/skeleton/skeleton.png")
    }
}