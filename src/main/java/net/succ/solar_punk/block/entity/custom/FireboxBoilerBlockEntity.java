package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.FireboxBoilerBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;

public class FireboxBoilerBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    // Slot 0: fuel input. Accepts anything a vanilla furnace would (coal, charcoal,
    // logs/planks, lava buckets, blaze rods, Biochar, ...) - no custom fuel tag needed.
    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return AbstractFurnaceBlockEntity.isFuel(stack);
        }

        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    // Output tank for Steam. No external insertion - only this BE's tick() fills it.
    public final FluidTank steamTank = new FluidTank(Config.fireboxBoilerTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.STEAM_SOURCE.get());
        }

        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // Input tank for water, filled via bucket or piped in from any side.
    public final FluidTank waterTank = new FluidTank(Config.fireboxBoilerTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(Fluids.WATER);
        }

        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // Routes fill(water) → waterTank, drain() → steamTank. Exposed on every side -
    // unlike the Solar Heater there's no sky check to protect, so no face is blocked.
    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? steamTank.getFluid() : waterTank.getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            return tank == 0 ? steamTank.getCapacity() : waterTank.getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 ? steamTank.isFluidValid(stack) : waterTank.isFluidValid(stack);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid().isSame(Fluids.WATER)) return waterTank.fill(resource, action);
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return steamTank.drain(resource, action);
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return steamTank.drain(maxDrain, action);
        }
    };

    private int burnTimeRemaining = 0;

    public FireboxBoilerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean wasLit = getBlockState().getValue(FireboxBoilerBlock.LIT);
        int rate = Config.fireboxBoilerSteamPerTick;
        boolean hasWater = waterTank.getFluidAmount() >= rate;
        boolean hasRoom  = steamTank.getFluidAmount() + rate <= steamTank.getCapacity();

        // Light a fresh piece of fuel, but only once it can actually be put to use -
        // no point burning coal with no water to boil or nowhere for the steam to go.
        if (burnTimeRemaining <= 0 && hasWater && hasRoom) {
            ItemStack fuel = itemHandler.getStackInSlot(0);
            // getBurnTime(null): same "generic" recipe type vanilla's own isFuel() checks
            // against - respects modded fuel values, unlike the deprecated static fuel map.
            int ticks = fuel.isEmpty() ? 0 : fuel.getBurnTime(null);
            if (ticks > 0) {
                itemHandler.extractItem(0, 1, false);
                burnTimeRemaining = ticks;
                setChanged();
            }
        }

        boolean lit = burnTimeRemaining > 0;
        if (lit && hasWater && hasRoom) {
            burnTimeRemaining--;
            waterTank.drain(rate, IFluidHandler.FluidAction.EXECUTE);
            steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), rate), IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }

        if (lit != wasLit)
            level.setBlock(worldPosition, getBlockState().setValue(FireboxBoilerBlock.LIT, lit), 3);

        if (level.getGameTime() % 20 == 0)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("BurnTime", burnTimeRemaining);
        FluidTankNBTHelper.save(tag, "SteamTank", steamTank);
        FluidTankNBTHelper.save(tag, "WaterTank", waterTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        burnTimeRemaining = tag.getInt("BurnTime");
        FluidTankNBTHelper.load(tag, "SteamTank", steamTank);
        FluidTankNBTHelper.load(tag, "WaterTank", waterTank);
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
        CreateLang.translate("solar_punk.tooltip.firebox_boiler_header").forGoggles(tooltip);

        ItemStack fuel = itemHandler.getStackInSlot(0);
        if (!fuel.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.fuel")
                    .style(ChatFormatting.GRAY)
                    .add(fuel.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                    .forGoggles(tooltip, 1);
        }
        if (burnTimeRemaining > 0) {
            CreateLang.translate("solar_punk.tooltip.burn_time")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(burnTimeRemaining).text(" t").style(ChatFormatting.YELLOW).component())
                    .forGoggles(tooltip, 1);
        }

        CreateLang.translate("solar_punk.tooltip.water")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(waterTank.getFluidAmount())
                        .text(" / " + Config.fireboxBoilerTank + " mB")
                        .style(ChatFormatting.AQUA)
                        .component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.steam")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(steamTank.getFluidAmount())
                        .text(" / " + Config.fireboxBoilerTank + " mB")
                        .style(ChatFormatting.AQUA)
                        .component())
                .forGoggles(tooltip, 1);

        return true;
    }
}
