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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.LithiumBrineExtractorBlock;
import net.succ.solar_punk.item.ModItems;

import java.util.List;

// Renewable Lithium floor (see plan_for_fusion.md's Fuel chain section): Water + Salt
// slowly -> Lithium Dust, with a small chance of a Beryllium Dust bonus alongside it.
// Kinetic-powered like the Biofilter/Deuterium Extractor - needs a shaft into its
// bottom face and stalls without one. Deliberately much slower than mining Lithium Ore
// - this exists so a player who's stripped every vein in reach is never hard-blocked,
// not as a replacement for mining.
public class LithiumBrineExtractorBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

    // Slot 0: Salt input. Slot 1: Lithium Dust output. Slot 2: Beryllium Dust bonus
    // output. Neither output slot accepts external insertion.
    public final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && stack.is(ModItems.SALT.get());
        }

        @Override
        public int getSlotLimit(int slot) { return 64; }

        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    public final FluidTank waterTank = new FluidTank(Config.lithiumBrineExtractorTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(Fluids.WATER);
        }

        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    private int progress = 0;

    public LithiumBrineExtractorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float calculateStressApplied() {
        float impact = Config.lithiumBrineExtractorSu;
        this.lastStressApplied = impact;
        return impact;
    }

    public boolean isPowered() {
        return Math.abs(getSpeed()) > 0;
    }

    private boolean canOutputLithium() {
        ItemStack current = itemHandler.getStackInSlot(1);
        return current.isEmpty()
                || (current.is(ModItems.LITHIUM_DUST.get()) && current.getCount() < current.getMaxStackSize());
    }

    private boolean canOutputBeryllium() {
        ItemStack current = itemHandler.getStackInSlot(2);
        return current.isEmpty()
                || (current.is(ModItems.BERYLLIUM_DUST.get()) && current.getCount() < current.getMaxStackSize());
    }

    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        // Unconditional, before the canRun branch below can return early - otherwise
        // topping off just the water tank (with no Salt loaded yet, or while unpowered)
        // never syncs to the client and the goggle tooltip shows stale tank contents.
        if (level.getGameTime() % 20 == 0) sendData();

        boolean canRun = isPowered()
                && waterTank.getFluidAmount() >= Config.lithiumBrineExtractorWaterPerCycle
                && !itemHandler.getStackInSlot(0).isEmpty()
                && canOutputLithium();

        boolean currentlyLit = getBlockState().getValue(LithiumBrineExtractorBlock.LIT);

        if (!canRun) {
            if (progress > 0) { progress = 0; setChanged(); }
            if (currentlyLit) setLit(false);
            return;
        }

        if (!currentlyLit) setLit(true);
        progress++;
        setChanged();

        if (progress >= Config.lithiumBrineExtractorCycleTicks) {
            progress = 0;
            waterTank.drain(Config.lithiumBrineExtractorWaterPerCycle, IFluidHandler.FluidAction.EXECUTE);
            itemHandler.extractItem(0, 1, false);
            outputLithium();
            if (canOutputBeryllium() && level.getRandom().nextInt(100) < Config.lithiumBrineExtractorBerylliumChance)
                outputBeryllium();
            setChanged();
        }

        if (level.getGameTime() % 20 == 0) sendData();
    }

    private void outputLithium() {
        ItemStack current = itemHandler.getStackInSlot(1);
        if (current.isEmpty()) itemHandler.setStackInSlot(1, new ItemStack(ModItems.LITHIUM_DUST.get()));
        else itemHandler.setStackInSlot(1, current.copyWithCount(current.getCount() + 1));
    }

    private void outputBeryllium() {
        ItemStack current = itemHandler.getStackInSlot(2);
        if (current.isEmpty()) itemHandler.setStackInSlot(2, new ItemStack(ModItems.BERYLLIUM_DUST.get()));
        else itemHandler.setStackInSlot(2, current.copyWithCount(current.getCount() + 1));
    }

    private void setLit(boolean lit) {
        if (level == null) return;
        level.setBlock(worldPosition, getBlockState().setValue(LithiumBrineExtractorBlock.LIT, lit), 3);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.lithium_brine_extractor_header").forGoggles(tooltip);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        CreateLang.translate("solar_punk.tooltip.lithium_brine_extractor_progress")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(progress).text(" / " + Config.lithiumBrineExtractorCycleTicks).style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.lithium_brine_extractor_water")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(waterTank.getFluidAmount())
                        .text(" / " + waterTank.getCapacity() + " mB")
                        .style(ChatFormatting.AQUA)
                        .component())
                .forGoggles(tooltip, 1);

        ItemStack salt = itemHandler.getStackInSlot(0);
        if (!salt.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.lithium_brine_extractor_salt")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(salt.getCount()).style(ChatFormatting.WHITE).component())
                    .forGoggles(tooltip, 1);
        }

        ItemStack lithium = itemHandler.getStackInSlot(1);
        if (!lithium.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.lithium_brine_extractor_output")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(lithium.getCount()).style(ChatFormatting.WHITE).component())
                    .forGoggles(tooltip, 1);
        }

        ItemStack beryllium = itemHandler.getStackInSlot(2);
        if (!beryllium.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.lithium_brine_extractor_bonus")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(beryllium.getCount()).style(ChatFormatting.WHITE).component())
                    .forGoggles(tooltip, 1);
        }

        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("Progress", progress);
        FluidTankNBTHelper.save(tag, "WaterTank", waterTank);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        FluidTankNBTHelper.load(tag, "WaterTank", waterTank);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
}
