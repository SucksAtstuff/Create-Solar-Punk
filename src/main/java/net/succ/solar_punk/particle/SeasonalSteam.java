package net.succ.solar_punk.particle;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.succ.solar_punk.util.SeasonalColors;

/**
 * Geyser steam, coloured for holidays (SeasonalColors.steam) and with sparklers at New Year's.
 * Stands in for ParticleTypes.CLOUD wherever a geyser puffs.
 */
public final class SeasonalSteam {
    private SeasonalSteam() {}

    public static ParticleOptions particle(RandomSource random) {
        int color = SeasonalColors.steam(random);
        return color == -1 ? ParticleTypes.CLOUD : ColorParticleOption.create(ModParticles.TINTED_STEAM.get(), color);
    }

    /** A client-side steam puff, plus now and then a sparkler at New Year's. */
    public static void add(Level level, double x, double y, double z, double vx, double vy, double vz) {
        RandomSource random = level.random;
        level.addParticle(particle(random), x, y, z, vx, vy, vz);
        if (SeasonalColors.sparklers() && random.nextInt(3) == 0)
            level.addParticle(random.nextBoolean() ? ParticleTypes.FIREWORK : ParticleTypes.ELECTRIC_SPARK,
                    x, y, z, vx * 0.5, vy * 0.5, vz * 0.5);
    }

    /** sendParticles for steam. Each particle picks its own colour, so mixes like Pride's rainbow stay mixed. */
    public static void send(ServerLevel level, double x, double y, double z, int count,
                            double dx, double dy, double dz, double speed) {
        RandomSource random = level.random;
        if (SeasonalColors.steam(random) == -1) {
            level.sendParticles(ParticleTypes.CLOUD, x, y, z, count, dx, dy, dz, speed);
        } else {
            for (int i = 0; i < count; i++)
                level.sendParticles(particle(random), x, y, z, 1, dx, dy, dz, speed);
        }
        if (SeasonalColors.sparklers())
            level.sendParticles(ParticleTypes.FIREWORK, x, y, z, Math.max(1, count / 3), dx, dy, dz, speed * 2);
    }
}
