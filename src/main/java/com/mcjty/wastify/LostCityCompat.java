package com.mcjty.wastify;

import net.minecraft.world.level.Level;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

public class LostCityCompat {

    private static boolean hasLostCities = false;

    public static void register() {
        hasLostCities = ModList.get().isLoaded("lostcities");
        if (hasLostCities) {
            registerInternal();
        }
    }

    private static boolean registered = false;
    private static final LostCityInternal lostCityInternal = new LostCityInternal();

    public static boolean hasLostCities() {
        return hasLostCities;
    }

    public static boolean isInSphere(Level level, int chunkX, int chunkZ) {
        if (hasLostCities) {
            return LostCityInternal.isInSphere(level, chunkX, chunkZ);
        }
        return false;
    }

    private static void registerInternal() {
        if (registered) {
            return;
        }
        registered = true;
        Wastify.LOGGER.info("RFTools Dimensions detected LostCities: enabling support");
        InterModComms.sendTo("lostcities", "getLostCities", LostCityInternal.GetLostCity::new);
    }

}
