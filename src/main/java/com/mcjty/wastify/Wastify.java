package com.mcjty.wastify;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Wastify.MODID)
public class Wastify {

    public static final String MODID = "wastify";

    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Feature<?>> FEATURE_REGISTRY = DeferredRegister.create(Registry.FEATURE_REGISTRY, MODID);
    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOMESOURCE_REGISTRY = DeferredRegister.create(Registry.BIOME_SOURCE_REGISTRY, MODID);

    public static final RegistryObject<Codec<WastifyBiomeSource>> WASTIFY_BIOME_SOURCE = BIOMESOURCE_REGISTRY.register("wastify_biomes", () -> WastifyBiomeSource.CODEC);

    public Wastify() {
        Config.register();

        // Register the setup method for modloading
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Wastify::init);
        FEATURE_REGISTRY.register(bus);
        BIOMESOURCE_REGISTRY.register(bus);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WastifyFeature.registerConfiguredFeatures();
//            Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(Wastify.MODID, "wastify_biomes"), WastifyBiomeSource.CODEC);
        });
    }
}
