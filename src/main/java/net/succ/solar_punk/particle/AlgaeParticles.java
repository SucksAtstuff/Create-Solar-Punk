package net.succ.solar_punk.particle;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Client-side particles for a running Bioreactor, called from the block's animateTick: bubbles
 * rising from the aerator at the bottom and specks of algae drifting through the culture.
 * Everything stays inside the tank and below the culture's surface.
 */
public final class AlgaeParticles {

    /**
     * @param minX   the tank's west edge (block coordinates)
     * @param minZ   the tank's north edge
     * @param bottom the bottom of the culture (the top of the Bioreactor Base)
     * @param width  the tank's width in blocks (it's square)
     * @param depth  how deep the culture is, in blocks
     */
    public static void spawn(Level level, double minX, double minZ, double bottom, int width, double depth) {
        if (depth <= 0.1) return;
        RandomSource random = level.random;
        double inset = 0.1;
        double span = width - 2 * inset;
        // Scale with the tank, so a wide one isn't sparse: a few per block of footprint.
        int bubbles = width * width;
        for (int i = 0; i < bubbles; i++) {
            if (random.nextInt(3) != 0) continue;
            double y = bottom + random.nextDouble() * 0.2;
            level.addParticle(ModParticles.ALGAE_BUBBLE.get(),
                    minX + inset + random.nextDouble() * span, y, minZ + inset + random.nextDouble() * span,
                    0, bottom + depth - y, 0);
        }
        int specks = Math.max(1, (int) (width * width * depth / 4));
        for (int i = 0; i < specks; i++) {
            if (random.nextInt(4) != 0) continue;
            double y = bottom + random.nextDouble() * depth;
            level.addParticle(ModParticles.ALGAE_SPECK.get(),
                    minX + inset + random.nextDouble() * span, y, minZ + inset + random.nextDouble() * span,
                    0, bottom + depth - y, 0);
        }
    }

    private AlgaeParticles() {}
}
