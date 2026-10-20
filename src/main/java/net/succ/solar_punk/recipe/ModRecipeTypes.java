package net.succ.solar_punk.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
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
            type("solar_heating");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SolarHeaterRecipe>> SOLAR_HEATING_SERIALIZER =
            serializer("solar_heating", SolarHeaterRecipe.CODEC, SolarHeaterRecipe.STREAM_CODEC);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CrystallizerRecipe>> CRYSTALLIZING =
            type("crystallizing");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrystallizerRecipe>> CRYSTALLIZING_SERIALIZER =
            serializer("crystallizing", CrystallizerRecipe.CODEC, CrystallizerRecipe.STREAM_CODEC);

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> type(String name) {
        return RECIPE_TYPES.register(name, () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, name)));
    }

    private static <T extends Recipe<?>> DeferredHolder<RecipeSerializer<?>, RecipeSerializer<T>> serializer(
            String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return RECIPE_SERIALIZERS.register(name, () -> new RecipeSerializer<>() {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodec;
            }
        });
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
