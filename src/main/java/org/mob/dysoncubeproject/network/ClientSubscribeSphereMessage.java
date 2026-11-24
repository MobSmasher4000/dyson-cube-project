package org.mob.dysoncubeproject.network;

import com.hrznstudio.titanium.network.Message;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;

public class ClientSubscribeSphereMessage extends Message {

    public String sphereId;

    public ClientSubscribeSphereMessage() {
    }

    public ClientSubscribeSphereMessage(String sphereId) {
        this.sphereId = sphereId;
    }

    @Override
    protected void handleServerMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                var level = sp.level();
                var data = DysonSphereProgressSavedData.get(level);
                if (data != null && sphereId != null) {
                    if (sphereId.equals("-")) {
                        data.getSubscribedPlayers().remove(sp.getStringUUID());
                        data.getSubscribedPlayers().put(sp.getStringUUID(), sp.getStringUUID());
                    } else {
                        data.getSubscribedPlayers().put(sp.getStringUUID(), sphereId);
                    }
                    data.setDirty();
                }
            }
        });
    }
}
