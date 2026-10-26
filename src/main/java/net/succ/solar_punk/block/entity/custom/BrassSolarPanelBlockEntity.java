package net.succ.solar_punk.block.entity.custom;

import net.succ.solar_punk.client.sound.MachineSoundInstance;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.client.sound.BrassPanelSoundInstance;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.util.SolarHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrassSolarPanelBlockEntity extends BlockEntity implements IHaveGoggleInformation {

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

    public final GeneratingEnergyStorage energyStorage = new GeneratingEnergyStorage(Config.brassBuffer, Config.brassMaxExtract);

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private BrassPanelSoundInstance soundInstance;

    // Last tick's output, synced for the goggle tooltip. The client can't always work it
    // out itself: in Cosmonautics deep space the sun position is server-only data.
    private int lastGenerated = 0;

    public BrassSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private int getGeneratedFE() {
        float base = SolarHelper.byPhase(level, worldPosition, Config.brassMorningFe, Config.brassNoonFe);
        if (base == 0) return 0;
        return Math.round(base * SolarHelper.outputMultiplier(level, worldPosition));
    }

    public void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
            return;
        }

        int generated = getGeneratedFE();
        lastGenerated = generated;
        if (generated > 0) {
            energyStorage.generate(generated);
            setChanged();
        }

        if (energyStorage.getEnergyStored() > 0) {
            // Config.brassMaxExtract is a total per-tick cap shared across all faces, so a
            // panel touching several receivers can't multiply its output.
            int remainingBudget = Config.brassMaxExtract;
            for (Direction dir : Direction.values()) {
                if (remainingBudget <= 0) break;
                IEnergyStorage neighbor = level.getCapability(
                        Capabilities.EnergyStorage.BLOCK,
                        worldPosition.relative(dir),
                        dir.getOpposite()
                );
                if (neighbor != null && neighbor.canReceive()) {
                    int toSend = energyStorage.extractEnergy(remainingBudget, true);
                    int accepted = neighbor.receiveEnergy(toSend, false);
                    if (accepted > 0) {
                        energyStorage.extractEnergy(accepted, false);
                        remainingBudget -= accepted;
                        setChanged();
                    }
                }
            }
        }

        if (level.getGameTime() % 20 == 0) {
            boolean active = generated > 0;
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof BrassSolarPanelBlock)) return;
            if (state.getValue(BrassSolarPanelBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(BrassSolarPanelBlock.LIT, active), 3);
            level.sendBlockUpdated(worldPosition, state, level.getBlockState(worldPosition), 2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        soundInstance = MachineSoundInstance.keepPlaying(level, soundInstance,
                getBlockState().getValue(BrassSolarPanelBlock.LIT), () -> new BrassPanelSoundInstance(this));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.fe_header").forGoggles(tooltip);

        int generated = lastGenerated;
        CreateLang.translate("solar_punk.tooltip.generating")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(generated))
                        .withStyle(generated > 0 ? ChatFormatting.GREEN : ChatFormatting.RED))
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
        tag.putInt("generated", lastGenerated);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setStored(tag.getInt("energy"));
        lastGenerated = tag.getInt("generated");
    }
}