package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.GeyserWellBlock;
import net.succ.solar_punk.block.entity.custom.GeyserWellBlockEntity;

/**
 * Turns the Geyser Well's two gauge needles to the strength of the vent below it
 * (GeyserWellBlockEntity.ventStrength()). The needles are their own models (cut out of the
 * block model), each swung around its dial's centre, from -60 degrees at nothing to +60 at full.
 */
public class GeyserWellRenderer implements BlockEntityRenderer<GeyserWellBlockEntity> {
    public static final PartialModel WEST_NEEDLE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/geyser_well_needle_west"));
    public static final PartialModel EAST_NEEDLE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/geyser_well_needle_east"));

    private static final float SWING = 60;

    public GeyserWellRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(GeyserWellBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                       int light, int overlay) {
        var state = be.getBlockState();
        var vb = buffer.getBuffer(RenderType.cutout());
        float yRot = state.getValue(GeyserWellBlock.FACING).toYRot() + 180; // north is 0, like the blockstate

        float strength = Mth.lerp(partialTicks, be.lastNeedle, be.needle);
        needle(ms, vb, WEST_NEEDLE, state, yRot, strength, light);
        needle(ms, vb, EAST_NEEDLE, state, yRot, strength, light);
    }

    private static void needle(PoseStack ms, com.mojang.blaze3d.vertex.VertexConsumer vb, PartialModel model,
                               net.minecraft.world.level.block.state.BlockState state, float yRot, float fill, int light) {
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(Axis.YP.rotationDegrees(-yRot));
        // Dial centre: y 8, z 8 in model pixels, so the block's centre line
        ms.mulPose(Axis.XP.rotationDegrees(-SWING + 2 * SWING * Mth.clamp(fill, 0, 1)));
        ms.translate(-0.5, -0.5, -0.5);
        CachedBuffers.partial(model, state).light(light).renderInto(ms, vb);
        ms.popPose();
    }
}
