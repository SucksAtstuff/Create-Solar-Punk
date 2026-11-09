package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.SolarPlantBedBlock;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.util.PlantGrowth;
import net.succ.solar_punk.util.SolarHelper;

import java.util.List;

/**
 * Solar Plant Bed. Its panel charges an internal buffer during the day, and at night the grow
 * light runs off that charge, lighting the bed up and speeding up the plant on top. Fully
 * self-contained: no FE in or out. A redstone signal keeps the light off. Checked once a
 * second, on a per-bed offset.
 */
public class SolarPlantBedBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public static class PlantBedEnergyStorage extends EnergyStorage {
        PlantBedEnergyStorage(int capacity) {
            super(capacity, 0, 0);
        }
        void generate(int amount) {
            energy = Math.min(capacity, energy + amount);
        }
        void use(int amount) {
            energy = Math.max(0, energy - amount);
        }
        void setStored(int amount) {
            energy = Math.max(0, Math.min(amount, capacity));
        }
    }

    public final PlantBedEnergyStorage energyStorage = new PlantBedEnergyStorage(Config.solarPlantBedBuffer);
    private int generating = 0;
    // What the client was last sent, so an idle bed doesn't send updates every second.
    private int syncedEnergy = -1, syncedGenerating = -1;

    public SolarPlantBedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (Math.floorMod(level.getGameTime() + worldPosition.hashCode(), 20) != 0) return;

        // The panel sits on top of the plant's cell, so the sky is checked from there.
        float base = SolarHelper.byPhase(level, worldPosition, Config.solarPlantBedMorningFe, Config.solarPlantBedNoonFe);
        generating = Math.round(base * SolarHelper.outputMultiplier(level, worldPosition));
        if (generating > 0) {
            energyStorage.generate(generating * 20);
            setChanged();
        }

        int perSecond = Config.solarPlantBedFePerTick * 20;
        boolean lit = !SolarHelper.isDaytime(level, worldPosition)
                && !level.hasNeighborSignal(worldPosition)
                && energyStorage.getEnergyStored() >= perSecond;
        if (lit && perSecond > 0) {
            energyStorage.use(perSecond);
            setChanged();
        }
        BlockState state = getBlockState();
        if (state.getValue(SolarPlantBedBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(SolarPlantBedBlock.LIT, lit), Block.UPDATE_ALL);

        if (lit) PlantGrowth.boost(serverLevel, List.of(worldPosition.above()), Config.solarPlantBedGrowthBoost);

        if (energyStorage.getEnergyStored() != syncedEnergy || generating != syncedGenerating) {
            syncedEnergy = energyStorage.getEnergyStored();
            syncedGenerating = generating;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.solar_plant_bed_header").forGoggles(tooltip);

        CreateLang.translate("solar_punk.tooltip.generating")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(generating))
                        .withStyle(generating > 0 ? ChatFormatting.GREEN : ChatFormatting.RED))
                .forGoggles(tooltip, 1);

        int usage = getBlockState().getValue(SolarPlantBedBlock.LIT) ? Config.solarPlantBedFePerTick : 0;
        CreateLang.translate("solar_punk.tooltip.consumption")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(usage)).withStyle(ChatFormatting.GOLD))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.stored")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.abbreviate(energyStorage.getEnergyStored())
                                + " / " + EnergyFormat.fe(energyStorage.getMaxEnergyStored()))
                        .withStyle(ChatFormatting.AQUA))
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
        tag.putInt("generating", generating);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setStored(tag.getInt("energy"));
        generating = tag.getInt("generating");
    }
}
