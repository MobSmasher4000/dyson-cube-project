package org.mob.dysoncubeproject.datagen;

import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.mob.dysoncubeproject.DCPContent;

import java.util.concurrent.CompletableFuture;

public class DCPRecipesProvider extends RecipeProvider {
    public DCPRecipesProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new DCPRecipesProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Dyson Cube Project Recipes";
        }
    }

    @Override
    public void buildRecipes() {
        TitaniumShapedRecipeBuilder.shapedRecipe(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.block().get())
                .pattern("DRB")
                .pattern("RCB")
                .pattern("SSS")
                .define('D', Items.DIAMOND)
                .define('R', Items.REDSTONE)
                .define('B', DCPContent.Items.BEAM.get())
                .define('C', Items.COPPER_BLOCK)
                .define('S', Items.SMOOTH_STONE_SLAB)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(DCPContent.Items.SOLAR_SAIL.get())
                .pattern("GCG")
                .pattern("GCG")
                .pattern("LCL")
                .define('G', Items.GLASS_PANE)
                .define('C', Items.COPPER_INGOT)
                .define('L', Items.LAPIS_LAZULI)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(DCPContent.Items.SOLAR_SAIL_PACKAGE.get())
                .pattern("GGG")
                .pattern("GIG")
                .pattern("GGG")
                .define('G', DCPContent.Items.SOLAR_SAIL.get())
                .define('I', Items.IRON_BLOCK)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(DCPContent.Items.BEAM.get(), 2)
                .pattern("NIN")
                .pattern("BIB")
                .pattern("NIN")
                .define('N', Items.IRON_NUGGET)
                .define('I', Items.IRON_BLOCK)
                .define('B', Items.IRON_BARS)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(DCPContent.Items.BEAM_PACKAGE.get())
                .pattern(" G ")
                .pattern("GIG")
                .pattern(" G ")
                .define('G', DCPContent.Items.BEAM.get())
                .define('I', Items.COPPER_BLOCK)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.asItem())
                .pattern("SSS")
                .pattern("NBN")
                .pattern("III")
                .define('S', DCPContent.Items.SOLAR_SAIL.get())
                .define('N', Items.SMOOTH_STONE_SLAB)
                .define('I', Items.IRON_BLOCK)
                .define('B', DCPContent.Items.BEAM.get())
                .save(output);
    }
}
