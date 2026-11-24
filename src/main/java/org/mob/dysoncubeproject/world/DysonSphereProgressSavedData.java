package org.mob.dysoncubeproject.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.mob.dysoncubeproject.DysonCubeProject;

import java.util.HashMap;
import java.util.Map;

public class DysonSphereProgressSavedData extends SavedData {

    public static final String ID = "dyson_sphere_progress";

    public static final Codec<DysonSphereProgressSavedData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, DysonSphereStructure.CODEC).optionalFieldOf("spheres", Map.of()).forGetter(d -> d.spheres),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("subscribedPlayers", Map.of()).forGetter(d -> d.subscribedPlayers)
    ).apply(i, DysonSphereProgressSavedData::new));

    public static final SavedDataType<DysonSphereProgressSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(DysonCubeProject.MOD_ID, ID),
            DysonSphereProgressSavedData::new,
            CODEC,
            DataFixTypes.LEVEL);

    private final HashMap<String, DysonSphereStructure> spheres;
    private final HashMap<String, String> subscribedPlayers;

    public DysonSphereProgressSavedData() {
        this.spheres = new HashMap<>();
        this.subscribedPlayers = new HashMap<>();
    }

    private DysonSphereProgressSavedData(Map<String, DysonSphereStructure> spheres, Map<String, String> subscribedPlayers) {
        this.spheres = new HashMap<>(spheres);
        this.subscribedPlayers = new HashMap<>(subscribedPlayers);
    }

    public static DysonSphereProgressSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
            return overworld.getDataStorage().computeIfAbsent(TYPE);
        }
        return null;
    }

    public CompoundTag toTag() {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    public static DysonSphereProgressSavedData fromTag(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    public HashMap<String, DysonSphereStructure> getSpheres() { return spheres; }

    public HashMap<String, String> getSubscribedPlayers() { return subscribedPlayers; }

    public String getSubscribedFor(String playerUUID) {
        return subscribedPlayers.getOrDefault(playerUUID, playerUUID);
    }
}