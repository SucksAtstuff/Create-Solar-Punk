package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;
import org.joml.Matrix4f;

// The reactor's "cool visual" - see the Visuals section of plan_for_fusion.md. Entirely
// code-only, no textures: a low-poly glowing sphere at the exact center, with three
// armillary-sphere rings orbiting around it, all drawn with RenderType.lightning() (the
// same untextured, additively-blended, vertex-colored render type vanilla uses for
// lightning bolts). Only draws once FusionReactorCoreBlockEntity reports the
// shell/blanket scan formed. Color and speed both track blanketEfficiency: calmer
// blue-white and a slower spin/pulse at Lithium-heavy composition, hotter orange-white
// and faster at Beryllium-heavy composition.
//
// Every face (sphere and rings alike) is emitted twice, once with its normal winding and
// once reversed, via the shared quad() helper - this geometry is static in world space,
// not billboarded, so with only one winding it's only visible from whichever side that
// winding happens to face once backface culling kicks in, which is exactly the "only
// shows up from some angles" bug. Emitting both windings makes every face visible from
// either side regardless of the render type's cull state, at the cost of double the
// vertices - trivial for a single small effect like this.
public class FusionReactorCoreRenderer implements BlockEntityRenderer<FusionReactorCoreBlockEntity> {

    private static final int RING_SEGMENTS = 48;
    private static final float RING_THICKNESS = 0.035f;

    private static final int SPHERE_LAT_SEGMENTS = 10;
    private static final int SPHERE_LON_SEGMENTS = 16;
    private static final float SPHERE_BASE_RADIUS = 0.45f;
    private static final float SPHERE_PULSE_AMOUNT = 0.05f;

    public FusionReactorCoreRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(FusionReactorCoreBlockEntity be, float partialTick, PoseStack poseStack,
                        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.formed || be.getLevel() == null) return;

        float t = be.blanketEfficiency;
        float r = 0.5f + 0.5f * t;
        float g = 0.65f - 0.15f * t;
        float b = 1.0f - 0.55f * t;

        float time = be.getLevel().getGameTime() + partialTick;
        float speed = 2f + 4f * t;

        VertexConsumer vb = bufferSource.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float sphereRadius = SPHERE_BASE_RADIUS + SPHERE_PULSE_AMOUNT * (float) Math.sin(time * 0.1 * speed);
        renderSphere(poseStack, vb, sphereRadius, r, g, b, 0.35f);

        renderRing(poseStack, vb, 0.9f, Axis.XP, 0f, time * speed, r, g, b, 0.85f);
        renderRing(poseStack, vb, 1.3f, Axis.XP, 60f, -time * speed * 0.7f, r, g, b, 0.85f);
        renderRing(poseStack, vb, 1.7f, Axis.ZP, 60f, time * speed * 0.5f, r, g, b, 0.85f);

        poseStack.popPose();
    }

    // Low-poly UV sphere centered on the core, built from lat/long quads. Additively
    // blended overlapping faces (near + far side of the sphere from any given screen
    // pixel) stack into a brighter core, giving the "glowing orb" read without needing a
    // texture or a bloom pass.
    private static void renderSphere(PoseStack poseStack, VertexConsumer vb, float radius,
                                      float r, float g, float b, float alpha) {
        Matrix4f mat = poseStack.last().pose();
        for (int lat = 0; lat < SPHERE_LAT_SEGMENTS; lat++) {
            float theta0 = (float) Math.PI * lat / SPHERE_LAT_SEGMENTS;
            float theta1 = (float) Math.PI * (lat + 1) / SPHERE_LAT_SEGMENTS;
            for (int lon = 0; lon < SPHERE_LON_SEGMENTS; lon++) {
                float phi0 = (float) (2 * Math.PI * lon / SPHERE_LON_SEGMENTS);
                float phi1 = (float) (2 * Math.PI * (lon + 1) / SPHERE_LON_SEGMENTS);

                quad(vb, mat,
                        spherePoint(theta0, phi0, radius), spherePoint(theta0, phi1, radius),
                        spherePoint(theta1, phi1, radius), spherePoint(theta1, phi0, radius),
                        r, g, b, alpha);
            }
        }
    }

    private static float[] spherePoint(float theta, float phi, float radius) {
        float sinTheta = (float) Math.sin(theta);
        return new float[] {
                sinTheta * (float) Math.cos(phi) * radius,
                (float) Math.cos(theta) * radius,
                sinTheta * (float) Math.sin(phi) * radius
        };
    }

    // Draws one ring as a closed band of quads standing on-edge around a circle of the
    // given radius, lying in the local XZ plane before the tilt/spin transform is
    // applied - i.e. a short cylinder wall, not a billboarded sprite, so it reads as a
    // ring from any camera angle (bar looking straight down its own edge).
    private static void renderRing(PoseStack poseStack, VertexConsumer vb, float radius, Axis tiltAxis,
                                    float tiltDegrees, float spinDegrees, float r, float g, float b, float alpha) {
        poseStack.pushPose();
        if (tiltDegrees != 0f) poseStack.mulPose(tiltAxis.rotationDegrees(tiltDegrees));
        poseStack.mulPose(Axis.YP.rotationDegrees(spinDegrees));

        Matrix4f mat = poseStack.last().pose();
        for (int i = 0; i < RING_SEGMENTS; i++) {
            double a0 = 2 * Math.PI * i / RING_SEGMENTS;
            double a1 = 2 * Math.PI * (i + 1) / RING_SEGMENTS;
            float x0 = (float) (Math.cos(a0) * radius);
            float z0 = (float) (Math.sin(a0) * radius);
            float x1 = (float) (Math.cos(a1) * radius);
            float z1 = (float) (Math.sin(a1) * radius);

            quad(vb, mat,
                    new float[]{x0, -RING_THICKNESS, z0}, new float[]{x0, RING_THICKNESS, z0},
                    new float[]{x1, RING_THICKNESS, z1}, new float[]{x1, -RING_THICKNESS, z1},
                    r, g, b, alpha);
        }
        poseStack.popPose();
    }

    // Emits a quad twice - once as given, once with reversed winding - so it renders
    // from both sides no matter which way backface culling ends up facing. See the
    // class doc for why this matters here.
    private static void quad(VertexConsumer vb, Matrix4f mat, float[] v0, float[] v1, float[] v2, float[] v3,
                              float r, float g, float b, float alpha) {
        vb.addVertex(mat, v0[0], v0[1], v0[2]).setColor(r, g, b, alpha);
        vb.addVertex(mat, v1[0], v1[1], v1[2]).setColor(r, g, b, alpha);
        vb.addVertex(mat, v2[0], v2[1], v2[2]).setColor(r, g, b, alpha);
        vb.addVertex(mat, v3[0], v3[1], v3[2]).setColor(r, g, b, alpha);

        vb.addVertex(mat, v0[0], v0[1], v0[2]).setColor(r, g, b, alpha);
        vb.addVertex(mat, v3[0], v3[1], v3[2]).setColor(r, g, b, alpha);
        vb.addVertex(mat, v2[0], v2[1], v2[2]).setColor(r, g, b, alpha);
        vb.addVertex(mat, v1[0], v1[1], v1[2]).setColor(r, g, b, alpha);
    }
}
