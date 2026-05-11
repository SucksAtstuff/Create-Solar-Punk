package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;

public class AndesiteSolarPanelBlockEntity extends GeneratingKineticBlockEntity {

    private static final float MORNING_RPM = 8f;
    private static final float NOON_RPM = 16f;
    private static final float MORNING_CAPACITY = 128f;
    private static final float NOON_CAPACITY = 256f;

    private static final long NOON_START    = 2000;
    private static final long EVENING_START = 10000;
    private static final long NIGHT_START   = 12000;

    public AndesiteSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private enum Phase { NIGHT, MORNING, NOON, EVENING }

    private Phase getPhase() {
        if (level == null) return Phase.NIGHT;
        long time = level.getDayTime() % 24000;
        if (time < NOON_START)    return Phase.MORNING;
        if (time < EVENING_START) return Phase.NOON;
        if (time < NIGHT_START)   return Phase.EVENING;
        return Phase.NIGHT;
    }

    private boolean hasSkyAccess() {
        return level != null && level.canSeeSky(worldPosition.above());
    }

    @Override
    public float getGeneratedSpeed() {
        if (!hasSkyAccess()) return 0;
        return switch (getPhase()) {
            case MORNING, EVENING -> MORNING_RPM;
            case NOON -> level.isRaining() ? MORNING_RPM : NOON_RPM;
            case NIGHT -> 0;
        };
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!hasSkyAccess()) return 0;
        return switch (getPhase()) {
            case MORNING, EVENING -> MORNING_CAPACITY;
            case NOON -> level.isRaining() ? MORNING_CAPACITY : NOON_CAPACITY;
            case NIGHT -> 0;
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && level.getGameTime() % 20 == 0) {
            updateGeneratedRotation();
            boolean active = getGeneratedSpeed() != 0;
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(AndesiteSolarPanelBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(AndesiteSolarPanelBlock.LIT, active), 3);
        }
    }
}