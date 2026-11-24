package org.mob.dysoncubeproject.client.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mob.dysoncubeproject.block.RayReceiverControllerBlock;
import org.mob.dysoncubeproject.block.tile.RayReceiverBlockEntity;
import org.mob.dysoncubeproject.client.DCPExtraModels;
import org.mob.dysoncubeproject.client.DCPRenderTypes;
import org.mob.dysoncubeproject.client.render.DCPGeometry;
import org.mob.dysoncubeproject.client.render.DCPRender;
import org.mob.dysoncubeproject.util.DCPTime;

public class RayReceiverRender implements BlockEntityRenderer<RayReceiverBlockEntity, RayReceiverRender.State> {

    public static class State extends BlockEntityRenderState {
        public float pitch, partial, time;
        public BlockPos pos = BlockPos.ZERO;
        public QuadCollection base, plate, stands, lens;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RayReceiverBlockEntity be, State s, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, cameraPos, crumbling);
        s.pitch = Mth.rotLerp(partialTick, be.getPrevPitch(), be.getCurrentPitch());
        s.partial = partialTick;
        s.pos = be.getBlockPos();
        var level = be.getLevel();
        boolean canWork = level != null && DCPTime.isDay(level) && !level.isRaining() && level.canSeeSky(be.getBlockPos().above());
        s.time = canWork ? DCPGeometry.seconds(level.getGameTime(), partialTick) : -1f;
        s.base = DCPRender.model(DCPExtraModels.RAY_RECEIVER_BASE);
        s.plate = DCPRender.model(DCPExtraModels.RAY_RECEIVER_PLATE);
        s.stands = DCPRender.model(DCPExtraModels.RAY_RECEIVER_LENS_STANDS);
        s.lens = DCPRender.model(DCPExtraModels.RAY_RECEIVER_LENS);
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        DCPRender.submitModel(collector, pose, s.base, s.lightCoords);

        pose.translate(0, 2, 0);
        DCPRender.submitModel(collector, pose, s.plate, s.lightCoords);

        pose.pushPose();
        pose.translate(0, 2, 1);
        pose.mulPose(Axis.YP.rotationDegrees(90));
        DCPRender.submitModel(collector, pose, s.stands, s.lightCoords);

        pose.rotateAround(Axis.XP.rotationDegrees(-90), 0, 0.55f, 0.5f);
        pose.rotateAround(Axis.XP.rotationDegrees(360 - s.pitch - 180), 0, 0.55f, 0.5f);
        DCPRender.submitModel(collector, pose, s.lens, s.lightCoords);
        pose.popPose();

        if (s.time >= 0){
            final float t = s.time;
            final float size = 0.75f;
            final float ox = s.pos.getX() & 15;
            final float oz = s.pos.getZ() & 15;

            collector.submitCustomGeometry(pose, DCPRenderTypes.holoHex(), (p, vc) -> {
                float r = 0.5f, g = 0.9f, b = 0.9f;
                topFace(p, vc, -1, 0.3f, -1, 2, 2, ox, oz, t, size, r, g, b, 0.85f);
            });
        }

        pose.popPose();
    }

    private static void hexVertex(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z,
                                  float localU, float localV, float t, float size,
                                  float r, float g, float b, float a) {
        vc.addVertex(p, x, y, z)
                .setColor(r, g, b, a)
                .setUv(t, size)
                .setUv2(Math.round(localU * 1000f) + 10000, Math.round(localV * 1000f) + 10000);
    }

    private static void topFace(PoseStack.Pose p, VertexConsumer vc, float minX, float y, float minZ, float maxX, float maxZ,
                                float ox, float oz, float t, float size, float r, float g, float b, float a) {
        hexVertex(vc, p, minX, y, minZ, minX + ox, minZ + oz, t, size, r, g, b, a);
        hexVertex(vc, p, minX, y, maxZ, minX + ox, maxZ + oz, t, size, r, g, b, a);
        hexVertex(vc, p, maxX, y, maxZ, maxX + ox, maxZ + oz, t, size, r, g, b, a);
        hexVertex(vc, p, maxX, y, minZ, maxX + ox, minZ + oz, t, size, r, g, b, a);
    }

    @Override
    public AABB getRenderBoundingBox(RayReceiverBlockEntity blockEntity) {
        return RayReceiverControllerBlock.MULTIBLOCK_STRUCTURE.getAABB(blockEntity.getBlockPos());
    }
}