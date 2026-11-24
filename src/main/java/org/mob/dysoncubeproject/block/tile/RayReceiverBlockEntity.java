package org.mob.dysoncubeproject.block.tile;

import org.mob.dysoncubeproject.Config;
import org.mob.dysoncubeproject.DCPContent;
import org.mob.dysoncubeproject.client.gui.DysonProgressGuiAddon;
import org.mob.dysoncubeproject.client.gui.SubscribeDysonGuiAddon;
import org.mob.dysoncubeproject.client.gui.UnsubscribeDysonGuiAddon;
import org.mob.dysoncubeproject.util.DCPTime;
import org.mob.dysoncubeproject.world.DysonSphereStructure;
import org.mob.dysoncubeproject.world.DysonSphereProgressSavedData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.api.IFactory;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

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

    private float prevPitch = 270f;

    public RayReceiverBlockEntity(BasicTileBlock<RayReceiverBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
        this.dysonSphereId = "";
        this.energyStorageComponent = new EnergyStorageComponent<>(Config.RAY_RECEIVER_POWER_BUFFER, 0, Integer.MAX_VALUE, 19, 22);
        this.currentPitch = 270;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, RayReceiverBlockEntity blockEntity) {
        if (DCPTime.isDay(level) && !level.isRaining() && level.canSeeSky(pos.above())) {
            var dyson = DysonSphereProgressSavedData.get(level);
            var extractingAmount = Math.min(Config.RAY_RECEIVER_EXTRACT_POWER, this.energyStorageComponent.getMaxEnergyStored() - this.energyStorageComponent.getEnergyStored());
            var extracted = dyson.getSpheres().computeIfAbsent(this.dysonSphereId, s -> new DysonSphereStructure()).extractPower(extractingAmount);
            this.energyStorageComponent.setEnergyStored(this.energyStorageComponent.getEnergyStored() + extracted);
        }

        EnergyHandler handler = level.getCapability(Capabilities.Energy.BLOCK, pos.below(), Direction.UP);
        if (handler != null) {
            int toSend = Math.min(Config.RAY_RECEIVER_EXTRACT_POWER, this.energyStorageComponent.getEnergyStored());
            if (toSend > 0) {
                try (var tx = Transaction.openRoot()) {
                    int inserted = handler.insert(toSend, tx);
                    if (inserted > 0) {
                        tx.commit();
                        this.energyStorageComponent.setEnergyStored(this.energyStorageComponent.getEnergyStored() - inserted);
                    }
                }
            }
        }

        final float oldPitch = this.currentPitch;
        float targetPitch = DCPTime.timeOfDay(level) * 360f;

        if (targetPitch >= 90 && targetPitch <= 270) {
            targetPitch = 270;
        }

        float diff = ((targetPitch - this.currentPitch + 540f) % 360f) - 180f;
        if (Math.abs(diff) <= 1f) {
            this.currentPitch = targetPitch;
        } else {
            this.currentPitch = (this.currentPitch + Math.signum(diff) + 360f) % 360f;
        }

        if (oldPitch != this.currentPitch) {
            syncObject(this.currentPitch);
            markForUpdate();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientTick(Level level, BlockPos pos, BlockState state, RayReceiverBlockEntity blockEntity) {
        this.prevPitch = this.currentPitch;

        if (level instanceof ClientLevel && (level.getGameTime() + pos.asLong()) % (17 * 20) == 0 && DCPTime.isDay(level) && !level.isRaining()) {
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(DCPContent.Sounds.RAY.get(), SoundSource.BLOCKS, 0.5f, 1f, level.getRandom(), pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    @Override
    public InteractionResult onActivated(Player player, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        openGui(player);
        return InteractionResult.SUCCESS;
    }

    public void openGui(Player player) {
        if (player instanceof ServerPlayer sp) {
            sp.openMenu(this, (buffer) -> LocatorFactory.writePacketBuffer(buffer, new TileEntityLocatorInstance(this.worldPosition)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull List<IFactory<?>> getScreenAddons() {
        List<IFactory<?>> list = new ArrayList<>();
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
        syncObject(this.dysonSphereId);
        markForUpdate();
    }

    public EnergyStorageComponent<RayReceiverBlockEntity> getEnergyStorageComponent() {
        return energyStorageComponent;
    }

    public float getCurrentPitch() {
        return currentPitch;
    }

    public float getPrevPitch() {
        return prevPitch;
    }
}