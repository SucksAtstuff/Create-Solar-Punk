package net.succ.solar_punk.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public record SolarHeaterRecipe(Ingredient ingredient, FluidStack result) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<SolarHeaterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SolarHeaterRecipe::ingredient),
            FluidStack.CODEC.fieldOf("result").forGetter(SolarHeaterRecipe::result)
    ).apply(instance, SolarHeaterRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SolarHeaterRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, SolarHeaterRecipe::ingredient,
                    FluidStack.OPTIONAL_STREAM_CODEC, SolarHeaterRecipe::result,
                    SolarHeaterRecipe::new
            );

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SOLAR_HEATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SOLAR_HEATING.get();
    }
}
