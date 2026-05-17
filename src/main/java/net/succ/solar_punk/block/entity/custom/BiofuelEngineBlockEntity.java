package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;

public class BiofuelEngineBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    private static final float RPM      = 16f;
    private static final float CAPACITY = 256f;
    public static final int FUEL_CAPACITY  = 8000;
    public static final int CONSUME_MB     = 50;
    public static final int CONSUME_PERIOD = 40;

    public final FluidTank biofuelTank = new FluidTank(FUEL_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.BIOFUEL_SOURCE.get());
        }
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    public BiofuelEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        return biofuelTank.getFluidAmount() > 0 ? RPM : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return biofuelTank.getFluidAmount() > 0 ? CAPACITY : 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        if (level.getGameTime() % CONSUME_PERIOD == 0 && biofuelTank.getFluidAmount() >= CONSUME_MB)
            biofuelTank.drain(CONSUME_MB, IFluidHandler.FluidAction.EXECUTE);

        if (level.getGameTime() % 20 == 0) {
            updateGeneratedRotation();
            boolean active = biofuelTank.getFluidAmount() > 0;
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(BiofuelEngineBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(BiofuelEngineBlock.LIT, active), 3);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (!biofuelTank.isEmpty()) {
            CompoundTag t = new CompoundTag();
            t.putString("Fluid", BuiltInRegistries.FLUID.getKey(biofuelTank.getFluid().getFluid()).toString());
            t.putInt("Amount", biofuelTank.getFluidAmount());
            tag.put("BiofuelTank", t);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("BiofuelTank")) {
            CompoundTag t = tag.getCompound("BiofuelTank");
            Fluid f = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(t.getString("Fluid"))).orElse(Fluids.EMPTY);
            if (f != Fluids.EMPTY) biofuelTank.setFluid(new FluidStack(f, t.getInt("Amount")));
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.biofuel_engine_header").forGoggles(tooltip);
        CreateLang.translate("solar_punk.tooltip.biofuel")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(biofuelTank.getFluidAmount())
                        .text(" / " + FUEL_CAPACITY + " mB").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.consumption")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(CONSUME_MB).text(" mB / " + CONSUME_PERIOD + "t").style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);
        return true;
    }
}
