package org.mob.dysoncubeproject.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.mob.dysoncubeproject.Config;
import org.mob.dysoncubeproject.client.DCPRenderTypes;
import org.mob.dysoncubeproject.client.DCPShaders;
import org.mob.dysoncubeproject.world.ClientDysonSphere;

public class SkyRender {

    public static void onRenderStage(RenderLevelStageEvent event) {
        // Draw after vanilla sky (sun/moon) so we render "on top of the sun"
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.isRainingAt(mc.player.getOnPos())) return;

        var subscribedTo = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSubscribedPlayers().getOrDefault(mc.player.getStringUUID(), mc.player.getStringUUID());
        var sphere = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().getOrDefault(subscribedTo, null);

        if (sphere == null) return;
        var progress = (float) sphere.getProgress();
        if (Config.SHOW_AT_MAX_PROGRESS) progress = 1f;
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        float skyAngle = mc.level.getTimeOfDay(event.getPartialTick()) * 360.0f;

        if (DCPShaders.DYSON_SUN != null) {
            // Update custom uniforms for the shader
            ShaderInstance shader = DCPShaders.HOLO_HEX;

            try {

                var uTime = shader.getUniform("uTime");
                if (uTime != null) uTime.set((Minecraft.getInstance().level.getGameTime() % 100000) / 20.0f);
                var uValid = shader.getUniform("uValid");
                if (uValid != null) uValid.set(true ? 1.0f : 0.0f);
                var uSize = shader.getUniform("uSize");
                if (uSize != null) uSize.set(25f);
                var uCam = shader.getUniform("uCamPos");
                if (uCam != null) {
                    var c = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                    try {
                        uCam.set(0f, 0f, 0f);
                    } catch (Throwable t) {
                        try {
                            uCam.set((float) c.x, (float) c.y, (float) c.z, 1.0f);
                        } catch (Throwable ignored2) {
                        }
                    }
                }
            } catch (Throwable ignored) {
            }


            pose.pushPose();
            //pose.mulPose(event.getModelViewMatrix());
            pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.mulPose(Axis.XP.rotationDegrees(skyAngle));

            pose.translate(-30.0f, 0.0f, -310.0f);

            // Build a square in local XY plane; shader expects half-extent ~30 (it divides by 60.0 for UV)
            float s = 30.0f;
            float r = 0.5f, g = 0.9f, b = 0.9f, a = 0.7f;
            float rainLevel = 1.0F - mc.level.getRainLevel(event.getPartialTick());
            a *= rainLevel;

            // Render distance proof
            Matrix4f currentProj = event.getProjectionMatrix();
            float extractedFov = (float) (2.0 * Math.atan(1.0 / currentProj.m11()));
            float extractedAspect = currentProj.m11() / currentProj.m00();
            Matrix4f infiniteProj = new Matrix4f().setPerspective(extractedFov, extractedAspect, 0.05F, 10000.0F);

            // Force the matrix into the global system
            RenderSystem.setProjectionMatrix(infiniteProj, VertexSorting.DISTANCE_TO_ORIGIN);

            // Setup the custom render state manually
            RenderType holoType = DCPRenderTypes.holoHex();
            holoType.setupRenderState();

            // Draw DIRECTLY to the GPU, bypassing the deferred MultiBufferSource
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Quad (counter-clockwise) at z=0 in local space after transformations
            emit(bufferbuilder, pose, 0, s, 0.0f, r, g, b, a);
            emit(bufferbuilder, pose, s * 2 * progress, s, 0.0f, r, g, b, a);
            emit(bufferbuilder, pose, s * 2 * progress, -s, 0.0f, r, g, b, a);
            emit(bufferbuilder, pose, 0, -s, 0.0f, r, g, b, a);

            // Flush this render type to ensure it draws this frame
            tesselator.end();

            // Cleanup the state
            holoType.clearRenderState();
            RenderSystem.setProjectionMatrix(currentProj, VertexSorting.DISTANCE_TO_ORIGIN);

            pose.popPose();

        }

    }

    private static void emit(VertexConsumer vc, PoseStack pose, float x, float y, float z, float r, float g, float b, float a) {
        // Submit world-space coordinates; the shader's ModelViewMat from the pose will handle transforms
        vc.vertex(pose.last().pose(), x, y, z).color(r, g, b, a).endVertex();
    }

}