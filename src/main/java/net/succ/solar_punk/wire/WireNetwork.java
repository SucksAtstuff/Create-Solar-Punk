package net.succ.solar_punk.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.succ.solar_punk.block.entity.custom.WireConnectorBlockEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared bookkeeping for wire networks. A network is never stored: each Wire Connector finds
 * the connectors it can reach by walking the wires through loaded connectors, and caches the
 * result until any wire or connector anywhere changes ({@link #topologyVersion()}).
 *
 * <p>Each wire carries at most its tier's rate per tick, shared by everything routed through
 * it that tick, so a thin wire in the middle of a network limits what gets past it.
 */
public final class WireNetwork {

    /** One wire, the same whichever end it's seen from. */
    public record WireKey(long a, long b) {
        public static WireKey of(BlockPos x, BlockPos y) {
            long lx = x.asLong(), ly = y.asLong();
            return lx < ly ? new WireKey(lx, ly) : new WireKey(ly, lx);
        }
    }

    public record Step(WireKey key, WireTier tier) {}

    /** A connector reachable from the one that built this, and the wires on the way there. */
    public record Endpoint(BlockPos pos, List<Step> path) {}

    private static final AtomicInteger TOPOLOGY = new AtomicInteger();
    private static final Map<Level, Usage> USAGE = new WeakHashMap<>();
    // Set while energy is being routed, so a receiver that pushes energy straight back into
    // a connector can't recurse through the network.
    private static boolean routing = false;

    private static class Usage {
        long tick = Long.MIN_VALUE;
        final Map<WireKey, Integer> used = new HashMap<>();
    }

    public static int topologyVersion() {
        return TOPOLOGY.get();
    }

    public static void changed() {
        TOPOLOGY.incrementAndGet();
    }

    public static boolean isRouting() {
        return routing;
    }

    public static void setRouting(boolean value) {
        routing = value;
    }

    /** FE that can still go along this path this tick. */
    public static int capacity(Level level, List<Step> path) {
        Usage usage = usage(level);
        int capacity = Integer.MAX_VALUE;
        for (Step step : path)
            capacity = Math.min(capacity, step.tier().rate() - usage.used.getOrDefault(step.key(), 0));
        return Math.max(0, capacity);
    }

    public static void use(Level level, List<Step> path, int amount) {
        Usage usage = usage(level);
        for (Step step : path) usage.used.merge(step.key(), amount, Integer::sum);
    }

    private static Usage usage(Level level) {
        Usage usage = USAGE.computeIfAbsent(level, l -> new Usage());
        long now = level.getGameTime();
        if (usage.tick != now) {
            usage.tick = now;
            usage.used.clear();
        }
        return usage;
    }

    /**
     * Every connector reachable from {@code start} over loaded wires, nearest (fewest wires)
     * first. A wire only counts if both ends still list it.
     */
    public static List<Endpoint> findEndpoints(Level level, WireConnectorBlockEntity start) {
        List<Endpoint> endpoints = new ArrayList<>();
        Map<BlockPos, List<Step>> paths = new HashMap<>();
        ArrayDeque<WireConnectorBlockEntity> queue = new ArrayDeque<>();
        paths.put(start.getBlockPos(), List.of());
        queue.add(start);
        while (!queue.isEmpty()) {
            WireConnectorBlockEntity current = queue.poll();
            List<Step> path = paths.get(current.getBlockPos());
            for (WireConnectorBlockEntity.Connection wire : current.connections()) {
                BlockPos next = wire.other();
                if (paths.containsKey(next) || !level.isLoaded(next)) continue;
                if (!(level.getBlockEntity(next) instanceof WireConnectorBlockEntity other)
                        || !other.isConnectedTo(current.getBlockPos())) continue;
                List<Step> nextPath = new ArrayList<>(path);
                nextPath.add(new Step(WireKey.of(current.getBlockPos(), next), wire.tier()));
                paths.put(next, nextPath);
                endpoints.add(new Endpoint(next, List.copyOf(nextPath)));
                queue.add(other);
            }
        }
        return endpoints;
    }

    private WireNetwork() {}
}
