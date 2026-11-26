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
 * block model), each swung around its bottom end, from -60 degrees at nothing to +60 at full.
 */
public class GeyserWellRenderer implements BlockEntityRenderer<GeyserWellBlockEntity> {
    public static final PartialModel WEST_NEEDLE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/geyser_well_needle_west"));
    public static final PartialModel EAST_NEEDLE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/geyser_well_needle_east"));

    private static final float SWING = 60;
    // The needle's bottom end in the model (y 6.5, z 8 in pixels): the point it swings around
    private static final float PIVOT_Y = 6.5f / 16, PIVOT_Z = 8f / 16;

    public GeyserWellRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(GeyserWellBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                       int light, int overlay) {
        var state = be.getBlockState();
        var vb = buffer.getBuffer(RenderType.cutout());
        float yRot = state.getValue(GeyserWellBlock.FACING).toYRot() + 180; // north is 0, like the blockstate

        float strength = Mth.lerp(partialTicks, be.lastNeedle, be.needle);
        // Seen from outside, the same turn reads the other way on the far side, so the east
        // needle swings the opposite way to rise left to right like the west one
        needle(ms, vb, WEST_NEEDLE, state, yRot, strength, 1, light);
        needle(ms, vb, EAST_NEEDLE, state, yRot, strength, -1, light);
    }

    private static void needle(PoseStack ms, com.mojang.blaze3d.vertex.VertexConsumer vb, PartialModel model,
                               net.minecraft.world.level.block.state.BlockState state, float yRot, float fill, int direction, int light) {
        ms.pushPose();
        // Face the block's way (around the block's centre), then swing around the needle's base
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(Axis.YP.rotationDegrees(-yRot));
        ms.translate(0, PIVOT_Y - 0.5, PIVOT_Z - 0.5);
        ms.mulPose(Axis.XP.rotationDegrees(direction * (-SWING + 2 * SWING * Mth.clamp(fill, 0, 1))));
        ms.translate(-0.5, -PIVOT_Y, -PIVOT_Z);
        CachedBuffers.partial(model, state).light(light).renderInto(ms, vb);
        ms.popPose();
    }
}
