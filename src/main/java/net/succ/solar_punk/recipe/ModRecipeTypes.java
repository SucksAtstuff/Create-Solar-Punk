package net.succ.solar_punk.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, SolarPunk.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, SolarPunk.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<SolarHeaterRecipe>> SOLAR_HEATING =
            RECIPE_TYPES.register("solar_heating", () ->
                    RecipeType.simple(ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "solar_heating")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SolarHeaterRecipe>> SOLAR_HEATING_SERIALIZER =
            RECIPE_SERIALIZERS.register("solar_heating", () -> new RecipeSerializer<>() {
                @Override
                public MapCodec<SolarHeaterRecipe> codec() {
                    return SolarHeaterRecipe.CODEC;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, SolarHeaterRecipe> streamCodec() {
                    return SolarHeaterRecipe.STREAM_CODEC;
                }
            });

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
