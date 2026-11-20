package net.succ.solar_punk.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.succ.solar_punk.block.entity.custom.WireConnectorBlockEntity;
import net.succ.solar_punk.block.entity.custom.WireNodeBlockEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared bookkeeping for wire networks. A network is never stored: each Wire Connector finds
 * the connectors it can reach by walking the wires through loaded wire nodes, and caches the
 * result until any wire, node or switch anywhere changes ({@link #topologyVersion()}).
 *
 * <p>Each wire carries at most its tier's rate per tick, shared by everything routed through
 * it that tick, so a thin wire in the middle of a network limits what gets past it.
 */
public final class WireNetwork {

    /** One terminal of one wire node. */
    public record End(BlockPos pos, int terminal) {}

    /** One wire, the same whichever end it's seen from. */
    public record WireKey(long a, int ta, long b, int tb) {
        public static WireKey of(BlockPos x, int tx, BlockPos y, int ty) {
            long lx = x.asLong(), ly = y.asLong();
            boolean xFirst = lx < ly || (lx == ly && tx <= ty);
            return xFirst ? new WireKey(lx, tx, ly, ty) : new WireKey(ly, ty, lx, tx);
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

    private record Visit(WireNodeBlockEntity node, int terminal) {}

    /**
     * Every connector reachable from {@code start} over loaded wires, nearest (fewest wires)
     * first. A wire only counts if both ends still list it. A closed switch joins its two
     * terminals without costing a wire; an open one stops the walk at the terminal it came in on.
     */
    public static List<Endpoint> findEndpoints(Level level, WireConnectorBlockEntity start) {
        List<Endpoint> endpoints = new ArrayList<>();
        Map<End, List<Step>> paths = new HashMap<>();
        ArrayDeque<Visit> queue = new ArrayDeque<>();
        paths.put(new End(start.getBlockPos(), 0), List.of());
        queue.add(new Visit(start, 0));
        while (!queue.isEmpty()) {
            Visit visit = queue.poll();
            WireNodeBlockEntity current = visit.node();
            BlockPos here = current.getBlockPos();
            List<Step> path = paths.get(new End(here, visit.terminal()));

            // Across a closed switch to its other terminals, for free, so it's walked first.
            if (current.joinsTerminals()) {
                for (int t = 0; t < current.terminals(); t++) {
                    End across = new End(here, t);
                    if (paths.containsKey(across)) continue;
                    paths.put(across, path);
                    queue.addFirst(new Visit(current, t));
                }
            }

            for (WireNodeBlockEntity.Connection wire : current.connections()) {
                if (wire.terminal() != visit.terminal()) continue;
                End next = new End(wire.other(), wire.otherTerminal());
                if (paths.containsKey(next) || !level.isLoaded(next.pos())) continue;
                if (!(level.getBlockEntity(next.pos()) instanceof WireNodeBlockEntity other)
                        || !other.isConnectedTo(next.terminal(), here, visit.terminal())) continue;
                List<Step> nextPath = new ArrayList<>(path);
                nextPath.add(new Step(WireKey.of(here, visit.terminal(), next.pos(), next.terminal()), wire.tier()));
                paths.put(next, nextPath);
                if (other instanceof WireConnectorBlockEntity)
                    endpoints.add(new Endpoint(next.pos(), List.copyOf(nextPath)));
                queue.add(new Visit(other, next.terminal()));
            }
        }
        return endpoints;
    }

    private WireNetwork() {}
}
