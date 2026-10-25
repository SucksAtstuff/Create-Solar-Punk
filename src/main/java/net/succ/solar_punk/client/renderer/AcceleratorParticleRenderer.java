package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.block.entity.custom.ParticleAcceleratorInputBlockEntity;
import org.joml.Matrix4f;

// Draws the Particle Accelerator Input's filter slot (via SmartBlockEntityRenderer) and the
// two beams that run around the ring in opposite directions while it crafts: glowing cores
// with soft halos and fading trails, following the tube's centre line through the glass
// pipes. They meet in the Input as the craft finishes, and the collision flashes there.
public class AcceleratorParticleRenderer extends SmartBlockEntityRenderer<ParticleAcceleratorInputBlockEntity> {

    // One beam in Tritium-palette cyan, the other in amber so they're easy to tell apart
    private static final float[] CYAN_CORE = rgb(0xB8F6FF), CYAN_GLOW = rgb(0x5FE3FF);
    private static final float[] AMBER_CORE = rgb(0xFFE9C4), AMBER_GLOW = rgb(0xFFB45F);
    private static final float[] WHITE = rgb(0xFFFFFF);
    private static final int TRAIL_LENGTH = 8;

    public AcceleratorParticleRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ParticleAcceleratorInputBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if (be.getParticlePath().isEmpty() || be.getParticleLength() <= 0) return;

        // Additive, unlit: glows in the dark and shows through the glass
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = ms.last().pose();
        Vec3 origin = Vec3.atLowerCornerOf(be.getBlockPos());
        double collision = ParticleAcceleratorInputBlockEntity.COLLISION_POINT;

        if (be.isRunning()) {
            double distance = be.getBeamDistance(partialTicks);
            // the trail stretches as the beams speed up
            double spacing = 0.08 + 0.3 * be.getBeamProgress(partialTicks);
            beam(consumer, pose, be, origin, collision + distance, -spacing, CYAN_CORE, CYAN_GLOW);
            beam(consumer, pose, be, origin, collision - distance, spacing, AMBER_CORE, AMBER_GLOW);
        }

        float flash = be.getFlash(partialTicks);
        if (flash > 0) {
            Vec3 p = be.pointAlongPath(collision).subtract(origin);
            float grow = 1 - flash;
            cube(consumer, pose, p, 0.2f + 0.45f * grow, CYAN_GLOW, 0.5f * flash);
            cube(consumer, pose, p, 0.12f + 0.3f * grow, AMBER_GLOW, 0.6f * flash);
            cube(consumer, pose, p, 0.08f + 0.12f * grow, WHITE, flash);
        }
    }

    /** One beam: a core with a halo at {@code head}, and a trail fading out behind it. */
    private static void beam(VertexConsumer consumer, Matrix4f pose, ParticleAcceleratorInputBlockEntity be,
                             Vec3 origin, double head, double trailStep, float[] core, float[] glow) {
        for (int i = TRAIL_LENGTH; i >= 1; i--) {
            float fade = 1 - i / (float) (TRAIL_LENGTH + 1);
            Vec3 p = be.pointAlongPath(head + i * trailStep).subtract(origin);
            cube(consumer, pose, p, 0.045f * fade + 0.02f, glow, 0.45f * fade);
        }
        Vec3 p = be.pointAlongPath(head).subtract(origin);
        cube(consumer, pose, p, 0.11f, glow, 0.35f);  // halo
        cube(consumer, pose, p, 0.06f, core, 0.95f);  // core
    }

    private static float[] rgb(int color) {
        return new float[]{(color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f};
    }

    private static void cube(VertexConsumer c, Matrix4f pose, Vec3 centre, float half, float[] rgb, float alpha) {
        float x0 = (float) centre.x - half, x1 = (float) centre.x + half;
        float y0 = (float) centre.y - half, y1 = (float) centre.y + half;
        float z0 = (float) centre.z - half, z1 = (float) centre.z + half;
        float r = rgb[0], g = rgb[1], b = rgb[2];
        // -Y, +Y, -Z, +Z, -X, +X
        quad(c, pose, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, alpha);
        quad(c, pose, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, alpha);
        quad(c, pose, x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0, r, g, b, alpha);
        quad(c, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, alpha);
        quad(c, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, alpha);
        quad(c, pose, x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0, r, g, b, alpha);
    }

    private static void quad(VertexConsumer c, Matrix4f pose,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float r, float g, float b, float a) {
        c.addVertex(pose, ax, ay, az).setColor(r, g, b, a);
        c.addVertex(pose, bx, by, bz).setColor(r, g, b, a);
        c.addVertex(pose, cx, cy, cz).setColor(r, g, b, a);
        c.addVertex(pose, dx, dy, dz).setColor(r, g, b, a);
    }

    // The particle runs around the whole ring, far from the Input itself
    @Override
    public boolean shouldRenderOffScreen(ParticleAcceleratorInputBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(ParticleAcceleratorInputBlockEntity be) {
        return be.getParticlePath().isEmpty() ? super.getRenderBoundingBox(be) : AABB.INFINITE;
    }
}
