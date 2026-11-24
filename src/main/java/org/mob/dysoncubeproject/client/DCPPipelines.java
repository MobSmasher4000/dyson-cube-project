package org.mob.dysoncubeproject.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.mob.dysoncubeproject.DysonCubeProject;

/** Replaces ClientSetup#registerShaders and the old shader JSONs (delete those). */
public final class DCPPipelines {

    private DCPPipelines() {}

    public static void register(RegisterRenderPipelinesEvent event) {
        DCPShaders.HOLOGRAM = reg("hologram", BlendFunction.TRANSLUCENT);
        DCPShaders.HOLO_HEX = reg("holo_hex", BlendFunction.TRANSLUCENT);
        DCPShaders.DYSON_SUN = reg("dyson_sun", BlendFunction.TRANSLUCENT);

        DCPShaders.RAIL_ELECTRIC = reg("rail_electric", BlendFunction.LIGHTNING);
        DCPShaders.RAIL_BEAM = reg("rail_beam", BlendFunction.LIGHTNING);

        event.registerPipeline(DCPShaders.HOLOGRAM);
        event.registerPipeline(DCPShaders.HOLO_HEX);
        event.registerPipeline(DCPShaders.DYSON_SUN);
        event.registerPipeline(DCPShaders.RAIL_ELECTRIC);
        event.registerPipeline(DCPShaders.RAIL_BEAM);
    }

    private static RenderPipeline reg(String name, BlendFunction blend) {
        RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation(id("pipeline/" + name))
                .withVertexShader(id("core/" + name))
                .withFragmentShader(id("core/" + name))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withColorTargetState(new ColorTargetState(blend))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
        return pipeline;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DysonCubeProject.MOD_ID, path);
    }
}