package com.mcjty.wastify;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mcjty.wastify.Wastify.FEATURE_REGISTRY;

public class WastifyFeature extends Feature<NoneFeatureConfiguration> {

    public static final RegistryObject<WastifyFeature> WASTIFY_FEATURE = FEATURE_REGISTRY.register("wastify", WastifyFeature::new);
    public static Holder<PlacedFeature> WASTIFY_CONFIGURED_FEATURE;

    private Map<Block, BlockState> blockReplacements = null;

    public WastifyFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    public static void registerConfiguredFeatures() {
        WASTIFY_CONFIGURED_FEATURE = registerPlacedFeature(CountPlacement.of(1));
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> Holder<PlacedFeature> registerPlacedFeature(PlacementModifier... placementModifiers) {
        Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> configuredFeatureHolder = Holder.direct(new ConfiguredFeature<>(WASTIFY_FEATURE.get(), FeatureConfiguration.NONE));
        return PlacementUtils.register("wastify:wastify", configuredFeatureHolder, placementModifiers);
    }

//    public static void registxerConfiguredFeatures() {
//        Registry<ConfiguredFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_FEATURE;
//
//        WASTIFY_CONFIGURED_FEATURE = WASTIFY_FEATURE
//                .configured(NoFeatureConfig.NONE)
//                .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(1, 0, 1)));
//
//        Registry.register(registry, new ResourceLocation(Wastify.MODID, "wastify"), WASTIFY_CONFIGURED_FEATURE);
//    }

    private void initBlockReplacements() {
        if (blockReplacements == null) {
            blockReplacements = new HashMap<>();

            List<? extends String> replacements = Config.BLOCK_REPLACEMENTS.get();
            for (String replacement : replacements) {
                String[] split = StringUtils.split(replacement, '=');
                Block source = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0]));
                if (source == null) {
                    Wastify.LOGGER.warn("Block '" + split[0] + "' is missing!");
                } else {
                    Block dest = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[1]));
                    if (dest == null) {
                        Wastify.LOGGER.warn("Block '" + split[1] + "' is missing!");
                    } else {
                        blockReplacements.put(source, dest.defaultBlockState());
                    }
                }
            }
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        initBlockReplacements();
        if (blockReplacements.isEmpty()) {
            return true;
        }
        WorldGenRegion region = (WorldGenRegion) context.level();
        for (int cx = -1 ; cx < 2 ; cx++) {
            for (int cz = -1 ; cz < 2 ; cz++) {
                int chunkX = region.getCenter().x+cx;
                int chunkZ = region.getCenter().z+cz;
                ChunkAccess chunk = region.getChunk(chunkX, chunkZ);

                ChunkStatus status = chunk.getStatus();
                if (!status.isOrAfter(ChunkStatus.FEATURES)) {
                    continue;
                }

                BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
                for (int y = context.level().getMinBuildHeight(); y < context.level().getMaxBuildHeight(); y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            mpos.set(x, y, z);
                            BlockState state = chunk.getBlockState(mpos);
                            BlockState dest = blockReplacements.get(state.getBlock());
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
