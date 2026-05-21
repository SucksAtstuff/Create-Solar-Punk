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
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;

public class SolarPowerTowerRenderer extends SafeBlockEntityRenderer<SolarPowerTowerBlockEntity> {

    public static final PartialModel GAUGE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/solar_power_tower/gauge"));

    public static final PartialModel GAUGE_DIAL = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/solar_power_tower/gauge_dial"));

    public SolarPowerTowerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(SolarPowerTowerBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer, int light, int overlay) {
        if (!be.isController()) return;
        if (be.getWidth() < 3) return;

        BlockState blockState = be.getBlockState();
        VertexConsumer vb = buffer.getBuffer(RenderType.cutout());

        float waterFill = be.waterTank.getCapacity() > 0
                ? be.waterTank.getFluidAmount() / (float) be.waterTank.getCapacity()
                : 0f;
        float needleAngle = Mth.lerp(waterFill, 0f, -90f);

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

            CachedBuffers.partial(GAUGE_DIAL, blockState)
                    .rotateYDegrees(yRot)
                    .uncenter()
                    .translate(be.getWidth() / 2f - 6 / 16f, 0, 0)
                    .translate(px, py, pz)
                    .rotateXDegrees(needleAngle)
                    .translate(-px, -py, -pz)
                    .light(light)
                    .renderInto(ms, vb);

            ms.popPose();
        }

        ms.popPose();
    }
}
