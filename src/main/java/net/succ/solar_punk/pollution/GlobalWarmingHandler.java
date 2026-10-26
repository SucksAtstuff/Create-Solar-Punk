package net.succ.solar_punk.pollution;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.compat.sable.SableCompat;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

// Global warming: every second, active pollution sources near players add pollution to
// the chunks around them. Pollution slowly decays, leaves absorb some, and past
// Config.biomeDecayThreshold a chunk's surface decays and its biome turns into the dead
// biome. Once it drops back below, the chunk heals and its biome is restored.
public class GlobalWarmingHandler {

    // Blocks matched by Config.autoDetectKeywords, built lazily and reset on config reload.
    private static Set<Block> autoDetectedSources = null;

    public static void invalidateAutoDetectedCache() {
        autoDetectedSources = null;
    }

    private static Set<Block> getAutoDetectedSources() {
        if (autoDetectedSources != null) return autoDetectedSources;

        Set<Block> result = new HashSet<>();
        List<? extends String> keywords = Config.autoDetectKeywords;
        if (keywords != null && !keywords.isEmpty()) {
            for (Block block : BuiltInRegistries.BLOCK) {
                String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
                if (block.defaultBlockState().is(ModTags.Blocks.POLLUTION_SOURCES)) continue;
                if (Config.pollutionBlacklist.contains(BuiltInRegistries.BLOCK.getKey(block).toString())) continue;
                for (String keyword : keywords) {
                    if (path.contains(keyword)) {
                        result.add(block);
                        break;
                    }
                }
            }
        }
        autoDetectedSources = result;
        return result;
    }

