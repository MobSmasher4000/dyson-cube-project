package org.mob.dysoncubeproject.client;

import com.hrznstudio.titanium.event.handler.EventManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.mob.dysoncubeproject.DCPAttachments;
import org.mob.dysoncubeproject.DCPContent;
import org.mob.dysoncubeproject.block.tile.EMRailEjectorBlockEntity;
import org.mob.dysoncubeproject.block.tile.RayReceiverBlockEntity;
import org.mob.dysoncubeproject.client.render.HologramRender;
import org.mob.dysoncubeproject.client.render.SkyRender;
import org.mob.dysoncubeproject.client.tile.EMRailEjectorRender;
import org.mob.dysoncubeproject.client.tile.RayReceiverRender;

public class ClientSetup {

    public static void init() {
        EventManager.forge(ExtractLevelRenderStateEvent.class).process(SkyRender::onExtract).subscribe();
        EventManager.forge(ExtractLevelRenderStateEvent.class).process(HologramRender::onExtract).subscribe();
        EventManager.forge(RenderLevelStageEvent.AfterSky.class).process(SkyRender::onAfterSky).subscribe();
        EventManager.forge(SubmitCustomGeometryEvent.class).process(HologramRender::onSubmit).subscribe();

        EventManager.mod(RegisterRenderPipelinesEvent.class).process(DCPPipelines::register).subscribe();
        EventManager.mod(ModelEvent.RegisterStandalone.class).process(DCPExtraModels::register).subscribe();
        EventManager.mod(ModelEvent.BakingCompleted.class).process(DCPExtraModels::load).subscribe();
        EventManager.mod(EntityRenderersEvent.RegisterRenderers.class).process(event -> {
            event.registerBlockEntityRenderer((BlockEntityType<? extends EMRailEjectorBlockEntity>) DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.type().get(), context -> new EMRailEjectorRender());
            event.registerBlockEntityRenderer((BlockEntityType<? extends RayReceiverBlockEntity>) DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.type().get(), context -> new RayReceiverRender());
        }).subscribe();

        EventManager.forge(ItemTooltipEvent.class).process(itemTooltipEvent -> {
            var stack = itemTooltipEvent.getItemStack();
            if (stack.getOrDefault(DCPAttachments.SOLAR_SAIL, 0) > 0) {
                itemTooltipEvent.getToolTip().add(Component.translatable("tooltip.dysoncubeproject.contains_solar_sails", stack.getOrDefault(DCPAttachments.SOLAR_SAIL, 0))
                        .withColor(DCPContent.CYAN_COLOR));
            }
            if (stack.getOrDefault(DCPAttachments.BEAM, 0) > 0) {
                itemTooltipEvent.getToolTip().add(Component.translatable("tooltip.dysoncubeproject.contains_beams", stack.getOrDefault(DCPAttachments.BEAM, 0))
                        .withColor(DCPContent.CYAN_COLOR));
            }
            if (stack.is(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.asItem())) {
                itemTooltipEvent.getToolTip().add(Component.translatable("tooltip.dysoncubeproject.power_optional")
                        .withColor(DCPContent.CYAN_COLOR));
            }
        }).subscribe();
    }
}