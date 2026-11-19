package org.mob.dysoncubeproject.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;
import org.mob.dysoncubeproject.Config;

public class DysonSphereStructure implements INBTSerializable<CompoundTag> {

    private int beams;
    private int solarPanels;
    private int storedPower;
    private int lastConsumedPower;
    private String password = "";
    private boolean hasPassword = false;

    public DysonSphereStructure() {
        this(0, 0);
    }

    public DysonSphereStructure(int beams, int solarPanels) {
        this.beams = beams;
        this.solarPanels = solarPanels;
    }

    public int getBeams() {
        return beams;
    }

    public void setBeams(int beams) {
        this.beams = beams;
    }

    public int getSolarPanels() {
        return solarPanels;
    }

    public void setSolarPanels(int solarPanels) {
        this.solarPanels = solarPanels;
    }

    public int getMaxSolarPanels() {
        return Math.min(Config.MAX_SOLAR_PANELS, beams * Config.BEAM_TO_SOLAR_PANEL_RATIO);
    }

    public double getProgress() {
        return solarPanels / (double) Config.MAX_SOLAR_PANELS;
    }

    public int getMaxBeams() {
        return (int) Math.ceil(Config.MAX_SOLAR_PANELS / (double) Config.BEAM_TO_SOLAR_PANEL_RATIO);
    }

    public void increaseBeams(int amount) {
        this.beams += amount;
        if (this.beams > getMaxBeams()) this.beams = getMaxBeams();
    }

    public void increaseSolarPanels(int amount) {
        this.solarPanels += amount;
        if (this.solarPanels > getMaxSolarPanels()) this.solarPanels = getMaxSolarPanels();
    }

    public void generatePower() {
        this.lastConsumedPower = 0;
        this.storedPower = Math.min(this.solarPanels * Config.POWER_PER_SAIL, this.storedPower + this.solarPanels * Config.POWER_PER_SAIL);
    }

    public int extractPower(int amount) {
        int extracted = Math.min(amount, this.storedPower);
        this.storedPower -= extracted;
        this.lastConsumedPower += extracted;
        return extracted;
    }

    public int getStoredPower() {
        return storedPower;
    }

    public int getLastConsumedPower() {
        return lastConsumedPower;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? "" : password.trim();
        this.hasPassword = !this.password.isEmpty();
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("beams", beams);
        compoundTag.putInt("solarPanels", solarPanels);
        compoundTag.putInt("storedPower", storedPower);
        compoundTag.putInt("lastConsumedPower", lastConsumedPower);
        compoundTag.putString("password", password);
        compoundTag.putBoolean("hasPassword", hasPassword);
        return compoundTag;
    }

    public CompoundTag serializeClientNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("beams", beams);
        compoundTag.putInt("solarPanels", solarPanels);
        compoundTag.putInt("storedPower", storedPower);
        compoundTag.putInt("lastConsumedPower", lastConsumedPower);
        compoundTag.putBoolean("hasPassword", !password.isEmpty());
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        this.beams = compoundTag.getInt("beams");
        this.solarPanels = compoundTag.getInt("solarPanels");
        this.storedPower = compoundTag.getInt("storedPower");
        this.lastConsumedPower = compoundTag.getInt("lastConsumedPower");
        if (compoundTag.contains("password")) {
            setPassword(compoundTag.getString("password"));
        } else {
            this.password = "";
            this.hasPassword = compoundTag.getBoolean("hasPassword");
        }
    }
}