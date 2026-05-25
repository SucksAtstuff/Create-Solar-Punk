package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;

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
        }

        // Decay: heat slowly dissipates whether or not a boiler is attached
        if (heatStored > 0) {
            heatStored = Math.max(0, heatStored - Config.heatBatteryHeatDecay);
            changed = true;
        }

        int newHeat = heatStored <= 0 ? 0 : heatStored >= Config.heatBatteryMaxHeat / 4 ? 2 : 1;
        if (newHeat != getBlockState().getValue(HeatBatteryBlock.HEAT)) {
            level.setBlock(worldPosition, getBlockState().setValue(HeatBatteryBlock.HEAT, newHeat), 3);
        }

        if (changed) setChanged();

        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    // Returns a BoilerHeater heat value:
    //   2 = superheated (>= 50% charge)
    //   1 = heated      (any charge)
    //  -1 = no heat     (empty)
    public int getHeatLevel() {
        if (heatStored <= 0) return -1;
        if (heatStored >= Config.heatBatteryMaxHeat / 4) return 2;
        return 1;
    }

    public int getHeatStored() { return heatStored; }

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

        int level = getHeatLevel();
        Component levelText;
        if (level == 2) {
            levelText = Component.literal("Superheated").withStyle(ChatFormatting.RED);
        } else if (level == 1) {
            levelText = Component.literal("Heated").withStyle(ChatFormatting.YELLOW);
        } else {
            levelText = Component.literal("None").withStyle(ChatFormatting.DARK_GRAY);
        }
        CreateLang.translate("solar_punk.tooltip.heat_level")
                .style(ChatFormatting.GRAY)
                .add(levelText)
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.heat_stored")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(heatStored)
                        .text(" / " + Config.heatBatteryMaxHeat)
                        .style(ChatFormatting.GOLD)
                        .component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.molten_salt")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(fluidTank.getFluidAmount())
                        .text(" / " + Config.heatBatteryTank + " mB")
                        .style(ChatFormatting.AQUA)
                        .component())
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("HeatStored", heatStored);
        FluidStack fluid = fluidTank.getFluid();
        if (!fluid.isEmpty()) {
            CompoundTag fluidTag = new CompoundTag();
            fluidTag.putString("Fluid", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            fluidTag.putInt("Amount", fluid.getAmount());
            tag.put("FluidTank", fluidTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heatStored = tag.getInt("HeatStored");
        if (tag.contains("FluidTank")) {
            CompoundTag fluidTag = tag.getCompound("FluidTank");
            ResourceLocation fluidId = ResourceLocation.parse(fluidTag.getString("Fluid"));
            Fluid f = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(Fluids.EMPTY);
            if (f != Fluids.EMPTY) {
                fluidTank.setFluid(new FluidStack(f, fluidTag.getInt("Amount")));
            }
        }
    }
}