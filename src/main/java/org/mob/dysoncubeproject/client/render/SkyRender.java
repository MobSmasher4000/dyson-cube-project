package org.mob.dysoncubeproject.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.mob.dysoncubeproject.Config;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.client.DCPShaders;
import org.mob.dysoncubeproject.world.ClientDysonSphere;

public class SkyRender {

    public record SkyData(float sunAngleRad, float progress, float rainBrightness, float timeSeconds) {}

    public static final ContextKey<SkyData> SKY_DATA =
            new ContextKey<>(Identifier.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "dyson_sky"));

    private static final SkyData INACTIVE = new SkyData(0f, 0f, 1f, 0f);

    public static void onExtract(ExtractLevelRenderStateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        event.getRenderState().setRenderData(SKY_DATA, INACTIVE);
        if (mc.player == null) return;
        var level = event.getLevel();
        if (level.isRainingAt(mc.player.getOnPos())) return;

        var subscribedTo = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSubscribedPlayers()
                .getOrDefault(mc.player.getStringUUID(), mc.player.getStringUUID());
        var sphere = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().getOrDefault(subscribedTo, null);
        if (sphere == null) return;

        float progress = (float) sphere.getProgress();
        if (Config.SHOW_AT_MAX_PROGRESS) progress = 1f;

        float partial = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float sunAngle = event.getCamera().attributeProbe().getValue(EnvironmentAttributes.SUN_ANGLE, partial)
                * ((float) Math.PI / 180F);
        float rainBrightness = 1.0F - level.getRainLevel(partial);

        event.getRenderState().setRenderData(SKY_DATA, new SkyData(sunAngle, progress, rainBrightness,
                DCPGeometry.seconds(level.getGameTime(), partial)));
    }

    public static void onAfterSky(RenderLevelStageEvent.AfterSky event) {
        SkyData data = event.getLevelRenderState().getRenderData(SKY_DATA);
        if (data == null || data.progress() <= 0f || DCPShaders.HOLO_HEX == null) return;

        Matrix4f modelView = new Matrix4f(event.getModelViewMatrix());
        float s = 30.0f;
        modelView.rotateY((float) Math.toRadians(-90.0))
                .rotateX((float) Math.toRadians(90.0))
                .rotateX(data.sunAngleRad())
                .translate(-30.0f, 0.0f, -310.0f)
                .scale(2.0f * s * data.progress(), s, 1.0f);   // unit quad x:[0,1] y:[-1,1]

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                modelView, new Vector4f(1f, 1f, 1f, 1f), new Vector3f(), new Matrix4f());

        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(6);
        GpuTextureView color = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView depth = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

        float a = 0.7f * data.rainBrightness();
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
            BufferBuilder b = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            float t = data.timeSeconds();
            b.addVertex(0f, 1f, 0f).setUv(t, 1f).setColor(0.5f, 0.9f, 0.9f, a);
            b.addVertex(1f, 1f, 0f).setUv(t, 1f).setColor(0.5f, 0.9f, 0.9f, a);
            b.addVertex(1f, -1f, 0f).setUv(t, 1f).setColor(0.5f, 0.9f, 0.9f, a);
            b.addVertex(0f, -1f, 0f).setUv(t, 1f).setColor(0.5f, 0.9f, 0.9f, a);

            try (MeshData mesh = b.buildOrThrow();
                 GpuBuffer vertices = RenderSystem.getDevice().createBuffer(() -> "Dyson sun quad", 32, mesh.vertexBuffer());
                 RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
                         .createRenderPass(() -> "Dyson sun", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
                pass.setPipeline(DCPShaders.DYSON_SUN);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", dynamicTransforms);
                pass.setVertexBuffer(0, vertices);
                pass.setIndexBuffer(indexBuffer, indices.type());
                pass.drawIndexed(0, 0, 6, 1);
            }
        }
    }
}