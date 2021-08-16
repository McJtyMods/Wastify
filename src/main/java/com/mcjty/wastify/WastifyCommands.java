package com.mcjty.wastify;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class WastifyCommands {

    public static ArgumentBuilder<CommandSource, ?> registerListBiomes(CommandDispatcher<CommandSource> dispatcher) {
        return net.minecraft.command.Commands.literal("listbiomes")
                .requires(cs -> cs.hasPermission(2))
                .executes(context -> {
                    Registry<Biome> biomeRegistry = WorldGenRegistries.BIOME;
                    for (Map.Entry<RegistryKey<Biome>, Biome> entry : biomeRegistry.entrySet()) {
                        ResourceLocation id = entry.getKey().location();
                        System.out.println(id);
                    }
                    return 0;
                });
    }

    public static ArgumentBuilder<CommandSource, ?> registerListBlocks(CommandDispatcher<CommandSource> dispatcher) {
        return net.minecraft.command.Commands.literal("listblocks")
                .requires(cs -> cs.hasPermission(2))
                .executes(context -> {
                    for (Map.Entry<RegistryKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
                        ResourceLocation id = entry.getKey().location();
                        System.out.println(id);
                    }
                    return 0;
                });
    }

}
