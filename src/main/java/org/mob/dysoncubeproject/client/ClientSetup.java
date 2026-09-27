package org.mob.dysoncubeproject.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.block.ModBlocks;
import org.mob.dysoncubeproject.block.entity.*;
import org.mob.dysoncubeproject.client.render.HologramRender;
import org.mob.dysoncubeproject.client.render.SkyRender;
import org.mob.dysoncubeproject.client.tile.EMRailEjectorRender;
import org.mob.dysoncubeproject.client.tile.RayReceiverRender;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.*;

import java.util.function.Function;

public class ClientSetup {

    public static void init() {
        EventManager.forge(RenderHighlightEvent.Block.class).process(HologramRender::blockOverlayEvent).subscribe();
        EventManager.forge(RenderLevelStageEvent.class).process(SkyRender::onRenderStage).subscribe();
        EventManager.mod(RegisterShadersEvent.class).process(ClientSetup::registerShaders).subscribe();

        EventManager.mod(ModelEvent.BakingCompleted.class).process(event -> {
            ModelBakery modelBakery = event.getModelBakery();

            ModelBaker baker = new ModelBaker() {
                @Override
                public UnbakedModel getModel(ResourceLocation location) {
                    return modelBakery.getModel(location);
                }

                @Override
                public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
                    return Material::sprite;
                }

                @Override
                public @Nullable BakedModel bake(ResourceLocation location, ModelState state) {
                    return event.getModels().get(location);
                }

                @Override
                public @Nullable BakedModel bake(ResourceLocation location, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
                    UnbakedModel model = getModel(location);
                    return model != null ? model.bake(this, sprites, state, location) : null;
                }
            };

            DCPExtraModels.EM_RAILEJECTOR_BASE = bakeModel(ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "block/em_railejector_base"), modelBakery, baker);
            DCPExtraModels.EM_RAILEJECTOR_GUN = bakeModel(ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "block/em_railejector_gun"), modelBakery, baker);
            DCPExtraModels.EM_RAILEJECTOR_PROJECTILE = bakeModel(ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "block/em_railejector_projectile"), modelBakery, baker);
            DCPExtraModels.RAY_RECEIVER_BASE = bakeModel(ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "block/ray_receiver_base"), modelBakery, baker);
            DCPExtraModels.RAY_RECEIVER_PLATE = bakeModel(ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "block/ray_receiver_plate"), modelBakery, baker);
            DCPExtraModels.RAY_RECEIVER_LENS = bakeModel(ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "block/ray_receiver_lens"), modelBakery, baker);
            DCPExtraModels.RAY_RECEIVER_LENS_STANDS = bakeModel(ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "block/ray_receiver_lens_stands"), modelBakery, baker);
        }).subscribe();

        EventManager.mod(EntityRenderersEvent.RegisterRenderers.class).process(event -> {
            event.registerBlockEntityRenderer((BlockEntityType<? extends EMRailEjectorBlockEntity>) ModBlocks.EM_RAILEJECTOR_CONTROLLER.getRight().get(), context -> new EMRailEjectorRender());
            event.registerBlockEntityRenderer((BlockEntityType<? extends RayReceiverBlockEntity>) ModBlocks.RAY_RECEIVER_CONTROLLER.getRight().get(), context -> new RayReceiverRender());
        }).subscribe();
    }

    public static void registerShaders(RegisterShadersEvent event) {
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "hologram"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> DCPShaders.HOLOGRAM = s);
        } catch (Exception e) {
            DCPShaders.HOLOGRAM = null;
        }
        // Register Dyson Sun shader
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "dyson_sun"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> DCPShaders.DYSON_SUN = s);
        } catch (Exception e) {
            DCPShaders.DYSON_SUN = null;
        }
        // Register Holographic Hex shader
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "holo_hex"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> DCPShaders.HOLO_HEX = s);
        } catch (Exception e) {
            DCPShaders.HOLO_HEX = null;
        }
        // Register Rail Electric shader
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "rail_electric"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> DCPShaders.RAIL_ELECTRIC = s);
        } catch (Exception e) {
            DCPShaders.RAIL_ELECTRIC = null;
        }
        // Register Rail Beam shader
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "rail_beam"), DefaultVertexFormat.POSITION_COLOR);
            event.registerShader(shader, s -> DCPShaders.RAIL_BEAM = s);
        } catch (Exception e) {
            DCPShaders.RAIL_BEAM = null;
        }
    }

    private static BakedModel bakeModel(ResourceLocation model, ModelBakery modelBakery, ModelBaker baker) {
        UnbakedModel unbakedModel = modelBakery.getModel(model);
        return unbakedModel.bake(baker, Material::sprite, BlockModelRotation.X0_Y0, model);
    }
}