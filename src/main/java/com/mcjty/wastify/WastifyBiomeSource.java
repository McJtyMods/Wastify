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
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WastifyBiomeSource extends BiomeSource {
    public static final Codec<WastifyBiomeSource> CODEC = RecordCodecBuilder.create(
            instance -> instance
                    .group(
                            RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(provider -> provider.biomes),
                            BiomeSource.CODEC.fieldOf("adapt_biome_source").forGetter(provider -> provider.biomeSource),
                            Codec.STRING.optionalFieldOf("default").forGetter(provider -> Optional.ofNullable(provider.defaultBiomeId)),
                            Codec.list(Codec.STRING).fieldOf("mapping").forGetter(provider -> provider.biomeMappingList)
                    )
                    .apply(instance, instance.stable(WastifyBiomeSource::new)));

    private final Registry<Biome> biomes;
    private final BiomeSource biomeSource;
    private final List<String> biomeMappingList;
    private final String defaultBiomeId;
    private final Holder<Biome> defaultBiome;
    private Map<Biome, Holder<Biome>> biomeMapping = null;

    public WastifyBiomeSource(Registry<Biome> biomes, BiomeSource biomeSource, Optional<String> defaultBiome, List<String> biomeMappingList) {
        super(biomeSource.possibleBiomes().stream());
        this.biomes = biomes;
        this.biomeSource = biomeSource;
        this.defaultBiomeId = defaultBiome.orElse(null);
        if (defaultBiomeId == null) {
            this.defaultBiome = null;
        } else {
            ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(defaultBiomeId));
            this.defaultBiome = biomes.getHolderOrThrow(key);
        }
        this.biomeMappingList = biomeMappingList;
    }

    @Override
    @Nonnull
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    private void initBiomeMapping() {
        if (biomeMapping == null) {
            biomeMapping = new HashMap<>();

            for (String replacement : biomeMappingList) {
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
        Holder<Biome> biome = biomeSource.getNoiseBiome(x, y, z, sampler);
        return biomeMapping.getOrDefault(biome.get(), defaultBiome == null ? biome : defaultBiome);
    }
}
