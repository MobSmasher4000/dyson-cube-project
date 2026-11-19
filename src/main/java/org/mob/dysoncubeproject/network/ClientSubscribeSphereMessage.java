package org.mob.dysoncubeproject.network;

import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ClientSubscribeSphereMessage extends Message {

    public String sphereId;

    public ClientSubscribeSphereMessage() {
    }

    public ClientSubscribeSphereMessage(String sphereId) {
        this.sphereId = sphereId;
    }

    @Override
    protected void handleMessage(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer sp = context.getSender();
            if (sp != null) {
                var level = sp.level();
                var data = DysonSphereProgressSavedData.get(level);
                if (data != null && sphereId != null) {
                    if (sphereId.equals("-")) {
                        data.getSubscribedPlayers().remove(sp.getStringUUID());
                    } else {
                        data.getSubscribedPlayers().put(sp.getStringUUID(), sphereId);
                    }
                    data.setDirty();
                }
            }
        });
        context.setPacketHandled(true);
    }
}