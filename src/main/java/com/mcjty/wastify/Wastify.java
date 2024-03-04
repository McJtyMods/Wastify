package com.mcjty.wastify;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Wastify.MODID)
public class Wastify {

    public static final String MODID = "wastify";

    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOMESOURCE_REGISTRY = DeferredRegister.create(Registries.BIOME_SOURCE, MODID);
    public static final Supplier<Codec<WastifyBiomeSource>> WASTIFY_BIOME_SOURCE = BIOMESOURCE_REGISTRY.register("wastify_biomes", () -> WastifyBiomeSource.CODEC);

    public Wastify() {
        Config.register();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BIOMESOURCE_REGISTRY.register(bus);
        bus.addListener(this::onSetup);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal(Wastify.MODID)
                        .then(WastifyCommands.registerListBiomes(dispatcher)));
    }

    public void onSetup(FMLCommonSetupEvent event) {
        LostCityCompat.register();
    }

}
