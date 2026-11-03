package net.succ.solar_punk.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The colour of a flower's petals, read from its own texture, so the grey flower on a
 * flowering Trellis matches whatever flower was used (and follows resource packs). Stem and
 * leaf pixels (saturated greens) are skipped and the rest averaged.
 */
public final class FlowerTint {
    private FlowerTint() {}

    private static final int FALLBACK = 0xFFFFFF;

    // Keyed by sprite, so a resource reload (which makes new sprites) recomputes. Block colours
    // are asked for from chunk-building threads, hence the synchronised map.
    private static final Map<TextureAtlasSprite, Integer> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    public static int of(Block flower) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getParticleIcon(flower.defaultBlockState());
        return CACHE.computeIfAbsent(sprite, s -> petalColour(s.contents()));
    }

    private static int petalColour(SpriteContents contents) {
        NativeImage image = contents.getOriginalImage();
        long r = 0, g = 0, b = 0, count = 0;
        long allR = 0, allG = 0, allB = 0, allCount = 0;
        // Only the first frame: the image holds every animation frame stacked vertically.
        for (int y = 0; y < contents.height(); y++) {
            for (int x = 0; x < contents.width(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                if (FastColor.ABGR32.alpha(abgr) < 128) continue;
                int red = FastColor.ABGR32.red(abgr), green = FastColor.ABGR32.green(abgr), blue = FastColor.ABGR32.blue(abgr);
                allR += red; allG += green; allB += blue; allCount++;
                if (isLeafGreen(red, green, blue)) continue;
                r += red; g += green; b += blue; count++;
            }
        }
        if (count == 0) {
            if (allCount == 0) return FALLBACK;
            r = allR; g = allG; b = allB; count = allCount;
        }
        return FastColor.ARGB32.color(255, (int) (r / count), (int) (g / count), (int) (b / count));
    }

    // A saturated colour with a hue between yellow-green and cyan (70-170 degrees).
    private static boolean isLeafGreen(int red, int green, int blue) {
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        if (max == 0 || max != green || (max - min) < 0.2f * max) return false;
        // With green the largest channel, hue = 120 + 60 * (blue - red) / (max - min).
        float hue = 120 + 60f * (blue - red) / (max - min);
        return hue > 70 && hue < 170;
    }
}
