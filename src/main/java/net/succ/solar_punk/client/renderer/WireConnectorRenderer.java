package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.WireConnectorBlock;
import net.succ.solar_punk.block.entity.custom.WireConnectorBlockEntity;
import net.succ.solar_punk.util.SeasonalEvent;

/**
 * Draws the wires hanging from a Wire Connector as sagging round tubes, wrapped in their
 * metal's twisted-strand texture (textures/entity/wire/). Each wire is drawn by only one of its two ends (the one with the
 * lower position), so it isn't drawn twice. At Christmas every wire is green cable hung with
 * glowing coloured bulbs.
 */
public class WireConnectorRenderer implements BlockEntityRenderer<WireConnectorBlockEntity> {
    private static final int SEGMENTS = 24;
    private static final int SIDES = 8;
    private static final double RADIUS = 0.015;
    // How far the middle of a wire hangs below the straight line, per block of length.
    private static final double SAG_PER_BLOCK = 0.07;

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

    public WireConnectorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(WireConnectorBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) return;
        BlockPos pos = be.getBlockPos();
        Vec3 origin = Vec3.atLowerCornerOf(pos);
        Vec3 from = WireConnectorBlock.attachPoint(pos, facing(be.getBlockState()));

        for (WireConnectorBlockEntity.Connection wire : be.connections()) {
            if (wire.other().compareTo(pos) < 0) continue;
            Vec3 to = WireConnectorBlock.attachPoint(wire.other(), facing(level.getBlockState(wire.other())));
            ResourceLocation texture = christmas ? CHRISTMAS_CABLE : wire.tier().texture();
            VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
            drawWire(level, poseStack, consumer, from.subtract(origin), to.subtract(origin), origin);
            if (christmas)
                drawBulbs(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(CHRISTMAS_BULB)),
                        from.subtract(origin), to.subtract(origin));
        }
    }

    private static Direction facing(BlockState state) {
        return state.hasProperty(WireConnectorBlock.FACING) ? state.getValue(WireConnectorBlock.FACING) : Direction.UP;
    }

    private static void drawWire(Level level, PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to, Vec3 origin) {
        Vec3 span = to.subtract(from);
        double sag = span.length() * SAG_PER_BLOCK;
        PoseStack.Pose pose = poseStack.last();

        // A ring of vertices at each point along the wire, turned to follow the curve, joined
        // into a round tube. The texture wraps once around it and repeats once per block of
        // length, so the strands keep the same size on every wire.
        Vec3[] ring = new Vec3[SIDES + 1], normals = new Vec3[SIDES + 1];
        Vec3[] prevRing = null, prevNormals = null;
        Vec3 prevPoint = null;
        float prevU = 0;
        int prevLight = 0;
        for (int i = 0; i <= SEGMENTS; i++) {
            float t = (float) i / SEGMENTS;
            Vec3 point = point(from, span, sag, t);
            Vec3 tangent = span.add(0, -sag * 4 * (1 - 2 * t), 0).normalize();
            Vec3 side = tangent.cross(new Vec3(0, 1, 0));
            if (side.lengthSqr() < 1.0E-6) side = new Vec3(1, 0, 0);
            side = side.normalize();
            Vec3 up = side.cross(tangent).normalize();
            for (int k = 0; k <= SIDES; k++) {
                double angle = Math.PI * 2 * k / SIDES;
                normals[k] = side.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
                ring[k] = point.add(normals[k].scale(RADIUS));
            }
            float u = prevPoint == null ? 0 : prevU + (float) point.distanceTo(prevPoint);
            int light = light(level, point.add(origin));
            if (prevRing != null) {
                for (int k = 0; k < SIDES; k++) {
                    float v0 = (float) k / SIDES, v1 = (float) (k + 1) / SIDES;
                    vertex(consumer, pose, prevRing[k], prevU, v0, prevLight, prevNormals[k]);
                    vertex(consumer, pose, prevRing[k + 1], prevU, v1, prevLight, prevNormals[k + 1]);
                    vertex(consumer, pose, ring[k + 1], u, v1, light, normals[k + 1]);
                    vertex(consumer, pose, ring[k], u, v0, light, normals[k]);
                }
            }
            prevRing = ring.clone();
            prevNormals = normals.clone();
            prevPoint = point;
            prevU = u;
            prevLight = light;
        }
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

    // A parabola close enough to a hanging cable at these lengths.
    private static Vec3 point(Vec3 from, Vec3 span, double sag, float t) {
        return from.add(span.scale(t)).add(0, -sag * 4 * t * (1 - t), 0);
    }

    private static int light(Level level, Vec3 point) {
        return LevelRenderer.getLightColor(level, BlockPos.containing(point));
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 p, float u, float v, int light, Vec3 n) {
        consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) n.x, (float) n.y, (float) n.z);
    }

    // Wires reach well outside the block, so keep drawing while any part could be on screen.
    @Override
    public AABB getRenderBoundingBox(WireConnectorBlockEntity be) {
        AABB box = new AABB(be.getBlockPos());
        for (WireConnectorBlockEntity.Connection wire : be.connections())
            box = box.minmax(new AABB(wire.other()));
        return box.inflate(0, 2, 0);
    }

    @Override
    public boolean shouldRenderOffScreen(WireConnectorBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
