package net.succ.solar_punk.compat.newage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import org.antarcticgardens.cna.content.heat.HeatBlockEntity;

// The Heat Battery as a Create: New Age heat block. Only loaded when New Age is installed
// (see NewAgeHeat.createBattery), since it implements their interface.
//
// Implementing HeatBlockEntity is what makes their pipes connect to it. Their pipes don't
// average heat with it though (canAdd is false): they'd treat its temperature as an amount
// of heat, and a battery holds far more heat than its temperature. Instead the battery trades
// heat with its neighbours itself, once a second, moving half the difference between its
// temperature and theirs. One rate is used both ways, so no loop can make heat from nothing.
public class NewAgeHeatBatteryBlockEntity extends HeatBatteryBlockEntity implements HeatBlockEntity {

    // Share of the temperature difference that moves each second. 0.5 meets halfway, like
    // New Age's own averaging between two blocks.
    private static final float CONDUCTANCE = 0.5f;

    public NewAgeHeatBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEAT_BATTERY.get(), pos, state);
    }

    static HeatBatteryBlockEntity create(BlockPos pos, BlockState state) {
        return new NewAgeHeatBatteryBlockEntity(pos, state);
    }

    @Override
    protected boolean afterCharge() {
        if (level == null || level.getGameTime() % 20 != 0 || NewAgeHeat.isDisabled()) return false;
        try {
            return exchange();
        } catch (Throwable t) {
            NewAgeHeat.disable(t);
            return false;
        }
    }

    private boolean exchange() {
        double rate = Config.heatBatteryNewAgeHeatRate;
        boolean changed = false;
        for (Direction dir : Direction.values()) {
            BlockEntity other = level.getBlockEntity(worldPosition.relative(dir));
            // New Age passes the side the heat comes in from, as seen from the asker
            if (!(other instanceof HeatBlockEntity hbe) || other instanceof NewAgeHeatBatteryBlockEntity
                    || !hbe.canAdd(dir)) continue;

            float flow = (NewAgeHeat.temperature(this) - hbe.getHeat()) * CONDUCTANCE;
            if (flow > 0) {
                // push, limited by what the battery holds
                flow = (float) Math.min(flow, getHeatStored() / rate);
            } else {
                // pull, limited by the room left in the battery
                flow = (float) -Math.min(-flow, (Config.heatBatteryMaxHeat - getHeatStored()) / rate);
            }
            int batteryHeat = -(int) Math.round(flow * rate);
            if (batteryHeat == 0) continue;
            hbe.addHeat(flow);
            addHeat(batteryHeat);
            changed = true;
        }
        return changed;
    }

    // HeatBlockEntity

    @Override
    public float getHeat() {
        return NewAgeHeat.temperature(this);
    }

    /** Heat pushed in directly by a New Age block, converted at the usual rate. */
    @Override
    public void addHeat(float amount) {
        addHeat((int) Math.round(amount * Config.heatBatteryNewAgeHeatRate));
        setChanged();
    }

    /** Ignored: their averaging is turned off (canAdd), and a temperature can't be set directly. */
    @Override
    public void setHeat(float amount) {
    }

    @Override
    public boolean canConnect(Direction from) {
        return true;
    }

    @Override
    public boolean canAdd(Direction from) {
        return false;
    }

    @Override
    public float maxHeat() {
        return Config.heatBatteryNewAgeMaxTemperature;
    }
}
