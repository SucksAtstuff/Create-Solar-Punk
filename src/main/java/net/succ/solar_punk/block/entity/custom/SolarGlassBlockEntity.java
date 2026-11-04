package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.util.SolarHelper;

/**
 * Solar Glass and Solar Glass Pane. Needs open sky above like the other panels, so it works as
 * a skylight or a window with nothing over it; the pane makes less than the full block. No
 * angle bonus, so a window works the same whichever way it faces.
 */
public class SolarGlassBlockEntity extends SharedSolarBlockEntity {

    public SolarGlassBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, Config.solarGlassBuffer, Config.solarGlassMaxExtract);
    }

    @Override
    protected int computeGenerating() {
        boolean pane = getBlockState().is(ModBlocks.SOLAR_GLASS_PANE.get());
        float base = pane
                ? SolarHelper.byPhase(level, worldPosition, Config.solarGlassPaneMorningFe, Config.solarGlassPaneNoonFe)
                : SolarHelper.byPhase(level, worldPosition, Config.solarGlassMorningFe, Config.solarGlassNoonFe);
        return Math.round(base * SolarHelper.outputMultiplier(level, worldPosition));
    }
}
