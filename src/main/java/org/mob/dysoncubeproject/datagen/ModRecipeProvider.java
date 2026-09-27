package org.mob.dysoncubeproject.datagen;

import net.allthemods.alltheores.blocks.BlockList;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.mob.dysoncubeproject.block.ModBlocks;
import org.mob.dysoncubeproject.item.ModItems;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.EM_RAILEJECTOR_CONTROLLER.getKey().get())
                .pattern("DRB")
                .pattern("RCB")
                .pattern("SSS")
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('B', ModItems.BEAM.get())
                .define('C', Tags.Items.STORAGE_BLOCKS_COPPER)
                .define('S', Items.SMOOTH_STONE_SLAB)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOLAR_SAIL.get())
                .pattern("GCG")
                .pattern("GCG")
                .pattern("LCL")
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('L', Tags.Items.GEMS_LAPIS)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOLAR_SAIL_PACKAGE.get())
                .pattern("GGG")
                .pattern("GIG")
                .pattern("GGG")
                .define('G', ModItems.SOLAR_SAIL.get())
                .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BEAM.get(), 2)
                .pattern("NIN")
                .pattern("BIB")
                .pattern("NIN")
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('B', Items.IRON_BARS)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BEAM_PACKAGE.get())
                .pattern(" G ")
                .pattern("GIG")
                .pattern(" G ")
                .define('G', ModItems.BEAM.get())
                .define('I', Tags.Items.STORAGE_BLOCKS_COPPER)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.RAY_RECEIVER_CONTROLLER.getKey().get())
                .pattern("SSS")
                .pattern("NBN")
                .pattern("III")
                .define('S', ModItems.SOLAR_SAIL.get())
                .define('N', Items.SMOOTH_STONE_SLAB)
                .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('B', ModItems.BEAM.get())
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(consumer);


    }

}
