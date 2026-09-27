package org.mob.dysoncubeproject;

import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import org.mob.dysoncubeproject.block.ModBlocks;
import org.mob.dysoncubeproject.client.ClientSetup;
import org.mob.dysoncubeproject.datagen.ModLootTableProvider;
import org.mob.dysoncubeproject.item.ModItems;
import org.mob.dysoncubeproject.network.ClientSubscribeSphereMessage;
import org.mob.dysoncubeproject.network.DysonSphereSyncMessage;
import org.mob.dysoncubeproject.sound.ModSounds;
import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;
import org.mob.dysoncubeproject.world.DysonSphereStructure;
import org.slf4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DysonCubeProject.MOD_ID)
public class DysonCubeProject extends ModuleController {
    public static final String MOD_ID = "dysoncubeproject";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static NetworkHandler NETWORK = new NetworkHandler(MOD_ID);

    public DysonCubeProject() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);
        ModSounds.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        NETWORK.registerMessage(DysonSphereSyncMessage.class);
        NETWORK.registerMessage(ClientSubscribeSphereMessage.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.init();
        }

        EventManager.forge(TickEvent.LevelTickEvent.class).process(event -> {
            if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
                if (serverLevel.dimension() == Level.OVERWORLD) {
                    var data = DysonSphereProgressSavedData.get(serverLevel);
                    if (data == null) return;

                    if (serverLevel.getGameTime() % 4 == 0) {
                        var packet = new DysonSphereSyncMessage(data.save(new CompoundTag()));
                        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                            NETWORK.get().send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), packet);
                        }
                    }
                    if (serverLevel.getGameTime() % 200 == 0) {
                        publishAdvancements(serverLevel, data);
                    }
                    data.getSpheres().values().forEach(DysonSphereStructure::generatePower);
                    data.setDirty();
                }
            }
        }).subscribe();

        EventManager.forge(RegisterCommandsEvent.class).process(event -> {
            var dispatcher = event.getDispatcher();
            SuggestionProvider<CommandSourceStack> sphereIdSuggestions = (ctx, builder) -> {
                var level = ctx.getSource().getLevel();
                var data = DysonSphereProgressSavedData.get(level);
                if (data != null) {
                    return SharedSuggestionProvider.suggest(data.getSpheres().keySet(), builder);
                }
                return SharedSuggestionProvider.suggest(java.util.List.of(), builder);
            };
            dispatcher.register(Commands.literal("dysoncubeproject")
                    .requires(source -> source.hasPermission(4))
                    .then(Commands.literal("set")
                            .then(Commands.literal("beams")
                                    .then(Commands.argument("sphereId", StringArgumentType.string()).suggests(sphereIdSuggestions)
                                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                    .executes(ctx -> {
                                                        var source = ctx.getSource();
                                                        var level = source.getLevel();
                                                        var sphereId = StringArgumentType.getString(ctx, "sphereId");
                                                        final int input = IntegerArgumentType.getInteger(ctx, "value");
                                                        var data = DysonSphereProgressSavedData.get(level);
                                                        if (data == null) return 0;
                                                        var config = data.getSpheres().computeIfAbsent(sphereId, s -> new DysonSphereStructure());
                                                        final int clamped = Math.max(0, Math.min(input, config.getMaxBeams()));
                                                        config.setBeams(clamped);
                                                        data.setDirty();
                                                        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Set beams for sphere '" + sphereId + "' to " + clamped), true);
                                                        return 1;
                                                    })))))
                    .then(Commands.literal("set")
                            .then(Commands.literal("panels")
                                    .then(Commands.argument("sphereId", StringArgumentType.string()).suggests(sphereIdSuggestions)
                                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                    .executes(ctx -> {
                                                        var source = ctx.getSource();
                                                        var level = source.getLevel();
                                                        var sphereId = StringArgumentType.getString(ctx, "sphereId");
                                                        final int input = IntegerArgumentType.getInteger(ctx, "value");
                                                        var data = DysonSphereProgressSavedData.get(level);
                                                        if (data == null) return 0;
                                                        var config = data.getSpheres().computeIfAbsent(sphereId, s -> new DysonSphereStructure());
                                                        final int clamped = Math.max(0, Math.min(input, config.getMaxSolarPanels()));
                                                        config.setSolarPanels(clamped);
                                                        data.setDirty();
                                                        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Set solar panels for sphere '" + sphereId + "' to " + clamped), true);
                                                        return 1;
                                                    })))))
                    .then(Commands.literal("add")
                            .then(Commands.literal("beams")
                                    .then(Commands.argument("sphereId", StringArgumentType.string()).suggests(sphereIdSuggestions)
                                            .then(Commands.argument("delta", IntegerArgumentType.integer())
                                                    .executes(ctx -> {
                                                        var source = ctx.getSource();
                                                        var level = source.getLevel();
                                                        var sphereId = StringArgumentType.getString(ctx, "sphereId");
                                                        int delta = IntegerArgumentType.getInteger(ctx, "delta");
                                                        var data = DysonSphereProgressSavedData.get(level);
                                                        if (data == null) return 0;
                                                        var config = data.getSpheres().computeIfAbsent(sphereId, s -> new DysonSphereStructure());
                                                        int newVal = Math.max(0, Math.min(config.getBeams() + delta, config.getMaxBeams()));
                                                        config.setBeams(newVal);
                                                        data.setDirty();
                                                        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Beams for sphere '" + sphereId + "' is now " + newVal), true);
                                                        return 1;
                                                    })))))
                    .then(Commands.literal("add")
                            .then(Commands.literal("panels")
                                    .then(Commands.argument("sphereId", StringArgumentType.string()).suggests(sphereIdSuggestions)
                                            .then(Commands.argument("delta", IntegerArgumentType.integer())
                                                    .executes(ctx -> {
                                                        var source = ctx.getSource();
                                                        var level = source.getLevel();
                                                        var sphereId = StringArgumentType.getString(ctx, "sphereId");
                                                        int delta = IntegerArgumentType.getInteger(ctx, "delta");
                                                        var data = DysonSphereProgressSavedData.get(level);
                                                        if (data == null) return 0;
                                                        var config = data.getSpheres().computeIfAbsent(sphereId, s -> new DysonSphereStructure());
                                                        int newVal = Math.max(0, Math.min(config.getSolarPanels() + delta, config.getMaxSolarPanels()));
                                                        config.setSolarPanels(newVal);
                                                        data.setDirty();
                                                        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Solar panels for sphere '" + sphereId + "' is now " + newVal), true);
                                                        return 1;
                                                    })))))
            );
        }).subscribe();

    }

    @Override
    protected void initModules() {
    }

    public static void publishAdvancements(ServerLevel server, DysonSphereProgressSavedData data) {
        for (String player : data.getSubscribedPlayers().keySet()) {
            ServerPlayer serverPlayer = null;
            for (ServerPlayer player1 : server.getServer().getPlayerList().getPlayers()) {
                if (player1.getGameProfile().getId().toString().equals(player)) {
                    serverPlayer = player1;
                    break;
                }
            }
            if (serverPlayer == null) continue;
            var sphereName = data.getSubscribedFor(player);
            if (sphereName == null) continue;
            if (!data.getSpheres().containsKey(sphereName)) continue;
            var sphere = data.getSpheres().get(sphereName);
            if (sphere == null) continue;
            var progress = sphere.getProgress() * 100;
            var amounts = new int[]{5, 15, 25, 50, 75, 100};
            for (int amount : amounts) {
                if (progress >= amount) {
                    Advancement advancement = server.getServer().getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath(MOD_ID, "main/em_railejector_controller/" + amount));
                    if (advancement != null) {
                        var progressData = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                        if (!progressData.isDone()) {
                            for (String criterion : progressData.getRemainingCriteria()) {
                                serverPlayer.getAdvancements().award(advancement, criterion);
                            }
                        }
                    }
                }
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from Dyson Cube Project");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

    @Override
    public void addDataProvider(GatherDataEvent event) {
        super.addDataProvider(event);
        List<Block> blocks = ForgeRegistries.BLOCKS.getValues().stream().filter(block -> ForgeRegistries.BLOCKS.getKey(block).getNamespace().equals(DysonCubeProject.MOD_ID)).toList();
        event.getGenerator().addProvider(true, new ModLootTableProvider(event.getGenerator(), NonNullLazy.of(() -> blocks)));
    }
}
