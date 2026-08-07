package ru.tfc_aeronautics.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import net.dries007.tfc.common.blocks.LargeVesselBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.crop.Crop;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.feature.tree.ForestConfig;
import net.dries007.tfc.world.settings.RockSettings;

import org.jetbrains.annotations.Nullable;

import ru.tfc_aeronautics.Aeronautics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Rewrites the building materials of an atmospheric-template structure to match the
 * surrounding TFC terrain it is being placed in.
 *
 * <p>Resolves local wood species, soil variant, surface rock and an optional crop choice
 * once per placement (sampling the world <em>before</em> the template overwrites it), then
 * applies the substitutions block-by-block in {@link #processBlock}. Source blocks are
 * matched by family (e.g. "any TFC log", "any TFC mud brick") rather than by their
 * hard-coded NBT variant, so re-authoring a template with different default wood or rock
 * keeps the same processor working.
 *
 * <p>Replaces the older {@code GraveyardMaterialProcessor} for newly added structures.
 * The legacy class is kept for the {@code ancient_graveyard} piece so existing worlds
 * continue to load.
 */
public class LocalMaterialProcessor extends StructureProcessor {
    public static final MapCodec<LocalMaterialProcessor> CODEC = MapCodec.unit(LocalMaterialProcessor::passthrough);

    private static final float GLAZE_CHANCE = 0.5f;
    private static final int WOOD_SEARCH_RADIUS = 32;
    private static final int WOOD_SEARCH_VERTICAL = 14;
    private static final int WOOD_SEARCH_Y_OFFSET = -2;

    private static final List<Wood.BlockType> REPLACEABLE_WOOD_TYPES = List.of(
        Wood.BlockType.LOG,
        Wood.BlockType.STRIPPED_LOG,
        Wood.BlockType.PLANKS,
        Wood.BlockType.SLAB,
        Wood.BlockType.DOOR,
        Wood.BlockType.CHEST,
        Wood.BlockType.BARREL,
        Wood.BlockType.TOOL_RACK,
        Wood.BlockType.LEAVES,
        Wood.BlockType.FALLEN_LEAVES
    );

    private static final List<Rock.BlockType> REPLACEABLE_ROCK_TYPES = List.of(
        Rock.BlockType.RAW,
        Rock.BlockType.BRICKS,
        Rock.BlockType.COBBLE,
        Rock.BlockType.GRAVEL,
        Rock.BlockType.CHISELED
    );

    private static final List<SoilBlockType> REPLACEABLE_SOIL_TYPES = List.of(
        SoilBlockType.GRASS,
        SoilBlockType.DIRT,
        SoilBlockType.DUFF,
        SoilBlockType.COARSE_DIRT,
        SoilBlockType.ROOTED_DIRT,
        SoilBlockType.GRASS_PATH,
        SoilBlockType.FARMLAND,
        SoilBlockType.CLAY_GRASS,
        SoilBlockType.CLAY_DUFF,
        SoilBlockType.CLAY,
        SoilBlockType.MUD_BRICKS
    );

    private static final Map<Block, Wood.BlockType> WOOD_BLOCKS = new HashMap<>();
    private static final Map<Block, Rock.BlockType> ROCK_BLOCKS = new HashMap<>();
    private static final Map<Block, SoilBlockType> SOIL_BLOCKS = new HashMap<>();
    /** Reverse map so we can resolve a {@link RockSettings} back to a {@link Rock} enum. */
    private static final Map<Block, Rock> ROCK_BY_BLOCK = new HashMap<>();

    static {
        for (Wood wood : Wood.values()) {
            for (Wood.BlockType type : REPLACEABLE_WOOD_TYPES) {
                WOOD_BLOCKS.put(wood.getBlock(type).get(), type);
            }
        }
        for (Rock rock : Rock.values()) {
            for (Rock.BlockType type : REPLACEABLE_ROCK_TYPES) {
                final Block block = rock.getBlock(type).get();
                ROCK_BLOCKS.put(block, type);
                ROCK_BY_BLOCK.put(block, rock);
            }
        }
        for (SoilBlockType type : REPLACEABLE_SOIL_TYPES) {
            for (SoilBlockType.Variant variant : SoilBlockType.Variant.values()) {
                SOIL_BLOCKS.put(TFCBlocks.SOIL.get(type).get(variant).get(), type);
            }
        }
    }

    @Nullable private final Wood localWood;
    @Nullable private final SoilBlockType.Variant localSoil;
    @Nullable private final Rock localRock;
    @Nullable private final Block vessel;
    @Nullable private final Crop cropChoice;
    private final float crackedChance;
    private final float mossyChance;
    private final boolean replaceCrops;
    @Nullable private final RandomSource blockRandom;
    private int bricksProcessed;
    private int bricksCracked;

    private LocalMaterialProcessor(
        @Nullable Wood localWood,
        @Nullable SoilBlockType.Variant localSoil,
        @Nullable Rock localRock,
        @Nullable Block vessel,
        @Nullable Crop cropChoice,
        float crackedChance,
        float mossyChance,
        boolean replaceCrops,
        @Nullable RandomSource blockRandom
    ) {
        this.localWood = localWood;
        this.localSoil = localSoil;
        this.localRock = localRock;
        this.vessel = vessel;
        this.cropChoice = cropChoice;
        this.crackedChance = crackedChance;
        this.mossyChance = mossyChance;
        this.replaceCrops = replaceCrops;
        this.blockRandom = blockRandom;
    }

    /**
     * Called once after {@link #processBlock} has run for every block in the template;
     * logs the brick totals so we can confirm cracked coverage without manually
     * counting in the world.
     */
    public void logBrickStats() {
        Aeronautics.LOGGER.info(
            "LocalMaterialProcessor brick stats: processed={}, cracked={}, crackedFraction={}",
            bricksProcessed, bricksCracked,
            bricksProcessed == 0 ? 0f : (float) bricksCracked / bricksProcessed);
    }

    private static LocalMaterialProcessor passthrough() {
        return new LocalMaterialProcessor(null, null, null, null, null, 0f, 0f, false, null);
    }

    /**
     * Material set baked into the JSON config of each structure. Decoded once and passed
     * verbatim to {@link #resolve} so the structure's configuration (cracked chance,
     * crop replacement, etc.) is centralised.
     */
    public record MaterialConfig(
        float crackedChance,
        float mossyChance,
        boolean replaceCrops,
        boolean placeSurfaceMarker
    ) {
        public static final MaterialConfig DEFAULT = new MaterialConfig(0f, 0f, false, false);

        public static final Codec<MaterialConfig> CODEC = RecordCodecBuilder.<MaterialConfig>mapCodec(instance ->
            instance.group(
                Codec.FLOAT.optionalFieldOf("cracked_chance", 0f).forGetter(MaterialConfig::crackedChance),
                Codec.FLOAT.optionalFieldOf("mossy_chance", 0f).forGetter(MaterialConfig::mossyChance),
                Codec.BOOL.optionalFieldOf("replace_crops", false).forGetter(MaterialConfig::replaceCrops),
                Codec.BOOL.optionalFieldOf("place_surface_marker", false).forGetter(MaterialConfig::placeSurfaceMarker)
            ).apply(instance, MaterialConfig::new)
        ).codec();
    }

    /**
     * Samples the surrounding world and bakes the resulting replacement choices into a
     * processor instance. The result is the per-placement snapshot of "what does local
     * mean here"; subsequent {@link #processBlock} calls apply it without further I/O.
     */
    public static LocalMaterialProcessor resolve(LevelReader level, BoundingBox box, RandomSource random, MaterialConfig config) {
        final BlockPos center = box.getCenter();
        final Wood wood = resolveWood(level, box, center);
        final SoilBlockType.Variant soil = resolveSoil(level, box, center);
        final Rock rock = resolveRock(level, center);
        final DyeColor[] colors = DyeColor.values();
        final Block vessel = random.nextFloat() < GLAZE_CHANCE
            ? TFCBlocks.GLAZED_LARGE_VESSELS.get(colors[random.nextInt(colors.length)]).get()
            : null;
        // Per-template block random so the cracked-roll is independent per block.
        // Using settings.getRandom(pos) from vanilla yields a LegacyRandomSource seeded
        // by Mth.getSeed(pos); all blocks in a small structure share near-identical
        // seeds and the float roll collapses to "all or nothing".
        final RandomSource blockRandom = RandomSource.create(random.nextLong());
        Aeronautics.LOGGER.info(
            "LocalMaterialProcessor.resolve at {}: rock={}, crackedChance={}, mossyChance={}, replaceCrops={}",
            center, rock, config.crackedChance, config.mossyChance, config.replaceCrops);
        // The crop pick is keyed on the structure's centre BlockPos, not on the
        // structure RNG: both this processor and the farmer_house vessel filler
        // (FarmerHouseEffects.fillVessel) call FarmerHouseCrops.pick with the same
        // centre and must agree on which crop is in play.
        final Crop crop = config.replaceCrops
            ? FarmerHouseCrops.pick(level, center).orElse(null)
            : null;
        return new LocalMaterialProcessor(
            wood, soil, rock, vessel,
            crop,
            config.crackedChance,
            config.mossyChance,
            config.replaceCrops,
            blockRandom
        );
    }

    /**
     * Locates the local wood species by scanning the area around the structure.
     *
     * <p>Farmer_house and similar small surface structures often land on meadows with
     * no LOG block in range; LEAVES drift further and reliably encode the species
     * even when only the canopy is nearby. We therefore make two passes over the same
     * volume: LOGs first (they pin the trunk exactly), then LEAVES as a fallback.
     *
     * <p>Vertical range is anchored to the centre of the structure and offset upward
     * (typical tree canopies sit above the surface), since {@code box.minY()} for a
     * surface-placed template is the floor of the building — below the actual ground
     * where trees grow.
     */
    @Nullable
    private static Wood resolveWood(LevelReader level, BoundingBox box, BlockPos center) {
        final Wood logHit = scanForWood(level, center, Wood.BlockType.LOG);
        if (logHit != null) {
            return logHit;
        }
        final Wood leafHit = scanForWood(level, center, Wood.BlockType.LEAVES);
        if (leafHit != null) {
            return leafHit;
        }
        // No wood in range: pick the species TFC's own forest feature would have chosen
        // here, so structures don't suddenly become acacia in climates that don't grow
        // acacia (eternal frost, taiga, etc.).
        final Wood climateHit = resolveClimateFallback(level, center);
        if (climateHit != null) {
            return climateHit;
        }
        Aeronautics.LOGGER.warn("LocalMaterialProcessor: no wood found in scan radius and no climate-valid species for {}; defaulting to acacia", center);
        return Wood.ACACIA;
    }

    /**
     * Picks a {@link Wood} species valid for the local climate by walking TFC's own
     * {@code forest_trees} configured-feature tag and filtering by
     * {@link ForestConfig.Entry#isValid}. Mirrors {@code ForestFeature.getTrees}:
     * filter by climate, sort by {@code distanceFromMean} (closest first), then take
     * the head. Returns {@code null} if the tag can't be resolved or no entry matches —
     * the caller falls back to {@link Wood#ACACIA} in that case.
     *
     * <p>Rain variance is flipped for the southern hemisphere (matching
     * {@code ForestFeature}); without that, chunks south of the equator would always
     * miss species like pine that read variance as a signed range. Falls back to
     * {@link WorldGenLevel}-aware hemisphere detection; older call paths that hand us a
     * plain {@link LevelReader} just assume the northern sign.
     */
    @Nullable
    private static Wood resolveClimateFallback(LevelReader level, BlockPos center) {
        final ChunkData chunkData;
        final float seaLevelTemp;
        final float groundwater;
        final float rawRainVariance;
        final int elevation = center.getY();
        try {
            chunkData = ChunkData.get(level.getChunk(center));
            if (chunkData == ChunkData.EMPTY) {
                return null;
            }
            seaLevelTemp = chunkData.getAverageSeaLevelTemp(center);
            groundwater = chunkData.getAverageGroundwater(center);
            rawRainVariance = chunkData.getRainVariance(center);
        } catch (RuntimeException e) {
            return null;
        }

        final float temperature = EnvironmentHelpers.adjustAvgTempForElev(elevation, seaLevelTemp);
        final boolean northern = !(level instanceof WorldGenLevel worldGenLevel)
            || SolarCalculator.getInNorthernHemisphere(center, worldGenLevel.getLevel());
        final float rainVariance = rawRainVariance * (northern ? 1f : -1f);

        final Registry<ConfiguredFeature<?, ?>> registry;
        try {
            registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        } catch (RuntimeException e) {
            return null;
        }
        final TagKey<ConfiguredFeature<?, ?>> forestTrees = TagKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath("tfc", "forest_trees"));

        final Optional<? extends net.minecraft.core.HolderSet<ConfiguredFeature<?, ?>>> tag = registry.getTag(forestTrees);
        if (tag.isEmpty()) {
            return null;
        }

        final List<ForestConfig.Entry> matching = new ArrayList<>();
        for (Holder<ConfiguredFeature<?, ?>> holder : tag.get()) {
            if (holder.value().config() instanceof ForestConfig.Entry entry
                && entry.isValid(temperature, groundwater, rainVariance, elevation)) {
                matching.add(entry);
            }
        }
        if (matching.isEmpty()) {
            return null;
        }
        matching.sort(Comparator.comparingDouble(entry ->
            entry.distanceFromMean(temperature, groundwater, rainVariance, elevation)));

        final ForestConfig.Entry closest = matching.get(0);
        final String speciesPath = closest.treeFeature().unwrapKey()
            .map(key -> key.location().getPath())
            .map(path -> path.substring(path.lastIndexOf('/') + 1))
            .orElse(null);
        if (speciesPath == null) {
            return null;
        }
        for (Wood wood : Wood.values()) {
            if (wood.getSerializedName().equals(speciesPath)) {
                return wood;
            }
        }
        return null;
    }

    @Nullable
    private static Wood scanForWood(LevelReader level, BlockPos center, Wood.BlockType target) {
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final int baseY = center.getY() + WOOD_SEARCH_Y_OFFSET;
        // Walk outward in Chebyshev-distance shells so the tree closest to the
        // structure wins regardless of where it sits in the search box. A linear
        // corner-to-corner scan finds whatever happens to be at (-R, -R) first;
        // with a wider radius that becomes a distant acacia instead of the pine
        // standing right next to the building.
        for (int dist = 0; dist <= WOOD_SEARCH_RADIUS; dist++) {
            for (int dx = -dist; dx <= dist; dx++) {
                for (int dz = -dist; dz <= dist; dz++) {
                    if (Math.abs(dx) < dist && Math.abs(dz) < dist) {
                        continue;
                    }
                    for (int dy = -WOOD_SEARCH_VERTICAL; dy <= WOOD_SEARCH_VERTICAL; dy++) {
                        cursor.set(center.getX() + dx, baseY + dy, center.getZ() + dz);
                        final BlockState state = level.getBlockState(cursor);
                        final Wood.BlockType type = WOOD_BLOCKS.get(state.getBlock());
                        if (type == target) {
                            final Wood found = findWoodForBlock(state.getBlock(), target);
                            if (found != null) {
                                return found;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static Wood findWoodForBlock(Block block, Wood.BlockType target) {
        for (Wood wood : Wood.values()) {
            if (wood.getBlock(target).get() == block) {
                return wood;
            }
        }
        return null;
    }

    @Nullable
    private static SoilBlockType.Variant resolveSoil(LevelReader level, BoundingBox box, BlockPos center) {
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = box.maxY() + 4; y >= box.minY(); y--) {
            cursor.set(center.getX(), y, center.getZ());
            final SoilBlockType type = SOIL_BLOCKS.get(level.getBlockState(cursor).getBlock());
            if (type != null) {
                final SoilBlockType.Variant variant = findVariantForBlock(level.getBlockState(cursor).getBlock());
                if (variant != null) {
                    return variant;
                }
            }
        }
        return SoilBlockType.Variant.MOLLISOL;
    }

    @Nullable
    private static SoilBlockType.Variant findVariantForBlock(Block block) {
        for (SoilBlockType type : REPLACEABLE_SOIL_TYPES) {
            for (SoilBlockType.Variant variant : SoilBlockType.Variant.values()) {
                if (TFCBlocks.SOIL.get(type).get(variant).get() == block) {
                    return variant;
                }
            }
        }
        return null;
    }

    @Nullable
    private static Rock resolveRock(LevelReader level, BlockPos center) {
        // Preferred path: TFC's ChunkData carries the surface rock directly.
        try {
            final ChunkData data = ChunkData.get(level.getChunk(center));
            if (data != ChunkData.EMPTY) {
                final Rock r = lookupRock(data.getRockData().getSurfaceRock(center.getX(), center.getZ()));
                if (r != null) {
                    return r;
                }
            }
        } catch (RuntimeException e) {
            // fall through to column sample
        }
        // Fallback: walk the actual world column and pick the first TFC rock block we
        // see. ChunkData is sometimes EMPTY during early worldgen stages or for chunks
        // that were generated by a different mod path; without this fallback localRock
        // would be null and the entire rock-replacement branch (including the cracked
        // brick roll) is skipped, leaving the template's default rocks untouched.
        return sampleRockFromColumn(level, center);
    }

    @Nullable
    private static Rock sampleRockFromColumn(LevelReader level, BlockPos center) {
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final int minY = Math.max(level.getMinBuildHeight(), center.getY() - 32);
        final int maxY = Math.min(level.getMaxBuildHeight(), center.getY() + 64);
        for (int y = maxY; y >= minY; y--) {
            cursor.set(center.getX(), y, center.getZ());
            final Rock rock = ROCK_BY_BLOCK.get(level.getBlockState(cursor).getBlock());
            if (rock != null) {
                return rock;
            }
        }
        return Rock.GRANITE;
    }

    /**
     * Resolves a {@link RockSettings} back to its {@link Rock} enum. RockSettings is a
     * record of blocks with no back-reference, so we match by cobble (every rock has one)
     * with a raw-block fallback. Returns null when the rock isn't in our replacement set.
     */
    @Nullable
    public static Rock lookupRock(RockSettings settings) {
        if (settings == null) {
            return null;
        }
        Rock byCobble = ROCK_BY_BLOCK.get(settings.cobble());
        if (byCobble != null) {
            return byCobble;
        }
        return ROCK_BY_BLOCK.get(settings.raw());
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader level,
        BlockPos offset,
        BlockPos pos,
        StructureTemplate.StructureBlockInfo blockInfo,
        StructureTemplate.StructureBlockInfo relativeBlockInfo,
        StructurePlaceSettings settings
    ) {
        final BlockState state = relativeBlockInfo.state();
        final Block block = state.getBlock();
        final RandomSource random = blockRandom != null ? blockRandom : settings.getRandom(pos);

        // Wood replacement
        if (localWood != null) {
            final Wood.BlockType type = WOOD_BLOCKS.get(block);
            if (type != null) {
                final BlockState replacement = localWood.getBlock(type).get().defaultBlockState();
                return withState(relativeBlockInfo, copyProperties(state, replacement));
            }
        }

        // Soil replacement (grass, dirt, farmland, mud_bricks, etc.)
        if (localSoil != null) {
            final SoilBlockType type = SOIL_BLOCKS.get(block);
            if (type != null) {
                final BlockState replacement = TFCBlocks.SOIL.get(type).get(localSoil).get().defaultBlockState();
                return withState(relativeBlockInfo, copyProperties(state, replacement));
            }
        }

        // Rock replacement (raw, bricks, cobble, gravel, chiseled) + optional cracked bricks
        if (localRock != null) {
            final Rock.BlockType type = ROCK_BLOCKS.get(block);
            if (type != null) {
                if (type == Rock.BlockType.BRICKS) {
                    bricksProcessed++;
                }
                final Rock.BlockType chosen = chooseRockVariant(type, random);
                if (type == Rock.BlockType.BRICKS && chosen == Rock.BlockType.CRACKED_BRICKS) {
                    bricksCracked++;
                }
                final BlockState replacement = localRock.getBlock(chosen).get().defaultBlockState();
                return withState(relativeBlockInfo, copyProperties(state, replacement));
            }
        }

        // Vessel glaze
        if (vessel != null && block instanceof LargeVesselBlock) {
            return withState(relativeBlockInfo, copyProperties(state, vessel.defaultBlockState()));
        }

        // Crop replacement (only for the configured structures)
        if (replaceCrops && cropChoice != null && block instanceof CropBlock) {
            final BlockState replacement = TFCBlocks.CROPS.get(cropChoice).get().defaultBlockState();
            return withState(relativeBlockInfo, copyProperties(state, replacement));
        }

        return relativeBlockInfo;
    }

    /**
     * Copies every property that {@code source} has and {@code target} also has. The
     * target block may be a different family (e.g. log → log of a different wood), so
     * properties that exist on only one side are ignored.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState copyProperties(BlockState source, BlockState target) {
        BlockState result = target;
        for (Property property : source.getProperties()) {
            if (target.hasProperty(property)) {
                result = result.setValue(property, source.getValue(property));
            }
        }
        return result;
    }

    private Rock.BlockType chooseRockVariant(Rock.BlockType base, RandomSource random) {
        if (base == Rock.BlockType.BRICKS && crackedChance > 0f && random.nextInt(100) < Math.round(crackedChance * 100f)) {
            return Rock.BlockType.CRACKED_BRICKS;
        }
        return base;
    }

    private static StructureTemplate.StructureBlockInfo withState(StructureTemplate.StructureBlockInfo info, BlockState state) {
        return new StructureTemplate.StructureBlockInfo(info.pos(), state, info.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return AeronauticsProcessorTypes.LOCAL_MATERIAL.get();
    }
}
