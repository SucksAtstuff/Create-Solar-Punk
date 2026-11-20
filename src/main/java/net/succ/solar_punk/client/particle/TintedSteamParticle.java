package net.succ.solar_punk.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.PlayerCloudParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;

/** Vanilla's Cloud particle, tinted. Keeps Cloud's slight random shading. */
public class TintedSteamParticle extends PlayerCloudParticle {
    protected TintedSteamParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz, SpriteSet sprites, ColorParticleOption color) {
        super(level, x, y, z, vx, vy, vz, sprites);
        rCol *= color.getRed();
        gCol *= color.getGreen();
        bCol *= color.getBlue();
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<ColorParticleOption> {
        @Override
        public Particle createParticle(ColorParticleOption type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new TintedSteamParticle(level, x, y, z, vx, vy, vz, sprites, type);
        }
    }
}
