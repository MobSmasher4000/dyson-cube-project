package org.mob.dysoncubeproject.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.mob.dysoncubeproject.DCPContent;
import org.mob.dysoncubeproject.DysonCubeProject;

import java.util.concurrent.CompletableFuture;

public class DCPBlockTagsProvider extends BlockTagsProvider {
    public DCPBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, DysonCubeProject.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.block().get())
                .add(DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.block().get())
                .add(DCPContent.Blocks.MULTIBLOCK_STRUCTURE.block().get())
        ;
    }
}