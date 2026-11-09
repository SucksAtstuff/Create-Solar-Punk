package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.SolarShinglesBlock;
import net.succ.solar_punk.util.SolarHelper;

/**
 * A shingle's output depends on which way its slope faces. Buffering and passing energy across
 * the roof are in {@link SharedSolarBlockEntity}.
 */
public class SolarShinglesBlockEntity extends SharedSolarBlockEntity {

    // The roof rises one block every two, so the panels are tilted by atan(1/2), facing downhill.
    private static final double TILT = Math.atan(0.5);
    // The most a well-angled shingle makes compared to a flat one, so low sun can't spike it.
    private static final double MAX_TILT_FACTOR = 1.5;

    // Solar Glass Shingles make what a Solar Glass block does, with the same roof angle bonus.
    private final boolean glass;

    public SolarShinglesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, state.is(ModBlocks.SOLAR_GLASS_SHINGLES.get()));
    }

    private SolarShinglesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean glass) {
        super(type, pos, state,
                glass ? Config.solarGlassBuffer : Config.shingleBuffer,
                glass ? Config.solarGlassMaxExtract : Config.shingleMaxExtract);
        this.glass = glass;
    }

    @Override
    protected int computeGenerating() {
        float base = glass
                ? SolarHelper.byPhase(level, worldPosition, Config.solarGlassMorningFe, Config.solarGlassNoonFe)
                : SolarHelper.byPhase(level, worldPosition, Config.shingleMorningFe, Config.shingleNoonFe);
        if (base == 0) return 0;
        Direction downhill = getBlockState().getValue(SolarShinglesBlock.FACING).getOpposite();
        Vec3 normal = new Vec3(downhill.getStepX() * Math.sin(TILT), Math.cos(TILT), downhill.getStepZ() * Math.sin(TILT));
        double tilt = SolarHelper.tiltFactor(level, worldPosition, normal, MAX_TILT_FACTOR);
        return (int) Math.round(base * tilt * SolarHelper.outputMultiplier(level, worldPosition));
    }
}
