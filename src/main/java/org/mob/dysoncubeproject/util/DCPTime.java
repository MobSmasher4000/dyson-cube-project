package org.mob.dysoncubeproject.util;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class DCPTime {

    public static long dayTime(Level level) {
        return Math.floorMod(level.getDefaultClockTime(), 24000L);
    }

    public static float timeOfDay(Level level) {
        double d = Mth.frac(dayTime(level) / 24000.0D - 0.25D);
        double e = 0.5D - Math.cos(d * Math.PI) / 2.0D;
        return (float) (d * 2.0D + e) / 3.0F;
    }

    public static boolean isDay(Level level) {
        return dayTime(level) < 12000L;
    }

    public static boolean isNight(Level level) {
        return !isDay(level);
    }
}