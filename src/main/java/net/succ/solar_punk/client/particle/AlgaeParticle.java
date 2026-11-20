package net.succ.solar_punk.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * The Bioreactor's culture particles (see AlgaeParticles). A bubble rises from where it's
 * spawned and wobbles; a speck of algae drifts slowly and fades. The spawner passes the room
 * above the particle (to the culture's surface) as velocity Y, since the particle can't see
 * the tank's fluid level: neither goes above it, and a bubble pops when it gets there.
 * No collision, so they pass through the tank's inner faces like the fluid render does.
 */
public class AlgaeParticle extends TextureSheetParticle {
    private final boolean bubble;
    private final double surfaceY;
    private final float wobblePhase;

    protected AlgaeParticle(ClientLevel level, double x, double y, double z, double room,
                            SpriteSet sprites, boolean bubble) {
        super(level, x, y, z);
        this.bubble = bubble;
        this.surfaceY = y + Math.max(0, room);
        this.wobblePhase = random.nextFloat() * Mth.TWO_PI;
        this.hasPhysics = false;
        this.gravity = 0;
        pickSprite(sprites);
        if (bubble) {
            // Rise at 0.03-0.05 blocks a tick, living just long enough to reach the surface.
            yd = 0.03 + random.nextDouble() * 0.02;
            lifetime = Math.max(1, (int) (room / yd));
            quadSize = 0.03f + random.nextFloat() * 0.03f;
            rCol = 0.85f; gCol = 1.0f; bCol = 0.9f;
        } else {
            xd = (random.nextDouble() - 0.5) * 0.006;
            yd = (random.nextDouble() - 0.4) * 0.004;
            zd = (random.nextDouble() - 0.5) * 0.006;
            lifetime = 60 + random.nextInt(60);
            quadSize = 0.02f + random.nextFloat() * 0.02f;
            float shade = 0.6f + random.nextFloat() * 0.3f;
            rCol = 0.12f * shade; gCol = 0.45f * shade; bCol = 0.2f * shade;
        }
    }

    @Override
    public void tick() {
        xo = x; yo = y; zo = z;
        if (age++ >= lifetime || y >= surfaceY) {
            remove();
            return;
        }
        if (bubble) {
            // A small sideways wobble on the way up.
            float t = age * 0.3f + wobblePhase;
            xd = Mth.sin(t) * 0.004;
            zd = Mth.cos(t) * 0.004;
        } else {
            // Fade in and out instead of popping.
            float life = (float) age / lifetime;
            alpha = Math.min(1, Math.min(life, 1 - life) * 5);
        }
        move(xd, Math.min(yd, surfaceY - y), zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites, boolean bubble) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new AlgaeParticle(level, x, y, z, vy, sprites, bubble);
        }
    }
}
