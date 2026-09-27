package org.mob.dysoncubeproject.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.item.custom.DysonComponentItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, DysonCubeProject.MOD_ID);

    // Solar Sail
    public static final RegistryObject<Item> SOLAR_SAIL = ITEMS.register("solar_sail",
            () -> new DysonComponentItem(1,0));
    public static final RegistryObject<Item> SOLAR_SAIL_PACKAGE = ITEMS.register("solar_sail_package",
            () -> new DysonComponentItem(8,0));

    // Beam
    public static final RegistryObject<Item> BEAM = ITEMS.register("beam",
            () -> new DysonComponentItem(0,1));
    public static final RegistryObject<Item> BEAM_PACKAGE = ITEMS.register("beam_package",
            () -> new DysonComponentItem(0,4));



    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
