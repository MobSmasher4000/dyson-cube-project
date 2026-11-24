package org.mob.dysoncubeproject.compat.jade;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.mob.dysoncubeproject.block.MultiblockStructureBlock;
import org.mob.dysoncubeproject.block.tile.MultiblockStructureBlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class DCPJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor) -> {
            if (accessor instanceof BlockAccessor blockAccessor
                    && blockAccessor.getBlock() instanceof MultiblockStructureBlock
                    && blockAccessor.getBlockEntity() instanceof MultiblockStructureBlockEntity be) {
                Level level = blockAccessor.getLevel();
                BlockPos controllerPos = be.getControllerPos();
                if (controllerPos == null) {
                    controllerPos = MultiblockStructureBlock.findController(level, blockAccessor.getPosition());
                }
                if (controllerPos != null) {
                    BlockState controllerState = level.getBlockState(controllerPos);
                    BlockEntity controllerBE = level.getBlockEntity(controllerPos);
                    BlockHitResult originalHit = blockAccessor.getHitResult();
                    BlockHitResult redirectedHit = new BlockHitResult(
                            originalHit.getLocation(),
                            originalHit.getDirection(),
                            controllerPos,
                            originalHit.isInside()
                    );
                    return registration.blockAccessor()
                            .from(blockAccessor)
                            .blockState(controllerState)
                            .blockEntity(controllerBE)
                            .hit(redirectedHit)
                            .build();
                }
            }
            return accessor;
        });
    }
}