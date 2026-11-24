package org.mob.dysoncubeproject.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.mob.dysoncubeproject.Config;

public class DysonSphereStructure {

    public static final Codec<DysonSphereStructure> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("beams", 0).forGetter(DysonSphereStructure::getBeams),
            Codec.INT.optionalFieldOf("solarPanels", 0).forGetter(DysonSphereStructure::getSolarPanels),
            Codec.INT.optionalFieldOf("storedPower", 0).forGetter(DysonSphereStructure::getStoredPower),
            Codec.INT.optionalFieldOf("lastConsumedPower", 0).forGetter(DysonSphereStructure::getLastConsumedPower)
    ).apply(i, DysonSphereStructure::new));

    private int beams;
    private int solarPanels;
    private int storedPower;
    private int lastConsumedPower;

    public DysonSphereStructure() {
        this(0, 0);
    }

    public DysonSphereStructure(int beams, int solarPanels) {
        this(beams, solarPanels, 0, 0);
    }

    public DysonSphereStructure(int beams, int solarPanels, int storedPower, int lastConsumedPower) {
        this.beams = beams;
        this.solarPanels = solarPanels;
        this.storedPower = storedPower;
        this.lastConsumedPower = lastConsumedPower;
    }

    public int getBeams() { return beams; }

    public void setBeams(int beams) { this.beams = beams; }

    public int getSolarPanels() { return solarPanels; }

    public void setSolarPanels(int solarPanels) { this.solarPanels = solarPanels; }

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

    public int getStoredPower() { return storedPower; }

    public int getLastConsumedPower() { return lastConsumedPower; }
}