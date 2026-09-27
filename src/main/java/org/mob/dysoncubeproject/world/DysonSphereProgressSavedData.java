package org.mob.dysoncubeproject.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;

public class DysonSphereProgressSavedData extends SavedData {

    public static final String ID = "dyson_sphere_progress";

    private HashMap<String, DysonSphereStructure> spheres;
    private HashMap<String, String> subscribedPlayers;

    public DysonSphereProgressSavedData() {
        super();
        this.spheres = new HashMap<>();
        this.subscribedPlayers = new HashMap<>();
    }

    public static DysonSphereProgressSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            ServerLevel serverWorld = serverLevel.getServer().getLevel(Level.OVERWORLD);
            DysonSphereProgressSavedData data = serverWorld.getDataStorage().computeIfAbsent(
                    DysonSphereProgressSavedData::load,
                    DysonSphereProgressSavedData::new,
                    ID
            );
            return data;
        }
        return null;
    }

    public static DysonSphereProgressSavedData load(CompoundTag compoundTag) {
        var data = new DysonSphereProgressSavedData();
        var spheres = compoundTag.getCompound("spheres");
        for (String key : spheres.getAllKeys()) {
            data.spheres.put(key, new DysonSphereStructure());
            data.spheres.get(key).deserializeNBT(spheres.getCompound(key));
        }
        var subscribedPlayers = compoundTag.getCompound("subscribedPlayers");
        for (String key : subscribedPlayers.getAllKeys()) {
            data.subscribedPlayers.put(key, subscribedPlayers.getString(key));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        var spheres = new CompoundTag();
        for (String key : this.spheres.keySet()) {
            spheres.put(key, this.spheres.get(key).serializeNBT());
        }
        var subscribedPlayers = new CompoundTag();
        for (String key : this.subscribedPlayers.keySet()) {
            subscribedPlayers.putString(key, this.subscribedPlayers.get(key));
        }

        compoundTag.put("spheres", spheres);
        compoundTag.put("subscribedPlayers", subscribedPlayers);
        return compoundTag;
    }

    public HashMap<String, DysonSphereStructure> getSpheres() {
        return spheres;
    }

    public HashMap<String, String> getSubscribedPlayers() {
        return subscribedPlayers;
    }

    public String getSubscribedFor(String playerUUID) {
        return subscribedPlayers.getOrDefault(playerUUID, playerUUID);
    }
}