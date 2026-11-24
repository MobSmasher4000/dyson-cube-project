package org.mob.dysoncubeproject.client;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class DCPRenderTypes {

    private static RenderType DYSON_SUN;
    private static RenderType HOLOGRAM;
    private static RenderType HOLO_HEX;
    private static RenderType RAIL_ELECTRIC_LINES;
    private static RenderType RAIL_BEAM;

    public static RenderType dysonSun() {
        if (DYSON_SUN == null) {
            RenderSetup setup = RenderSetup.builder(DCPShaders.DYSON_SUN)
                    .createRenderSetup();

            DYSON_SUN = RenderType.create("dysoncubeproject_sun", setup);
        }
        return DYSON_SUN;
    }

    public static RenderType hologram() {
        if (HOLOGRAM == null) {
            RenderSetup setup = RenderSetup.builder(DCPShaders.HOLOGRAM)
                    .createRenderSetup();

            HOLOGRAM = RenderType.create("dysoncubeproject_hologram", setup);
        }
        return HOLOGRAM;
    }

    public static RenderType railElectricLines() {
        if (RAIL_ELECTRIC_LINES == null) {
            RenderSetup setup = RenderSetup.builder(DCPShaders.RAIL_ELECTRIC)
                    .createRenderSetup();

            RAIL_ELECTRIC_LINES = RenderType.create("dysoncubeproject_rail_electric_lines", setup);
        }
        return RAIL_ELECTRIC_LINES;
    }

    public static RenderType holoHex() {
        if (HOLO_HEX == null) {
            RenderSetup setup = RenderSetup.builder(DCPShaders.HOLO_HEX)
                    .createRenderSetup();

            HOLO_HEX = RenderType.create("dysoncubeproject_holo_hex", setup);
        }
        return HOLO_HEX;
    }

    public static RenderType railBeam() {
        if (RAIL_BEAM == null) {
            RenderSetup setup = RenderSetup.builder(DCPShaders.RAIL_BEAM)
                    .createRenderSetup();

            RAIL_BEAM = RenderType.create("dysoncubeproject_rail_beam", setup);
        }
        return RAIL_BEAM;
    }
}