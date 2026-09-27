package org.mob.dysoncubeproject.block.entity;

import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.block.tile.ITickableBlockEntity;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.client.screen.asset.IHasAssetProvider;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.network.IButtonHandler;
import com.hrznstudio.titanium.network.locator.LocatorFactory;
import com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.mob.dysoncubeproject.Config;
import org.mob.dysoncubeproject.block.ModBlocks;
import org.mob.dysoncubeproject.block.custom.RayReceiverControllerBlock;
import org.mob.dysoncubeproject.client.gui.DysonProgressGuiAddon;
import org.mob.dysoncubeproject.client.gui.SubscribeDysonGuiAddon;
import org.mob.dysoncubeproject.client.gui.UnsubscribeDysonGuiAddon;
import org.mob.dysoncubeproject.sound.ModSounds;
import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;
import org.mob.dysoncubeproject.world.DysonSphereStructure;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RayReceiverBlockEntity extends BasicTile<RayReceiverBlockEntity> implements IScreenAddonProvider, ITickableBlockEntity<RayReceiverBlockEntity>, MenuProvider, IButtonHandler, IContainerAddonProvider, IHasAssetProvider, IComponentHarness {


    @Save
    private String dysonSphereId;
    @Save
    private EnergyStorageComponent<RayReceiverBlockEntity> energyStorageComponent;
    @Save
    private float currentPitch;

    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> this.energyStorageComponent);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
    }

    public RayReceiverBlockEntity(BasicTileBlock<RayReceiverBlockEntity> base, BlockPos pos, BlockState state) {
        super(base, ModBlocks.RAY_RECEIVER_CONTROLLER.getRight().get(), pos, state);
        this.dysonSphereId = "";
        this.energyStorageComponent = new EnergyStorageComponent<>(Config.RAY_RECEIVER_POWER_BUFFER, 0, Integer.MAX_VALUE, 19, 22);
        this.currentPitch = 270;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, RayReceiverBlockEntity blockEntity) {
        if (level.isDay() && !level.isRaining() && level.canSeeSky(pos.above())) {
            var dyson = DysonSphereProgressSavedData.get(level);
            var extractingAmount = Math.min(Config.RAY_RECEIVER_EXTRACT_POWER, this.energyStorageComponent.getMaxEnergyStored() - this.energyStorageComponent.getEnergyStored());
            var extracted = dyson.getSpheres().computeIfAbsent(this.dysonSphereId, s -> new DysonSphereStructure()).extractPower(extractingAmount);
            this.energyStorageComponent.setEnergyStored(this.energyStorageComponent.getEnergyStored() + extracted);
        }
        
        BlockEntity belowEntity = level.getBlockEntity(pos.below());
        if (belowEntity != null) {
            belowEntity.getCapability(ForgeCapabilities.ENERGY, Direction.UP).ifPresent(capability -> {
                if (capability.canReceive()) {
                    var received = capability.receiveEnergy(Math.min(Config.RAY_RECEIVER_EXTRACT_POWER, this.energyStorageComponent.getEnergyStored()), true);
                    this.energyStorageComponent.setEnergyStored(this.energyStorageComponent.getEnergyStored() - received);
                    capability.receiveEnergy(received, false);
                }
            });
        }

        float targetPitch = level.getTimeOfDay(1f) * 360f;


        if (targetPitch >= 90 && targetPitch <= 270) {
            targetPitch = 270;
        }


        if ((this.currentPitch) % 360 <= targetPitch) {
            this.currentPitch = Math.min((this.currentPitch + 1) % 360, targetPitch);
        } else if (this.currentPitch > targetPitch) {
            this.currentPitch = Math.max(this.currentPitch - 1, targetPitch);
        }

        pushEnergy(level, pos);

        syncObject(currentPitch);
    }

    private void pushEnergy(Level level, BlockPos pos) {
        if (this.energyStorageComponent.getEnergyStored() <= 0) return;

        for (Direction direction : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(direction));
            if (be != null) {
                be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(neighborEnergy -> {
                    if (neighborEnergy.canReceive()) {

                        int maxPush = Config.RAY_RECEIVER_EXTRACT_POWER;

                        int extractedSim = this.energyStorageComponent.extractEnergy(maxPush, true);

                        if (extractedSim > 0) {
                            int accepted = neighborEnergy.receiveEnergy(extractedSim, false);

                            if (accepted > 0) {
                                this.energyStorageComponent.extractEnergy(accepted, false);
                            }
                        }
                    }
                });
            }
            if (this.energyStorageComponent.getEnergyStored() <= 0) break;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientTick(Level level, BlockPos pos, BlockState state, RayReceiverBlockEntity blockEntity) {
        if (level instanceof ClientLevel clientLevel && (level.getGameTime() + pos.asLong()) % (17 * 20) == 0 && level.dayTime() % 24000 < 12000 && !level.isRaining()) {
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(ModSounds.RAY.get(), SoundSource.BLOCKS, 0.5f, 1f, level.getRandom(), pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    @Override
    public InteractionResult onActivated(Player player, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        openGui(player);
        return InteractionResult.SUCCESS;
    }

    public void openGui(Player player) {
        if (player instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp, this, (buffer) -> LocatorFactory.writePacketBuffer(buffer, new TileEntityLocatorInstance(this.worldPosition)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> list = new ArrayList<>();
        list.addAll(this.energyStorageComponent.getScreenAddons());
        list.add(() -> new DysonProgressGuiAddon(this.dysonSphereId, 56, 24));
        list.add(() -> new SubscribeDysonGuiAddon(this.dysonSphereId, 9, 24 + 60));
        list.add(() -> new UnsubscribeDysonGuiAddon(9 + 18, 24 + 60));
        return list;
    }

    @Override
    public IAssetProvider getAssetProvider() {
        return IAssetProvider.DEFAULT_PROVIDER;
    }

    @Override
    public @NotNull List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        var list = new ArrayList<IFactory<? extends IContainerAddon>>();
        list.addAll(this.energyStorageComponent.getContainerAddons());
        return list;
    }

    @Override
    public void handleButtonMessage(int i, Player player, CompoundTag compoundTag) {

    }

    @Nullable
    public AbstractContainerMenu createMenu(int menu, Inventory inventoryPlayer, Player entityPlayer) {
        return new BasicAddonContainer(this, new TileEntityLocatorInstance(this.worldPosition), this.getWorldPosCallable(), inventoryPlayer, menu);
    }

    @Nonnull
    public Component getDisplayName() {
        return Component.translatable(this.getBasicTileBlock().getDescriptionId()).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
    }

    public ContainerLevelAccess getWorldPosCallable() {
        return this.getLevel() != null ? ContainerLevelAccess.create(this.getLevel(), this.getBlockPos()) : ContainerLevelAccess.NULL;
    }

    @Override
    public Level getComponentWorld() {
        return this.level;
    }

    @Override
    public void markComponentForUpdate(boolean b) {
        this.markForUpdate();
    }

    @Override
    public void markComponentDirty() {
        this.markForUpdate();
    }

    public String getDysonSphereId() {
        return dysonSphereId;
    }

    public void setDysonSphereId(String dysonSphereId) {
        this.dysonSphereId = dysonSphereId;
    }

    public EnergyStorageComponent<RayReceiverBlockEntity> getEnergyStorageComponent() {
        return energyStorageComponent;
    }

    public float getCurrentPitch() {
        return currentPitch;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return RayReceiverControllerBlock.MULTIBLOCK_STRUCTURE.getAABB(this.getBlockPos());
    }
}