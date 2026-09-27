package org.mob.dysoncubeproject.compat.jade;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.mob.dysoncubeproject.block.entity.MultiblockStructureBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class DCPJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {

        registration.addRayTraceCallback((hit, accessor, originalAccessor) -> {
            
            if (accessor instanceof BlockAccessor blockAccessor) {
                if (blockAccessor.getBlockEntity() instanceof MultiblockStructureBlockEntity dummyTile) {
                    BlockPos controllerPos = dummyTile.getControllerPos();
                    
                    if (controllerPos != null && blockAccessor.getLevel().getBlockEntity(controllerPos) != null) {
                        BlockState controllerState = blockAccessor.getLevel().getBlockState(controllerPos);

                        return registration.blockAccessor()
                                .from(blockAccessor)
                                .blockEntity(blockAccessor.getLevel().getBlockEntity(controllerPos))
                                .blockState(controllerState)
                                .build();
                    }
                }
            }
            return accessor;
        });
        
    }
}