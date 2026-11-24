package org.mob.dysoncubeproject.client;

import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.mob.dysoncubeproject.DysonCubeProject;

public class DCPExtraModels {

    public static final StandaloneModelKey<QuadCollection> EM_RAILEJECTOR_GUN_KEY = new StandaloneModelKey<>(() -> "dysoncubeproject:em_railejector_gun");
    public static final StandaloneModelKey<QuadCollection> EM_RAILEJECTOR_BASE_KEY = new StandaloneModelKey<>(() -> "dysoncubeproject:em_railejector_base");
    public static final StandaloneModelKey<QuadCollection> EM_RAILEJECTOR_PROJECTILE_KEY = new StandaloneModelKey<>(() -> "dysoncubeproject:em_railejector_projectile");

    public static final StandaloneModelKey<QuadCollection> RAY_RECEIVER_BASE_KEY = new StandaloneModelKey<>(() -> "dysoncubeproject:ray_receiver_base");
    public static final StandaloneModelKey<QuadCollection> RAY_RECEIVER_PLATE_KEY = new StandaloneModelKey<>(() -> "dysoncubeproject:ray_receiver_plate");
    public static final StandaloneModelKey<QuadCollection> RAY_RECEIVER_LENS_KEY = new StandaloneModelKey<>(() -> "dysoncubeproject:ray_receiver_lens");
    public static final StandaloneModelKey<QuadCollection> RAY_RECEIVER_LENS_STANDS_KEY = new StandaloneModelKey<>(() -> "dysoncubeproject:ray_receiver_lens_stands");

    public static QuadCollection EM_RAILEJECTOR_GUN;
    public static QuadCollection EM_RAILEJECTOR_BASE;
    public static QuadCollection EM_RAILEJECTOR_PROJECTILE;

    public static QuadCollection RAY_RECEIVER_BASE;
    public static QuadCollection RAY_RECEIVER_PLATE;
    public static QuadCollection RAY_RECEIVER_LENS;
    public static QuadCollection RAY_RECEIVER_LENS_STANDS;


    public static void register(ModelEvent.RegisterStandalone event) {
        reg(event, EM_RAILEJECTOR_BASE_KEY, "block/em_railejector_base");
        reg(event, EM_RAILEJECTOR_GUN_KEY, "block/em_railejector_gun");
        reg(event, EM_RAILEJECTOR_PROJECTILE_KEY, "block/em_railejector_projectile");
        reg(event, RAY_RECEIVER_BASE_KEY, "block/ray_receiver_base");
        reg(event, RAY_RECEIVER_PLATE_KEY, "block/ray_receiver_plate");
        reg(event, RAY_RECEIVER_LENS_KEY, "block/ray_receiver_lens");
        reg(event, RAY_RECEIVER_LENS_STANDS_KEY, "block/ray_receiver_lens_stands");
    }


    public static void load(ModelEvent.BakingCompleted event) {
        var mm = event.getModelManager();
        EM_RAILEJECTOR_BASE = mm.getStandaloneModel(EM_RAILEJECTOR_BASE_KEY);
        EM_RAILEJECTOR_GUN = mm.getStandaloneModel(EM_RAILEJECTOR_GUN_KEY);
        EM_RAILEJECTOR_PROJECTILE = mm.getStandaloneModel(EM_RAILEJECTOR_PROJECTILE_KEY);
        RAY_RECEIVER_BASE = mm.getStandaloneModel(RAY_RECEIVER_BASE_KEY);
        RAY_RECEIVER_PLATE = mm.getStandaloneModel(RAY_RECEIVER_PLATE_KEY);
        RAY_RECEIVER_LENS = mm.getStandaloneModel(RAY_RECEIVER_LENS_KEY);
        RAY_RECEIVER_LENS_STANDS = mm.getStandaloneModel(RAY_RECEIVER_LENS_STANDS_KEY);
    }

    private static void reg(ModelEvent.RegisterStandalone event, StandaloneModelKey<QuadCollection> key, String path) {
        event.register(key, SimpleUnbakedStandaloneModel.quadCollection(Identifier.fromNamespaceAndPath(DysonCubeProject.MOD_ID, path)));
    }
}