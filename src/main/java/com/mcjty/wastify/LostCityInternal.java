package com.mcjty.wastify;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostcities.api.ILostSphere;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public class LostCityInternal {

    static ILostCities lostCities = null;

    public static boolean isInSphere(Level level, int x, int z) {
        ILostCityInformation lostInfo = lostCities.getLostInfo(level);
        if (lostInfo == null) {
            return false;
        }
        ILostSphere sphere = lostInfo.getSphere(x, z);
        if (sphere == null) {
            return false;
        }
        if (!sphere.isEnabled()) {
            return false;
        }
        double sqdist = sphere.getCenterPos().distToCenterSqr(x, sphere.getCenterPos().getY(), z);
        double dist = Math.sqrt(sqdist);
        return dist < sphere.getRadius()-5; // Take some distance from the border
    }

    public static class GetLostCity implements Function<ILostCities, Void> {

        @Override
        public Void apply(ILostCities tm) {
            lostCities = tm;
            return null;
        }
    }
}
