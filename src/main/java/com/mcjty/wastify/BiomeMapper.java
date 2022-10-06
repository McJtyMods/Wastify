package com.mcjty.wastify;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BiomeMapper {
    final Registry<Biome> biomes;
    private final List<String> biomeMappingList;
    private final String defaultBiomeId;
    private final Holder<Biome> defaultBiome;
    private Map<Biome, Holder<Biome>> biomeMapping = null;

    public BiomeMapper(Registry<Biome> biomes, Optional<String> defaultBiomeId, List<String> biomeMappingList) {
        this.biomes = biomes;
        this.biomeMappingList = biomeMappingList;
        this.defaultBiomeId = defaultBiomeId.orElse(null);
        if (this.defaultBiomeId == null) {
            this.defaultBiome = null;
        } else {
            ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(this.defaultBiomeId));
            this.defaultBiome = biomes.getHolderOrThrow(key);
        }
    }

    public List<String> getBiomeMappingList() {
        return biomeMappingList;
    }

    public String getDefaultBiomeId() {
        return defaultBiomeId;
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


    public Holder<Biome> getMappedBiome(Holder<Biome> biome) {
        initBiomeMapping();
        return biomeMapping.getOrDefault(biome.value(), defaultBiome == null ? biome : defaultBiome);
    }
}
