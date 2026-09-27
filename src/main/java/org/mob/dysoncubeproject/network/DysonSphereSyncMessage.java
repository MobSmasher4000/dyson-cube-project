package org.mob.dysoncubeproject.network;

import net.minecraftforge.network.NetworkEvent;
import org.mob.dysoncubeproject.world.ClientDysonSphere;
import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.nbt.CompoundTag;

public class DysonSphereSyncMessage extends Message {

    public CompoundTag tag;

    public DysonSphereSyncMessage() {

    }

    public DysonSphereSyncMessage(CompoundTag tag) {
        this.tag = tag;
    }

    @Override
    protected void handleMessage(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ClientDysonSphere.DYSON_SPHERE_PROGRESS = DysonSphereProgressSavedData.load(tag);
        });
        context.setPacketHandled(true);
    }
}