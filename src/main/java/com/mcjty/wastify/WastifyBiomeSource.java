package com.mcjty.wastify;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WastifyBiomeSource extends BiomeSource {
    public static final Codec<WastifyBiomeSource> CODEC = RecordCodecBuilder.create(
            instance -> instance
                    .group(
                            RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(provider -> provider.biomes),
                            Codec.STRING.fieldOf("dimension").forGetter(provider -> provider.dimensionId),
                            BiomeSource.CODEC.fieldOf("adapt_biome_source").forGetter(provider -> provider.biomeSource),
                            Codec.STRING.optionalFieldOf("default").forGetter(provider -> Optional.ofNullable(provider.mapper.getDefaultBiomeId())),
                            Codec.STRING.optionalFieldOf("sphere_default").forGetter(provider -> Optional.ofNullable(provider.sphereMapper.getDefaultBiomeId())),
                            Codec.list(Codec.STRING).fieldOf("mapping").forGetter(provider -> provider.mapper.getBiomeMappingList()),
                            Codec.list(Codec.STRING).optionalFieldOf("sphere_mapping").forGetter(provider -> Optional.ofNullable(provider.sphereMapper.getBiomeMappingList()))
                    )
                    .apply(instance, instance.stable(WastifyBiomeSource::new)));

    private final String dimensionId;
    private final Registry<Biome> biomes;
    private final BiomeSource biomeSource;
    private final BiomeMapper mapper;
    private final BiomeMapper sphereMapper;
    private Level level;    // Only needed for Lost Cities

    public WastifyBiomeSource(Registry<Biome> biomes, String dimensionId, BiomeSource biomeSource,
                              Optional<String> defaultBiome,
                              Optional<String> defaultSphereBiome,
                              List<String> biomeMappingList,
                              Optional<List<String>> sphereBiomeMappingList) {
        super(biomeSource.possibleBiomes().stream());
        this.dimensionId = dimensionId;
        this.biomes = biomes;
        this.biomeSource = biomeSource;
        this.mapper = new BiomeMapper(biomes, defaultBiome, biomeMappingList);
        this.sphereMapper = new BiomeMapper(biomes, defaultSphereBiome, sphereBiomeMappingList.orElse(Collections.emptyList()));
    }

    @Override
    @Nonnull
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long pSeed) {
        return new WastifyBiomeSource(biomes, dimensionId, biomeSource, Optional.ofNullable(mapper.getDefaultBiomeId()),
                Optional.ofNullable(sphereMapper.getDefaultBiomeId()), mapper.getBiomeMappingList(),
                sphereMapper.getBiomeMappingList().isEmpty() ? Optional.empty() : Optional.of(sphereMapper.getBiomeMappingList()));
    }

    @Override
    @Nonnull
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        Holder<Biome> biome = biomeSource.getNoiseBiome(x, y, z, sampler);
        BiomeMapper m = mapper;
        if (LostCityCompat.hasLostCities()) {
            if (level == null) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                level = server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimensionId)));
            }
            if (level != null && LostCityCompat.isInSphere(level, x << 2, z << 2)) {
                m = sphereMapper;
            }
        }
        return m.getMappedBiome(biome);
    }
}
