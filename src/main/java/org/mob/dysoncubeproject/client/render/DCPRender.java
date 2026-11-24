package org.mob.dysoncubeproject.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import org.jetbrains.annotations.Nullable;

public final class DCPRender {

    private DCPRender() {}

    public static @Nullable QuadCollection model(@Nullable QuadCollection model) {
        return model;
    }

    public static void submitModel(SubmitNodeCollector collector, PoseStack pose, @Nullable QuadCollection model, int lightCoords) {
        if (model == null) return;

        QuadInstance instance = new QuadInstance();
        instance.setColor(0xFFFFFFFF);
        instance.setLightCoords(lightCoords);
        instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

        collector.submitCustomGeometry(pose, RenderTypes.solidMovingBlock(), (p, vc) -> {
            for (var quad : model.getAll()) {
                vc.putBakedQuad(p, quad, instance);
            }
        });
    }
}