    /**
     * Pollution per second this block emits right now, or -1 if it isn't an active
     * source: not in the tag or auto-detected, unlit, or blacklisted.
     */
    public static int sourcePollution(BlockState state, Set<Block> autoSources) {
        boolean inTag = state.is(ModTags.Blocks.POLLUTION_SOURCES);
        if (!inTag && !autoSources.contains(state.getBlock())) return -1;
        if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) return -1;

        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if (Config.pollutionBlacklist.contains(blockId)) return -1;
        return inTag ? Config.perBlockPollution.getOrDefault(blockId, Config.pollutionPerSource)
                     : Config.autoDetectPollution;
    }

    /**
     * Calls {@code action} for every chunk whose nearest point is within {@code radius}
     * blocks of {@code pos}. A radius of 0 means just the chunk {@code pos} is in.
     */
    public static void forEachChunkInRadius(BlockPos pos, int radius, Consumer<ChunkPos> action) {
        ChunkPos center = new ChunkPos(pos);
        int radiusChunks = radius > 0 ? (int) Math.ceil(radius / 16.0) : 0;
        double radiusSq = (double) radius * radius;
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                ChunkPos chunk = new ChunkPos(center.x + dx, center.z + dz);
                if (radius > 0) {
                    int nearX = Math.max(chunk.getMinBlockX(), Math.min(pos.getX(), chunk.getMaxBlockX()));
                    int nearZ = Math.max(chunk.getMinBlockZ(), Math.min(pos.getZ(), chunk.getMaxBlockZ()));
                    double distSq = (double) (nearX - pos.getX()) * (nearX - pos.getX())
                                  + (double) (nearZ - pos.getZ()) * (nearZ - pos.getZ());
                    if (distSq > radiusSq) continue;
                }
                action.accept(chunk);
            }
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!Config.globalWarmingEnabled) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        long gameTime = serverLevel.getGameTime();
        if (gameTime % 20 != 0) return;

        Map<ChunkPos, Long> activePollution = countActiveSources(serverLevel);

        PollutionSavedData data = PollutionSavedData.get(serverLevel);

        for (Map.Entry<ChunkPos, Long> entry : activePollution.entrySet()) {
            data.addPollution(entry.getKey(), entry.getValue());
        }

        if (Config.pollutionDecayRate > 0) {
            data.decayAll(Config.pollutionDecayRate);
        }

        if (gameTime % Config.biomeDecayInterval == 0) {
            handlePollutedChunks(serverLevel, data);
        }
    }

    private static Map<ChunkPos, Long> countActiveSources(ServerLevel level) {
        Map<ChunkPos, Long> totals = new HashMap<>();
        int viewDist = Math.min(level.getServer().getPlayerList().getViewDistance(), 16);
        Set<Block> autoSources = getAutoDetectedSources();

        Set<ChunkPos> visited = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -viewDist; dx <= viewDist; dx++) {
                for (int dz = -viewDist; dz <= viewDist; dz++) {
                    ChunkPos cp = new ChunkPos(center.x + dx, center.z + dz);
                    if (!visited.add(cp)) continue;

                    LevelChunk chunk = level.getChunkSource().getChunkNow(cp.x, cp.z);
                    if (chunk == null) continue;

                    for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                        int amount = sourcePollution(level.getBlockState(pos), autoSources);
                        if (amount < 0) continue;

                        level.sendParticles(
                            ParticleTypes.LARGE_SMOKE,
                            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                            2, 0.15, 0.05, 0.15, 0.01
                        );
                        forEachChunkInRadius(pos, Config.pollutionRadiusBlocks,
                                affected -> totals.merge(affected, (long) amount, Long::sum));
                    }
                }
            }
        }

        if (ModList.get().isLoaded("sable")) {
            SableCompat.countSubLevelSources(level, totals, autoSources);
        }

        return totals;
    }

    private static void handlePollutedChunks(ServerLevel level, PollutionSavedData data) {
        long threshold = Config.biomeDecayThreshold;

        // tryParse: dead_biome isn't validated, and a typo shouldn't throw every interval
        ResourceLocation deadBiomeId = ResourceLocation.tryParse(Config.deadBiome);
        Optional<Holder.Reference<Biome>> deadBiomeHolder = deadBiomeId == null ? Optional.empty()
            : level.registryAccess()
                .registry(Registries.BIOME)
                .flatMap(reg -> reg.getHolder(ResourceKey.create(Registries.BIOME, deadBiomeId)));

        Holder<Biome> deadBiome = deadBiomeHolder.orElse(null);
        BiomeResolver allDead = deadBiome != null ? (x, y, z, sampler) -> deadBiome : null;
        BiomeResolver original = level.getChunkSource().getGenerator().getBiomeSource()::getNoiseBiome;

        List<ChunkAccess> biomesToResend = new ArrayList<>();

        for (Map.Entry<ChunkPos, Long> entry : new ArrayList<>(data.getChunkPollution().entrySet())) {
            long pollution = entry.getValue();
            if (pollution <= 0) continue;

            ChunkPos chunkPos = entry.getKey();
            if (!level.isLoaded(chunkPos.getMiddleBlockPosition(64))) continue;

            // Leaf absorption: trees slow the spread before they die off
            if (Config.leafAbsorptionPerInterval > 0) {
                long absorbed = countLeaves(level, chunkPos) * Config.leafAbsorptionPerInterval;
                if (absorbed > 0) data.reducePollution(chunkPos, absorbed);
            }

            if (pollution >= threshold) {
                // High pollution: decay blocks and convert biome
                replaceSurfaceBlocks(level, chunkPos, GlobalWarmingHandler::getDecayReplacement);
                if (allDead != null) {
                    ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
                    if (chunk != null) {
                        chunk.fillBiomesFromNoise(allDead, level.getChunkSource().randomState().sampler());
                        chunk.setUnsaved(true);
                        biomesToResend.add(chunk);
                        data.markConverted(chunkPos);
                    }
                }
            } else {
                // Below threshold: heal blocks gradually
                replaceSurfaceBlocks(level, chunkPos, GlobalWarmingHandler::getHealReplacement);
            }
        }

        // Restore biomes for converted chunks that have dropped below the threshold
        for (ChunkPos chunkPos : new ArrayList<>(data.getConvertedChunks())) {
            if (data.getPollution(chunkPos) >= threshold) continue;
            if (!level.isLoaded(chunkPos.getMiddleBlockPosition(64))) continue;

            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
            if (chunk != null) {
                chunk.fillBiomesFromNoise(original, level.getChunkSource().randomState().sampler());
                chunk.setUnsaved(true);
                biomesToResend.add(chunk);
                data.markRestored(chunkPos);
            }

            // Keep healing blocks even after biome is restored, until pollution is gone
            if (data.getPollution(chunkPos) > 0)
                replaceSurfaceBlocks(level, chunkPos, GlobalWarmingHandler::getHealReplacement);
        }

        if (!biomesToResend.isEmpty()) {
            level.getChunkSource().chunkMap.resendBiomesForChunks(biomesToResend);
        }
    }

    private static long countLeaves(ServerLevel level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (chunk == null) return 0;
        long count = 0;
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int worldX = chunkPos.getMinBlockX() + lx;
                int worldZ = chunkPos.getMinBlockZ() + lz;
                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);
                for (int dy = -2; dy <= 10; dy++) {
                    if (level.getBlockState(new BlockPos(worldX, surfaceY + dy, worldZ)).is(BlockTags.LEAVES)) count++;
                }
            }
        }
        return count;
    }

    // Picks Config.blocksDecayedPerInterval random columns in the chunk and replaces the
    // first block near the surface that `replacement` has a replacement for.
    private static void replaceSurfaceBlocks(ServerLevel level, ChunkPos chunkPos,
                                             Function<BlockState, BlockState> replacement) {
        if (Config.blocksDecayedPerInterval <= 0) return;
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (chunk == null) return;
        RandomSource random = level.random;

        for (int i = 0; i < Config.blocksDecayedPerInterval; i++) {
            int lx = random.nextInt(16);
            int lz = random.nextInt(16);
            int worldX = chunkPos.getMinBlockX() + lx;
            int worldZ = chunkPos.getMinBlockZ() + lz;
            int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);

            for (int dy = 0; dy >= -4; dy--) {
                BlockPos pos = new BlockPos(worldX, surfaceY + dy, worldZ);
                BlockState state = level.getBlockState(pos);
                BlockState replaced = replacement.apply(state);
                if (replaced == null) continue;

                // Double-tall blocks: remove the upper half first
                if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                        && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
                }
                level.setBlock(pos, replaced, 3);
                break;
            }
        }
    }

    // Reverses getDecayReplacement one step at a time. Never returns a double-tall block.
    @Nullable
    private static BlockState getHealReplacement(BlockState state) {
        Block block = state.getBlock();
        if (block == ModBlocks.ASH_BLOCK.get()) {
            return ModBlocks.RUINED_DIRT.get().defaultBlockState();
        }
        if (block == ModBlocks.RUINED_DIRT.get()) {
            return ModBlocks.DEAD_GRASS_BLOCK.get().defaultBlockState();
        }
        if (block == ModBlocks.DEAD_GRASS_BLOCK.get()) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
        if (block == Blocks.DEAD_BUSH) {
            return Blocks.SHORT_GRASS.defaultBlockState();
        }
        if (block == ModBlocks.DEAD_GRASS.get()) {
            return Blocks.SHORT_GRASS.defaultBlockState();
        }
        return null;
    }

    @Nullable
    private static BlockState getDecayReplacement(BlockState state) {
        Block block = state.getBlock();

        // Ground cover decays: grass -> dead_grass -> ruined_dirt -> ash
        if (block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || block == Blocks.PODZOL) {
            return ModBlocks.DEAD_GRASS_BLOCK.get().defaultBlockState();
        }
        if (block == ModBlocks.DEAD_GRASS_BLOCK.get()) {
            return ModBlocks.RUINED_DIRT.get().defaultBlockState();
        }
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
            return ModBlocks.RUINED_DIRT.get().defaultBlockState();
        }
        if (block == ModBlocks.RUINED_DIRT.get()) {
            return ModBlocks.ASH_BLOCK.get().defaultBlockState();
        }

        // Leaves drop away
        if (state.is(BlockTags.LEAVES)) {
            return Blocks.AIR.defaultBlockState();
        }

        // Flowers wither into dead bushes (bottom half only, or single-block)
        if (state.is(BlockTags.SMALL_FLOWERS) || state.is(BlockTags.TALL_FLOWERS)) {
            if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                    || state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                return Blocks.DEAD_BUSH.defaultBlockState();
            }
            return null;
        }

        // Grasses and ferns die
        if (block == Blocks.SHORT_GRASS || block == Blocks.FERN) {
            return ModBlocks.DEAD_GRASS.get().defaultBlockState();
        }
        if (block == ModBlocks.DEAD_GRASS.get()) {
            return Blocks.AIR.defaultBlockState();
        }
        if (block == Blocks.TALL_GRASS || block == Blocks.LARGE_FERN) {
            if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                    || state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                return Blocks.AIR.defaultBlockState();
            }
            return null;
        }

        return null;
    }
}