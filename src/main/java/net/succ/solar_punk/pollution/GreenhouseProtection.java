package net.succ.solar_punk.pollution;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The blocks sealed greenhouses keep safe from global warming decay: their inside space and
 * the shell around it (floor, walls, roof). Each running Greenhouse Controller registers its
 * area after every scan and drops it when it stops running, so nothing is saved: after a
 * reload the controllers re-register on their next scan.
 */
public final class GreenhouseProtection {
    private GreenhouseProtection() {}

    // Per level (weak, so a closed singleplayer world doesn't linger), per controller position.
    private static final Map<ServerLevel, Map<BlockPos, LongSet>> AREAS = new WeakHashMap<>();

    public static void register(ServerLevel level, BlockPos controller, LongSet area) {
        AREAS.computeIfAbsent(level, l -> new HashMap<>()).put(controller.immutable(), area);
    }

    public static void unregister(ServerLevel level, BlockPos controller) {
        Map<BlockPos, LongSet> areas = AREAS.get(level);
        if (areas != null) areas.remove(controller);
    }

    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        Map<BlockPos, LongSet> areas = AREAS.get(level);
        if (areas == null || areas.isEmpty()) return false;
        long packed = pos.asLong();
        for (LongSet area : areas.values()) {
            if (area.contains(packed)) return true;
        }
        return false;
    }
}
