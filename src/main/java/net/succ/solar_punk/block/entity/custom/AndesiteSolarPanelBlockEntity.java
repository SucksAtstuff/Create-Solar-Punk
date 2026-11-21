package net.succ.solar_punk.block.entity.custom;

import net.succ.solar_punk.SolarPunk;
import studio.offbyone.lib.util.GeneratorHelper;
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

    // Create expects a generator's speed and capacity to change only together with
    // updateGeneratedRotation(): its own checks (validation, propagation, network
    // add/remove) read getGeneratedSpeed() at any moment. Working them out from the sun on
    // every call let them flip at dawn and dusk between our once-a-second updates, so
    // Create zeroed the speed without detaching the panel and the network broke apart
    // (panels and shafts popping off). So they're stored, and only refreshed right before
    // updateGeneratedRotation(). Saved, so a reloaded panel matches the network it left.
    private float generatedSpeed = 0;
    private float generatedCapacity = 0;

    // Plants below cool the panel (agrivoltaics). Only the SU goes up, so the RPM stays clean.
    private boolean plantBelow = false;

    @Override
    public float getGeneratedSpeed() {
        return generatedSpeed;
    }

    @Override
    public float calculateAddedStressCapacity() {
        this.lastCapacityProvided = generatedCapacity;
        return generatedCapacity;
    }

    @Override
    public void updateGeneratedRotation() {
        if (level != null && !level.isClientSide) {
            generatedSpeed = computeSpeed();
            generatedCapacity = generatedSpeed == 0 ? 0 : computeCapacity();
        }
        super.updateGeneratedRotation();
    }

    private float computeSpeed() {
        float speed = SolarPunk.SOLAR.byPhase(level, worldPosition, Config.andesiteMorningRpm, Config.andesiteNoonRpm);
        // Round so the network keeps clean integer RPM; the season only shifts every few
        // in-game days, so this doesn't churn the kinetic network.
        if (speed != 0) speed = Math.round(speed * SolarPunk.SOLAR.outputMultiplier(level, worldPosition));
        return GeneratorHelper.directed(speed, rotationDirection);
    }

    private float computeCapacity() {
        float capacity = SolarPunk.SOLAR.byPhase(level, worldPosition, Config.andesiteMorningSu, Config.andesiteNoonSu);
        return capacity == 0 ? 0 : capacity * SolarPunk.SOLAR.outputMultiplier(level, worldPosition)
                * Agrivoltaics.multiplier(plantBelow);
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && level.getGameTime() % 20 == 0) {
            plantBelow = Agrivoltaics.hasPlantBelow(level, worldPosition);
            // Also syncs the speed, capacity and plant to the client for goggles.
            updateGeneratedRotation();
            boolean active = generatedSpeed != 0;
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof AndesiteSolarPanelBlock)) return;
            if (state.getValue(AndesiteSolarPanelBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(AndesiteSolarPanelBlock.LIT, active), 3);
            int signal = SolarPunk.SOLAR.comparatorSignal(level, worldPosition, active);
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
        tag.putFloat("SolarSpeed", generatedSpeed);
        tag.putFloat("SolarCapacity", generatedCapacity);
        tag.putBoolean("PlantBelow", plantBelow);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        generatedSpeed = tag.getFloat("SolarSpeed");
        generatedCapacity = tag.getFloat("SolarCapacity");
        plantBelow = tag.getBoolean("PlantBelow");
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
