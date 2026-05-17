package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.custom.KineticBatteryBlock;

import java.util.List;

public class KineticBatteryBlockEntity extends GeneratingKineticBlockEntity {

    private float chargeLevel = 0f;

    public static final float MAX_CHARGE = 1200f;
    private static final float CHARGE_RATE = 1f;
    private static final float DISCHARGE_RATE = 0.5f;
    private static final float BATTERY_RPM = 16f;
    private static final float BATTERY_CAPACITY = 16f;

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
        float capacity = 0f;
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(KineticBatteryBlock.LIT) && state.getValue(KineticBatteryBlock.LIT))
                capacity = BATTERY_CAPACITY;
        }
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        CreateLang.translate("solar_punk.tooltip.kinetic_battery_header").forGoggles(tooltip);

        int pct = (int)(chargeLevel / MAX_CHARGE * 100);
        CreateLang.translate("solar_punk.tooltip.charge")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(pct)
                        .text("%")
                        .style(pct > 0 ? ChatFormatting.GOLD : ChatFormatting.RED)
                        .component())
                .forGoggles(tooltip, 1);

        if (chargeLevel > 0) {
            int seconds = (int)(chargeLevel / DISCHARGE_RATE / 20);
            String timeStr = seconds >= 60
                    ? (seconds / 60) + "m " + (seconds % 60) + "s"
                    : seconds + "s";
            CreateLang.translate("solar_punk.tooltip.runtime")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.builder().text(ChatFormatting.AQUA, timeStr).component())
                    .forGoggles(tooltip, 1);
        }

        return true;
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

        if (changed) {
            setChanged();
            if (level.getGameTime() % 20 == 0) sendData();
        }
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
