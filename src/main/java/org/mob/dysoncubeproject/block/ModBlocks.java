package org.mob.dysoncubeproject.block;

import com.hrznstudio.titanium.module.DeferredRegistryHelper;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.block.custom.EMRailEjectorControllerBlock;
import org.mob.dysoncubeproject.block.custom.MultiblockStructureBlock;
import org.mob.dysoncubeproject.block.custom.RayReceiverControllerBlock;
import org.mob.dysoncubeproject.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static DeferredRegistryHelper REGISTRY = new DeferredRegistryHelper(DysonCubeProject.MOD_ID);
    public static TitaniumTab TAB = new TitaniumTab(ResourceLocation.fromNamespaceAndPath(DysonCubeProject.MOD_ID, "main"));
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, DysonCubeProject.MOD_ID);

    public static int CYAN_COLOR = 0xFF80E6E6;

    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> MULTIBLOCK_STRUCTURE = REGISTRY.registerBlockWithTile("multiblock_structure", MultiblockStructureBlock::new, null);
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> EM_RAILEJECTOR_CONTROLLER = REGISTRY.registerBlockWithTile("em_railejector_controller", EMRailEjectorControllerBlock::new, TAB);
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> RAY_RECEIVER_CONTROLLER = REGISTRY.registerBlockWithTile("ray_receiver_controller", RayReceiverControllerBlock::new, TAB);

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
