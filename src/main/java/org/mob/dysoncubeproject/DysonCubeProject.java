package org.mob.dysoncubeproject;

import net.minecraft.data.PackOutput;
import org.mob.dysoncubeproject.block.tile.EMRailEjectorBlockEntity;
import org.mob.dysoncubeproject.block.tile.RayReceiverBlockEntity;
import org.mob.dysoncubeproject.client.ClientSetup;
import org.mob.dysoncubeproject.datagen.*;
import org.mob.dysoncubeproject.network.ClientSubscribeSphereMessage;
import org.mob.dysoncubeproject.network.DysonSphereSyncMessage;
import org.mob.dysoncubeproject.world.DysonSphereStructure;
import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

import java.util.List;

@Mod(DysonCubeProject.MOD_ID)
public class DysonCubeProject extends ModuleController {

    public static final String MOD_ID = "dysoncubeproject";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static NetworkHandler NETWORK = new NetworkHandler(MOD_ID);

    public DysonCubeProject(Dist dist, IEventBus modEventBus, ModContainer modContainer) {
        super(modContainer);
        NETWORK.registerMessage("dyson_sphere_sync", DysonSphereSyncMessage.class);
        NETWORK.registerMessage("client_subscribe_sphere", ClientSubscribeSphereMessage.class);


        if (dist == Dist.CLIENT) ClientSetup.init();

        EventManager.forge(LevelTickEvent.Pre.class).process(post -> {
            if (post.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
                var data = DysonSphereProgressSavedData.get(serverLevel);
                if (post.getLevel().getGameTime() % 4 == 0) {
                    var packet = new DysonSphereSyncMessage(data.toTag());
                    for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                        NETWORK.sendTo(packet, player);
                    }
                }
                if (post.getLevel().getGameTime() % 200 == 0) {
                    publishAdvancements(serverLevel, data);
                }
                data.getSpheres().values().forEach(DysonSphereStructure::generatePower);
                data.setDirty();
            }
        }).subscribe();

        EventManager.mod(RegisterCapabilitiesEvent.class).process(event -> {
            event.registerBlock(Capabilities.Item.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (level instanceof ServerLevel serverLevel && blockEntity instanceof EMRailEjectorBlockEntity emRailEjectorBlockEntity && direction == Direction.DOWN) {
                    return emRailEjectorBlockEntity.getInput();
                }
                return null;
            }, DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.getBlock());
            event.registerBlock(Capabilities.Energy.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (level instanceof ServerLevel serverLevel && blockEntity instanceof EMRailEjectorBlockEntity emRailEjectorBlockEntity && direction == Direction.DOWN) {
                    return emRailEjectorBlockEntity.getPower();
                }
                return null;
            }, DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.getBlock());
            event.registerBlock(Capabilities.Energy.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (level instanceof ServerLevel serverLevel && level.getBlockEntity(blockPos.below()) instanceof EMRailEjectorBlockEntity emRailEjectorBlockEntity) {
                    return emRailEjectorBlockEntity.getPower();
                }
                return null;
            }, DCPContent.Blocks.MULTIBLOCK_STRUCTURE.getBlock());
            event.registerBlock(Capabilities.Energy.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
                if (level instanceof ServerLevel serverLevel && blockEntity instanceof RayReceiverBlockEntity rayReceiverBlockEntity && direction == Direction.DOWN) {
                    return rayReceiverBlockEntity.getEnergyStorageComponent();
                }
                return null;
            }, DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock());
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
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
        DCPAttachments.DR.register(modEventBus);
    }


    @Override
    protected void initModules() {
        addCreativeTab("main", () -> new ItemStack(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER), "dyson_cube_project", DCPContent.TAB);
        DCPContent.Blocks.init();
        DCPContent.Items.init();
        DCPContent.Sounds.init();
    }

    public static void publishAdvancements(ServerLevel server, DysonSphereProgressSavedData data) {
        for (ServerPlayer serverPlayer : server.getServer().getPlayerList().getPlayers()) {
            var sphereName = data.getSubscribedFor(serverPlayer.getStringUUID());
            if (sphereName == null) continue;
            var sphere = data.getSpheres().get(sphereName);
            if (sphere == null) continue;
            var progress = sphere.getProgress() * 100;
            var amounts = new int[]{5, 15, 25, 50, 75, 100};
            for (int amount : amounts) {
                if (progress >= amount) {
                    var advancement = server.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(MOD_ID, "main/em_railejector_controller/" + amount));
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

    @Override
    public void addDataProvider(GatherDataEvent event) {
        super.addDataProvider(event);
        PackOutput packOutput = event.getGenerator().getPackOutput();
        var lookupProvider = event.getLookupProvider();

        event.addProvider(new DCPModelProvider(packOutput));
        event.addProvider(new DCPBlockTagsProvider(packOutput, lookupProvider));
        event.addProvider(new DCPRecipesProvider.Runner(packOutput, lookupProvider));
        event.addProvider(new DCPAdvancementProvider(packOutput, lookupProvider));
        event.addProvider(new DCPLootTableDataProvider(event.getGenerator(), () -> List.of(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER.getBlock(),
                DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock()), event.getLookupProvider()));
    }
}