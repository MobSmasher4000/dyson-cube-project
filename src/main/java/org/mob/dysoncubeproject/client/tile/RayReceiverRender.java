package org.mob.dysoncubeproject.client.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.mob.dysoncubeproject.block.entity.RayReceiverBlockEntity;
import org.mob.dysoncubeproject.client.DCPExtraModels;
import org.mob.dysoncubeproject.client.DCPRenderTypes;
import org.mob.dysoncubeproject.client.DCPShaders;

public class RayReceiverRender implements BlockEntityRenderer<RayReceiverBlockEntity> {

    private static void drawBoxTopFace(PoseStack pose, VertexConsumer vc, float minX, float y, float minZ, float maxX, float maxZ, float r, float g, float b, float a) {
        // Top (XZ plane)
        emit(vc, pose, minX, y, minZ, minX, minZ, r, g, b, a);
        emit(vc, pose, minX, y, maxZ, minX, maxZ, r, g, b, a);
        emit(vc, pose, maxX, y, maxZ, maxX, maxZ, r, g, b, a);
        emit(vc, pose, maxX, y, minZ, maxX, minZ, r, g, b, a);
    }

    private static void drawBoxSideFace(PoseStack pose, VertexConsumer vc, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        // North (z = minZ)
        emit(vc, pose, minX, minY, minZ, minX, minY, r, g, b, a);
        emit(vc, pose, minX, maxY, minZ, minX, maxY, r, g, b, a);
        emit(vc, pose, maxX, maxY, minZ, maxX, maxY, r, g, b, a);
        emit(vc, pose, maxX, minY, minZ, maxX, minY, r, g, b, a);
        // South (z = maxZ)
        emit(vc, pose, minX, minY, maxZ, minX, minY, r, g, b, a);
        emit(vc, pose, maxX, minY, maxZ, maxX, minY, r, g, b, a);
        emit(vc, pose, maxX, maxY, maxZ, maxX, maxY, r, g, b, a);
        emit(vc, pose, minX, maxY, maxZ, minX, maxY, r, g, b, a);
        // West (x = minX)
        emit(vc, pose, minX, minY, minZ, minZ, minY, r, g, b, a);
        emit(vc, pose, minX, minY, maxZ, maxZ, minY, r, g, b, a);
        emit(vc, pose, minX, maxY, maxZ, maxZ, maxY, r, g, b, a);
        emit(vc, pose, minX, maxY, minZ, minZ, maxY, r, g, b, a);
        // East (x = maxX)
        emit(vc, pose, maxX, minY, minZ, minZ, minY, r, g, b, a);
        emit(vc, pose, maxX, maxY, minZ, minZ, maxY, r, g, b, a);
        emit(vc, pose, maxX, maxY, maxZ, maxZ, maxY, r, g, b, a);
        emit(vc, pose, maxX, minY, maxZ, maxZ, minY, r, g, b, a);
    }

    private static void emit(VertexConsumer vc, PoseStack pose, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        vc.vertex(pose.last().pose(), x, y, z).color(r, g, b, a).uv(u, v).endVertex();
    }

    @Override
    public void render(RayReceiverBlockEntity rayReceiverBlockEntity, float partial, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.solid()), null, DCPExtraModels.RAY_RECEIVER_BASE, 1.0f, 1.0f, 1.0f, combinedLightIn, combinedOverlayIn);

        poseStack.translate(0, 2, 0);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.solid()), null, DCPExtraModels.RAY_RECEIVER_PLATE, 1.0f, 1.0f, 1.0f, combinedLightIn, combinedOverlayIn);

        poseStack.pushPose();
        poseStack.translate(0, 2, 1);
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.solid()), null, DCPExtraModels.RAY_RECEIVER_LENS_STANDS, 1.0f, 1.0f, 1.0f, combinedLightIn, combinedOverlayIn);

        poseStack.translate(0, 0.55f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.mulPose(Axis.XP.rotationDegrees(360 - rayReceiverBlockEntity.getCurrentPitch() - 180));
        poseStack.translate(0, -0.55f, -0.5f);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.solid()), null, DCPExtraModels.RAY_RECEIVER_LENS, 1.0f, 1.0f, 1.0f, combinedLightIn, combinedOverlayIn);
        poseStack.popPose();

        if (DCPShaders.RECEIVER_HEX != null) {
            ShaderInstance shader = DCPShaders.RECEIVER_HEX;

            var time = shader.getUniform("uTime");
            if (time != null) {
                time.set((rayReceiverBlockEntity.getLevel().getGameTime() % 100000) / 20.0f);
            }

            float r = 0.5f, g = 0.9f, b = 0.9f;
            VertexConsumer quad = multiBufferSource.getBuffer(DCPRenderTypes.receiverHex());

            drawBoxTopFace(poseStack, quad, -1, 0.3f, -1, 2, 2, r, g, b, 0.85f);
            drawBoxSideFace(poseStack, quad, 0.2499f, 0.5f, 0.2499f, 0.751f, 1.75f, 0.751f, r, g, b, 0.25f);
        }

        poseStack.popPose();
    }
}