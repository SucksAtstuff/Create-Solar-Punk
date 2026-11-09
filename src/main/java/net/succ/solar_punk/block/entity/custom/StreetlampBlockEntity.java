package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.StreetlampBlock;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.util.SolarHelper;

import java.util.List;

/**
 * Lower half of a Streetlamp. Takes FE from any side (a cable, or a Brass Solar Panel or
 * Solar Glass pushing into it) and lights up from dusk to dawn while it has enough stored
 * for the next second. Checked once a second, on a per-lamp offset so a street of lamps
 * doesn't all switch on the same tick.
 */
public class StreetlampBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public static class LampEnergyStorage extends EnergyStorage {
        LampEnergyStorage(int capacity, int maxReceive) {
            super(capacity, maxReceive, 0);
        }
        void use(int amount) {
            energy = Math.max(0, energy - amount);
        }
        void setStored(int amount) {
            energy = Math.max(0, Math.min(amount, capacity));
        }
    }

    public final LampEnergyStorage energyStorage = new LampEnergyStorage(Config.streetlampBuffer, Config.streetlampMaxReceive);
    // What the client was last sent, so an idle lamp doesn't send updates every second.
    private int syncedEnergy = -1;

    public StreetlampBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (Math.floorMod(level.getGameTime() + worldPosition.hashCode(), 20) != 0) return;

        int perSecond = Config.streetlampFePerTick * 20;
        boolean lit = !SolarHelper.isDaytime(level, worldPosition)
                && energyStorage.getEnergyStored() >= perSecond;
        if (lit && perSecond > 0) {
            energyStorage.use(perSecond);
            setChanged();
        }
        setLit(lit);

        if (energyStorage.getEnergyStored() != syncedEnergy) {
            syncedEnergy = energyStorage.getEnergyStored();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    // Both halves carry LIT: the upper one gives off the light, the lower one picks the model.
    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (state.getValue(StreetlampBlock.LIT) == lit) return;
        level.setBlock(worldPosition, state.setValue(StreetlampBlock.LIT, lit), Block.UPDATE_ALL);
        BlockPos above = worldPosition.above();
        BlockState upper = level.getBlockState(above);
        if (upper.is(state.getBlock()))
            level.setBlock(above, upper.setValue(StreetlampBlock.LIT, lit), Block.UPDATE_ALL);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.streetlamp_header").forGoggles(tooltip);

        int usage = getBlockState().getValue(StreetlampBlock.LIT) ? Config.streetlampFePerTick : 0;
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setStored(tag.getInt("energy"));
    }
}
