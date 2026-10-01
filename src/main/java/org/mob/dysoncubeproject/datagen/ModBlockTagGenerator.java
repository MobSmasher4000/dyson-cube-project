package org.mob.dysoncubeproject.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, DysonCubeProject.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.RAY_RECEIVER_CONTROLLER.getKey().get())
                .add(ModBlocks.EM_RAILEJECTOR_CONTROLLER.getKey().get())
        ;

        this.tag(BG2DENY)
                .add(ModBlocks.RAY_RECEIVER_CONTROLLER.getKey().get())
                .add(ModBlocks.EM_RAILEJECTOR_CONTROLLER.getKey().get())
                .add(ModBlocks.MULTIBLOCK_STRUCTURE.getKey().get())
        ;
    }

    public static final TagKey<Block> BG2DENY = BlockTags.create(ResourceLocation.fromNamespaceAndPath("buildinggadgets2", "deny"));
}
