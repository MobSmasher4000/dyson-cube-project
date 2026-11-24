package org.mob.dysoncubeproject.block.tile;

import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.block.tile.ITickableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.mob.dysoncubeproject.block.DefaultMultiblockControllerBlock;

public class MultiblockStructureBlockEntity extends BasicTile<MultiblockStructureBlockEntity> implements ITickableBlockEntity<MultiblockStructureBlockEntity> {

    @Save
    private BlockPos controllerPos;

    private VoxelShape cachedShape;

    public MultiblockStructureBlockEntity(BasicTileBlock<MultiblockStructureBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide() && controllerPos != null
                && this.level.getBlockState(controllerPos).getBlock() instanceof DefaultMultiblockControllerBlock<?>) {
            this.level.destroyBlock(controllerPos, true);
        }
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public void clientTick(Level level, BlockPos pos, BlockState state, MultiblockStructureBlockEntity blockEntity) {
        if (controllerPos != null) {
            var destroyingBlocks = Minecraft.getInstance().levelRenderer.destroyingBlocks;
            for (Integer i : destroyingBlocks.keySet()) {
                var progress = destroyingBlocks.get(i);
                if (progress.getPos().equals(pos)) {
                    Minecraft.getInstance().levelRenderer.destroyBlockProgress(i, controllerPos, progress.getProgress());
                }
            }
        }
    }

    @Override
    public InteractionResult onActivated(Player player, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        if (controllerPos != null && this.level.getBlockEntity(controllerPos) instanceof BasicTile<?> controller) {
            return controller.onActivated(player, hand, facing, hitX, hitY, hitZ);
        }
        return super.onActivated(player, hand, facing, hitX, hitY, hitZ);
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        this.cachedShape = null;
        syncObject(this.controllerPos);
        markForUpdate();
    }

    public VoxelShape getCachedShape() {
        return cachedShape;
    }

    public void setCachedShape(VoxelShape cachedShape) {
        this.cachedShape = cachedShape;
    }
}