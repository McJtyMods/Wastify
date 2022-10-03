package com.mcjty.wastify;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class WastifyBiomeSource extends BiomeSource {
    public static final Codec<WastifyBiomeSource> CODEC = RecordCodecBuilder.create(
            instance -> instance
                    .group(
                            Codec.LONG.fieldOf("seed").stable().forGetter(provider -> provider.seed),
                            Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter(provider -> provider.largeBiomes),
                            RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(provider -> provider.biomes))
                    .apply(instance, instance.stable(WastifyBiomeSource::new)));

//    private final Layer noiseBiomeLayer;
    private final MultiNoiseBiomeSource adapted;
    private final long seed;
    private final boolean largeBiomes;
    private final Registry<Biome> biomes;
    private Map<Biome, Holder<Biome>> biomeMapping = null;

    public WastifyBiomeSource(long seed, boolean largeBiomes, Registry<Biome> biomes) {
        super(getPossibleBiomes(biomes));
        this.seed = seed;
        this.largeBiomes = largeBiomes;
        this.biomes = biomes;
        this.adapted = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomes);
    }

    private static Supplier<List<Holder<Biome>>> getPossibleBiomes(Registry<Biome> biomes) {
        return () -> MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomes).possibleBiomes().stream().toList();
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    private void initBiomeMapping() {
        if (biomeMapping == null) {
            biomeMapping = new HashMap<>();

            List<? extends String> replacements = Config.BIOME_REPLACEMENTS.get();
            for (String replacement : replacements) {
                String[] split = StringUtils.split(replacement, '=');
                Biome source = biomes.get(new ResourceLocation(split[0]));
                if (source == null) {
                    Wastify.LOGGER.warn("Biome '" + split[0] + "' is missing!");
                } else {
                    Biome dest = biomes.get(new ResourceLocation(split[1]));
                    if (dest == null) {
                        Wastify.LOGGER.warn("Biome '" + split[1] + "' is missing!");
                    } else {
                        Optional<ResourceKey<Biome>> key = biomes.getResourceKey(dest);
                        key.ifPresent(h -> {
                            biomeMapping.put(source, biomes.getHolderOrThrow(h));
                        });
                    }
                }
            }
        }
    }


    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        initBiomeMapping();
        Holder<Biome> biome = adapted.getNoiseBiome(x, y, z, sampler);
        return biomeMapping.getOrDefault(biome.get(), biome);
    }
}
