package net.succ.solar_punk.pollution;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The blocks sealed greenhouses keep safe from global warming decay: their inside space and
 * the shell around it (floor, walls, roof). Each running Greenhouse Controller registers its
 * area after every scan and drops it when it stops running, so nothing is saved: after a
 * reload the controllers re-register on their next scan.
 *
 * <p>Biomes are stored in 4x4x4 cells (quarts), so each area also keeps the cells it touches.
 * When a polluted chunk turns into the dead biome, those cells keep their normal biome, which
 * leaves a patch of living biome (grass and foliage colors, Serene Seasons) inside the greenhouse.
 */
public final class GreenhouseProtection {
    private GreenhouseProtection() {}

    private record Area(LongSet blocks, LongSet quarts, Set<ChunkPos> chunks) {}

    // Per level (weak, so a closed singleplayer world doesn't linger), per controller position.
    private static final Map<ServerLevel, Map<BlockPos, Area>> AREAS = new WeakHashMap<>();

    /**
     * Registers (or replaces) a controller's area. Returns the chunks whose biome cells changed
     * coverage, so the caller can refresh their biomes; empty if the area is the same as before.
     */
    public static Set<ChunkPos> register(ServerLevel level, BlockPos controller, LongSet blocks) {
        LongSet quarts = new LongOpenHashSet();
        Set<ChunkPos> chunks = new HashSet<>();
        for (LongIterator it = blocks.iterator(); it.hasNext(); ) {
            long packed = it.nextLong();
            int x = BlockPos.getX(packed), y = BlockPos.getY(packed), z = BlockPos.getZ(packed);
            if (quarts.add(BlockPos.asLong(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z)))) {
                chunks.add(new ChunkPos(x >> 4, z >> 4));
            }
        }
        Area old = AREAS.computeIfAbsent(level, l -> new HashMap<>())
                .put(controller.immutable(), new Area(blocks, quarts, chunks));
        if (old != null && old.quarts.equals(quarts)) return Set.of();
        if (old != null) chunks = union(chunks, old.chunks);
        return chunks;
    }

    /** Drops a controller's area. Returns the chunks it covered, so their biomes can be refreshed. */
    public static Set<ChunkPos> unregister(ServerLevel level, BlockPos controller) {
        Map<BlockPos, Area> areas = AREAS.get(level);
        if (areas == null) return Set.of();
        Area old = areas.remove(controller);
        return old == null ? Set.of() : old.chunks;
    }

    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        Map<BlockPos, Area> areas = AREAS.get(level);
        if (areas == null || areas.isEmpty()) return false;
        long packed = pos.asLong();
        for (Area area : areas.values()) {
            if (area.blocks.contains(packed)) return true;
        }
        return false;
    }

    /** Whether the biome cell at these quart coordinates touches a sealed greenhouse. */
    public static boolean isProtectedQuart(ServerLevel level, int quartX, int quartY, int quartZ) {
        Map<BlockPos, Area> areas = AREAS.get(level);
        if (areas == null || areas.isEmpty()) return false;
        long packed = BlockPos.asLong(quartX, quartY, quartZ);
        for (Area area : areas.values()) {
            if (area.quarts.contains(packed)) return true;
        }
        return false;
    }

    private static Set<ChunkPos> union(Set<ChunkPos> a, Set<ChunkPos> b) {
        Set<ChunkPos> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }
}
