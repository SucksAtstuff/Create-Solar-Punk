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
import net.succ.create_solar_powered.recipe.ModRecipeTypes;
import net.succ.create_solar_powered.recipe.SolarHeaterRecipe;

import java.util.Optional;

public class SolarHeaterBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {

    public static final int MAX_PROGRESS = 200;
    public static final int TANK_CAPACITY = 8000;

    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (level == null) return true;
            return level.getRecipeManager()
                    .getAllRecipesFor(ModRecipeTypes.SOLAR_HEATING.get())
                    .stream()
                    .anyMatch(r -> r.value().ingredient().test(stack));
        }

        @Override
        public int getSlotLimit(int slot) { return 1; }

        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    public final FluidTank fluidTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    private int progress = 0;

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

        if (!isSunShining()) {
            if (progress > 0) { progress = 0; setChanged(); }
            if (currentlyLit) setLit(false);
            return;
        }

        Optional<SolarHeaterRecipe> recipeOpt = findRecipe();
        if (recipeOpt.isEmpty()) {
            if (progress > 0) { progress = 0; setChanged(); }
            if (currentlyLit) setLit(false);
            return;
        }

        FluidStack output = recipeOpt.get().result();
        if (fluidTank.getFluidAmount() + output.getAmount() > TANK_CAPACITY) {
            if (currentlyLit) setLit(false);
            return;
        }

        if (!currentlyLit) setLit(true);

        if (level.isRaining() && level.getGameTime() % 2 != 0) return;

        progress++;
        setChanged();

        if (progress >= MAX_PROGRESS) {
            fluidTank.fill(output.copy(), IFluidHandler.FluidAction.EXECUTE);
            itemHandler.extractItem(0, 1, false);
            progress = 0;
            setChanged();
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
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        if (tag.contains("FluidTank")) {
            CompoundTag fluidTag = tag.getCompound("FluidTank");
            ResourceLocation fluidId = ResourceLocation.parse(fluidTag.getString("Fluid"));
            Fluid f = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(Fluids.EMPTY);
            if (f != Fluids.EMPTY) {
                fluidTank.setFluid(new FluidStack(f, fluidTag.getInt("Amount")));
            }
        }
    }

    public int getProgress() { return progress; }
}
