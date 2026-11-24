package net.succ.solar_punk.recipe;

import com.mojang.serialization.Codec;
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
 * Solar Heater evaporation: {@code ingredient} (a fluid and how many mB) dries into {@code result}
 * in the sun over {@code processingTime} ticks. Water -> Salt by default; datapacks and KubeJS can
 * add more. The heater's input tank takes any fluid one of these recipes uses.
 */
public record EvaporatingRecipe(FluidStack ingredient, ItemStack result, int processingTime)
        implements Recipe<EvaporatingRecipe.Input> {

    public static final MapCodec<EvaporatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidStack.CODEC.fieldOf("ingredient").forGetter(EvaporatingRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(EvaporatingRecipe::result),
            Codec.intRange(1, 1_000_000).optionalFieldOf("processing_time", 200).forGetter(EvaporatingRecipe::processingTime)
    ).apply(instance, EvaporatingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EvaporatingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    FluidStack.STREAM_CODEC, EvaporatingRecipe::ingredient,
                    ItemStack.STREAM_CODEC, EvaporatingRecipe::result,
                    ByteBufCodecs.VAR_INT, EvaporatingRecipe::processingTime,
                    EvaporatingRecipe::new
            );

    /** The fluid in the Solar Heater's input tank. */
    public record Input(FluidStack fluid) implements RecipeInput {
        @Override public ItemStack getItem(int index) { return ItemStack.EMPTY; }
        @Override public int size() { return 0; }
    }

    /** Whether this recipe uses the fluid (whatever the amount). */
    public boolean uses(FluidStack fluid) {
        return !fluid.isEmpty() && fluid.getFluid().isSame(ingredient.getFluid());
    }

    @Override
    public boolean matches(Input input, Level level) {
        return uses(input.fluid());
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
        return ModRecipeTypes.EVAPORATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.EVAPORATING.get();
    }
}
