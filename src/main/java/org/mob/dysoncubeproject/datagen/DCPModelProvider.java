package org.mob.dysoncubeproject.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import org.mob.dysoncubeproject.DCPContent;
import org.mob.dysoncubeproject.DysonCubeProject;

import java.util.stream.Stream;

public class DCPModelProvider extends ModelProvider {
    public DCPModelProvider(PackOutput output) {
        super(output, DysonCubeProject.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(DCPContent.Items.BEAM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(DCPContent.Items.BEAM_PACKAGE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(DCPContent.Items.SOLAR_SAIL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(DCPContent.Items.SOLAR_SAIL_PACKAGE.get(), ModelTemplates.FLAT_ITEM);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }
}