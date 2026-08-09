package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.entity.custom.SolarMirrorBlockEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws the rotating post and mirror plate separately from the static baked-model base,
 * continuously reorienting them every frame like a real dual-axis heliostat: the post
 * yaws (azimuth) to face the target direction, and the plate additionally tilts
 * (elevation) on top of that to bisect the angle between the sun and the mirror's
 * linked Solar Power Tower.
 */
public class SolarMirrorRenderer extends SafeBlockEntityRenderer<SolarMirrorBlockEntity> {

    public static final PartialModel POST = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/solar_mirror_post"));
    public static final PartialModel PLATE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/solar_mirror_plate"));

    private static final Vector3f UP = new Vector3f(0, 1, 0);

    // The post pivots around the block's own vertical center axis (x=8, z=8 in the
    // 16-unit model space). The plate's extra elevation tilt pivots around the hinge
    // where it meets the top of the post (x=8, y=20, z=8), i.e. (0.5, 1.25, 0.5) in
    // block-local coordinates - matches the "post"/"mirror" element geometry in
    // solar_mirror_post.json / solar_mirror_plate.json.
    private static final double PIVOT_X = 0.5;
    private static final double PIVOT_Z = 0.5;
    private static final double HINGE_Y = 20.0 / 16.0;

    public SolarMirrorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(SolarMirrorBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer, int light, int overlay) {
        Level level = be.getLevel();
        if (level == null) return;

        Vector3f normal = computeMirrorNormal(be, level, partialTicks);

        // Decompose the target normal into a yaw (azimuth) that points the post/plate
        // assembly at the right compass direction, and a tilt (elevation) applied only
        // to the plate on top of that yaw. Deriving elevation from the normal's own
        // representation *inside* the already-yawed local frame (rather than a
        // hand-picked spherical formula) guarantees azimuthRotation * elevationRotation
        // maps local +Y exactly onto `normal`, regardless of axis-convention mistakes.
        float azimuth = (float) Math.atan2(normal.x(), normal.z());
        Quaternionf azimuthRotation = new Quaternionf().rotateY(azimuth);
        Vector3f localNormal = azimuthRotation.transformInverse(new Vector3f(normal));
        float elevation = (float) Math.atan2(localNormal.z(), localNormal.y());
        Quaternionf elevationRotation = new Quaternionf().rotateX(elevation);

        VertexConsumer vb = buffer.getBuffer(RenderType.cutout());

        // Post: yaws only, pivoting around the block's vertical center axis.
        ms.pushPose();
        ms.translate(PIVOT_X, 0, PIVOT_Z);
        ms.mulPose(azimuthRotation);
        ms.translate(-PIVOT_X, 0, -PIVOT_Z);
        CachedBuffers.partial(POST, be.getBlockState()).light(light).renderInto(ms, vb);
        ms.popPose();

        // Plate: yaws with the post (same pivot), then additionally tilts around the
        // hinge at the post's top.
        ms.pushPose();
        ms.translate(PIVOT_X, 0, PIVOT_Z);
        ms.mulPose(azimuthRotation);
        ms.translate(-PIVOT_X, 0, -PIVOT_Z);
        ms.translate(PIVOT_X, HINGE_Y, PIVOT_Z);
        ms.mulPose(elevationRotation);
        ms.translate(-PIVOT_X, -HINGE_Y, -PIVOT_Z);
        CachedBuffers.partial(PLATE, be.getBlockState()).light(light).renderInto(ms, vb);
        ms.popPose();
    }

    private Vector3f computeMirrorNormal(SolarMirrorBlockEntity be, Level level, float partialTicks) {
        BlockPos towerPos = be.getLinkedTower();
        if (towerPos == null) return new Vector3f(UP); // idle: rest flat

        Vector3f sunDir = sunDirection(level, partialTicks);

        BlockPos mirrorPos = be.getBlockPos();
        Vector3f towerDir = new Vector3f(
                towerPos.getX() - mirrorPos.getX(),
                towerPos.getY() - mirrorPos.getY(),
                towerPos.getZ() - mirrorPos.getZ());
        if (towerDir.lengthSquared() < 1.0E-6f) return new Vector3f(UP);
        towerDir.normalize();

        // Heliostat law: the mirror's normal bisects the incoming sunlight direction
        // and the direction to the target it needs to reflect that light onto.
        Vector3f normal = new Vector3f(sunDir).add(towerDir);
        if (normal.lengthSquared() < 1.0E-6f) return new Vector3f(UP);
        return normal.normalize();
    }

    // Reproduces the direction vanilla points its sun billboard in LevelRenderer#renderSky:
    // posestack.mulPose(Axis.YP.rotationDegrees(-90)); posestack.mulPose(Axis.XP.rotationDegrees(angle));
    // applied to the local point (0, 100, 0). Composing those two rotation matrices by hand
    // resolves to (-sin(angle), cos(angle), 0) - verified against the decompiled source, not
    // guessed; the previous (cos, sin, 0) was off by a consistent 90 degrees.
    private static Vector3f sunDirection(Level level, float partialTicks) {
        float angle = level.getSunAngle(partialTicks);
        return new Vector3f(-Mth.sin(angle), Mth.cos(angle), 0.0F).normalize();
    }
}
