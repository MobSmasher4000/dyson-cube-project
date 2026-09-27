package org.mob.dysoncubeproject.datagen;

import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.NonNullLazy;
import org.mob.dysoncubeproject.block.ModBlocks;

import java.util.List;

public class ModLootTableProvider extends TitaniumLootTableProvider {

    private final NonNullLazy<List<Block>> blocksToProcess;

    public ModLootTableProvider(DataGenerator dataGenerator, NonNullLazy<List<Block>> blocks) {
        super(dataGenerator, blocks);
        this.blocksToProcess = blocks;
    }

    @Override
    protected BasicBlockLootTables createBlockLootTables() {
        return new BasicBlockLootTables(this.blocksToProcess){
            @Override
            protected void generate() {
                super.generate();
                add(ModBlocks.EM_RAILEJECTOR_CONTROLLER.getKey().get(), droppingSelf(ModBlocks.EM_RAILEJECTOR_CONTROLLER.getKey().get()));
                add(ModBlocks.RAY_RECEIVER_CONTROLLER.getKey().get(), droppingSelf(ModBlocks.RAY_RECEIVER_CONTROLLER.getKey().get()));
            }
        };
    }
}