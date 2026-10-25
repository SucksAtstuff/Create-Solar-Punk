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
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Particle Accelerator recipe: any number of item ingredients (with counts) in the
 * Accelerator Input -> one result. The ring has to be at least {@code minDiameter} wide,
 * spinning at {@code minSpeed} RPM or more. {@code stress} is the SU it draws at minimum
 * speed on a ring of exactly the minimum diameter; bigger rings draw proportionally more.
 * Datapacks can add more.
 */
public record ParticleAcceleratorRecipe(
        List<SizedIngredient> ingredients,
        ItemStack result,
        int minDiameter,
        float stress,
        int minSpeed,
        int processingTime
) implements Recipe<ParticleAcceleratorRecipe.Input> {

    public static final MapCodec<ParticleAcceleratorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SizedIngredient.FLAT_CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(ParticleAcceleratorRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(ParticleAcceleratorRecipe::result),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("min_diameter").forGetter(ParticleAcceleratorRecipe::minDiameter),
            Codec.floatRange(0, Float.MAX_VALUE).fieldOf("stress").forGetter(ParticleAcceleratorRecipe::stress),
            Codec.intRange(1, 256).fieldOf("min_speed").forGetter(ParticleAcceleratorRecipe::minSpeed),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("processing_time").forGetter(ParticleAcceleratorRecipe::processingTime)
    ).apply(instance, ParticleAcceleratorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleAcceleratorRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), ParticleAcceleratorRecipe::ingredients,
                    ItemStack.STREAM_CODEC, ParticleAcceleratorRecipe::result,
                    ByteBufCodecs.VAR_INT, ParticleAcceleratorRecipe::minDiameter,
                    ByteBufCodecs.FLOAT, ParticleAcceleratorRecipe::stress,
                    ByteBufCodecs.VAR_INT, ParticleAcceleratorRecipe::minSpeed,
                    ByteBufCodecs.VAR_INT, ParticleAcceleratorRecipe::processingTime,
                    ParticleAcceleratorRecipe::new
            );

    /** The items in the Accelerator Input's input slots. */
    public record Input(List<ItemStack> items) implements RecipeInput {
        @Override public ItemStack getItem(int index) { return items.get(index); }
        @Override public int size() { return items.size(); }
    }

    /** Stress impact per RPM on a ring of the given diameter (what Create's network uses). */
    public float stressImpactFor(int diameter) {
        return stress / minSpeed * diameter / (float) minDiameter;
    }

    /**
     * Whether the items cover every ingredient's count, like a Basin: any slots, any
     * order. Each item is only counted once, against the first ingredient that wants it.
     */
    @Override
    public boolean matches(Input input, Level level) {
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : input.items())
            if (!stack.isEmpty()) remaining.add(stack.copy());
        for (SizedIngredient ingredient : ingredients) {
            int needed = ingredient.count();
            for (ItemStack stack : remaining) {
                if (needed <= 0) break;
                if (stack.isEmpty() || !ingredient.ingredient().test(stack)) continue;
                int taken = Math.min(needed, stack.getCount());
                stack.shrink(taken);
                needed -= taken;
            }
            if (needed > 0) return false;
        }
        return true;
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
        return ModRecipeTypes.PARTICLE_ACCELERATOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.PARTICLE_ACCELERATOR.get();
    }
}
