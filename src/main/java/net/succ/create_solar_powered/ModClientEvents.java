package net.succ.create_solar_powered;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.succ.create_solar_powered.block.entity.ModBlockEntities;

@EventBusSubscriber(modid = Create_solar_powered.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("removal")
public class ModClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ANDESITE_SOLAR_PANEL.get(), KineticBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KINETIC_BATTERY.get(), KineticBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
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
