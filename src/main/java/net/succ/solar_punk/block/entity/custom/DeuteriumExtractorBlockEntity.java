package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.DeuteriumExtractorBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;

// Water -> Deuterium converter (see plan_for_fusion.md's Fuel chain section). Kinetic-
// powered, like the Biofilter: needs a shaft into its bottom face and stalls without
// one, rather than running as a free passive trickle. Unlike the Solar Heater, needs no
// sunlight - deuterium extraction happens in a sealed tank, not on a rooftop.
public class DeuteriumExtractorBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

    public final FluidTank waterTank = new FluidTank(Config.deuteriumExtractorTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(Fluids.WATER);
        }

        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // Output-only in practice - combinedFluidHandler.fill() never routes anything into
    // it, matching SolarHeaterBlockEntity's fluidTank convention.
    public final FluidTank deuteriumTank = new FluidTank(Config.deuteriumExtractorTank) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? deuteriumTank.getFluid() : waterTank.getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            return tank == 0 ? deuteriumTank.getCapacity() : waterTank.getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 ? deuteriumTank.isFluidValid(stack) : waterTank.isFluidValid(stack);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid().isSame(Fluids.WATER)) return waterTank.fill(resource, action);
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return deuteriumTank.drain(resource, action);
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return deuteriumTank.drain(maxDrain, action);
        }
    };

    private int progress = 0;

    public DeuteriumExtractorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float calculateStressApplied() {
        float impact = Config.deuteriumExtractorSu;
        this.lastStressApplied = impact;
        return impact;
    }

    public boolean isPowered() {
        return Math.abs(getSpeed()) > 0;
    }

    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        boolean canRun = isPowered()
                && waterTank.getFluidAmount() >= Config.deuteriumExtractorWaterPerCycle
                && deuteriumTank.getFluidAmount() + Config.deuteriumExtractorDeuteriumPerCycle <= deuteriumTank.getCapacity();

        boolean currentlyLit = getBlockState().getValue(DeuteriumExtractorBlock.LIT);

        if (!canRun) {
            if (progress > 0) { progress = 0; setChanged(); }
            if (currentlyLit) setLit(false);
            return;
        }

        if (!currentlyLit) setLit(true);
        progress++;
        setChanged();

        if (progress >= Config.deuteriumExtractorCycleTicks) {
            progress = 0;
            waterTank.drain(Config.deuteriumExtractorWaterPerCycle, IFluidHandler.FluidAction.EXECUTE);
            deuteriumTank.fill(new FluidStack(ModFluids.DEUTERIUM_SOURCE.get(), Config.deuteriumExtractorDeuteriumPerCycle),
                    IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }

        if (level.getGameTime() % 20 == 0) sendData();
    }

    private void setLit(boolean lit) {
        if (level == null) return;
        level.setBlock(worldPosition, getBlockState().setValue(DeuteriumExtractorBlock.LIT, lit), 3);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.deuterium_extractor_header").forGoggles(tooltip);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        CreateLang.translate("solar_punk.tooltip.deuterium_extractor_progress")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(progress).text(" / " + Config.deuteriumExtractorCycleTicks).style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.deuterium_extractor_water")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(waterTank.getFluidAmount())
                        .text(" / " + waterTank.getCapacity() + " mB")
                        .style(ChatFormatting.AQUA)
                        .component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.deuterium_extractor_output")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(deuteriumTank.getFluidAmount())
                        .text(" / " + deuteriumTank.getCapacity() + " mB")
                        .style(ChatFormatting.AQUA)
                        .component())
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Progress", progress);
        FluidTankNBTHelper.save(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.save(tag, "DeuteriumTank", deuteriumTank);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        progress = tag.getInt("Progress");
        FluidTankNBTHelper.load(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.load(tag, "DeuteriumTank", deuteriumTank);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
}