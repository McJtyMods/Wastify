package com.mcjty.wastify;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.fml.ModLoadingContext;
import net.neoforged.neoforge.fml.config.ModConfig;

public class Config {

    public static final String CATEGORY_GENERAL = "general";

    public static ModConfigSpec COMMON_CONFIG;

    public static void register() {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        COMMON_BUILDER.comment("General").push(CATEGORY_GENERAL);

        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
    }
}
