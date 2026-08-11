package net.succ.solar_punk.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Generic "cool/quench two fluids into an item, plus an optional fluid byproduct" recipe,
 * used by the Crystallizer block. Not tied to Molten Salt at all - inputB and byproduct
 * are both optional (pass FluidStack.EMPTY to omit them), so this same recipe type covers
 * anything shaped like "Fluid A (+ optional Fluid B) -> Item (+ optional Fluid C)".
 * Datapacks can add more recipes of this type without any code changes.
 */
public record CrystallizerRecipe(
        FluidStack inputA,
        FluidStack inputB,
        ItemStack result,
        FluidStack byproduct,
        int processingTime
) implements Recipe<CrystallizerRecipe.Input> {

    public static final MapCodec<CrystallizerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidStack.CODEC.fieldOf("input_a").forGetter(CrystallizerRecipe::inputA),
            FluidStack.CODEC.optionalFieldOf("input_b", FluidStack.EMPTY).forGetter(CrystallizerRecipe::inputB),
            ItemStack.CODEC.fieldOf("result").forGetter(CrystallizerRecipe::result),
            FluidStack.CODEC.optionalFieldOf("byproduct", FluidStack.EMPTY).forGetter(CrystallizerRecipe::byproduct),
            com.mojang.serialization.Codec.INT.fieldOf("processing_time").forGetter(CrystallizerRecipe::processingTime)
    ).apply(instance, CrystallizerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystallizerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    FluidStack.OPTIONAL_STREAM_CODEC, CrystallizerRecipe::inputA,
                    FluidStack.OPTIONAL_STREAM_CODEC, CrystallizerRecipe::inputB,
                    ItemStack.OPTIONAL_STREAM_CODEC, CrystallizerRecipe::result,
                    FluidStack.OPTIONAL_STREAM_CODEC, CrystallizerRecipe::byproduct,
                    ByteBufCodecs.VAR_INT, CrystallizerRecipe::processingTime,
                    CrystallizerRecipe::new
            );

    /** Not slot-based like a crafting grid - just the two fluids currently sitting in the block's tanks. */
    public record Input(FluidStack fluidA, FluidStack fluidB) implements RecipeInput {
        @Override public ItemStack getItem(int index) { return ItemStack.EMPTY; }
        @Override public int size() { return 0; }
    }

    @Override
    public boolean matches(Input input, Level level) {
        if (inputA.isEmpty() || !input.fluidA().getFluid().isSame(inputA.getFluid())) return false;
        return inputB.isEmpty() || input.fluidB().getFluid().isSame(inputB.getFluid());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CRYSTALLIZING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CRYSTALLIZING.get();
    }
}
