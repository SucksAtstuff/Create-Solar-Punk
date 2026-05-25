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
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.KineticBatteryBlock;

import java.util.List;

public class KineticBatteryBlockEntity extends GeneratingKineticBlockEntity {

    private float chargeLevel = 0f;

    public KineticBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float getChargeLevel() { return chargeLevel; }

    @Override
    public float getGeneratedSpeed() {
        if (level == null) return 0f;
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(KineticBatteryBlock.LIT)) return 0f;
        return state.getValue(KineticBatteryBlock.LIT) ? Config.kineticBatteryRpm : 0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = 0f;
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(KineticBatteryBlock.LIT) && state.getValue(KineticBatteryBlock.LIT))
                capacity = Config.kineticBatterySu;
        }
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        CreateLang.translate("solar_punk.tooltip.kinetic_battery_header").forGoggles(tooltip);

        int pct = (int)(chargeLevel / Config.kineticBatteryMaxCharge * 100);
        CreateLang.translate("solar_punk.tooltip.charge")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(pct)
                        .text("%")
                        .style(pct > 0 ? ChatFormatting.GOLD : ChatFormatting.RED)
                        .component())
                .forGoggles(tooltip, 1);

        if (chargeLevel > 0) {
            int seconds = (int)(chargeLevel / (float) Config.kineticBatteryDischargeRate / 20);
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
            chargeLevel -= (float) Config.kineticBatteryDischargeRate;
            if (chargeLevel < 0) chargeLevel = 0;
            changed = true;
        } else if (!powered && Math.abs(getSpeed()) > 0 && chargeLevel < Config.kineticBatteryMaxCharge) {
            chargeLevel = Math.min(Config.kineticBatteryMaxCharge, chargeLevel + (float) Config.kineticBatteryChargeRate);
            changed = true;
        }

        boolean shouldBeLit = powered && chargeLevel > 0;
        if (currentlyLit != shouldBeLit) {
            level.setBlock(worldPosition, state.setValue(KineticBatteryBlock.LIT, shouldBeLit), 3);
            updateGeneratedRotation();
        }

        if (changed) {
            setChanged();
            if (level.getGameTime() % 20 == 0) {
                sendData();
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
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
