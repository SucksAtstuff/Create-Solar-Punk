package net.succ.create_solar_powered.block.entity.custom;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.create_solar_powered.block.custom.KineticBatteryBlock;

public class KineticBatteryBlockEntity extends GeneratingKineticBlockEntity {

    private float chargeLevel = 0f;

    public static final float MAX_CHARGE = 1200f;
    private static final float CHARGE_RATE = 1f;
    private static final float DISCHARGE_RATE = 0.5f;
    private static final float BATTERY_RPM = 16f;
    private static final float BATTERY_CAPACITY = 256f;

    public KineticBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float getChargeLevel() { return chargeLevel; }

    @Override
    public float getGeneratedSpeed() {
        if (level == null) return 0f;
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(KineticBatteryBlock.LIT)) return 0f;
        return state.getValue(KineticBatteryBlock.LIT) ? BATTERY_RPM : 0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (level == null) return 0f;
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(KineticBatteryBlock.LIT)) return 0f;
        return state.getValue(KineticBatteryBlock.LIT) ? BATTERY_CAPACITY : 0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        BlockState state = level.getBlockState(worldPosition);
        boolean currentlyLit = state.getValue(KineticBatteryBlock.LIT);
        boolean powered = level.hasNeighborSignal(worldPosition);
        boolean changed = false;

        if (currentlyLit) {
            // Discharging — deplete charge each tick
            chargeLevel -= DISCHARGE_RATE;
            if (chargeLevel < 0) chargeLevel = 0;
            changed = true;
        } else if (!powered && Math.abs(getSpeed()) > 0 && chargeLevel < MAX_CHARGE) {
            // Charging — absorb from spinning network when not powered
            chargeLevel = Math.min(MAX_CHARGE, chargeLevel + CHARGE_RATE);
            changed = true;
        }

        boolean shouldBeLit = powered && chargeLevel > 0;
        if (currentlyLit != shouldBeLit) {
            level.setBlock(worldPosition, state.setValue(KineticBatteryBlock.LIT, shouldBeLit), 3);
            updateGeneratedRotation();
        }

        if (changed) setChanged();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("ChargeLevel", chargeLevel);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        chargeLevel = tag.getFloat("ChargeLevel");
    }
}
