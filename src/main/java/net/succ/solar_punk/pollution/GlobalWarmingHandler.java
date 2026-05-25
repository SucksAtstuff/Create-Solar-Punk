package net.succ.solar_punk.pollution;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GlobalWarmingHandler {

    public static final TagKey<Block> POLLUTION_SOURCES = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "pollution_sources")
    );

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

        Set<ChunkPos> visited = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -viewDist; dx <= viewDist; dx++) {
                for (int dz = -viewDist; dz <= viewDist; dz++) {
                    ChunkPos cp = new ChunkPos(center.x + dx, center.z + dz);
                    if (!visited.add(cp)) continue;

                    LevelChunk chunk = level.getChunkSource().getChunkNow(cp.x, cp.z);
                    if (chunk == null) continue;

                    for (Map.Entry<BlockPos, BlockEntity> beEntry : chunk.getBlockEntities().entrySet()) {
                        BlockPos pos = beEntry.getKey();
                        BlockState state = level.getBlockState(pos);

                        if (!state.is(POLLUTION_SOURCES)) continue;
                        if (!isActive(state)) continue;

                        level.sendParticles(
                            ParticleTypes.LARGE_SMOKE,
                            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                            2, 0.15, 0.05, 0.15, 0.01
                        );

                        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                        int amount = Config.perBlockPollution.getOrDefault(blockId, Config.pollutionPerSource);
                        totals.merge(cp, (long) amount, Long::sum);
                    }
                }
            }
        }

        return totals;
    }

    private static boolean isActive(BlockState state) {
        if (state.hasProperty(BlockStateProperties.LIT)) {
            return state.getValue(BlockStateProperties.LIT);
        }
        return true;
    }

    private static void handlePollutedChunks(ServerLevel level, PollutionSavedData data) {
        long threshold = Config.biomeDecayThreshold;

        Optional<Holder.Reference<Biome>> deadBiomeHolder = level.registryAccess()
            .registry(Registries.BIOME)
            .flatMap(reg -> reg.getHolder(ResourceKey.create(
                Registries.BIOME,
                ResourceLocation.parse(Config.deadBiome)
            )));

        Holder<Biome> deadBiome = deadBiomeHolder.orElse(null);
        BiomeResolver allDead = deadBiome != null ? (x, y, z, sampler) -> deadBiome : null;

        List<ChunkAccess> biomesToResend = new ArrayList<>();

        for (Map.Entry<ChunkPos, Long> entry : data.getChunkPollution().entrySet()) {
            long pollution = entry.getValue();
            if (pollution <= 0) continue;

            ChunkPos chunkPos = entry.getKey();
            if (!level.isLoaded(chunkPos.getMiddleBlockPosition(64))) continue;

            // Gradual block decay for any polluted chunk
            if (Config.blocksDecayedPerInterval > 0) {
                decayBlocks(level, chunkPos, level.random);
            }

            // Full biome conversion only at threshold
            if (pollution >= threshold && allDead != null) {
                ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
                if (chunk != null) {
                    chunk.fillBiomesFromNoise(allDead, level.getChunkSource().randomState().sampler());
                    chunk.setUnsaved(true);
                    biomesToResend.add(chunk);
                }
            }
        }

        if (!biomesToResend.isEmpty()) {
            level.getChunkSource().chunkMap.resendBiomesForChunks(biomesToResend);
        }
    }

    private static void decayBlocks(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (chunk == null) return;

        for (int i = 0; i < Config.blocksDecayedPerInterval; i++) {
            int lx = random.nextInt(16);
            int lz = random.nextInt(16);
            int worldX = chunkPos.getMinBlockX() + lx;
            int worldZ = chunkPos.getMinBlockZ() + lz;
            int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);

            // Scan down from the surface looking for the first replaceable block
            for (int dy = 0; dy >= -4; dy--) {
                BlockPos pos = new BlockPos(worldX, surfaceY + dy, worldZ);
                BlockState state = level.getBlockState(pos);
                BlockState replacement = getDecayReplacement(state);
                if (replacement == null) continue;

                // Double-tall blocks: remove the upper half first
                if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                        && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
                }
                level.setBlock(pos, replacement, 3);
                break;
            }
        }
    }

    private static BlockState getDecayReplacement(BlockState state) {
        Block block = state.getBlock();

        // Ground cover decays: grass → dead_grass → ruined_dirt → ash
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