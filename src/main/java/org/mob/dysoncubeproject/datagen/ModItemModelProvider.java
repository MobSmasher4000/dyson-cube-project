package org.mob.dysoncubeproject.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, DysonCubeProject.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.COMPRESSED_BEAM_PACKAGE);
        simpleItem(ModItems.LIVING_COMPRESSED_BEAM_PACKAGE);
        simpleItem(ModItems.COMPRESSED_SOLAR_SAIL_PACKAGE);
        simpleItem(ModItems.LIVING_COMPRESSED_SOLAR_SAIL_PACKAGE);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item){
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(DysonCubeProject.MOD_ID, "item/" + item.getId().getPath()));
    }
}
