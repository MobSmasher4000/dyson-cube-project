package org.mob.dysoncubeproject.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.block.DefaultMultiblockControllerBlock;
import org.mob.dysoncubeproject.client.DCPRenderTypes;
import org.mob.dysoncubeproject.client.DCPShaders;
import org.mob.dysoncubeproject.multiblock.MultiblockStructure;

public class HologramRender {

    public record HoloData(BlockPos min, int sizeX, int sizeY, int sizeZ, int halfX, int halfZ,
                           boolean valid, float time, float r, float g, float b, Vec3 camera) {}

    public static final ContextKey<HoloData> HOLO_DATA =
            new ContextKey<>(Identifier.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "hologram"));

    private static final HoloData INACTIVE = new HoloData(null, 0, 0, 0, 0, 0, false, 0f, 0f, 0f, 0f, Vec3.ZERO);

    public static void onExtract(ExtractLevelRenderStateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        var level = event.getLevel();
        event.getRenderState().setRenderData(HOLO_DATA, INACTIVE);
        if (player == null) return;

        DefaultMultiblockControllerBlock<?> controller = getHeldController(player.getMainHandItem());
        if (controller == null) controller = getHeldController(player.getOffhandItem());
        if (controller == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit)) return;

        BlockPos anchor = hit.getBlockPos().relative(hit.getDirection());
        MultiblockStructure structure = controller.getMultiblockStructure();
        int sizeX = structure.getSizeX(), sizeY = structure.getSizeY(), sizeZ = structure.getSizeZ();
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) return;

        int halfX = sizeX / 2, halfZ = sizeZ / 2;
        BlockPos min = anchor.offset(-halfX, 0, -halfZ);
        boolean valid = structure.validateSpace(level, anchor);

        float partial = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float pulseTime = (float) ((level.getGameTime() % 2000) + partial);
        float s = (float) (0.5 + 0.5 * Math.sin(pulseTime * 0.02));
        float r = valid ? (0.2f + 0.3f * (1.0f - s)) : (0.8f + 0.2f * s);
        float g = valid ? (0.9f * s + 0.4f * (1.0f - s)) : (0.2f + 0.2f * (1.0f - s));
        float b = valid ? 0.9f : (0.1f + 0.2f * (1.0f - s));

        event.getRenderState().setRenderData(HOLO_DATA, new HoloData(min, sizeX, sizeY, sizeZ, halfX, halfZ, valid,
                DCPGeometry.seconds(level.getGameTime(), partial), r, g, b, event.getCamera().position()));
    }

    public static void onSubmit(SubmitCustomGeometryEvent event) {
        HoloData d = event.getLevelRenderState().getRenderData(HOLO_DATA);
        if (d == null || d.min() == null || DCPShaders.HOLOGRAM == null) return;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(d.min().getX() - d.camera().x, d.min().getY() - d.camera().y, d.min().getZ() - d.camera().z);

        float valid = d.valid() ? 1.0f : 0.0f;
        final float time = d.time();

        event.getSubmitNodeCollector().submitCustomGeometry(pose, DCPRenderTypes.hologram(), (p, vc) -> {
            drawBoxFaces(p, vc, 0.0, 0.001, 0.0, d.sizeX(), d.sizeY(), d.sizeZ(),
                    time, valid, d.r(), d.g(), d.b(), 0.85f);

            double cx = d.halfX(), cz = d.halfZ();
            float hr = d.valid() ? 0.35f : 1.0f, hg = d.valid() ? 1.0f : 0.35f, hb = d.valid() ? 1.0f : 0.25f;
            drawBoxFaces(p, vc, cx + 0.002, 0.002, cz + 0.002, cx + 1.0 - 0.002, 1.0 - 0.002, cz + 1.0 - 0.002,
                    time, valid, hr, hg, hb, 0.95f);
        });
        pose.popPose();
    }

    private static DefaultMultiblockControllerBlock<?> getHeldController(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DefaultMultiblockControllerBlock<?> controller) {
            return controller;
        }
        return null;
    }

    private static void v(VertexConsumer vc, PoseStack.Pose p, double x, double y, double z, float t, float valid, float r, float g, float b, float a) {
        DCPGeometry.vertex(vc, p, (float) x, (float) y, (float) z, t, valid, r, g, b, a);
    }

    private static void drawBoxFaces(PoseStack.Pose p, VertexConsumer vc, double x0, double y0, double z0, double x1, double y1, double z1,
                                     float t, float valid, float r, float g, float b, float a) {
        // bottom
        v(vc, p, x0, y0, z0, t, valid, r, g, b, a); v(vc, p, x1, y0, z0, t, valid, r, g, b, a); v(vc, p, x1, y0, z1, t, valid, r, g, b, a); v(vc, p, x0, y0, z1, t, valid, r, g, b, a);
        // top
        v(vc, p, x0, y1, z0, t, valid, r, g, b, a); v(vc, p, x0, y1, z1, t, valid, r, g, b, a); v(vc, p, x1, y1, z1, t, valid, r, g, b, a); v(vc, p, x1, y1, z0, t, valid, r, g, b, a);
        // north
        v(vc, p, x0, y0, z0, t, valid, r, g, b, a); v(vc, p, x0, y1, z0, t, valid, r, g, b, a); v(vc, p, x1, y1, z0, t, valid, r, g, b, a); v(vc, p, x1, y0, z0, t, valid, r, g, b, a);
        // south
        v(vc, p, x0, y0, z1, t, valid, r, g, b, a); v(vc, p, x1, y0, z1, t, valid, r, g, b, a); v(vc, p, x1, y1, z1, t, valid, r, g, b, a); v(vc, p, x0, y1, z1, t, valid, r, g, b, a);
        // west
        v(vc, p, x0, y0, z0, t, valid, r, g, b, a); v(vc, p, x0, y0, z1, t, valid, r, g, b, a); v(vc, p, x0, y1, z1, t, valid, r, g, b, a); v(vc, p, x0, y1, z0, t, valid, r, g, b, a);
        // east
        v(vc, p, x1, y0, z0, t, valid, r, g, b, a); v(vc, p, x1, y1, z0, t, valid, r, g, b, a); v(vc, p, x1, y1, z1, t, valid, r, g, b, a); v(vc, p, x1, y0, z1, t, valid, r, g, b, a);
    }
}