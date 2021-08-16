package com.mcjty.wastify;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Wastify.MODID)
public class Wastify {

    public static final String MODID = "wastify";

    public static final Logger LOGGER = LogManager.getLogger();

    public Wastify() {
        Config.register();

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Wastify::init);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Feature.class, WastifyFeature::registerFeatures);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WastifyFeature.registerConfiguredFeatures();
            Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(Wastify.MODID, "wastify_biomes"), WastifyBiomeProvider.CODEC);
        });
    }
}
