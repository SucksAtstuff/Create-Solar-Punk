package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.CrystallizerBlock;
import net.succ.solar_punk.recipe.CrystallizerRecipe;
import net.succ.solar_punk.recipe.ModRecipeTypes;
import net.succ.solar_punk.sound.ModSounds;

import java.util.List;
import java.util.Optional;

/**
 * Generic two-fluids-in, item-plus-optional-fluid-out machine. Everything about what it
 * accepts and produces comes from data-driven {@link CrystallizerRecipe}s - this class only
 * knows "tank A", "tank B", and "the byproduct tank", never a specific fluid or item.
 */
public class CrystallizerBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    // Slot 0: recipe result output. No external insertion - this block only ever produces
    // an item, never consumes one.
    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return false; }

        @Override
        public int getSlotLimit(int slot) { return 64; }

        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    public final FluidTank inputTankA = new FluidTank(Config.crystallizerTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return matchesKnownInputA(stack); }
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    public final FluidTank inputTankB = new FluidTank(Config.crystallizerTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return matchesKnownInputB(stack); }
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // Byproduct output. Must still validate true for whatever fluid a recipe declares (the
    // tank's own fill() checks isFluidValid, including our internal fill in tick()) -
    // external-fill blocking happens one layer up, in combinedFluidHandler.fill(), which
    // never routes anything external into this tank.
    public final FluidTank outputFluidTank = new FluidTank(Config.crystallizerTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return matchesKnownByproduct(stack); }
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // Routes fill(matches some recipe's input A) -> inputTankA, fill(matches input B) ->
    // inputTankB, drain() -> outputFluidTank. No sun/facing requirement, so every face is
    // safe to expose to pipes.
    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 3; }

        @Override public FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> inputTankA.getFluid();
                case 1 -> inputTankB.getFluid();
                default -> outputFluidTank.getFluid();
            };
        }

        @Override public int getTankCapacity(int tank) {
            return switch (tank) {
                case 0 -> inputTankA.getCapacity();
                case 1 -> inputTankB.getCapacity();
                default -> outputFluidTank.getCapacity();
            };
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return switch (tank) {
                case 0 -> inputTankA.isFluidValid(stack);
                case 1 -> inputTankB.isFluidValid(stack);
                default -> false;
            };
        }

        // A fluid that's input A for one recipe and input B for another is inherently
        // ambiguous - it's routed to tank A first. Fine for a first pass; pack authors
        // sharing a fluid across both roles should expect that.
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (matchesKnownInputA(resource)) return inputTankA.fill(resource, action);
            if (matchesKnownInputB(resource)) return inputTankB.fill(resource, action);
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return outputFluidTank.drain(resource, action);
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return outputFluidTank.drain(maxDrain, action);
        }
    };

    private int progress = 0;

    public CrystallizerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // -------------------------------------------------------------------------
    // Recipe lookup
    // -------------------------------------------------------------------------

    private boolean matchesKnownInputA(FluidStack stack) {
        if (level == null) return true; // permissive before the world is known, same as SolarHeaterBlockEntity
        return level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRYSTALLIZING.get()).stream()
                .anyMatch(r -> !r.value().inputA().isEmpty() && r.value().inputA().getFluid().isSame(stack.getFluid()));
    }

    private boolean matchesKnownInputB(FluidStack stack) {
        if (level == null) return true;
        return level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRYSTALLIZING.get()).stream()
                .anyMatch(r -> !r.value().inputB().isEmpty() && r.value().inputB().getFluid().isSame(stack.getFluid()));
    }

    private boolean matchesKnownByproduct(FluidStack stack) {
        if (level == null) return true;
        return level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CRYSTALLIZING.get()).stream()
                .anyMatch(r -> !r.value().byproduct().isEmpty() && r.value().byproduct().getFluid().isSame(stack.getFluid()));
    }

    private Optional<CrystallizerRecipe> findRecipe() {
        if (level == null) return Optional.empty();
        CrystallizerRecipe.Input input = new CrystallizerRecipe.Input(inputTankA.getFluid(), inputTankB.getFluid());
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.CRYSTALLIZING.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(r -> r.matches(input, level))
                .findFirst();
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    private boolean canOutputResult(CrystallizerRecipe recipe) {
        ItemStack current = itemHandler.getStackInSlot(0);
        return current.isEmpty()
                || (ItemStack.isSameItemSameComponents(current, recipe.result())
                        && current.getCount() + recipe.result().getCount() <= current.getMaxStackSize());
    }

    private boolean byproductHasRoom(CrystallizerRecipe recipe) {
        return outputFluidTank.getFluidAmount() + recipe.byproduct().getAmount() <= outputFluidTank.getCapacity();
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        Optional<CrystallizerRecipe> recipeOpt = findRecipe();
        if (recipeOpt.isEmpty()) {
            if (progress > 0) { progress = 0; setChanged(); }
            setLit(false);
            return;
        }
        CrystallizerRecipe recipe = recipeOpt.get();

        boolean canRun = inputTankA.getFluidAmount() >= recipe.inputA().getAmount()
                && (recipe.inputB().isEmpty() || inputTankB.getFluidAmount() >= recipe.inputB().getAmount())
                && canOutputResult(recipe)
                && (recipe.byproduct().isEmpty() || byproductHasRoom(recipe));

        if (!canRun) {
            if (progress > 0) { progress = 0; setChanged(); }
            setLit(false);
            return;
        }

        setLit(true);
        progress++;
        setChanged();

        if (progress >= recipe.processingTime()) {
            progress = 0;
            inputTankA.drain(recipe.inputA().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            if (!recipe.inputB().isEmpty())
                inputTankB.drain(recipe.inputB().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            if (!recipe.byproduct().isEmpty())
                outputFluidTank.fill(recipe.byproduct().copy(), IFluidHandler.FluidAction.EXECUTE);
            outputResult(recipe);
            setChanged();

            // One-shot quench hiss right as a cycle actually completes, not a looped
            // ambient hum - the Crystallizer is a repeated dunk-and-quench cycle, not a
            // continuously running machine, so the sound should punctuate each cycle the
            // same way geyser_puff punctuates each geyser burst rather than droning
            // throughout the whole processingTime(). Server-side playSound(null, ...)
            // broadcasts to every nearby client, not just a local-only sound.
            level.playSound(null, worldPosition, ModSounds.STEAM_HISS.get(), SoundSource.BLOCKS,
                    1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        }

        if (level.getGameTime() % 20 == 0)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    private void outputResult(CrystallizerRecipe recipe) {
        ItemStack current = itemHandler.getStackInSlot(0);
        if (current.isEmpty()) {
            itemHandler.setStackInSlot(0, recipe.result().copy());
        } else {
            itemHandler.setStackInSlot(0, current.copyWithCount(current.getCount() + recipe.result().getCount()));
        }
    }

    private void setLit(boolean lit) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.getBlock() instanceof CrystallizerBlock && state.getValue(CrystallizerBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(CrystallizerBlock.LIT, lit), 3);
        }
    }

    // -------------------------------------------------------------------------
    // Goggle tooltip
    // -------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.crystallizer_header").forGoggles(tooltip);

        CreateLang.translate("solar_punk.tooltip.crystallizer_progress")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(progress).style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);

        if (!inputTankA.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.crystallizer_input_a")
                    .style(ChatFormatting.GRAY)
                    .add(inputTankA.getFluid().getHoverName().copy().withStyle(ChatFormatting.GOLD))
                    .forGoggles(tooltip, 1);
            CreateLang.number(inputTankA.getFluidAmount())
                    .text(" / " + inputTankA.getCapacity() + " mB")
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 2);
        }

        if (!inputTankB.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.crystallizer_input_b")
                    .style(ChatFormatting.GRAY)
                    .add(inputTankB.getFluid().getHoverName().copy().withStyle(ChatFormatting.GOLD))
                    .forGoggles(tooltip, 1);
            CreateLang.number(inputTankB.getFluidAmount())
                    .text(" / " + inputTankB.getCapacity() + " mB")
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 2);
        }

        if (!outputFluidTank.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.crystallizer_byproduct")
                    .style(ChatFormatting.GRAY)
                    .add(outputFluidTank.getFluid().getHoverName().copy().withStyle(ChatFormatting.YELLOW))
                    .forGoggles(tooltip, 1);
            CreateLang.number(outputFluidTank.getFluidAmount())
                    .text(" / " + outputFluidTank.getCapacity() + " mB")
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 2);
        }

        ItemStack output = itemHandler.getStackInSlot(0);
        if (!output.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.crystallizer_item_output")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(output.getCount())
                            .text(" " + output.getHoverName().getString())
                            .style(ChatFormatting.WHITE).component())
                    .forGoggles(tooltip, 1);
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("Progress", progress);
        FluidTankNBTHelper.save(tag, "InputTankA", inputTankA);
        FluidTankNBTHelper.save(tag, "InputTankB", inputTankB);
        FluidTankNBTHelper.save(tag, "OutputFluidTank", outputFluidTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        FluidTankNBTHelper.load(tag, "InputTankA", inputTankA);
        FluidTankNBTHelper.load(tag, "InputTankB", inputTankB);
        FluidTankNBTHelper.load(tag, "OutputFluidTank", outputFluidTank);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
