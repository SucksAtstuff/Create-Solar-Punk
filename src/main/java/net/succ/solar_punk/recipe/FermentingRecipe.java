package net.succ.solar_punk.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Fermentation Vat recipe: one {@code ingredient} and {@code water} mB of Water -> {@code result},
 * all per footprint block; the Vat scales them by its width and height. {@code aerobic} recipes
 * (composting) take Oxygen and run faster with it. Datapacks and KubeJS can add more.
 */
public record FermentingRecipe(
        Ingredient ingredient,
        FluidStack result,
        int water,
        int processingTime,
        boolean aerobic
) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<FermentingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(FermentingRecipe::ingredient),
            FluidStack.CODEC.fieldOf("result").forGetter(FermentingRecipe::result),
            Codec.intRange(0, 1_000_000).optionalFieldOf("water", 1000).forGetter(FermentingRecipe::water),
            Codec.intRange(1, 1_000_000).optionalFieldOf("processing_time", 400).forGetter(FermentingRecipe::processingTime),
            Codec.BOOL.optionalFieldOf("aerobic", false).forGetter(FermentingRecipe::aerobic)
    ).apply(instance, FermentingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, FermentingRecipe::ingredient,
                    FluidStack.STREAM_CODEC, FermentingRecipe::result,
                    ByteBufCodecs.VAR_INT, FermentingRecipe::water,
                    ByteBufCodecs.VAR_INT, FermentingRecipe::processingTime,
                    ByteBufCodecs.BOOL, FermentingRecipe::aerobic,
                    FermentingRecipe::new
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
        return ModRecipeTypes.FERMENTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FERMENTING.get();
    }
}
