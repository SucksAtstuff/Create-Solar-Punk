package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;

public class FermentationVatRenderer extends SafeBlockEntityRenderer<FermentationVatBlockEntity> {

    public static final PartialModel GAUGE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/fermentation_vat/gauge"));

    public static final PartialModel GAUGE_DIAL = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/fermentation_vat/gauge_dial"));

    public FermentationVatRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(FermentationVatBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer, int light, int overlay) {
        if (!be.isController()) return;
        if (be.getWidth() < Config.fermentationVatMinWidth) return;

        BlockState blockState = be.getBlockState();
        VertexConsumer vb = buffer.getBuffer(RenderType.cutout());

        float waterFill = be.waterTank.getCapacity() > 0
                ? be.waterTank.getFluidAmount() / (float) be.waterTank.getCapacity()
                : 0f;
        // 0° when empty (needle horizontal, pointing right), -90° when full (needle pointing up)
        float needleAngle = Mth.lerp(waterFill, 0f, -90f);

        // Pivot = bottom-left corner of gauge face in block units (model coords / 16)
        float px = 16.1f / 16f, py = 5.5f / 16f, pz = 6.05f / 16f;

        ms.pushPose();
        TransformStack.of(ms).translate(be.getWidth() / 2f, 0.5, be.getWidth() / 2f);

        for (Direction d : Iterate.horizontalDirections) {
            ms.pushPose();
            float yRot = -d.toYRot() - 90;

            CachedBuffers.partial(GAUGE, blockState)
                    .rotateYDegrees(yRot)
                    .uncenter()
                    .translate(be.getWidth() / 2f - 6 / 16f, 0, 0)
                    .light(light)
                    .renderInto(ms, vb);

            // Last call = innermost = first applied to vertex (same convention as GAUGE body above).
            // Steps 4-6 (innermost) match the gauge body; steps 1-3 add pivot rotation in model space.
            CachedBuffers.partial(GAUGE_DIAL, blockState)
                    .rotateYDegrees(yRot)                              // 1: outermost — orient
                    .uncenter()                                         // 2
                    .translate(be.getWidth() / 2f - 6 / 16f, 0, 0)    // 3
                    .translate(px, py, pz)                             // 4: pivot back
                    .rotateXDegrees(needleAngle)                       // 5: rotate needle
                    .translate(-px, -py, -pz)                          // 6: innermost — move pivot to origin
                    .light(light)
                    .renderInto(ms, vb);

            ms.popPose();
        }

        ms.popPose();
    }
}
