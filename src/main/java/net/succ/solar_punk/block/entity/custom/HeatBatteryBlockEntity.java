package net.succ.solar_punk.block.entity.custom;

import net.succ.solar_punk.util.GoggleHelper;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.compat.newage.NewAgeHeat;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;
import net.succ.solar_punk.util.FluidTankNBTHelper;

public class HeatBatteryBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public final FluidTank fluidTank = new FluidTank(Config.heatBatteryTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.MOLTEN_SALT_SOURCE.get());
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private int heatStored = 0;
    private boolean advancementFired = false;

    public HeatBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        // Charge: consume 1 mB of molten salt and convert to heat
        if (!fluidTank.isEmpty() && heatStored < Config.heatBatteryMaxHeat) {
            fluidTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            heatStored = Math.min(Config.heatBatteryMaxHeat, heatStored + Config.heatBatteryHeatPerMb);
            changed = true;
            if (!advancementFired) {
                advancementFired = true;
                ModTriggers.fireNearby(level, worldPosition, ModTriggers.HEAT_STORED);
            }
        }

        // Decay: heat slowly dissipates whether or not a boiler is attached
        if (heatStored > 0) {
            heatStored = Math.max(0, heatStored - Config.heatBatteryHeatDecay);
            changed = true;
        }

        int newHeat = Math.max(0, getHeatLevel());
        if (newHeat != getBlockState().getValue(HeatBatteryBlock.HEAT)) {
            level.setBlock(worldPosition, getBlockState().setValue(HeatBatteryBlock.HEAT, newHeat), 3);
        }

        if (afterCharge()) changed = true;
        if (changed) setChanged();

        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    // Returns a BoilerHeater heat value:
    //   2 = superheated (>= 25% charge)
    //   1 = heated      (any charge)
    //  -1 = no heat     (empty)
    public int getHeatLevel() {
        if (heatStored <= 0) return -1;
        if (heatStored >= Config.heatBatteryMaxHeat / 4) return 2;
        return 1;
    }

    public int getHeatStored() { return heatStored; }

    /** The heat level as shown on goggles and Display Links. */
    public MutableComponent getHeatLevelText() {
        return switch (getHeatLevel()) {
            case 2 -> Component.literal("Superheated").withStyle(ChatFormatting.RED);
            case 1 -> Component.literal("Heated").withStyle(ChatFormatting.YELLOW);
            default -> Component.literal("None").withStyle(ChatFormatting.DARK_GRAY);
        };
    }

    /** Hook for subclasses, run each server tick after charging and decay. Returns whether heat changed. */
    protected boolean afterCharge() { return false; }

    /** Adds (or with a negative amount, takes) heat, kept within the battery's capacity. */
    public void addHeat(int amount) {
        heatStored = Math.max(0, Math.min(Config.heatBatteryMaxHeat, heatStored + amount));
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
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.heat_battery_header").forGoggles(tooltip);

        CreateLang.translate("solar_punk.tooltip.heat_level")
                .style(ChatFormatting.GRAY)
                .add(getHeatLevelText())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.heat_stored")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(heatStored)
                        .text(" / " + Config.heatBatteryMaxHeat)
                        .style(ChatFormatting.GOLD)
                        .component())
                .forGoggles(tooltip, 1);

        if (NewAgeHeat.LOADED) {
            CreateLang.translate("solar_punk.tooltip.new_age_temperature")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(Math.round(NewAgeHeat.temperature(this)))
                            .text(" / " + Config.heatBatteryNewAgeMaxTemperature)
                            .style(ChatFormatting.GOLD)
                            .component())
                    .forGoggles(tooltip, 1);
        }

        GoggleHelper.tank(tooltip, "molten_salt", fluidTank, ChatFormatting.AQUA);

        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("HeatStored", heatStored);
        FluidTankNBTHelper.save(tag, "FluidTank", fluidTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heatStored = tag.getInt("HeatStored");
        FluidTankNBTHelper.load(tag, "FluidTank", fluidTank);
    }
}