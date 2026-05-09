package net.succ.create_solar_powered.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.succ.create_solar_powered.block.custom.BrassSolarPanelBlock;

import java.util.List;

public class BrassSolarPanelBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    private static final int MORNING_FE_PER_TICK = 40;
    private static final int NOON_FE_PER_TICK    = 80;
    private static final int BUFFER_CAPACITY     = 100_000;
    private static final int MAX_EXTRACT         = 80;

    private static final long NOON_START    = 2000;
    private static final long EVENING_START = 10000;
    private static final long NIGHT_START   = 12000;

    private static class GeneratingEnergyStorage extends EnergyStorage {
        GeneratingEnergyStorage(int capacity, int maxExtract) {
            super(capacity, 0, maxExtract);
        }
        void generate(int amount) {
            energy = Math.min(energy + amount, capacity);
        }
        void setStored(int amount) {
            energy = Math.min(amount, capacity);
        }
    }

    public final GeneratingEnergyStorage energyStorage = new GeneratingEnergyStorage(BUFFER_CAPACITY, MAX_EXTRACT);

    public BrassSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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

    private int getGeneratedFE() {
        if (!hasSkyAccess()) return 0;
        return switch (getPhase()) {
            case MORNING, EVENING -> MORNING_FE_PER_TICK;
            case NOON -> level.isRaining() ? MORNING_FE_PER_TICK : NOON_FE_PER_TICK;
            case NIGHT -> 0;
        };
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        int generated = getGeneratedFE();
        if (generated > 0) {
            energyStorage.generate(generated);
            setChanged();
        }

        if (energyStorage.getEnergyStored() > 0) {
            for (Direction dir : Direction.values()) {
                IEnergyStorage neighbor = level.getCapability(
                        Capabilities.EnergyStorage.BLOCK,
                        worldPosition.relative(dir),
                        dir.getOpposite()
                );
                if (neighbor != null && neighbor.canReceive()) {
                    int toSend = energyStorage.extractEnergy(MAX_EXTRACT, true);
                    int accepted = neighbor.receiveEnergy(toSend, false);
                    if (accepted > 0) {
                        energyStorage.extractEnergy(accepted, false);
                        setChanged();
                    }
                }
            }
        }

        if (level.getGameTime() % 20 == 0) {
            boolean active = generated > 0;
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(BrassSolarPanelBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(BrassSolarPanelBlock.LIT, active), 3);
            level.sendBlockUpdated(worldPosition, state, level.getBlockState(worldPosition), 2);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("create_solar_powered.tooltip.fe_header").forGoggles(tooltip);

        int generated = getGeneratedFE();
        CreateLang.translate("create_solar_powered.tooltip.generating")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(generated)
                        .text(" FE/t")
                        .style(generated > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)
                        .component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("create_solar_powered.tooltip.stored")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(energyStorage.getEnergyStored())
                        .text(" / " + energyStorage.getMaxEnergyStored() + " FE")
                        .style(ChatFormatting.AQUA)
                        .component())
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setStored(tag.getInt("energy"));
    }
}