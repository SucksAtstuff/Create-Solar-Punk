package net.succ.solar_punk.compat.sable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.succ.solar_punk.pollution.GlobalWarmingHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SableCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("solarpunk/sable");

    private static final boolean LOADED = ModList.get().isLoaded("sable");

    /**
     * Where a block on a sub-level (an Aeronautics contraption, a Cosmonautics station) really
     * is. Its {@link BlockPos} is a plot coordinate far from the sub-level itself;
     * {@code worldCenter} is the centre of that block in the host level, and
     * {@code orientation} rotates the sub-level's own axes into world axes.
     */
    public record Placement(Object subLevel, Vec3 worldCenter, Quaterniondc orientation) {
        public BlockPos worldPos() {
            return BlockPos.containing(worldCenter);
        }

        /** A world-space direction in the sub-level's own frame. */
        public Vec3 toLocal(Vec3 worldDir) {
            Vector3d local = orientation.transformInverse(new Vector3d(worldDir.x, worldDir.y, worldDir.z));
            return new Vec3(local.x, local.y, local.z);
        }
    }

    private static boolean triedPlacementInit = false;
    private static Object helper;                 // Sable.HELPER
    private static Method mGetContaining;         // getContaining(Level, Vec3i) -> SubLevel
    private static Method mSubLevelPose;          // SubLevel.logicalPose()
    private static Method mPoseOrientation;       // Pose3dc.orientation()
    private static Method mPoseTransformPosition; // Pose3dc.transformPosition(Vec3)

    // Shade from other sub-levels. Optional: if these are missing, only shading stops working.
    private static Method mGetAllIntersecting;            // getAllIntersecting(Level, BoundingBox3dc)
    private static Constructor<?> cBoundingBox;           // BoundingBox3d(AABB)
    private static Method mPoseTransformPositionInverse;  // Pose3dc.transformPositionInverse(Vec3)

    /** A world-space ray segment re-expressed in one sub-level's plot coordinates. */
    public record PlotRay(Vec3 start, Vec3 end) {}

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
     * The sub-level holding the block at {@code pos}, or {@code null} for a normal block (or
     * when Sable is missing or its internals changed).
     */
    @Nullable
    public static Placement placement(Level level, BlockPos pos) {
        if (!LOADED || level == null) return null;
        if (!triedPlacementInit) initPlacement();
        if (mGetContaining == null) return null;
        try {
            Object subLevel = mGetContaining.invoke(helper, level, pos);
            if (subLevel == null) return null;
            Object pose = mSubLevelPose.invoke(subLevel);
            Vec3 center = (Vec3) mPoseTransformPosition.invoke(pose, Vec3.atCenterOf(pos));
            // The pose is live and changes as the sub-level moves, so copy the rotation.
            Quaterniondc orientation = new Quaterniond((Quaterniondc) mPoseOrientation.invoke(pose));
            return new Placement(subLevel, center, orientation);
        } catch (Throwable t) {
            LOGGER.warn("[SolarPunk] Sable sub-level lookup failed ({}); solar blocks on contraptions "
                    + "will only see their own contraption.", t.toString());
            mGetContaining = null;
            return null;
        }
    }

    private static synchronized void initPlacement() {
        if (triedPlacementInit) return;
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            helper = sableClass.getField("HELPER").get(null);
            mGetContaining = helper.getClass().getMethod("getContaining", Level.class, Vec3i.class);
            mSubLevelPose = Class.forName("dev.ryanhcode.sable.sublevel.SubLevel").getMethod("logicalPose");
            Class<?> poseClass = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc");
            mPoseOrientation = poseClass.getMethod("orientation");
            mPoseTransformPosition = poseClass.getMethod("transformPosition", Vec3.class);
        } catch (Throwable t) {
            LOGGER.warn("[SolarPunk] Couldn't hook Sable sub-levels ({}); solar blocks on contraptions "
                    + "will only see their own contraption.", t.toString());
            mGetContaining = null;
            triedPlacementInit = true;
            return;
        }
        try {
            Class<?> boxClass = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3dc");
            mGetAllIntersecting = helper.getClass().getMethod("getAllIntersecting", Level.class, boxClass);
            cBoundingBox = Class.forName("dev.ryanhcode.sable.companion.math.BoundingBox3d").getConstructor(AABB.class);
            mPoseTransformPositionInverse = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc")
                    .getMethod("transformPositionInverse", Vec3.class);
        } catch (Throwable t) {
            disableShade(t);
        }
        triedPlacementInit = true;
    }

    /**
     * The segment {@code start -> end} (world space) in the plot coordinates of every
     * sub-level whose bounds it crosses, except {@code exclude}. Callers walk each one
     * through the plot's blocks to see if that sub-level shades them.
     */
    public static List<PlotRay> raysThroughSubLevels(Level level, Vec3 start, Vec3 end, @Nullable Object exclude) {
        if (!LOADED || level == null) return List.of();
        if (!triedPlacementInit) initPlacement();
        if (mGetAllIntersecting == null) return List.of();
        try {
            Object box = cBoundingBox.newInstance(new AABB(start, end));
            List<PlotRay> rays = new ArrayList<>();
            for (Object subLevel : (Iterable<?>) mGetAllIntersecting.invoke(helper, level, box)) {
                if (subLevel == exclude) continue;
                Object pose = mSubLevelPose.invoke(subLevel);
                rays.add(new PlotRay(
                        (Vec3) mPoseTransformPositionInverse.invoke(pose, start),
                        (Vec3) mPoseTransformPositionInverse.invoke(pose, end)));
            }
            return rays;
        } catch (Throwable t) {
            disableShade(t);
            return List.of();
        }
    }

    private static void disableShade(Throwable t) {
        LOGGER.warn("[SolarPunk] Couldn't hook Sable sub-level queries ({}); contraptions won't shade "
                + "solar blocks.", t.toString());
        mGetAllIntersecting = null;
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
