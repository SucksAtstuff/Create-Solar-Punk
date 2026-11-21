package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.util.SeasonalEvent;
import net.succ.offbyonelib.wire.WireNodeBlockEntity;
import net.succ.offbyonelib.wire.client.WireRenderer;

/** Solarpunk's wires: the library's renderer, except at Christmas every wire is green cable hung with glowing coloured bulbs. */
public class SeasonalWireRenderer<T extends WireNodeBlockEntity> extends WireRenderer<T> {

    private static final ResourceLocation CHRISTMAS_CABLE =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/entity/wire/events/christmas/cable.png");
    private static final ResourceLocation CHRISTMAS_BULB =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/entity/wire/events/christmas/bulb.png");
    private static final double BULB_SPACING = 0.5;
    private static final double BULB_WIDTH = 0.045;
    private static final double BULB_HEIGHT = 0.07;
    // Red, green, blue, yellow, orange.
    private static final int[] BULB_COLORS = {0xE53935, 0x43A047, 0x1E88E5, 0xFDD835, 0xFB8C00};

    // Renderers are rebuilt on resource reload, the same moment seasonal models change.
    private final boolean christmas = SeasonalEvent.CHRISTMAS.isActive();

    public SeasonalWireRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected ResourceLocation wireTexture(WireNodeBlockEntity.Connection wire) {
        return christmas ? CHRISTMAS_CABLE : super.wireTexture(wire);
    }

    @Override
    protected void decorate(PoseStack poseStack, MultiBufferSource buffers, Vec3 from, Vec3 to) {
        if (christmas)
            drawBulbs(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(CHRISTMAS_BULB)), from, to);
    }

    // Bulbs hang evenly along the wire, full-bright, cycling through the colours.
    private static void drawBulbs(PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to) {
        Vec3 span = to.subtract(from);
        double length = span.length();
        int count = (int) (length / BULB_SPACING);
        if (count < 1) return;
        double sag = length * SAG_PER_BLOCK;
        PoseStack.Pose pose = poseStack.last();
        for (int n = 0; n < count; n++) {
            float t = (float) ((n + 0.5) / count);
            Vec3 top = point(from, span, sag, t).subtract(0, RADIUS, 0);
            drawBox(consumer, pose, top.add(-BULB_WIDTH / 2, -BULB_HEIGHT, -BULB_WIDTH / 2),
                    top.add(BULB_WIDTH / 2, 0, BULB_WIDTH / 2), BULB_COLORS[n % BULB_COLORS.length]);
        }
    }

    private static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, Vec3 min, Vec3 max, int color) {
        double x0 = min.x, y0 = min.y, z0 = min.z, x1 = max.x, y1 = max.y, z1 = max.z;
        face(consumer, pose, color, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        face(consumer, pose, color, 0, 1, 0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        face(consumer, pose, color, 0, 0, -1, x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0);
        face(consumer, pose, color, 0, 0, 1, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        face(consumer, pose, color, -1, 0, 0, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        face(consumer, pose, color, 1, 0, 0, x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0);
    }

    private static void face(VertexConsumer consumer, PoseStack.Pose pose, int color, float nx, float ny, float nz,
                             double ax, double ay, double az, double bx, double by, double bz,
                             double cx, double cy, double cz, double dx, double dy, double dz) {
        bulbVertex(consumer, pose, color, ax, ay, az, 0, 0, nx, ny, nz);
        bulbVertex(consumer, pose, color, bx, by, bz, 1, 0, nx, ny, nz);
        bulbVertex(consumer, pose, color, cx, cy, cz, 1, 1, nx, ny, nz);
        bulbVertex(consumer, pose, color, dx, dy, dz, 0, 1, nx, ny, nz);
    }

    private static void bulbVertex(VertexConsumer consumer, PoseStack.Pose pose, int color,
                                   double x, double y, double z, float u, float v, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }
}
