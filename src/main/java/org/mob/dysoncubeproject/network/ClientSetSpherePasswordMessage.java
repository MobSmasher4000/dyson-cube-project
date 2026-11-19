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

public class ClientSetSpherePasswordMessage extends Message {

    public String newPassword;

    public ClientSetSpherePasswordMessage() {
    }

    public ClientSetSpherePasswordMessage(String newPassword) {
        this.newPassword = newPassword == null ? "" : newPassword;
    }

    @Override
    protected void handleMessage(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer sp = context.getSender();
            if (sp != null) {
                var level = sp.level();
                var data = DysonSphereProgressSavedData.get(level);

                if (data != null) {
                    String ownerUUID = sp.getStringUUID();
                    var sphere = data.getSpheres().get(ownerUUID);

                    if (sphere != null) {
                        String sanitized = this.newPassword == null ? "" : this.newPassword.trim();
                        sphere.setPassword(sanitized);

                        // Disconnect any existing non-owner subscribers when password changes
                        if (!sanitized.isEmpty()) {
                            data.getSubscribedPlayers().entrySet().removeIf(entry ->
                                    entry.getValue().equals(ownerUUID) && !entry.getKey().equals(ownerUUID)
                            );
                        }

                        data.setDirty();
                        CompoundTag syncTag = new CompoundTag();
                        data.saveForClient(syncTag);
                        DysonCubeProject.NETWORK.get().send(PacketDistributor.ALL.noArg(), new DysonSphereSyncMessage(syncTag));

                        if (sanitized.isEmpty()) {
                            sp.sendSystemMessage(Component.translatable("tooltip.dysoncubeproject.password_cleared").withStyle(ChatFormatting.GREEN));
                        } else {
                            sp.sendSystemMessage(Component.translatable("tooltip.dysoncubeproject.password_updated").withStyle(ChatFormatting.GREEN));
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}