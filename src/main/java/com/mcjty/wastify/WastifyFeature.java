package com.mcjty.wastify;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WastifyFeature extends Feature<NoFeatureConfig> {

    @ObjectHolder("wastify:wastify")
    public static WastifyFeature WASTIFY_FEATURE;

    public static ConfiguredFeature<?, ?> WASTIFY_CONFIGURED_FEATURE;

    private Map<ResourceLocation, BlockState> blockReplacements = null;

    public WastifyFeature() {
        super(NoFeatureConfig.CODEC);
    }

    public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event) {
        event.getRegistry().register(new WastifyFeature().setRegistryName("wastify"));
    }

    public static void registerConfiguredFeatures() {
        Registry<ConfiguredFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_FEATURE;

        WASTIFY_CONFIGURED_FEATURE = WASTIFY_FEATURE
                .configured(NoFeatureConfig.NONE)
                .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(1, 0, 1)));

        Registry.register(registry, new ResourceLocation(Wastify.MODID, "wastify"), WASTIFY_CONFIGURED_FEATURE);
    }

    private void initBlockReplacements() {
        if (blockReplacements == null) {
            blockReplacements = new HashMap<>();

            List<? extends String> replacements = Config.BLOCK_REPLACEMENTS.get();
            for (String replacement : replacements) {
                String[] split = StringUtils.split(replacement, '=');
                Block dest = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[1]));
                if (dest == null) {
                    Wastify.LOGGER.warn("Block '" + split[1] + "' is missing!");
                } else {
                    blockReplacements.put(new ResourceLocation(split[0]), dest.defaultBlockState());
                }
            }
        }
    }

    @Override
    public boolean place(ISeedReader reader, ChunkGenerator chunkGenerator, Random random, BlockPos pos, NoFeatureConfig config) {
        initBlockReplacements();
        if (blockReplacements.isEmpty()) {
            return true;
        }
        WorldGenRegion region = (WorldGenRegion) reader;
        for (int cx = -1 ; cx < 2 ; cx++) {
            for (int cz = -1 ; cz < 2 ; cz++) {
                int chunkX = region.getCenterX()+cx;
                int chunkZ = region.getCenterZ()+cz;
                IChunk chunk = region.getChunk(chunkX, chunkZ);

                ChunkStatus status = chunk.getStatus();
                if (!status.isOrAfter(ChunkStatus.FEATURES)) {
                    continue;
                }

                BlockPos.Mutable mpos = new BlockPos.Mutable();
                for (int y = 0; y < reader.getHeight(); y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            mpos.set(x, y, z);
                            BlockState state = chunk.getBlockState(mpos);
                            BlockState dest = blockReplacements.get(state.getBlock().getRegistryName());
                            if (dest != null) {
                                chunk.setBlockState(mpos, dest, false);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
