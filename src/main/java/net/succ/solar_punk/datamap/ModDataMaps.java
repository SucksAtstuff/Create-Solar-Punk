package net.succ.solar_punk.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.succ.solar_punk.SolarPunk;
import org.jetbrains.annotations.Nullable;

/**
 * Values pack makers can change with a datapack ({@code data/<ns>/data_maps/<fluid|item>/<name>.json})
 * or with KubeJS ({@code SolarpunkEvents.turbineFuels(event => ...)}, declared in
 * {@code offbyonelib.kubejs.json}). Defaults come from ModDataMapProvider. Synced, so Goggles and
 * client-side checks see the same values.
 */
public final class ModDataMaps {

    /** A fluid the Steam Turbine burns: 1 mB of it counts as {@code steamValue} mB of Steam. */
    public record TurbineFuel(float steamValue) {
        public static final Codec<TurbineFuel> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExtraCodecs.POSITIVE_FLOAT.fieldOf("steam_value").forGetter(TurbineFuel::steamValue)
        ).apply(i, TurbineFuel::new));
    }

    /** An item the Biomass Gasifier burns, for {@code burnTicks}, leaving {@code biochar} Biochar. */
    public record GasifierFuel(int burnTicks, int biochar) {
        public static final Codec<GasifierFuel> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("burn_ticks").forGetter(GasifierFuel::burnTicks),
                Codec.intRange(0, 64).optionalFieldOf("biochar", 1).forGetter(GasifierFuel::biochar)
        ).apply(i, GasifierFuel::new));
    }

    /** A fluid the Biofuel Engine burns: a bucket of it runs the engine for {@code ticksPerBucket}. */
    public record EngineFuel(int ticksPerBucket) {
        public static final Codec<EngineFuel> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("ticks_per_bucket").forGetter(EngineFuel::ticksPerBucket)
        ).apply(i, EngineFuel::new));
    }

    /** A fluid the Heat Battery stores heat from: {@code heatPerMb} heat per mB. */
    public record HeatBatteryFluid(int heatPerMb) {
        public static final Codec<HeatBatteryFluid> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("heat_per_mb").forGetter(HeatBatteryFluid::heatPerMb)
        ).apply(i, HeatBatteryFluid::new));
    }

    public static final DataMapType<Fluid, TurbineFuel> TURBINE_FUELS =
            fluidMap("turbine_fuels", TurbineFuel.CODEC);
    public static final DataMapType<Item, GasifierFuel> GASIFIER_FUELS =
            DataMapType.builder(id("gasifier_fuels"), Registries.ITEM, GasifierFuel.CODEC)
                    .synced(GasifierFuel.CODEC, false).build();
    public static final DataMapType<Fluid, EngineFuel> ENGINE_FUELS =
            fluidMap("engine_fuels", EngineFuel.CODEC);
    public static final DataMapType<Fluid, HeatBatteryFluid> HEAT_BATTERY_FLUIDS =
            fluidMap("heat_battery_fluids", HeatBatteryFluid.CODEC);

    private ModDataMaps() {}

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(TURBINE_FUELS);
        event.register(GASIFIER_FUELS);
        event.register(ENGINE_FUELS);
        event.register(HEAT_BATTERY_FLUIDS);
    }

    @Nullable
    public static TurbineFuel turbineFuel(FluidStack stack) {
        return stack.isEmpty() ? null : stack.getFluidHolder().getData(TURBINE_FUELS);
    }

    @Nullable
    public static GasifierFuel gasifierFuel(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getItemHolder().getData(GASIFIER_FUELS);
    }

    @Nullable
    public static EngineFuel engineFuel(FluidStack stack) {
        return stack.isEmpty() ? null : stack.getFluidHolder().getData(ENGINE_FUELS);
    }

    @Nullable
    public static HeatBatteryFluid heatBatteryFluid(FluidStack stack) {
        return stack.isEmpty() ? null : stack.getFluidHolder().getData(HEAT_BATTERY_FLUIDS);
    }

    private static <T> DataMapType<Fluid, T> fluidMap(String name, Codec<T> codec) {
        return DataMapType.builder(id(name), Registries.FLUID, codec).synced(codec, false).build();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, name);
    }
}
