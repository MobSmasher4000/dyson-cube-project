package org.mob.dysoncubeproject.network;

import org.mob.dysoncubeproject.world.ClientDysonSphere;
import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DysonSphereSyncMessage extends Message {

    public CompoundTag tag;

    public DysonSphereSyncMessage() {
    }

    public DysonSphereSyncMessage(CompoundTag tag) {
        this.tag = tag;
    }

    @Override
    protected void handleServerMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDysonSphere.DYSON_SPHERE_PROGRESS = DysonSphereProgressSavedData.fromTag(tag);
        });
    }

    @Override
    public void handleClientMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDysonSphere.DYSON_SPHERE_PROGRESS = DysonSphereProgressSavedData.fromTag(tag);
        });
    }
}