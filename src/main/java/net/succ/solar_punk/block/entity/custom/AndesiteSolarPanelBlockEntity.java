package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;

public class AndesiteSolarPanelBlockEntity extends GeneratingKineticBlockEntity {

    public AndesiteSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        if (!SolarHelper.hasSkyAccess(level, worldPosition)) return 0;
        return switch (SolarHelper.getPhase(level)) {
            case MORNING, EVENING -> Config.andesiteMorningRpm;
            case NOON -> level.isRaining() ? Config.andesiteMorningRpm : Config.andesiteNoonRpm;
            case NIGHT -> 0;
        };
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = 0;
        if (SolarHelper.hasSkyAccess(level, worldPosition)) {
            capacity = switch (SolarHelper.getPhase(level)) {
                case MORNING, EVENING -> Config.andesiteMorningSu;
                case NOON -> level.isRaining() ? Config.andesiteMorningSu : Config.andesiteNoonSu;
                case NIGHT -> 0;
            };
        }
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && level.getGameTime() % 20 == 0) {
            updateGeneratedRotation();
            boolean active = getGeneratedSpeed() != 0;
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof AndesiteSolarPanelBlock)) return;
            if (state.getValue(AndesiteSolarPanelBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(AndesiteSolarPanelBlock.LIT, active), 3);
        }
    }
}
