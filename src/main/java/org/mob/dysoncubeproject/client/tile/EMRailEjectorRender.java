package org.mob.dysoncubeproject.client.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mob.dysoncubeproject.block.EMRailEjectorControllerBlock;
import org.mob.dysoncubeproject.block.tile.EMRailEjectorBlockEntity;
import org.mob.dysoncubeproject.client.DCPExtraModels;
import org.mob.dysoncubeproject.client.DCPRenderTypes;
import org.mob.dysoncubeproject.client.render.DCPGeometry;
import org.mob.dysoncubeproject.client.render.DCPRender;

public class EMRailEjectorRender implements BlockEntityRenderer<EMRailEjectorBlockEntity, EMRailEjectorRender.State> {

    private static final float LINE_WIDTH = 0.012f;

    public static class State extends BlockEntityRenderState {
        public float yaw, pitch, progress, maxProgress, partial;
        public long gameTime, lastExecution;
        public boolean hasLevel;
        public QuadCollection base, gun, projectile;
    }

    private final RandomSource random = RandomSource.create();

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EMRailEjectorBlockEntity be, State s, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, cameraPos, crumbling);
        s.yaw = be.getCurrentYaw();
        s.pitch = be.getCurrentPitch();
        s.progress = be.getProgressBarComponent().getProgress();
        s.maxProgress = be.getProgressBarComponent().getMaxProgress();
        s.lastExecution = be.getLastExecution();
        s.partial = partialTick;
        s.hasLevel = be.getLevel() != null;
        s.gameTime = s.hasLevel ? be.getLevel().getGameTime() : 0;
        s.base = DCPRender.model(DCPExtraModels.EM_RAILEJECTOR_BASE);
        s.gun = DCPRender.model(DCPExtraModels.EM_RAILEJECTOR_GUN);
        s.projectile = DCPRender.model(DCPExtraModels.EM_RAILEJECTOR_PROJECTILE);
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        DCPRender.submitModel(collector, pose, s.base, s.lightCoords);

        pose.translate(0, 2.5, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-90));
        pose.translate(1, 0, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-90));
        pose.mulPose(Axis.ZP.rotationDegrees(90));
        pose.rotateAround(Axis.XP.rotationDegrees(360 - s.yaw), 0, 0.5f, 0.5f);
        pose.rotateAround(Axis.ZP.rotationDegrees(360 - s.pitch), 0, 0.5f, 0.5f);

        DCPRender.submitModel(collector, pose, s.gun, s.lightCoords);

        if (s.hasLevel) {
            final float gameTime = s.gameTime;
            final float time = DCPGeometry.seconds(s.gameTime, s.partial);
            float shootWindow = 28f;
            float chargeWindow = 85f;
            float period = s.maxProgress;
            float t = s.progress;

            // CHARGING: electric arcs around the muzzle
            if (t >= period - chargeWindow) {
                final float chargeT = (t - (period - chargeWindow)) / chargeWindow;
                final float intensity = (float) Math.pow(chargeT, 3.0);
                pose.pushPose();
                pose.translate(0.12, 0.45, 0.5);
                collector.submitCustomGeometry(pose, DCPRenderTypes.railElectricLines(), (p, vc) -> {
                    int segments = 7;
                    float baseRadius = 0.62f + 0.05f * (float) Math.sin((gameTime + s.partial) * 0.2f);
                    float jitter = 0.05f;
                    float rf = 100 / 255f, gf = 200 / 255f, bf = 1f;
                    float af = Math.min(255, 60 + (int) (195 * intensity)) / 255f;
                    for (int ring = 0; ring < 38 * chargeT; ring++) {
                        float ringOffsetX = 0.05f * ring;
                        for (int i = 0; i < segments; i++) {
                            float seed = i * 17.0f + ring * 31.0f + random.nextFloat() * 6f;
                            float ang = (float) Math.toRadians((i * (360f / segments)) + (float) Math.sin((gameTime + s.partial + seed) * 0.6f) * 20f);
                            float ang2 = ang + (float) Math.toRadians(10 + (float) Math.sin((gameTime + s.partial + seed * 1.37f) * 0.9f) * 12f);
                            float rad1 = baseRadius + (float) Math.sin((gameTime + s.partial + seed) * 0.8f) * jitter;
                            float rad2 = baseRadius + 0.07f + (float) Math.sin((gameTime + s.partial + seed * 0.77f) * 0.8f) * jitter;
                            float expand = 1.0f + 0.04f * intensity;
                            float y1 = (float) Math.cos(ang) * rad1 * expand, z1 = (float) Math.sin(ang) * rad1 * expand;
                            float y2 = (float) Math.cos(ang2) * rad2 * expand, z2 = (float) Math.sin(ang2) * rad2 * expand;
                            DCPGeometry.ribbon(vc, p, ringOffsetX, y1, z1, 0.12f + ringOffsetX, y2, z2,
                                    LINE_WIDTH, time, 1.0f, rf, gf, bf, af);
                        }
                    }
                });
                pose.popPose();
            }

            t = s.gameTime - s.lastExecution;
            final float progress = t / shootWindow;

            // RAIL BEAM
            if (t > 0 && t < shootWindow) {
                pose.pushPose();
                pose.translate(0.12, 0.45, 0.5);
                final float beamLen = 160.0f * (2.0f - progress * 2);
                final float halfW = 0.10f + 0.06f * (1.0f - progress);
                final float beamIntensity = 1.2f * (1.0f - progress);
                collector.submitCustomGeometry(pose, DCPRenderTypes.railBeam(), (p, vc) -> {
                    float r = 0.9f, g = 1f, b = 1f, a = 1f;
                    DCPGeometry.vertex(vc, p, 0, -halfW, 0, time, beamIntensity, r, g, b, a);
                    DCPGeometry.vertex(vc, p, beamLen, -halfW, 0, time, beamIntensity, r, g, b, a);
                    DCPGeometry.vertex(vc, p, beamLen, halfW, 0, time, beamIntensity, r, g, b, a);
                    DCPGeometry.vertex(vc, p, 0, halfW, 0, time, beamIntensity, r, g, b, a);
                    DCPGeometry.vertex(vc, p, 0, 0, -halfW, time, beamIntensity, r, g, b, a);
                    DCPGeometry.vertex(vc, p, beamLen, 0, -halfW, time, beamIntensity, r, g, b, a);
                    DCPGeometry.vertex(vc, p, beamLen, 0, halfW, time, beamIntensity, r, g, b, a);
                    DCPGeometry.vertex(vc, p, 0, 0, halfW, time, beamIntensity, r, g, b, a);
                });
                pose.popPose();
            }

            // SHOCKWAVE RING
            float shockDur = 6.0f;
            if (t > 0 && t < shockDur) {
                pose.pushPose();
                pose.translate(0.12, 0.45, 0.5);
                final float radius = 0.2f + 0.9f * (t / shockDur);
                collector.submitCustomGeometry(pose, DCPRenderTypes.railElectricLines(), (p, vc) -> {
                    int segs = 32;
                    for (int depth = 0; depth < 7; depth++) {
                        for (int i = 0; i < segs; i++) {
                            double a0 = (Math.PI * 2 * i) / segs, a1 = (Math.PI * 2 * (i + 1)) / segs;
                            DCPGeometry.ribbon(vc, p,
                                    depth * 0.5f, (float) Math.cos(a0) * radius, (float) Math.sin(a0) * radius,
                                    depth * 0.5f, (float) Math.cos(a1) * radius, (float) Math.sin(a1) * radius,
                                    LINE_WIDTH, time, 1.0f, 1f, 1f, 1f, 1f);
                        }
                    }
                });
                pose.popPose();
            }

            // PROJECTILE
            if (s.projectile != null && t > 0 && t < shootWindow) {
                float distance = 0.5f + progress * 1000f;
                pose.pushPose();
                pose.translate(0.75, -0.1, 0);
                pose.translate(distance, 0, 0);
                DCPRender.submitModel(collector, pose, s.projectile, s.lightCoords);
                collector.submitCustomGeometry(pose, DCPRenderTypes.railBeam(), (p, vc) -> {
                    float h = 0.18f, r = 0.9f, g = 1f, b = 1f, a = 1f, i = 1.0f;
                    DCPGeometry.vertex(vc, p, -h, -h, 0, time, i, r, g, b, a); DCPGeometry.vertex(vc, p, h, -h, 0, time, i, r, g, b, a);
                    DCPGeometry.vertex(vc, p, h, h, 0, time, i, r, g, b, a); DCPGeometry.vertex(vc, p, -h, h, 0, time, i, r, g, b, a);
                    DCPGeometry.vertex(vc, p, -h, 0, -h, time, i, r, g, b, a); DCPGeometry.vertex(vc, p, h, 0, -h, time, i, r, g, b, a);
                    DCPGeometry.vertex(vc, p, h, 0, h, time, i, r, g, b, a); DCPGeometry.vertex(vc, p, -h, 0, h, time, i, r, g, b, a);
                });
                pose.popPose();
            }
        }
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(EMRailEjectorBlockEntity blockEntity) {
        return EMRailEjectorControllerBlock.MULTIBLOCK_STRUCTURE.getAABB(blockEntity.getBlockPos());
    }
}