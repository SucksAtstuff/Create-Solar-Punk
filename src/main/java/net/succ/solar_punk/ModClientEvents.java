package net.succ.solar_punk;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.KineticSprinklerItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.client.model.FermentationVatModel;
import net.succ.solar_punk.client.model.ModSpriteShifts;
import net.succ.solar_punk.client.model.SolarPowerTowerModel;
import net.succ.solar_punk.client.model.TurbineCasingGlassModel;
import net.succ.solar_punk.client.model.TurbineCasingModel;
import net.succ.solar_punk.client.renderer.AndesiteSolarPanelRenderer;
import net.succ.solar_punk.client.renderer.BiofilterRenderer;
import net.succ.solar_punk.client.renderer.FermentationVatRenderer;
import net.succ.solar_punk.client.renderer.GeyserCapRenderer;
import net.succ.solar_punk.client.renderer.KineticBatteryRenderer;
import net.succ.solar_punk.client.renderer.KineticSprinklerRenderer;
import net.succ.solar_punk.client.renderer.SolarPowerTowerRenderer;
import net.succ.solar_punk.client.renderer.TurbineRotorRenderer;
import net.succ.solar_punk.compat.ponder.SolarPunkPonderPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber(modid = SolarPunk.MODID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        KineticSprinklerItem item = (KineticSprinklerItem) ModBlocks.KINETIC_SPRINKLER.get().asItem();
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return GeoRenderProvider.of(item).getGeoItemRenderer();
            }
        }, item);
    }

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
        List<ModelResourceLocation> vatKeys    = new ArrayList<>();
        List<ModelResourceLocation> towerKeys  = new ArrayList<>();
        List<ModelResourceLocation> casingKeys = new ArrayList<>();
        List<ModelResourceLocation> glassKeys  = new ArrayList<>();
        for (ModelResourceLocation key : models.keySet()) {
            ResourceLocation id = key.id();
            if (!id.getNamespace().equals(SolarPunk.MODID)) continue;
            if (id.getPath().equals("fermentation_vat"))   vatKeys.add(key);
            if (id.getPath().equals("solar_power_tower"))  towerKeys.add(key);
            if (id.getPath().equals("turbine_casing"))       casingKeys.add(key);
            if (id.getPath().equals("turbine_casing_glass")) glassKeys.add(key);
        }
        for (ModelResourceLocation key : vatKeys)
            models.put(key, new FermentationVatModel(models.get(key)));
        for (ModelResourceLocation key : towerKeys)
            models.put(key, new SolarPowerTowerModel(models.get(key)));
        for (ModelResourceLocation key : casingKeys)
            models.put(key, new TurbineCasingModel(models.get(key)));
        for (ModelResourceLocation key : glassKeys)
            models.put(key, new TurbineCasingGlassModel(models.get(key)));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.GEYSER_CAP.get(), GeyserCapRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURBINE_ROTOR.get(), TurbineRotorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ANDESITE_SOLAR_PANEL.get(), AndesiteSolarPanelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KINETIC_BATTERY.get(), KineticBatteryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BIOFILTER.get(), BiofilterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FERMENTATION_VAT.get(), FermentationVatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_POWER_TOWER.get(), SolarPowerTowerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KINETIC_SPRINKLER.get(), KineticSprinklerRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModSpriteShifts.init();
            PonderIndex.addPlugin(new SolarPunkPonderPlugin());
            registerTooltips();

            SimpleBlockEntityVisualizer.builder(ModBlockEntities.TURBINE_ROTOR.get())
                    .factory(SingleAxisRotatingVisual::shaft)
                    .neverSkipVanillaRender()
                    .apply();
        });
    }

    private static void registerTooltips() {
        tip(ModBlocks.SOLAR_HEATER::get);
        tip(ModBlocks.ANDESITE_SOLAR_PANEL::get);
        tip(ModBlocks.BRASS_SOLAR_PANEL::get);
        tip(ModBlocks.HEAT_BATTERY::get);
        tip(ModBlocks.KINETIC_BATTERY::get);
        tip(ModBlocks.FERMENTATION_VAT::get);
        tip(ModBlocks.BIOMASS_GASIFIER::get);
        tip(ModBlocks.BIOFUEL_ENGINE::get);
        tip(ModBlocks.GEYSER_CAP::get);
        tip(ModBlocks.SOLAR_POWER_TOWER::get);
        tip(ModBlocks.SOLAR_MIRROR::get);
        tip(ModBlocks.BIOFILTER::get);
        tip(ModBlocks.KINETIC_SPRINKLER::get);
        tip(ModBlocks.TURBINE_ROTOR::get);
    }

    private static void tip(Supplier<? extends net.minecraft.world.level.block.Block> block) {
        Item item = block.get().asItem();
        TooltipModifier modifier = new ItemDescription.Modifier(item, Palette.STANDARD_CREATE);
        KineticStats stats = KineticStats.create(item);
        if (stats != null) modifier = modifier.andThen(stats);
        TooltipModifier.REGISTRY.register(item, modifier);
    }
}
