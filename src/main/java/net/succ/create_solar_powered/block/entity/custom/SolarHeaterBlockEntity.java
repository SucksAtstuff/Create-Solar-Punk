package net.succ.create_solar_powered.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.create_solar_powered.block.custom.SolarHeaterBlock;
import net.succ.create_solar_powered.item.ModItems;
import net.succ.create_solar_powered.recipe.ModRecipeTypes;
import net.succ.create_solar_powered.recipe.SolarHeaterRecipe;

import java.util.Optional;

public class SolarHeaterBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {

    public static final int MAX_PROGRESS = 200;
    public static final int TANK_CAPACITY = 8000;
    public static final int EVAPORATION_TIME = 200;
    public static final int WATER_PER_SALT = 250;

    // Slot 0: item melting input. Slot 1: evaporation salt output (no external insertion).
    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 1) return false;
            if (level == null) return true;
            return level.getRecipeManager()
                    .getAllRecipesFor(ModRecipeTypes.SOLAR_HEATING.get())
                    .stream()
                    .anyMatch(r -> r.value().ingredient().test(stack));
        }

        @Override
        public int getSlotLimit(int slot) { return slot == 0 ? 1 : 64; }

        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    // Output tank for melted fluids (molten salt, lava, etc.)
    public final FluidTank fluidTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // Input tank for water evaporation. Exposed on all non-top sides via combinedFluidHandler.
    public final FluidTank waterTank = new FluidTank(TANK_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(Fluids.WATER);
        }

        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // Routes fill(water) → waterTank, drain() → fluidTank. Exposed on all non-top faces.
    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? fluidTank.getFluid() : waterTank.getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            return tank == 0 ? fluidTank.getCapacity() : waterTank.getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 ? fluidTank.isFluidValid(stack) : waterTank.isFluidValid(stack);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid().isSame(Fluids.WATER)) return waterTank.fill(resource, action);
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return fluidTank.drain(resource, action);
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return fluidTank.drain(maxDrain, action);
        }
    };

    private int progress = 0;
    private int evaporationProgress = 0;

    public SolarHeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private Optional<SolarHeaterRecipe> findRecipe() {
        if (level == null) return Optional.empty();
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (stack.isEmpty()) return Optional.empty();
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.SOLAR_HEATING.get())
                .stream()
                .filter(r -> r.value().matches(input, level))
                .map(RecipeHolder::value)
                .findFirst();
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean currentlyLit = getBlockState().getValue(SolarHeaterBlock.LIT);
        boolean sunShining = isSunShining();

        if (!sunShining) {
            if (progress > 0) { progress = 0; setChanged(); }
            if (evaporationProgress > 0) { evaporationProgress = 0; setChanged(); }
            if (currentlyLit) setLit(false);
            return;
        }

        boolean rainPenalty = level.isRaining() && level.getGameTime() % 2 != 0;
        boolean shouldBeLit = false;

        // --- Item melting ---
        Optional<SolarHeaterRecipe> recipeOpt = findRecipe();
        if (recipeOpt.isPresent()) {
            FluidStack output = recipeOpt.get().result();
            if (fluidTank.getFluidAmount() + output.getAmount() <= TANK_CAPACITY) {
                shouldBeLit = true;
                if (!rainPenalty) {
                    progress++;
                    setChanged();
                    if (progress >= MAX_PROGRESS) {
                        fluidTank.fill(output.copy(), IFluidHandler.FluidAction.EXECUTE);
                        itemHandler.extractItem(0, 1, false);
                        progress = 0;
                        setChanged();
                    }
                }
            } else {
                if (progress > 0) { progress = 0; setChanged(); }
            }
        } else {
            if (progress > 0) { progress = 0; setChanged(); }
        }

        // --- Water evaporation ---
        if (waterTank.getFluidAmount() >= WATER_PER_SALT && canOutputSalt()) {
            shouldBeLit = true;
            if (!rainPenalty) {
                evaporationProgress++;
                setChanged();
                if (evaporationProgress >= EVAPORATION_TIME) {
                    waterTank.drain(WATER_PER_SALT, IFluidHandler.FluidAction.EXECUTE);
                    outputSalt();
                    evaporationProgress = 0;
                    setChanged();
                }
            }
        } else {
            if (evaporationProgress > 0) { evaporationProgress = 0; setChanged(); }
        }

        if (shouldBeLit != currentlyLit) setLit(shouldBeLit);
    }

    private boolean canOutputSalt() {
        ItemStack current = itemHandler.getStackInSlot(1);
        return current.isEmpty()
                || (current.is(ModItems.SALT.get()) && current.getCount() < current.getMaxStackSize());
    }

    private void outputSalt() {
        ItemStack current = itemHandler.getStackInSlot(1);
        if (current.isEmpty()) {
            itemHandler.setStackInSlot(1, new ItemStack(ModItems.SALT.get()));
        } else {
            itemHandler.setStackInSlot(1, current.copyWithCount(current.getCount() + 1));
        }
    }

    private boolean isSunShining() {
        if (level == null) return false;
        long time = level.getDayTime() % 24000;
        return time < 12000 && !level.isThundering() && level.canSeeSky(worldPosition.above());
    }

    private void setLit(boolean lit) {
        if (level == null) return;
        level.setBlock(worldPosition, getBlockState().setValue(SolarHeaterBlock.LIT, lit), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("EvaporationProgress", evaporationProgress);

        FluidStack fluid = fluidTank.getFluid();
        if (!fluid.isEmpty()) {
            CompoundTag fluidTag = new CompoundTag();
            fluidTag.putString("Fluid", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            fluidTag.putInt("Amount", fluid.getAmount());
            tag.put("FluidTank", fluidTag);
        }

        FluidStack water = waterTank.getFluid();
        if (!water.isEmpty()) {
            CompoundTag waterTag = new CompoundTag();
            waterTag.putString("Fluid", BuiltInRegistries.FLUID.getKey(water.getFluid()).toString());
            waterTag.putInt("Amount", water.getAmount());
            tag.put("WaterTank", waterTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        evaporationProgress = tag.getInt("EvaporationProgress");

        if (tag.contains("FluidTank")) {
            CompoundTag fluidTag = tag.getCompound("FluidTank");
            ResourceLocation fluidId = ResourceLocation.parse(fluidTag.getString("Fluid"));
            Fluid f = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(Fluids.EMPTY);
            if (f != Fluids.EMPTY) fluidTank.setFluid(new FluidStack(f, fluidTag.getInt("Amount")));
        }

        if (tag.contains("WaterTank")) {
            CompoundTag waterTag = tag.getCompound("WaterTank");
            ResourceLocation fluidId = ResourceLocation.parse(waterTag.getString("Fluid"));
            Fluid f = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(Fluids.EMPTY);
            if (f != Fluids.EMPTY) waterTank.setFluid(new FluidStack(f, waterTag.getInt("Amount")));
        }
    }

    public int getProgress() { return progress; }
    public int getEvaporationProgress() { return evaporationProgress; }
}