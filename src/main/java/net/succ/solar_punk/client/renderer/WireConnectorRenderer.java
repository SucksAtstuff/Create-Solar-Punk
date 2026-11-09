package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.block.custom.WireConnectorBlock;
import net.succ.solar_punk.block.entity.custom.WireConnectorBlockEntity;

/**
 * Draws the wires hanging from a Wire Connector as sagging round tubes, wrapped in their
 * metal's twisted-strand texture (textures/entity/wire/). Each wire is drawn by only one of its two ends (the one with the
 * lower position), so it isn't drawn twice.
 */
public class WireConnectorRenderer implements BlockEntityRenderer<WireConnectorBlockEntity> {
    private static final int SEGMENTS = 24;
    private static final int SIDES = 8;
    private static final double RADIUS = 0.015;
    // How far the middle of a wire hangs below the straight line, per block of length.
    private static final double SAG_PER_BLOCK = 0.07;

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
            VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(wire.tier().texture()));
            drawWire(level, poseStack, consumer, from.subtract(origin), to.subtract(origin), origin);
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
