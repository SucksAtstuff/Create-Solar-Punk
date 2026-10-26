package net.succ.solar_punk.compat.sable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.succ.solar_punk.pollution.GlobalWarmingHandler;
import org.joml.Vector3dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SableCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("solarpunk/sable");

    private static boolean initialized = false;
    private static Method mGetContainer;
    private static Method mGetAllSubLevels;
    private static Method mLogicalPose;
    private static Method mPosition;
    private static Method mGetPlot;
    private static Method mGetChunkMin;
    private static Method mGetChunkMax;
    private static Method mGetChunk;
    private static Method mToLocal;

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            Class<?> containerClass = Class.forName("dev.ryanhcode.sable.api.sublevel.SubLevelContainer");
            mGetContainer   = containerClass.getMethod("getContainer", ServerLevel.class);
            mGetAllSubLevels = containerClass.getMethod("getAllSubLevels");

            Class<?> subLevelClass = Class.forName("dev.ryanhcode.sable.sublevel.SubLevel");
            mLogicalPose = subLevelClass.getMethod("logicalPose");
            mGetPlot     = subLevelClass.getMethod("getPlot");

            // Pose3dc is in the companion library; its position() returns Vector3dc (JOML)
            Class<?> pose3dcClass = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc");
            mPosition = pose3dcClass.getMethod("position");

            Class<?> levelPlotClass = Class.forName("dev.ryanhcode.sable.sublevel.plot.LevelPlot");
            mGetChunkMin = levelPlotClass.getMethod("getChunkMin");
            mGetChunkMax = levelPlotClass.getMethod("getChunkMax");
            // A sub-level's blocks live in the plot's own chunk storage, not the host
            // level's chunks. getChunk() takes a plot-local ChunkPos, while
            // getChunkMin/getChunkMax return global ones, hence toLocal().
            mGetChunk = levelPlotClass.getMethod("getChunk", ChunkPos.class);
            mToLocal = levelPlotClass.getMethod("toLocal", ChunkPos.class);
        } catch (Exception e) {
            // Sable present in ModList but reflection failed - disable quietly
            LOGGER.warn("[SolarPunk] Sable compat failed to initialise: {}", e.getMessage());
            mGetContainer = null;
        }
    }

    /**
     * Scans every active Sable sublevel in the given level for pollution sources and adds the
     * resulting amounts to {@code totals} keyed by the sublevel's real-world ChunkPos rather than
     * the reserved plot-chunk coordinates, so pollution appears where the airship actually is.
     */
    public static void countSubLevelSources(ServerLevel level, Map<ChunkPos, Long> totals, Set<Block> autoSources) {
        if (!initialized) init();
        if (mGetContainer == null) return;

        try {
            Object container = mGetContainer.invoke(null, level);
            if (container == null) return;

            List<?> subLevels = (List<?>) mGetAllSubLevels.invoke(container);
            if (subLevels == null || subLevels.isEmpty()) return;

            for (Object subLevel : subLevels) {
                // Get the sublevel's logical position in the parent world
                Object pose = mLogicalPose.invoke(subLevel);
                Vector3dc pos = (Vector3dc) mPosition.invoke(pose);
                ChunkPos worldChunk = new ChunkPos(BlockPos.containing(pos.x(), pos.y(), pos.z()));

                // Scan the plot chunks that back this sublevel
                Object plot = mGetPlot.invoke(subLevel);
                ChunkPos plotMin = (ChunkPos) mGetChunkMin.invoke(plot);
                ChunkPos plotMax = (ChunkPos) mGetChunkMax.invoke(plot);

                for (int cx = plotMin.x; cx <= plotMax.x; cx++) {
                    for (int cz = plotMin.z; cz <= plotMax.z; cz++) {
                        // Read through the plot (with a plot-local pos), not the host
                        // level, which has nothing at these coordinates.
                        ChunkPos localPos = (ChunkPos) mToLocal.invoke(plot, new ChunkPos(cx, cz));
                        LevelChunk chunk = (LevelChunk) mGetChunk.invoke(plot, localPos);
                        if (chunk == null) continue;

                        for (BlockPos bePos : chunk.getBlockEntities().keySet()) {
                            // Same reason: the block state only exists in the plot's chunk.
                            BlockState state = chunk.getBlockState(bePos);

                            int amount = GlobalWarmingHandler.sourcePollution(state, autoSources);
                            if (amount < 0) continue;
                            totals.merge(worldChunk, (long) amount, Long::sum);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[SolarPunk] Sable sublevel pollution scan failed: {}", e.getMessage());
            mGetContainer = null; // disable for remainder of session
        }
    }
}
