package com.mcjty.wastify;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.layer.Layer;
import net.minecraft.world.gen.layer.LayerUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WastifyBiomeProvider extends BiomeProvider {
    public static final Codec<WastifyBiomeProvider> CODEC = RecordCodecBuilder.create(
            instance -> instance
                    .group(
                            Codec.LONG.fieldOf("seed").stable().forGetter(provider -> provider.seed),
                            Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter(provider -> provider.largeBiomes),
                            RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(provider -> provider.biomes))
                    .apply(instance, instance.stable(WastifyBiomeProvider::new)));

    private final Layer noiseBiomeLayer;
    private static final List<RegistryKey<Biome>> POSSIBLE_BIOMES = ImmutableList.of(Biomes.OCEAN, Biomes.PLAINS, Biomes.DESERT, Biomes.MOUNTAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMP, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_MOUNTAINS, Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE, Biomes.BEACH, Biomes.DESERT_HILLS, Biomes.WOODED_HILLS, Biomes.TAIGA_HILLS, Biomes.MOUNTAIN_EDGE, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.JUNGLE_EDGE, Biomes.DEEP_OCEAN, Biomes.STONE_SHORE, Biomes.SNOWY_BEACH, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.DARK_FOREST, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA_HILLS, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.WOODED_MOUNTAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.BADLANDS, Biomes.WOODED_BADLANDS_PLATEAU, Biomes.BADLANDS_PLATEAU, Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.SUNFLOWER_PLAINS, Biomes.DESERT_LAKES, Biomes.GRAVELLY_MOUNTAINS, Biomes.FLOWER_FOREST, Biomes.TAIGA_MOUNTAINS, Biomes.SWAMP_HILLS, Biomes.ICE_SPIKES, Biomes.MODIFIED_JUNGLE, Biomes.MODIFIED_JUNGLE_EDGE, Biomes.TALL_BIRCH_FOREST, Biomes.TALL_BIRCH_HILLS, Biomes.DARK_FOREST_HILLS, Biomes.SNOWY_TAIGA_MOUNTAINS, Biomes.GIANT_SPRUCE_TAIGA, Biomes.GIANT_SPRUCE_TAIGA_HILLS, Biomes.MODIFIED_GRAVELLY_MOUNTAINS, Biomes.SHATTERED_SAVANNA, Biomes.SHATTERED_SAVANNA_PLATEAU, Biomes.ERODED_BADLANDS, Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, Biomes.MODIFIED_BADLANDS_PLATEAU);
    private final long seed;
    private final boolean largeBiomes;
    private final Registry<Biome> biomes;
    private Map<ResourceLocation, Biome> biomeMapping = null;

    public WastifyBiomeProvider(long seed, boolean largeBiomes, Registry<Biome> biomes) {
        super(java.util.stream.Stream.concat(POSSIBLE_BIOMES.stream(), net.minecraftforge.common.BiomeManager.getAdditionalOverworldBiomes().stream()).map((p_242638_1_) -> () -> biomes.getOrThrow(p_242638_1_)));
        this.seed = seed;
        this.largeBiomes = largeBiomes;
        this.biomes = biomes;
        this.noiseBiomeLayer = LayerUtil.getDefaultLayer(seed, false, largeBiomes ? 6 : 4, 4);
    }

    @Override
    protected Codec<? extends BiomeProvider> codec() {
        return CODEC;
    }

    @Override
    public BiomeProvider withSeed(long seed) {
        return new WastifyBiomeProvider(seed, this.largeBiomes, this.biomes);
    }

    private void initBiomeMapping() {
        if (biomeMapping == null) {
            biomeMapping = new HashMap<>();

            List<? extends String> replacements = Config.BIOME_REPLACEMENTS.get();
            for (String replacement : replacements) {
                String[] split = StringUtils.split(replacement, '=');
                Biome dest = biomes.get(new ResourceLocation(split[1]));
                if (dest == null) {
                    Wastify.LOGGER.warn("Biome '" + split[1] + "' is missing!");
                } else {
                    biomeMapping.put(new ResourceLocation(split[0]), dest);
                }
            }
        }
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        Biome biome = this.noiseBiomeLayer.get(this.biomes, x, z);
        initBiomeMapping();
        return biomeMapping.getOrDefault(biome.getRegistryName(), biome);
    }
}
