package org.mob.dysoncubeproject.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class DCPRenderTypes extends RenderType {

    private DCPRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static RenderType DYSON_SUN;
    private static RenderType HOLOGRAM;
    private static RenderType HOLO_HEX;
    private static RenderType RAIL_ELECTRIC_LINES;
    private static RenderType RAIL_BEAM;

    public static RenderType dysonSun() {
        if (DYSON_SUN == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> DCPShaders.DYSON_SUN))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);
            DYSON_SUN = RenderType.create("dysoncubeproject_sun",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state);
        }
        return DYSON_SUN;
    }

    public static RenderType hologram() {
        if (HOLOGRAM == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> DCPShaders.HOLOGRAM))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);
            HOLOGRAM = RenderType.create("dysoncubeproject_hologram",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state);
        }
        return HOLOGRAM;
    }

    public static RenderType railElectricLines() {
        if (RAIL_ELECTRIC_LINES == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> DCPShaders.RAIL_ELECTRIC))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);
            RAIL_ELECTRIC_LINES = RenderType.create("dysoncubeproject_rail_electric_lines",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state);
        }
        return RAIL_ELECTRIC_LINES;
    }

    public static RenderType holoHex() {
        if (HOLO_HEX == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> DCPShaders.HOLO_HEX))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);
            HOLO_HEX = RenderType.create("dysoncubeproject_holo_hex",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state);
        }
        return HOLO_HEX;
    }

    public static RenderType railBeam() {
        if (RAIL_BEAM == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> DCPShaders.RAIL_BEAM))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true);
            RAIL_BEAM = RenderType.create("dysoncubeproject_rail_beam",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state);
        }
        return RAIL_BEAM;
    }
}