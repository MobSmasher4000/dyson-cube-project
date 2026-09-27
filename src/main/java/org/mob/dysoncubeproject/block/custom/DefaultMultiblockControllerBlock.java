package org.mob.dysoncubeproject.block.custom;

import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.event.handler.EventManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;
import org.mob.dysoncubeproject.multiblock.MultiblockStructure;

public abstract class DefaultMultiblockControllerBlock<T extends BasicTile<T>> extends RotatableBlock<T> {

    static {
        EventManager.forge(BlockEvent.EntityPlaceEvent.class, EventPriority.HIGHEST).process(event -> {

        }).subscribe();
    }

    public DefaultMultiblockControllerBlock(String name, Properties properties, Class<T> tileClass) {
        super(name, properties.requiresCorrectToolForDrops().noOcclusion(), tileClass);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    public abstract MultiblockStructure getMultiblockStructure();

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (this.getMultiblockStructure().validateSpace(context.getLevel(), context.getClickedPos())) {
            return this.defaultBlockState();
        }
        return null;
    }
}