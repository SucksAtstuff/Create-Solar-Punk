package net.succ.solar_punk.block.entity.custom;

import net.succ.solar_punk.util.GeneratorHelper;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;

import java.util.List;
import net.succ.solar_punk.util.SolarHelper;
import net.succ.solar_punk.util.Agrivoltaics;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class AndesiteSolarPanelBlockEntity extends GeneratingKineticBlockEntity {

    ScrollOptionBehaviour<RotationDirection> rotationDirection;

    public AndesiteSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        rotationDirection = GeneratorHelper.rotationDirection(this, 5, (state, direction) -> direction.getAxis().isHorizontal());
        behaviours.add(rotationDirection);
    }

    @Override
    public float getGeneratedSpeed() {
        float speed = SolarHelper.byPhase(level, worldPosition, Config.andesiteMorningRpm, Config.andesiteNoonRpm);
        // Round so the network keeps clean integer RPM; the season only shifts every few
        // in-game days, so this doesn't churn the kinetic network.
        if (speed != 0) speed = Math.round(speed * SolarHelper.outputMultiplier(level, worldPosition));
        return GeneratorHelper.directed(speed, rotationDirection);
    }

    // Server-computed capacity, synced for the goggle tooltip. The client can't always work
    // it out itself: in Cosmonautics deep space the sun position is server-only data.
    private float syncedCapacity = 0;

    @Override
    public float calculateAddedStressCapacity() {
        if (level != null && level.isClientSide) return syncedCapacity;
        float capacity = computeCapacity();
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    // Plants below cool the panel (agrivoltaics). Only the SU goes up, so the RPM stays clean.
    private boolean plantBelow = false;

    private float computeCapacity() {
        float capacity = SolarHelper.byPhase(level, worldPosition, Config.andesiteMorningSu, Config.andesiteNoonSu);
        return capacity == 0 ? 0 : capacity * SolarHelper.outputMultiplier(level, worldPosition)
                * Agrivoltaics.multiplier(plantBelow);
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && level.getGameTime() % 20 == 0) {
            boolean plant = Agrivoltaics.hasPlantBelow(level, worldPosition);
            boolean plantChanged = plant != plantBelow;
            plantBelow = plant;
            updateGeneratedRotation();
            float capacity = computeCapacity();
            if (capacity != syncedCapacity || plantChanged) {
                syncedCapacity = capacity;
                sendData();
            }
            boolean active = getGeneratedSpeed() != 0;
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof AndesiteSolarPanelBlock)) return;
            if (state.getValue(AndesiteSolarPanelBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(AndesiteSolarPanelBlock.LIT, active), 3);
            int signal = SolarHelper.comparatorSignal(level, worldPosition, active);
            if (signal != comparatorSignal) {
                comparatorSignal = signal;
                level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
            }
        }
    }

    // Recomputed once a second on the server, as the sun moves.
    private int comparatorSignal = 0;

    /** 0 when not generating, otherwise 1-15 by how directly it faces the sun. Server only. */
    public int getComparatorSignal() {
        return comparatorSignal;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) tag.putFloat("SolarCapacity", syncedCapacity);
        if (clientPacket) tag.putBoolean("PlantBelow", plantBelow);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) syncedCapacity = tag.getFloat("SolarCapacity");
        if (clientPacket) plantBelow = tag.getBoolean("PlantBelow");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!plantBelow) return added;
        CreateLang.translate("solar_punk.tooltip.agrivoltaics",
                        Math.round(Config.agrivoltaicsBonus * 100))
                .style(ChatFormatting.GREEN)
                .forGoggles(tooltip, 1);
        return true;
    }
}
