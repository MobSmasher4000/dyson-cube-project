package org.mob.dysoncubeproject.block.entity;

import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.block.tile.ITickableBlockEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.mob.dysoncubeproject.block.ModBlocks;

import java.lang.reflect.Field;

public class MultiblockStructureBlockEntity extends BasicTile<MultiblockStructureBlockEntity> implements ITickableBlockEntity<MultiblockStructureBlockEntity> {

    @Save
    private BlockPos controllerPos;

    // Transient cache for this placeholder block's split shape
    private VoxelShape cachedShape;

    // Caches the reflection field for performance
    private static Field destroyingBlocksField = null;

    public MultiblockStructureBlockEntity(BasicTileBlock<MultiblockStructureBlockEntity> base, BlockPos pos, BlockState state) {
        super(base, ModBlocks.MULTIBLOCK_STRUCTURE.getRight().get(), pos, state);
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientTick(Level level, BlockPos pos, BlockState state, MultiblockStructureBlockEntity blockEntity) {
        if (controllerPos != null) {
            try {
                if (destroyingBlocksField == null) {
                    // "f_109121_" is SRG name for LevelRenderer.destroyingBlocks
                    destroyingBlocksField = ObfuscationReflectionHelper.findField(LevelRenderer.class, "f_109121_");
                    destroyingBlocksField.setAccessible(true);
                }

                @SuppressWarnings("unchecked")
                Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = (Int2ObjectMap<BlockDestructionProgress>) destroyingBlocksField.get(Minecraft.getInstance().levelRenderer);

                for (int i : destroyingBlocks.keySet()) {
                    BlockDestructionProgress progress = destroyingBlocks.get(i);
                    if (progress.getPos().equals(pos)) {
                        Minecraft.getInstance().levelRenderer.destroyBlockProgress(i, controllerPos, progress.getProgress());
                    }
                }
            } catch (Exception e) {
                // Silently ignore reflection failures to prevent client crashes
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
        // Invalidate cached shape when controller changes
        this.cachedShape = null;
        markForUpdate();
    }

    public VoxelShape getCachedShape() {
        return cachedShape;
    }

    public void setCachedShape(VoxelShape cachedShape) {
        this.cachedShape = cachedShape;
    }

}