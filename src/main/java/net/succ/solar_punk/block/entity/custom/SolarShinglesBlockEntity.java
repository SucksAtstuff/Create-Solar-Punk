package net.succ.solar_punk.block.entity.custom;

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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.SolarShinglesBlock;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.util.SolarHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Every shingle generates into its own buffer. Energy flows from a shingle to touching shingles
 * that hold less, so when one is drained (by a cable pulling from it, or by it pushing into a
 * neighbouring receiver) the rest of the roof flows towards it.
 */
public class SolarShinglesBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    private static class GeneratingEnergyStorage extends EnergyStorage {
        GeneratingEnergyStorage(int capacity, int maxExtract) {
            super(capacity, 0, maxExtract);
        }
        void generate(int amount) {
            energy = Math.min(energy + amount, capacity);
        }
        void setStored(int amount) {
            energy = Math.max(0, Math.min(amount, capacity));
        }
    }

    // A roof steps up one block every two, so the next row up can be diagonal (one over, one
    // up). The six faces plus the eight sideways-and-up/down cells cover every way two
    // shingles of a roof can touch.
    private static final List<BlockPos> ROOF_NEIGHBOURS = new ArrayList<>();
    static {
        for (Direction dir : Direction.values()) ROOF_NEIGHBOURS.add(BlockPos.ZERO.relative(dir));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            ROOF_NEIGHBOURS.add(BlockPos.ZERO.relative(dir).above());
            ROOF_NEIGHBOURS.add(BlockPos.ZERO.relative(dir).below());
        }
    }

    // The roof rises one block every two, so the panels are tilted by atan(1/2), facing downhill.
    private static final double TILT = Math.atan(0.5);
    // The most a well-angled shingle makes compared to a flat one, so low sun can't spike it.
    private static final double MAX_TILT_FACTOR = 1.5;

    public final GeneratingEnergyStorage energyStorage =
            new GeneratingEnergyStorage(Config.shingleBuffer, Config.shingleMaxExtract);

    // Recomputed once a second, and synced for the goggle tooltip (the sun position can be
    // server-only data in space).
    private int generating = 0;
    // What the client was last sent, so an idle roof doesn't send updates every second.
    private int syncedEnergy = -1;

    public SolarShinglesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private int computeGenerating() {
        float base = SolarHelper.byPhase(level, worldPosition, Config.shingleMorningFe, Config.shingleNoonFe);
        if (base == 0) return 0;
        Direction downhill = getBlockState().getValue(SolarShinglesBlock.FACING).getOpposite();
        Vec3 normal = new Vec3(downhill.getStepX() * Math.sin(TILT), Math.cos(TILT), downhill.getStepZ() * Math.sin(TILT));
        double tilt = SolarHelper.tiltFactor(level, worldPosition, normal, MAX_TILT_FACTOR);
        return (int) Math.round(base * tilt * SolarHelper.outputMultiplier(level, worldPosition));
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean second = level.getGameTime() % 20 == 0;
        int wasGenerating = generating;
        if (second) generating = computeGenerating();
        if (generating > 0) {
            energyStorage.generate(generating);
            setChanged();
        }

        shareWithRoof();
        pushToReceivers();

        if (second && (generating != wasGenerating || energyStorage.getEnergyStored() != syncedEnergy)) {
            syncedEnergy = energyStorage.getEnergyStored();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    // Hands energy to touching shingles that hold less: a quarter of the difference each, so
    // a drained shingle pulls the roof's energy towards it without the levels oscillating.
    private void shareWithRoof() {
        for (BlockPos offset : ROOF_NEIGHBOURS) {
            int mine = energyStorage.getEnergyStored();
            if (mine <= 0) return;
            BlockPos pos = worldPosition.offset(offset);
            if (!level.isLoaded(pos)) continue;
            if (!(level.getBlockEntity(pos) instanceof SolarShinglesBlockEntity other)) continue;
            int theirs = other.energyStorage.getEnergyStored();
            int room = other.energyStorage.getMaxEnergyStored() - theirs;
            int amount = Math.min((mine - theirs) / 4, room);
            if (amount <= 0) continue;
            energyStorage.setStored(mine - amount);
            other.energyStorage.setStored(theirs + amount);
            other.setChanged();
            setChanged();
        }
    }

    // Like the Brass Solar Panel: pushes into touching receivers that aren't shingles, with
    // max_extract as the total per tick across all faces.
    private void pushToReceivers() {
        int budget = Math.min(Config.shingleMaxExtract, energyStorage.getEnergyStored());
        for (Direction dir : Direction.values()) {
            if (budget <= 0) return;
            BlockPos pos = worldPosition.relative(dir);
            if (!level.isLoaded(pos) || level.getBlockEntity(pos) instanceof SolarShinglesBlockEntity) continue;
            IEnergyStorage neighbour = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, dir.getOpposite());
            if (neighbour == null || !neighbour.canReceive()) continue;
            int accepted = neighbour.receiveEnergy(energyStorage.extractEnergy(budget, true), false);
            if (accepted > 0) {
                energyStorage.extractEnergy(accepted, false);
                budget -= accepted;
                setChanged();
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.fe_header").forGoggles(tooltip);

        CreateLang.translate("solar_punk.tooltip.generating")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(generating))
                        .withStyle(generating > 0 ? ChatFormatting.GREEN : ChatFormatting.RED))
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
        tag.putInt("generated", generating);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setStored(tag.getInt("energy"));
        generating = tag.getInt("generated");
    }
}
