package net.succ.solar_punk.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.PipeShape;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, SolarPunk.MODID);

    // Which shape a Particle Accelerator Pipe item places. Missing means straight.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PipeShape>> PIPE_SHAPE =
            DATA_COMPONENTS.registerComponentType("pipe_shape", builder -> builder
                    .persistent(PipeShape.CODEC)
                    .networkSynchronized(PipeShape.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
