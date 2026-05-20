package net.succ.solar_punk;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.client.model.FermentationVatModel;
import net.succ.solar_punk.client.model.ModSpriteShifts;
import net.succ.solar_punk.client.model.SolarPowerTowerModel;
import net.succ.solar_punk.client.renderer.FermentationVatRenderer;
import net.succ.solar_punk.client.renderer.GeyserCapRenderer;
import net.succ.solar_punk.client.renderer.SolarPowerTowerRenderer;
import net.succ.solar_punk.compat.ponder.SolarPunkPonderPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = SolarPunk.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("removal")
public class ModClientEvents {

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Touching GAUGE here ensures FermentationVatRenderer is class-loaded and the
        // PartialModel is in PartialModel.ALL before Flywheel's handler iterates it.
        // The explicit register() call guarantees baking regardless of handler order.
        event.register(ModelResourceLocation.standalone(FermentationVatRenderer.GAUGE.modelLocation()));
        event.register(ModelResourceLocation.standalone(FermentationVatRenderer.GAUGE_DIAL.modelLocation()));
        event.register(ModelResourceLocation.standalone(SolarPowerTowerRenderer.GAUGE.modelLocation()));
        event.register(ModelResourceLocation.standalone(SolarPowerTowerRenderer.GAUGE_DIAL.modelLocation()));
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        List<ModelResourceLocation> vatKeys   = new ArrayList<>();
        List<ModelResourceLocation> towerKeys = new ArrayList<>();
        for (ModelResourceLocation key : models.keySet()) {
            ResourceLocation id = key.id();
            if (!id.getNamespace().equals(SolarPunk.MODID)) continue;
            if (id.getPath().equals("fermentation_vat"))   vatKeys.add(key);
            if (id.getPath().equals("solar_power_tower"))  towerKeys.add(key);
        }
        for (ModelResourceLocation key : vatKeys)
            models.put(key, new FermentationVatModel(models.get(key)));
        for (ModelResourceLocation key : towerKeys)
            models.put(key, new SolarPowerTowerModel(models.get(key)));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.GEYSER_CAP.get(), GeyserCapRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FERMENTATION_VAT.get(), FermentationVatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_POWER_TOWER.get(), SolarPowerTowerRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModSpriteShifts.init();
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
