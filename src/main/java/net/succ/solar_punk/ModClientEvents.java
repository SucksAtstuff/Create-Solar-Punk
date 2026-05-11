package net.succ.solar_punk;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.compat.ponder.SolarPunkPonderPlugin;

@EventBusSubscriber(modid = SolarPunk.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("removal")
public class ModClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PonderIndex.addPlugin(new SolarPunkPonderPlugin());

            VisualizerRegistry.setVisualizer(
                ModBlockEntities.ANDESITE_SOLAR_PANEL.get(),
                new SimpleBlockEntityVisualizer<>(
                    (context, blockEntity, partialTick) -> new OrientedRotatingVisual<>(
                        context, blockEntity, partialTick,
                        Direction.SOUTH, Direction.DOWN,
                        Models.partial(AllPartialModels.SHAFT_HALF)
                    ),
                    be -> false
                )
            );
            VisualizerRegistry.setVisualizer(
                ModBlockEntities.KINETIC_BATTERY.get(),
                new SimpleBlockEntityVisualizer<>(SingleAxisRotatingVisual::shaft, be -> false)
            );
        });
    }
}
