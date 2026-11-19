package org.mob.dysoncubeproject.network;

import com.hrznstudio.titanium.network.Message;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;

public class ClientSubscribeSphereMessage extends Message {

    public String sphereId;
    public String password;

    public ClientSubscribeSphereMessage() {
    }

    public ClientSubscribeSphereMessage(String sphereId) {
        this(sphereId, "");
    }

    public ClientSubscribeSphereMessage(String sphereId, String password) {
        this.sphereId = sphereId;
        this.password = password == null ? "" : password;
    }

    @Override
    protected void handleMessage(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer sp = context.getSender();
            if (sp != null) {
                var level = sp.level();
                var data = DysonSphereProgressSavedData.get(level);
                if (data != null && sphereId != null) {
                    String playerUUID = sp.getStringUUID();
                    if (sphereId.equals("-")) {
                        data.getSubscribedPlayers().remove(playerUUID);
                        syncToClients(data);
                    } else {
                        var targetSphere = data.getSpheres().get(sphereId);
                        if (targetSphere != null) {
                            boolean isOwner = sphereId.equals(playerUUID);
                            String actualPassword = targetSphere.getPassword();
                            boolean unlocked = actualPassword == null || actualPassword.isEmpty();
                            boolean matches = actualPassword != null && actualPassword.equals(this.password);

                            if (isOwner || unlocked || matches) {
                                data.getSubscribedPlayers().put(playerUUID, sphereId);
                                syncToClients(data);
                            } else {
                                sp.sendSystemMessage(Component.translatable("tooltip.dysoncubeproject.incorrect_password").withStyle(ChatFormatting.RED));
                            }
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    private void syncToClients(DysonSphereProgressSavedData data) {
        data.setDirty();
        CompoundTag syncTag = new CompoundTag();
        data.saveForClient(syncTag);
        DysonCubeProject.NETWORK.get().send(PacketDistributor.ALL.noArg(), new DysonSphereSyncMessage(syncTag));
    }
}