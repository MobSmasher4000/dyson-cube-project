package org.mob.dysoncubeproject.block;

import org.mob.dysoncubeproject.DCPContent;
import org.mob.dysoncubeproject.block.tile.MultiblockStructureBlockEntity;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MultiblockStructureBlock extends BasicTileBlock<MultiblockStructureBlockEntity> {

    public MultiblockStructureBlock() {
        super(Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().noLootTable().dynamicShape(), MultiblockStructureBlockEntity.class);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (blockPos, blockState) ->
                new MultiblockStructureBlockEntity((BasicTileBlock<MultiblockStructureBlockEntity>) DCPContent.Blocks.MULTIBLOCK_STRUCTURE.block().get(), DCPContent.Blocks.MULTIBLOCK_STRUCTURE.type().get(), blockPos, blockState);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    public static void createStructure(Level level, BlockPos controllerPos, BlockPos at) {
        level.setBlockAndUpdate(at, DCPContent.Blocks.MULTIBLOCK_STRUCTURE.getBlock().defaultBlockState());
        if (level.getBlockEntity(at) instanceof MultiblockStructureBlockEntity blockEntity) {
            blockEntity.setControllerPos(controllerPos);
        }
    }

    /**
     * Returns this multiblock placeholder's shape by splitting the controller's full shape into 16x16x16 blocks.
     * Controller is assumed to be the bottom-middle of the structure. Rotation is intentionally ignored.
     */
    public static VoxelShape getSplitShapeRelativeToController(BlockGetter level, BlockPos controllerPos, BlockPos currentPos, CollisionContext context) {
        if (level == null || controllerPos == null || currentPos == null) return Shapes.empty();
        BlockState controllerState = level.getBlockState(controllerPos);
        Block controllerBlock = controllerState.getBlock();
        if (!(controllerBlock instanceof DefaultMultiblockControllerBlock<?> controller)) return Shapes.empty();

        // Get the controller's full shape (may extend beyond [0,16])
        VoxelShape controllerShape = ((DefaultMultiblockControllerBlock) controllerBlock).getMultiblockStructure().getShape();
        if (controllerShape.isEmpty()) return Shapes.empty();
        // Compute the 16x16x16 cube in controller-local coordinates for the current block
        int dx = currentPos.getX() - controllerPos.getX();
        int dy = currentPos.getY() - controllerPos.getY();
        int dz = currentPos.getZ() - controllerPos.getZ();
        double minX = dx * 16.0;
        double minY = dy * 16.0;
        double minZ = dz * 16.0;
        double maxX = minX + 16.0;
        double maxY = minY + 16.0;
        double maxZ = minZ + 16.0;

        // Intersect controller shape with this cube
        VoxelShape cutter = Block.box(minX, minY, minZ, maxX, maxY, maxZ);
        VoxelShape piece = Shapes.join(controllerShape, cutter, BooleanOp.AND);
        if (piece.isEmpty()) return Shapes.empty();

        // Translate the piece into the local space of the current block [0,16]
        return translateShape(piece, -minX, -minY, -minZ);
    }

    private static VoxelShape translateShape(VoxelShape shape, double offX, double offY, double offZ) {
        if (shape.isEmpty()) return shape;
        VoxelShape result = Shapes.empty();
        for (AABB bb : shape.toAabbs()) {
            VoxelShape moved = Block.box(bb.minX * 16 + offX, bb.minY * 16 + offY, bb.minZ * 16 + offZ, bb.maxX * 16 + offX, bb.maxY * 16 + offY, bb.maxZ * 16 + offZ);
            result = Shapes.or(result, moved);
        }
        return result;
    }

    @Nullable
    public static BlockPos findController(BlockGetter level, BlockPos pos) {
        for (int dy = 0; dy <= 6; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos c = pos.offset(dx, -dy, dz);
                    if (level.getBlockState(c).getBlock() instanceof DefaultMultiblockControllerBlock<?> controller) {
                        var structure = controller.getMultiblockStructure();
                        int minX = c.getX() - structure.getSizeX() / 2;
                        int minZ = c.getZ() - structure.getSizeZ() / 2;
                        if (pos.getX() >= minX && pos.getX() < minX + structure.getSizeX()
                                && pos.getZ() >= minZ && pos.getZ() < minZ + structure.getSizeZ()
                                && pos.getY() >= c.getY() && pos.getY() < c.getY() + structure.getSizeY()) {
                            return c;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof MultiblockStructureBlockEntity be) {
            BlockPos controllerPos = be.getControllerPos();
            if (controllerPos == null) controllerPos = findController(level, pos);
            if (controllerPos != null) {
                VoxelShape cached = be.getCachedShape();
                if (cached != null && !cached.isEmpty()) {
                    return cached;
                }
                VoxelShape computed = getSplitShapeRelativeToController(level, controllerPos, pos, context);
                be.setCachedShape(computed);
                return computed;
            }
        }
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) { // VERIFY signature
        if (level.getBlockEntity(pos) instanceof MultiblockStructureBlockEntity be) {
            BlockPos controllerPos = be.getControllerPos();
            if (controllerPos == null) controllerPos = findController(level, pos);
            if (controllerPos != null) {
                return new ItemStack(level.getBlockState(controllerPos).getBlock());
            }
        }
        return ItemStack.EMPTY;
    }


}