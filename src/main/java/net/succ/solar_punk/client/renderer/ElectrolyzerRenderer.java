package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.succ.solar_punk.block.custom.ElectrolyzerBlock;
import net.succ.solar_punk.block.entity.custom.ElectrolyzerBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

/**
 * Draws the liquids inside the Electrolyzer's glass, like a Hofmann voltameter: Water in the
 * bulb on top, and in each tube a column of Water with that tube's gas collecting above it,
 * pushing the Water down as the gas tank fills. The boxes sit half a pixel inside the glass
 * (the model's old placeholder elements) and are laid out facing north, then turned with
 * the block.
 */
public class ElectrolyzerRenderer extends SafeBlockEntityRenderer<ElectrolyzerBlockEntity> {

    private static final float PX = 1 / 16f;
    // Tube insides, in pixels: x range per tube, shared y and z ranges.
    private static final float TUBE_Y0 = 1.5f, TUBE_Y1 = 9.5f, TUBE_Z0 = 4.5f, TUBE_Z1 = 11.5f;
    private static final float OXYGEN_X0 = 2.5f, OXYGEN_X1 = 5.5f;    // west tube
    private static final float HYDROGEN_X0 = 10.5f, HYDROGEN_X1 = 13.5f; // east tube
    // Bulb inside.
    private static final float BULB_XZ0 = 5.5f, BULB_XZ1 = 10.5f, BULB_Y0 = 11.5f, BULB_Y1 = 14.5f;
    // Thinner than this and a box isn't worth drawing.
    private static final float MIN = 0.05f;

    private static final FluidStack WATER = new FluidStack(Fluids.WATER, 1000);

    public ElectrolyzerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(ElectrolyzerBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        float water = be.waterLevel.getValue(partialTicks);
        float hydrogen = be.hydrogenLevel.getValue(partialTicks);
        float oxygen = be.oxygenLevel.getValue(partialTicks);

        Direction facing = be.getBlockState().getValue(ElectrolyzerBlock.FACING);
        ms.pushPose();
        ms.translate(0.5, 0, 0.5);
        // The blockstate turns the model clockwise (seen from above); PoseStack Y turns the other way.
        ms.mulPose(Axis.YP.rotationDegrees(-(facing.toYRot() + 180)));
        ms.translate(-0.5, 0, -0.5);

        // Bulb: Water from the bottom up.
        if (water > 0)
            box(WATER, BULB_XZ0, BULB_Y0, BULB_XZ0,
                    BULB_XZ1, BULB_Y0 + (BULB_Y1 - BULB_Y0) * water, BULB_XZ1, true, buffer, ms, light);

        tube(be.hydrogenTank.getFluid().isEmpty() ? null : new FluidStack(ModFluids.HYDROGEN_SOURCE.get(), 1000),
                hydrogen, water > 0, HYDROGEN_X0, HYDROGEN_X1, buffer, ms, light);
        tube(be.oxygenTank.getFluid().isEmpty() ? null : new FluidStack(ModFluids.OXYGEN_SOURCE.get(), 1000),
                oxygen, water > 0, OXYGEN_X0, OXYGEN_X1, buffer, ms, light);

        ms.popPose();
    }

    // Gas from the top down to its fill level, and Water (while there is any) from the
    // bottom up to meet it.
    private static void tube(FluidStack gas, float gasFill, boolean hasWater, float x0, float x1,
                             MultiBufferSource buffer, PoseStack ms, int light) {
        float split = TUBE_Y1 - (TUBE_Y1 - TUBE_Y0) * Math.min(1, gasFill);
        if (hasWater && split - TUBE_Y0 > MIN)
            box(WATER, x0, TUBE_Y0, TUBE_Z0, x1, split, TUBE_Z1, true, buffer, ms, light);
        if (gas != null && TUBE_Y1 - split > MIN)
            box(gas, x0, split, TUBE_Z0, x1, TUBE_Y1, TUBE_Z1, !hasWater, buffer, ms, light);
    }

    private static void box(FluidStack fluid, float x0, float y0, float z0, float x1, float y1, float z1,
                            boolean bottom, MultiBufferSource buffer, PoseStack ms, int light) {
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid,
                x0 * PX, y0 * PX, z0 * PX, x1 * PX, y1 * PX, z1 * PX,
                buffer, ms, light, bottom, false);
    }
}
