package net.succ.solar_punk.compat.cosmonautics;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.compat.sable.SableCompat;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Soft compat for Create: Cosmonautics - no Gradle dependency, same reflection-only approach
 * as {@link net.succ.solar_punk.compat.sable.SableCompat}. Works out where the real sun is
 * for a block in Cosmonautics' deep space dimension, so solar blocks there behave like they
 * do in the Overworld: the sun rises and sets as a station orbits or turns, and a planet
 * passing in front of the star is night.
 *
 * <p>Built against Cosmonautics' internal classes (it has no public API for this yet), so
 * anything going wrong just disables it and {@link #sunAt} returns {@code null}, which makes
 * SolarHelper fall back to the plain {@code solarpunk:always_sunny} tag behaviour.
 *
 * <p>Server side only: the orbit data lives in the server's saved data. On the client
 * {@link #sunAt} always returns {@code null}.
 */
public final class CosmonauticsSolar {

    private CosmonauticsSolar() {}

    private static final Logger LOGGER = LoggerFactory.getLogger("solarpunk/cosmonautics");

    private static final boolean LOADED = ModList.get().isLoaded("rocketnautics");

    private static final ResourceLocation DEEP_SPACE = ResourceLocation.fromNamespaceAndPath("rocketnautics", "deep_space");

    // The orbit only moves the sun a tiny amount per tick, and working it out runs Orekit
    // frame transforms for every planet - once a second per deep space region is plenty.
    private static final int CACHE_TICKS = 20;

    /**
     * The sun as seen from one block. {@code direction} is a unit vector in the block's own
     * frame (so {@code y} is height above that block's "up", even on a rotated station);
     * {@code visible} is false while a planet is in the way.
     */
    public record Sun(Vec3 direction, boolean visible) {}

    private record WorldSun(Vec3 direction, boolean visible, long stamp) {}

    private record Body(Vec3 offset, double radius) {}

    private static final Map<Long, WorldSun> CACHE = new HashMap<>();

    private static boolean triedInit = false;
    private static boolean disabled = false;

    // dev.devce.rocketnautics.content.orbit.DeepSpaceData
    private static Method mTooSoon, mGetData, mGetInstanceForPos, mGetUniverse;
    // ...DeepSpaceInstance
    private static Method mInstanceId, mInstancePosition, mInstanceCorrupted;
    // ...universe.DeepSpacePosition
    private static Method mCurrentPosition, mFrame, mLocalTime;
    // ...universe.UniverseDefinition / CubePlanet / PlanetExtras
    private static Method mGetPlanets, mRadius, mExtras, mStar, mPosInMyFrame;
    // org.hipparchus Vector3D
    private static Method mVecX, mVecY, mVecZ;

    /** True if this is Cosmonautics' deep space, regardless of side. */
    public static boolean isDeepSpace(Level level) {
        return LOADED && level != null && level.dimension().location().equals(DEEP_SPACE);
    }

    /**
     * Where the sun is for the block at {@code pos}, or {@code null} if that can't be worked
     * out (not deep space, client side, Cosmonautics changed, block outside any region...).
     */
    @Nullable
    public static Sun sunAt(Level level, BlockPos pos) {
        if (!isDeepSpace(level) || level.isClientSide || disabled || !Config.spaceRealSun) return null;
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        if (!triedInit) init();
        if (disabled) return null;

        try {
            if ((boolean) mTooSoon.invoke(null, server)) return null;

            // Blocks on a Sable station sit at plot coordinates, nowhere near where the
            // station actually is - use its real position for the region lookup and rotation.
            SableCompat.Placement placement = SableCompat.placement(level, pos);
            BlockPos regionPos = placement != null ? placement.worldPos() : pos;

            Object data = mGetData.invoke(null, server);
            Object instance = mGetInstanceForPos.invoke(data, regionPos.getX(), regionPos.getZ());
            if (instance == null || (boolean) mInstanceCorrupted.invoke(instance)) return null;

            WorldSun worldSun = worldSun(level, data, instance);
            if (worldSun == null) return null;

            Vec3 dir = worldSun.direction();
            // The sun relative to the station's own blocks.
            if (placement != null) dir = placement.toLocal(dir).normalize();
            return new Sun(dir, worldSun.visible());
        } catch (Throwable t) {
            disable("sun lookup failed", t);
            return null;
        }
    }

    @Nullable
    private static WorldSun worldSun(Level level, Object data, Object instance) throws Exception {
        long now = level.getGameTime();
        long id = (long) mInstanceId.invoke(instance);
        WorldSun cached = CACHE.get(id);
        // now < stamp: a different world was loaded since (singleplayer), so it's stale.
        if (cached != null && now >= cached.stamp() && now - cached.stamp() < CACHE_TICKS) return cached;

        Object position = mInstancePosition.invoke(instance);
        Object shipPos = mCurrentPosition.invoke(position);
        Object frame = mFrame.invoke(position);
        Object date = mLocalTime.invoke(position);

        // Each planet's position relative to the ship. Planet frames only translate (their
        // spin is stored separately), so these line up with the world axes - the same
        // assumption Cosmonautics' own sun lighting and solar panel make.
        Vec3 star = null;
        Collection<?> planets = (Collection<?>) mGetPlanets.invoke(mGetUniverse.invoke(data));
        List<Body> bodies = new ArrayList<>();
        for (Object planet : planets) {
            Vec3 toPlanet = vec(mPosInMyFrame.invoke(planet, date, shipPos, frame)).scale(-1);
            Object extras = mExtras.invoke(planet);
            if (extras != null && (boolean) mStar.invoke(extras)) {
                if (star == null) star = toPlanet;
            } else {
                bodies.add(new Body(toPlanet, (double) mRadius.invoke(planet)));
            }
        }
        if (star == null || star.lengthSqr() < 1e-6) return null;

        Vec3 dir = star.normalize();
        double starDistance = star.length();
        boolean visible = true;
        for (Body body : bodies) {
            // Ray from the ship toward the star vs. the planet's sphere.
            Vec3 toBody = body.offset();
            double along = toBody.dot(dir);
            if (along <= 0 || along >= starDistance) continue;
            double offAxisSqr = toBody.lengthSqr() - along * along;
            double radius = body.radius();
            if (offAxisSqr < radius * radius) {
                visible = false;
                break;
            }
        }

        WorldSun result = new WorldSun(dir, visible, now);
        CACHE.put(id, result);
        return result;
    }

    private static Vec3 vec(Object vector3D) throws Exception {
        return new Vec3((double) mVecX.invoke(vector3D), (double) mVecY.invoke(vector3D), (double) mVecZ.invoke(vector3D));
    }

    private static synchronized void init() {
        if (triedInit) return;
        triedInit = true;
        try {
            String orbit = "dev.devce.rocketnautics.content.orbit.";
            Class<?> dataClass = Class.forName(orbit + "DeepSpaceData");
            mTooSoon           = dataClass.getMethod("tooSoon", MinecraftServer.class);
            mGetData           = dataClass.getMethod("getInstance", MinecraftServer.class);
            mGetInstanceForPos = dataClass.getMethod("getInstanceForPos", int.class, int.class);
            mGetUniverse       = dataClass.getMethod("getUniverse");

            Class<?> instanceClass = Class.forName(orbit + "DeepSpaceInstance");
            mInstanceId        = instanceClass.getMethod("getId");
            mInstancePosition  = instanceClass.getMethod("getPosition");
            mInstanceCorrupted = instanceClass.getMethod("isCorrupted");

            Class<?> positionClass = Class.forName(orbit + "universe.DeepSpacePosition");
            mCurrentPosition = positionClass.getMethod("getCurrentPosition");
            mFrame           = positionClass.getMethod("getFrame");
            mLocalTime       = positionClass.getMethod("getLocalUniverseTime");

            Class<?> dateClass   = shadedClass("org.orekit.time.AbsoluteDate");
            Class<?> frameClass  = shadedClass("org.orekit.frames.Frame");
            Class<?> vectorClass = shadedClass("org.hipparchus.geometry.euclidean.threed.Vector3D");
            mVecX = vectorClass.getMethod("getX");
            mVecY = vectorClass.getMethod("getY");
            mVecZ = vectorClass.getMethod("getZ");

            mGetPlanets = Class.forName(orbit + "universe.UniverseDefinition").getMethod("getPlanets");
            Class<?> planetClass = Class.forName(orbit + "universe.CubePlanet");
            mRadius       = planetClass.getMethod("radius");
            mExtras       = planetClass.getMethod("extras");
            mPosInMyFrame = planetClass.getMethod("posInMyFrame", dateClass, vectorClass, frameClass);
            mStar = Class.forName(orbit + "universe.PlanetExtras").getMethod("star");
        } catch (Throwable t) {
            disable("couldn't find its orbit classes", t);
        }
    }

    /**
     * Cosmonautics' release jar bundles Orekit and Hipparchus relocated under its own
     * package; a dev environment may have them at their original names instead.
     */
    private static Class<?> shadedClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName("dev.devce.rocketnautics.shaded." + name.substring("org.".length()));
        } catch (ClassNotFoundException e) {
            return Class.forName(name);
        }
    }

    private static void disable(String reason, Throwable t) {
        disabled = true;
        LOGGER.warn("[SolarPunk] Create: Cosmonautics solar compat {} ({}); solar blocks in deep space "
                + "fall back to constant sunlight. A Cosmonautics update probably changed its internals.",
                reason, t.toString());
    }
}
