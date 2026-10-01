package org.mob.dysoncubeproject;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.mob.dysoncubeproject.block.ModBlocks;
import org.mob.dysoncubeproject.item.ModItems;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DysonCubeProject.MOD_ID);

    public static final RegistryObject<CreativeModeTab> DCP_TAB = CREATIVE_MODE_TABS.register("dcp_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SOLAR_SAIL.get()))
                    .title(Component.translatable("creative_tab.dyson_cube_project"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.SOLAR_SAIL.get());
                        pOutput.accept(ModItems.SOLAR_SAIL_PACKAGE.get());
                        pOutput.accept(ModItems.COMPRESSED_SOLAR_SAIL_PACKAGE.get());
                        pOutput.accept(ModItems.LIVING_COMPRESSED_SOLAR_SAIL_PACKAGE.get());
                        pOutput.accept(ModItems.BEAM.get());
                        pOutput.accept(ModItems.BEAM_PACKAGE.get());
                        pOutput.accept(ModItems.COMPRESSED_BEAM_PACKAGE.get());
                        pOutput.accept(ModItems.LIVING_COMPRESSED_BEAM_PACKAGE.get());
                        pOutput.accept(ModBlocks.EM_RAILEJECTOR_CONTROLLER.getLeft().get());
                        pOutput.accept(ModBlocks.RAY_RECEIVER_CONTROLLER.getLeft().get());

                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
