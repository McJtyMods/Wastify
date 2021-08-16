package com.mcjty.wastify;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

public class Config {

    public static final String CATEGORY_GENERAL = "general";

    public static ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BIOME_REPLACEMENTS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_REPLACEMENTS;

    public static void register() {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("General").push(CATEGORY_GENERAL);

        BIOME_REPLACEMENTS = COMMON_BUILDER
                .comment("This is a list of <oldbiome>=<newbiome> that will be used to map biomes")
                .defineList("biomeReplacements", Lists.newArrayList(), s -> s instanceof String);
        BLOCK_REPLACEMENTS = COMMON_BUILDER
                .comment("This is a list of <oldblock>=<newblock> that will be used to map blocks")
                .defineList("blockReplacements", Lists.newArrayList(), s -> s instanceof String);

        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
    }
}
