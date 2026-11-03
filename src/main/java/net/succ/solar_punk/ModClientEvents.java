package net.succ.solar_punk;

import net.succ.solar_punk.client.renderer.AcceleratorParticleRenderer;
import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.item.FiligreeRailingItem;
import net.succ.solar_punk.item.ParticleAcceleratorPipeItem;
import net.minecraft.client.renderer.item.ItemProperties;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.FusionReactorCasingBlock;
import net.succ.solar_punk.block.custom.FusionReactorCasingGlassBlock;
import net.succ.solar_punk.block.custom.KineticSprinklerItem;
import net.succ.solar_punk.block.custom.TurbineCasingBlock;
import net.succ.solar_punk.block.custom.TrellisBlock;
import net.succ.solar_punk.client.FlowerTint;
import net.succ.solar_punk.block.custom.TurbineCasingGlassBlock;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.client.model.CasingModel;
import net.succ.solar_punk.client.model.ModSpriteShifts;
import net.succ.solar_punk.client.model.TankModel;
import net.succ.solar_punk.client.renderer.FermentationVatRenderer;
import net.succ.solar_punk.client.renderer.FusionReactorCoreRenderer;
import net.succ.solar_punk.client.renderer.GeyserCapRenderer;
import net.succ.solar_punk.client.renderer.HalfShaftRenderer;
import net.succ.solar_punk.client.renderer.KineticBatteryRenderer;
import net.succ.solar_punk.client.renderer.KineticSprinklerRenderer;
import net.succ.solar_punk.client.renderer.SolarMirrorRenderer;
import net.succ.solar_punk.client.renderer.SolarPowerTowerRenderer;
import net.succ.solar_punk.client.renderer.TurbineRotorRenderer;
import net.succ.solar_punk.compat.ponder.SolarPunkPonderPlugin;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@EventBusSubscriber(modid = SolarPunk.MODID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        KineticSprinklerItem item = (KineticSprinklerItem) ModBlocks.KINETIC_SPRINKLER.get().asItem();
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return Objects.requireNonNull(GeoRenderProvider.of(item).getGeoItemRenderer());
            }
        }, item);
    }

    // Trellis tint 0: the grey vines take the biome's foliage colour, like leaves.
    // Tint 1: the grey flower takes the colour of the flower that was used (FlowerTint).
    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 1) {
                Block flower = state.getValue(TrellisBlock.PLANT).flowerBlock();
                return flower != null ? FlowerTint.of(flower) : -1;
            }
            return level != null && pos != null
                    ? BiomeColors.getAverageFoliageColor(level, pos)
                    : FoliageColor.getDefaultColor();
        }, ModBlocks.TRELLIS.get());
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Touching these PartialModels here class-loads their renderers before Flywheel's
        // handler iterates PartialModel.ALL, and registering them explicitly guarantees
        // they get baked regardless of handler order.
        event.register(ModelResourceLocation.standalone(FermentationVatRenderer.GAUGE.modelLocation()));
        event.register(ModelResourceLocation.standalone(FermentationVatRenderer.GAUGE_DIAL.modelLocation()));
        event.register(ModelResourceLocation.standalone(SolarPowerTowerRenderer.GAUGE.modelLocation()));
        event.register(ModelResourceLocation.standalone(SolarPowerTowerRenderer.GAUGE_DIAL.modelLocation()));
        event.register(ModelResourceLocation.standalone(SolarMirrorRenderer.POST.modelLocation()));
        event.register(ModelResourceLocation.standalone(SolarMirrorRenderer.PLATE.modelLocation()));
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Predicate<Block> turbineCasing = b -> b instanceof TurbineCasingBlock || b instanceof TurbineCasingGlassBlock;
        Predicate<Block> reactorCasing = b -> b instanceof FusionReactorCasingBlock || b instanceof FusionReactorCasingGlassBlock;

        // Block path -> wrapper that adds connected textures to the baked model.
        Map<String, UnaryOperator<BakedModel>> wrappers = Map.of(
                "fermentation_vat", m -> new TankModel(m, ModSpriteShifts.FERMENTATION_VAT,
                        ModSpriteShifts.FERMENTATION_VAT_TOP, ModSpriteShifts.FERMENTATION_VAT_INNER),
                "solar_power_tower", m -> new TankModel(m, ModSpriteShifts.SOLAR_POWER_TOWER,
                        ModSpriteShifts.SOLAR_POWER_TOWER_TOP, ModSpriteShifts.SOLAR_POWER_TOWER_INNER),
                "turbine_casing", m -> new CasingModel(m, ModSpriteShifts.TURBINE_CASING, turbineCasing),
                "turbine_casing_glass", m -> new CasingModel(m, ModSpriteShifts.TURBINE_CASING_GLASS, turbineCasing),
                "fusion_reactor_casing", m -> new CasingModel(m, ModSpriteShifts.FUSION_REACTOR_CASING, reactorCasing),
                "fusion_reactor_casing_glass", m -> new CasingModel(m, ModSpriteShifts.FUSION_REACTOR_CASING_GLASS, reactorCasing)
        );

        event.getModels().replaceAll((key, model) -> {
            if (!key.id().getNamespace().equals(SolarPunk.MODID)) return model;
            UnaryOperator<BakedModel> wrapper = wrappers.get(key.id().getPath());
            return wrapper != null ? wrapper.apply(model) : model;
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ANDESITE_SOLAR_PANEL.get(), HalfShaftRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BIOFILTER.get(), HalfShaftRenderer::new);
        // The Accelerator Input's filter slot and the particle running around its ring. The
        // shaft below is the neighbour's, so there's no shaft to draw here.
        event.registerBlockEntityRenderer(ModBlockEntities.PARTICLE_ACCELERATOR_INPUT.get(), AcceleratorParticleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BIOFUEL_ENGINE.get(), HalfShaftRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BIOMASS_GASIFIER.get(), HalfShaftRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GEYSER_CAP.get(), GeyserCapRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURBINE_ROTOR.get(), TurbineRotorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KINETIC_BATTERY.get(), KineticBatteryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FERMENTATION_VAT.get(), FermentationVatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_POWER_TOWER.get(), SolarPowerTowerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_MIRROR.get(), SolarMirrorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KINETIC_SPRINKLER.get(), KineticSprinklerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_REACTOR_CORE.get(), FusionReactorCoreRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModSpriteShifts.init();
            PonderIndex.addPlugin(new SolarPunkPonderPlugin());
            registerTooltips();

            // Which shape the Particle Accelerator Pipe item is set to, for its item model
            ItemProperties.register(ModBlocks.PARTICLE_ACCELERATOR_PIPE.get().asItem(),
                    ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "shape"),
                    (stack, level, entity, seed) -> ParticleAcceleratorPipeItem.getShape(stack).ordinal());
            // Same for the filigree railing items (flat, slope, steep slope)
            for (var railing : List.of(ModBlocks.BRASS_FILIGREE_RAILING, ModBlocks.ANDESITE_FILIGREE_RAILING))
                ItemProperties.register(railing.get().asItem(),
                        ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "railing_shape"),
                        (stack, level, entity, seed) -> FiligreeRailingItem.getShape(stack).ordinal());

            // Only the Turbine Rotor gets a Flywheel visualizer, since it really is a full
            // shaft. Half-shaft machines use HalfShaftRenderer instead.
            SimpleBlockEntityVisualizer.builder(ModBlockEntities.TURBINE_ROTOR.get())
                    .factory(SingleAxisRotatingVisual::shaft)
                    .neverSkipVanillaRender()
                    .apply();
        });
    }

    private static void registerTooltips() {
        tip(ModBlocks.SOLAR_HEATER);
        tip(ModBlocks.FIREBOX_BOILER);
        tip(ModBlocks.ANDESITE_SOLAR_PANEL);
        tip(ModBlocks.BRASS_SOLAR_PANEL);
        tip(ModBlocks.HEAT_BATTERY);
        tip(ModBlocks.KINETIC_BATTERY);
        tip(ModBlocks.FERMENTATION_VAT);
        tip(ModBlocks.BIOMASS_GASIFIER);
        tip(ModBlocks.BIOFUEL_ENGINE);
        tip(ModBlocks.GEYSER_CAP);
        tip(ModBlocks.GEYSER_VENT);
        tip(ModBlocks.SOLAR_POWER_TOWER);
        tip(ModBlocks.SOLAR_MIRROR);
        tip(ModBlocks.BIOFILTER);
        tip(ModBlocks.KINETIC_SPRINKLER);
        tip(ModBlocks.TURBINE_ROTOR);
        tip(ModBlocks.CRYSTALLIZER);
        tip(ModBlocks.FUSION_REACTOR_CORE);
        tip(ModBlocks.LITHIUM_BREEDER_MODULE);
        tip(ModBlocks.BERYLLIUM_REFLECTOR_MODULE);
        tip(ModBlocks.PARTICLE_ACCELERATOR_PIPE);
        tip(ModBlocks.BRASS_FILIGREE_RAILING);
        tip(ModBlocks.ANDESITE_FILIGREE_RAILING);
        tip(ModBlocks.PARTICLE_ACCELERATOR_INPUT);
        tip(ModBlocks.SALT_LAMP);
        tip(ModBlocks.GARDEN_LIGHT);
        tip(ModBlocks.TRELLIS);
        tip(ModBlocks.SOLAR_SHINGLES);
    }

    private static void tip(Supplier<? extends Block> block) {
        Item item = block.get().asItem();
        TooltipModifier modifier = new ItemDescription.Modifier(item, Palette.STANDARD_CREATE);
        KineticStats stats = KineticStats.create(item);
        if (stats != null) modifier = modifier.andThen(stats);
        TooltipModifier.REGISTRY.register(item, modifier);
    }
}
