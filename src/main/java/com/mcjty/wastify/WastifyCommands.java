package com.mcjty.wastify;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;

public class WastifyCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> registerListBiomes(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("listbiomes")
                .requires(cs -> cs.hasPermission(2))
                .executes(context -> {
                    for (Map.Entry<ResourceKey<Biome>, Biome> entry : context.getSource().registryAccess().registry(Registries.BIOME).get().entrySet()) {
                        ResourceLocation id = entry.getKey().location();
                        System.out.println(id);
                    }
                    return 0;
                });
    }

}
