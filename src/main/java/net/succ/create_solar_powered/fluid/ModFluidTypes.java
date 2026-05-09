package net.succ.create_solar_powered.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.succ.create_solar_powered.Create_solar_powered;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Create_solar_powered.MODID);

    public static final ResourceLocation MOLTEN_SALT_STILL =
            ResourceLocation.fromNamespaceAndPath(Create_solar_powered.MODID, "block/molten_salt_still");
    public static final ResourceLocation MOLTEN_SALT_FLOW =
            ResourceLocation.fromNamespaceAndPath(Create_solar_powered.MODID, "block/molten_salt_flow");
    public static final ResourceLocation WATER_OVERLAY =
            ResourceLocation.parse("block/water_overlay");

    public static final Supplier<FluidType> MOLTEN_SALT_TYPE = FLUID_TYPES.register("molten_salt",
            () -> new BaseFluidType(
                    MOLTEN_SALT_STILL,
                    MOLTEN_SALT_FLOW,
                    WATER_OVERLAY,
                    0xFFFFFF99,
                    new Vector3f(255f / 255f, 255f / 255f, 153f / 255f),
                    FluidType.Properties.create()
                            .density(3000)
                            .viscosity(6000)
                            .temperature(2100)
            ));

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}