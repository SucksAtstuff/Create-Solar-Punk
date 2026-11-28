package net.succ.solar_punk.datagen;

import net.succ.offbyonelib.datagen.AddonBlockStateProvider;
import net.succ.solar_punk.block.custom.HempCropBlock;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.succ.solar_punk.block.custom.PipeShape;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.succ.solar_punk.SolarPunk;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.custom.BioreactorBlock;
import net.succ.solar_punk.block.custom.FiligreeRailingBlock;
import net.succ.solar_punk.block.custom.RailingShape;
import net.succ.solar_punk.block.custom.FiligreeRailingSteepSlopeBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.succ.solar_punk.block.custom.FiligreeRailingSlopeBlock;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.block.custom.SolarMirrorBlock;
import net.succ.solar_punk.block.custom.RuinedSolarMirrorBlock;
import net.succ.solar_punk.block.custom.BrokenStreetlampBlock;
import net.succ.solar_punk.block.custom.StreetlampBlock;
import net.succ.offbyonelib.wire.WireConnectorBlock;
import net.succ.offbyonelib.wire.RedstoneWireRelayBlock;
import net.succ.offbyonelib.wire.WireSwitchBlock;
import net.succ.solar_punk.block.custom.EndPlateBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.succ.solar_punk.block.custom.GrowLampBlock;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;
import net.succ.solar_punk.block.custom.TrellisBlock;
import net.succ.solar_punk.block.custom.RailingTransition;
import net.succ.solar_punk.block.custom.TrellisPlant;
import net.succ.solar_punk.block.custom.SolarShinglesBlock;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.succ.solar_punk.block.custom.TurbineBladeBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.Map;

public class ModBlockStateProvider extends AddonBlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // No geometry: GeckoLib renders everything. The hand-crafted model only supplies the particle texture.
        ModelFile geyserCapBlock = new UncheckedModelFile(modLoc("block/geyser_cap"));
        getVariantBuilder(ModBlocks.GEYSER_CAP.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(geyserCapBlock).build());
        itemModels().withExistingParent("geyser_cap", modLoc("block/geyser_cap_display"));

        blockWithItem(ModBlocks.GEYSER_VENT);
        blockWithItem(ModBlocks.NETHER_GEYSER_VENT);
        facingCustomModelBlock(ModBlocks.RUSTED_GEYSER_CAP, true);
        // The block model has no gauge needles (GeyserWellRenderer draws them); the item keeps them
        facingCustomModelBlock(ModBlocks.GEYSER_WELL, false);
        itemModels().withExistingParent("geyser_well", modLoc("block/geyser_well_item"));

        litCustomModelBlock(ModBlocks.ANDESITE_SOLAR_PANEL, false);
        litCustomModelBlock(ModBlocks.BRASS_SOLAR_PANEL, true);
        litAxisModelBlock(ModBlocks.KINETIC_BATTERY);
        litFacingCustomModelBlock(ModBlocks.SOLAR_HEATER, true);
        furnaceStyleBlock(ModBlocks.CRYSTALLIZER,
                modLoc("block/crystallizer/crystallizer_side"),
                modLoc("block/crystallizer/crystallizer_front"),
                modLoc("block/crystallizer/crystallizer_front_on"),
                modLoc("block/crystallizer/crystallizer_top"));
        litFacingCustomModelBlock(ModBlocks.FIREBOX_BOILER, true);
        litFacingCustomModelBlock(ModBlocks.BIOMASS_GASIFIER, true);
        litFacingCustomModelBlock(ModBlocks.BIOFUEL_ENGINE, true);
        // The block model has no shaft (ElectricMotorRenderer spins one); the item keeps the modelled one.
        facingCustomModelBlock(ModBlocks.ELECTRIC_MOTOR, false);
        facingCustomModelBlock(ModBlocks.ELECTROLYZER, true);
        axisBlock(ModBlocks.FUEL_CELL.get(), modLoc("block/fuel_cell/fuel_cell_side"), modLoc("block/fuel_cell/fuel_cell_front"));
        simpleBlockItem(ModBlocks.FUEL_CELL.get(), new UncheckedModelFile(modLoc("block/fuel_cell")));
        endPlateBlock();
        itemModels().getBuilder(ModBlocks.ELECTRIC_MOTOR.getId().getPath())
                .parent(new UncheckedModelFile(modLoc("block/electric_motor_item")));
        fermentationVatBlock();
        bioreactorBlocks();
        blockWithItem(ModBlocks.SALT_BLOCK);
        blockWithItem(ModBlocks.SALT_BRICKS);
        blockWithItem(ModBlocks.ASH_BRICKS);
        blockWithItem(ModBlocks.HEMPCRETE);
        blockWithItem(ModBlocks.POLISHED_HEMPCRETE_BRICKS);
        simpleBlockWithItem(ModBlocks.SOLAR_GLASS.get(),
                models().cubeAll("solar_glass", blockTexture(ModBlocks.SOLAR_GLASS.get())).renderType("minecraft:cutout"));
        filigreeRailingBlock(ModBlocks.BRASS_FILIGREE_RAILING, ModBlocks.BRASS_FILIGREE_RAILING);
        filigreeRailingSlopeBlock(ModBlocks.BRASS_FILIGREE_RAILING_SLOPE, ModBlocks.BRASS_FILIGREE_RAILING);
        filigreeRailingSlopeBlock(ModBlocks.BRASS_FILIGREE_RAILING_STEEP_SLOPE, ModBlocks.BRASS_FILIGREE_RAILING);
        filigreeRailingBlock(ModBlocks.ANDESITE_FILIGREE_RAILING, ModBlocks.BRASS_FILIGREE_RAILING);
        filigreeRailingSlopeBlock(ModBlocks.ANDESITE_FILIGREE_RAILING_SLOPE, ModBlocks.ANDESITE_FILIGREE_RAILING);
        filigreeRailingSlopeBlock(ModBlocks.ANDESITE_FILIGREE_RAILING_STEEP_SLOPE, ModBlocks.ANDESITE_FILIGREE_RAILING);
        filigreeRailingBlock(ModBlocks.COPPER_FILIGREE_RAILING, ModBlocks.BRASS_FILIGREE_RAILING);
        filigreeRailingSlopeBlock(ModBlocks.COPPER_FILIGREE_RAILING_SLOPE, ModBlocks.COPPER_FILIGREE_RAILING);
        filigreeRailingSlopeBlock(ModBlocks.COPPER_FILIGREE_RAILING_STEEP_SLOPE, ModBlocks.COPPER_FILIGREE_RAILING);
        filigreeRailingBlock(ModBlocks.IRON_FILIGREE_RAILING, ModBlocks.BRASS_FILIGREE_RAILING);
        filigreeRailingSlopeBlock(ModBlocks.IRON_FILIGREE_RAILING_SLOPE, ModBlocks.IRON_FILIGREE_RAILING);
        filigreeRailingSlopeBlock(ModBlocks.IRON_FILIGREE_RAILING_STEEP_SLOPE, ModBlocks.IRON_FILIGREE_RAILING);
        // Like vanilla panes: the glass texture on the faces, a _pane_top strip on the edges,
        // and a flat item icon of the glass.
        paneBlockWithRenderType(ModBlocks.SOLAR_GLASS_PANE.get(), blockTexture(ModBlocks.SOLAR_GLASS.get()),
                modLoc("block/solar_glass_pane_top"), "minecraft:cutout");
        itemModels().withExistingParent("solar_glass_pane", mcLoc("item/generated"))
                .texture("layer0", blockTexture(ModBlocks.SOLAR_GLASS.get()));
        facingCustomModelBlock(ModBlocks.GREENHOUSE_CONTROLLER, true);
        greenhouseGlass(ModBlocks.GREENHOUSE_GLASS.get(), ModBlocks.GREENHOUSE_GLASS_PANE.get(), "greenhouse/greenhouse_glass");
        greenhouseGlass(ModBlocks.CLEAR_GREENHOUSE_GLASS.get(), ModBlocks.CLEAR_GREENHOUSE_GLASS_PANE.get(), "greenhouse/greenhouse_glass_clear");
        // Frosted panels: opaque textures, so a solid render type, but glass properties let light through.
        simpleBlockWithItem(ModBlocks.GREENHOUSE_PANEL.get(),
                models().cubeAll("greenhouse_panel", modLoc("block/greenhouse/greenhouse_panel")));
        simpleBlockWithItem(ModBlocks.LARGE_GREENHOUSE_PANEL.get(),
                models().cubeAll("large_greenhouse_panel", modLoc("block/greenhouse/greenhouse_panel_large")));
        ModBlockFamilies.ALL.forEach(this::blockFamily);
        // Placed switched on, so the item shows the glowing model.
        litFacingCustomModelBlock(ModBlocks.SALT_LAMP, false);
        simpleBlockItem(ModBlocks.SALT_LAMP.get(), new UncheckedModelFile(modLoc("block/salt_lamp_lit")));
        litCustomModelBlock(ModBlocks.GARDEN_LIGHT, false);
        simpleBlockItem(ModBlocks.GARDEN_LIGHT.get(), new UncheckedModelFile(modLoc("block/garden_light_lit")));
        streetlampBlock();
        wireConnectorBlock();
        wireSwitchBlock(ModBlocks.WIRE_SWITCH.get(), "wire_switch");
        wireSwitchBlock(ModBlocks.REDSTONE_WIRE_RELAY.get(), "redstone_wire_switch");
        growLampBlock();
        litCustomModelBlock(ModBlocks.SOLAR_PLANT_BED, false);
        simpleBlockItem(ModBlocks.SOLAR_PLANT_BED.get(), new UncheckedModelFile(modLoc("block/solar_plant_bed_lit")));
        blockWithItem(ModBlocks.CHARRED_TIMBER);
        trellisBlock();
        solarShinglesBlock(ModBlocks.SOLAR_SHINGLES.get(), "solar_shingles");
        solarShinglesBlock(ModBlocks.SOLAR_GLASS_SHINGLES.get(), "solar_glass_shingles");
        blockWithItem(ModBlocks.LITHIUM_ORE);
        blockWithItem(ModBlocks.DEEPSLATE_LITHIUM_ORE);
        blockWithItem(ModBlocks.LITHIUM_BLOCK);
        blockWithItem(ModBlocks.BERYLLIUM_BLOCK);
        simpleBlockWithItem(ModBlocks.DEAD_GRASS_BLOCK.get(), new UncheckedModelFile(modLoc("block/dead_grass_block")));
        simpleBlockWithItem(ModBlocks.RUINED_DIRT.get(), new UncheckedModelFile(modLoc("block/ruined_dirt")));
        simpleBlockWithItem(ModBlocks.ASH_BLOCK.get(), new UncheckedModelFile(modLoc("block/ash_block")));
        deadGrassPlantBlock();
        heatStateModelBlock(ModBlocks.HEAT_BATTERY);
        solarPowerTowerBlock();
        solarMirrorBlock();
        kineticSprinklerBlock();
        hempCropBlock();

        litAxisModelBlock(ModBlocks.TURBINE_ROTOR);
        turbineBladeBlock(ModBlocks.ANDESITE_TURBINE_BLADE);
        turbineBladeBlock(ModBlocks.BRASS_TURBINE_BLADE);

        // Fluid blocks: particle texture only, the fluid renderer handles the actual surface.
        fluidBlock(ModFluids.MOLTEN_SALT_BLOCK, modLoc("block/molten_salt_still"));
        fluidBlock(ModFluids.BIOFUEL_BLOCK,     mcLoc("block/water_still"));
        fluidBlock(ModFluids.FERTILIZER_BLOCK,  mcLoc("block/water_still"));
        fluidBlock(ModFluids.RICH_FERTILIZER_BLOCK, mcLoc("block/water_still"));
        fluidBlock(ModFluids.STEAM_BLOCK,       mcLoc("block/water_still"));
        fluidBlock(ModFluids.DEUTERIUM_BLOCK,   mcLoc("block/water_still"));
        fluidBlock(ModFluids.HYDROGEN_BLOCK,    mcLoc("block/water_still"));
        fluidBlock(ModFluids.OXYGEN_BLOCK,      mcLoc("block/water_still"));
        fluidBlock(ModFluids.HEMP_SEED_OIL_BLOCK, mcLoc("block/water_still"));
        fluidBlock(ModFluids.GEOTHERMAL_BRINE_BLOCK, mcLoc("block/water_still"));
        fluidBlock(ModFluids.ALGAE_CULTURE_BLOCK, mcLoc("block/water_still"));

        ResourceLocation casingTex = modLoc("block/industrial_iron_casing/industrial_iron_casing");
        ResourceLocation glassCasingTex = modLoc("block/industrial_iron_casing/industrial_iron_glass_casing");
        simpleBlockWithItem(ModBlocks.TURBINE_CASING.get(),
                models().cubeAll("turbine_casing", casingTex));
        simpleBlockWithItem(ModBlocks.TURBINE_CASING_GLASS.get(),
                models().cubeAll("turbine_casing_glass", glassCasingTex)
                        .renderType("minecraft:cutout"));

        ResourceLocation reactorCasingTex = modLoc("block/fusion_reactor_casing/fusion_reactor_casing");
        ResourceLocation reactorCasingGlassTex = modLoc("block/fusion_reactor_casing/fusion_reactor_casing_glass");
        simpleBlockWithItem(ModBlocks.FUSION_REACTOR_CASING.get(),
                models().cubeAll("fusion_reactor_casing", reactorCasingTex));
        simpleBlockWithItem(ModBlocks.FUSION_REACTOR_CASING_GLASS.get(),
                models().cubeAll("fusion_reactor_casing_glass", reactorCasingGlassTex)
                        .renderType("minecraft:cutout"));

        // Blanket modules use hand-crafted Blockbench models from src/main/resources.
        simpleBlockWithItem(ModBlocks.LITHIUM_BREEDER_MODULE.get(),
                new UncheckedModelFile(modLoc("block/lithium_breeder_module")));
        simpleBlockWithItem(ModBlocks.BERYLLIUM_REFLECTOR_MODULE.get(),
                new UncheckedModelFile(modLoc("block/beryllium_reflector_module")));

        // Particle Accelerator pipes: hand-crafted Blockbench models, turned by FACING.
        facingCustomModelBlock(ModBlocks.PARTICLE_ACCELERATOR_PIPE, false);
        facingCustomModelBlock(ModBlocks.PARTICLE_ACCELERATOR_PIPE_BEND, false);
        facingCustomModelBlock(ModBlocks.PARTICLE_ACCELERATOR_PIPE_BEND_MIRRORED, false);
        facingCustomModelBlock(ModBlocks.PARTICLE_ACCELERATOR_PIPE_DIAGONAL, false);
        facingCustomModelBlock(ModBlocks.PARTICLE_ACCELERATOR_INPUT, true);
        acceleratorPipeItemModel();

        // A hand-crafted cube while unformed, and a model with no geometry once formed so
        // the block doesn't hide FusionReactorCoreRenderer's effect. The item uses the cube.
        ModelFile fusionReactorCoreCube = new UncheckedModelFile(modLoc("block/fusion_reactor_core"));
        ModelFile fusionReactorCoreFormed = new UncheckedModelFile(modLoc("block/fusion_reactor_core_formed"));
        getVariantBuilder(ModBlocks.FUSION_REACTOR_CORE.get()).forAllStates(state ->
                ConfiguredModel.builder()
                        .modelFile(state.getValue(FusionReactorCoreBlock.FORMED) ? fusionReactorCoreFormed : fusionReactorCoreCube)
                        .build());
        itemModels().withExistingParent("fusion_reactor_core", modLoc("block/fusion_reactor_core"));
    }

    // Every variant of a ModBlockFamilies family (the library generates the shapes). Tinted
    // families (Rammed Earth) also cover their base block, on the library's tinted parents.
    private void blockFamily(BlockFamily family) {
        if (ModBlockFamilies.isTinted(family))
            tintedBlockFamily(family, ModBlockFamilies.baseTexture(family), ModBlockFamilies::tintedVariantTexture);
        else
            blockFamily(family, ModBlockFamilies.baseTexture(family));
    }

    // Greenhouse glass and its pane, like Solar Glass. Both kinds share one pane edge texture.
    private void greenhouseGlass(Block glass, IronBarsBlock pane, String texture) {
        ResourceLocation tex = modLoc("block/" + texture);
        String glassPath = BuiltInRegistries.BLOCK.getKey(glass).getPath();
        simpleBlockWithItem(glass, models().cubeAll(glassPath, tex).renderType("minecraft:cutout"));
        paneBlockWithRenderType(pane, tex, modLoc("block/greenhouse/greenhouse_glass_pane_top"), "minecraft:cutout");
        itemModels().withExistingParent(BuiltInRegistries.BLOCK.getKey(pane).getPath(), mcLoc("item/generated"))
                .texture("layer0", tex);
    }

    // A straight run (POST off) shows <id>_straight (running north-south), turned to the run's
    // axis. Everything else shows <id>_post plus an <id>_side (pointing north) turned to face
    // each connected direction. The item uses <id>_inventory. No uvlock: the models map their
    // own texture sheet. The brass railing's models are hand-crafted; other materials get
    // copies of them with their own textures/block/<material>_filigree.png.
    private void filigreeRailingBlock(DeferredBlock<FiligreeRailingBlock> block, DeferredBlock<FiligreeRailingBlock> source) {
        String path = block.getId().getPath();
        ModelFile post = filigreeRailingModel(block, source, "_post");
        ModelFile side = filigreeRailingModel(block, source, "_side");
        ModelFile straight = filigreeRailingModel(block, source, "_straight");
        ModelFile inventory = filigreeRailingModel(block, source, "_inventory");
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block.get());
        RailingTransition none = RailingTransition.NONE;
        builder.part().modelFile(straight).addModel()
                .condition(FiligreeRailingBlock.POST, false).condition(FiligreeRailingBlock.NORTH, true)
                .condition(FiligreeRailingBlock.TRANSITION, none).end();
        builder.part().modelFile(straight).rotationY(90).addModel()
                .condition(FiligreeRailingBlock.POST, false).condition(FiligreeRailingBlock.EAST, true)
                .condition(FiligreeRailingBlock.TRANSITION, none).end();
        builder.part().modelFile(post).addModel().condition(FiligreeRailingBlock.POST, true).end();
        // Where it meets a gentle slope's end, the START or END piece (authored climbing north)
        // replaces the railing, turned to the climb. The OBJ models are shared by every material.
        for (RailingTransition t : RailingTransition.values()) {
            if (t.kind == null) continue;
            String piece = t.kind == RailingTransition.Kind.START ? "_slope_start" : "_slope_end";
            builder.part().modelFile(new UncheckedModelFile(modLoc("block/" + path + piece)))
                    .rotationY(horizontalYRot(t.climb)).addModel()
                    .condition(FiligreeRailingBlock.TRANSITION, t).end();
        }
        Map<Direction, BooleanProperty> sides = Map.of(
                Direction.NORTH, FiligreeRailingBlock.NORTH, Direction.EAST, FiligreeRailingBlock.EAST,
                Direction.SOUTH, FiligreeRailingBlock.SOUTH, Direction.WEST, FiligreeRailingBlock.WEST);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            builder.part().modelFile(side).rotationY(horizontalYRot(direction)).addModel()
                    .condition(FiligreeRailingBlock.POST, true).condition(sides.get(direction), true).end();
        }
        // The item shows the shape it's set to: the "railing_shape" item property is the
        // RailingShape's ordinal (registered in ModClientEvents), and each override swaps in that
        // slope's item model (see filigreeRailingSlopeBlock).
        ItemModelBuilder item = itemModels().getBuilder(path).parent(inventory);
        for (RailingShape shape : RailingShape.values()) {
            if (shape == RailingShape.FLAT) continue;
            item.override()
                    .predicate(modLoc("railing_shape"), shape.ordinal())
                    .model(new UncheckedModelFile(modLoc("item/" + path + "_" + shape.getSerializedName())))
                    .end();
        }
    }

    private ModelFile filigreeRailingModel(DeferredBlock<FiligreeRailingBlock> block,
                                           DeferredBlock<FiligreeRailingBlock> source, String part) {
        String path = block.getId().getPath();
        String sourcePath = source.getId().getPath();
        if (path.equals(sourcePath)) return new UncheckedModelFile(modLoc("block/" + path + part));
        ResourceLocation texture = modLoc("block/" + path.replace("_railing", ""));
        return models().withExistingParent(path + part, modLoc("block/" + sourcePath + part))
                .texture("0", texture).texture("particle", texture).renderType("minecraft:cutout");
    }

    // The railing's slope models, authored climbing north and turned to FACING:
    // <railing>_slope_up (LOWER), <railing>_slope_down (UPPER) and <railing>_slope_full (FULL,
    // the steep slope). They're small hand-written JSONs that load the shared
    // filigree_railing_slope_*.obj with the material's texture (OBJ allows the half-block slope's
    // angle, which block model JSON doesn't). The item shows the piece it places by hand.
    private void filigreeRailingSlopeBlock(DeferredBlock<? extends FiligreeRailingSlopeBlock> block, DeferredBlock<?> railing) {
        String railingPath = railing.getId().getPath();
        Map<FiligreeRailingSlopeBlock.Part, ModelFile> models = Map.of(
                FiligreeRailingSlopeBlock.Part.LOWER, new UncheckedModelFile(modLoc("block/" + railingPath + "_slope_up")),
                FiligreeRailingSlopeBlock.Part.UPPER, new UncheckedModelFile(modLoc("block/" + railingPath + "_slope_down")),
                FiligreeRailingSlopeBlock.Part.FULL, new UncheckedModelFile(modLoc("block/" + railingPath + "_slope_full")));
        getVariantBuilder(block.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(models.get(state.getValue(FiligreeRailingSlopeBlock.PART)))
                .rotationY(horizontalYRot(state.getValue(FiligreeRailingSlopeBlock.FACING)))
                .build(), FiligreeRailingSlopeBlock.WATERLOGGED);
        // Item model for the railing item's slope shape. The slopes are taller than a block (the
        // steep one reaches down into the block below), so they're shrunk and re-centred.
        boolean steep = block.get() instanceof FiligreeRailingSteepSlopeBlock;
        itemModels().getBuilder(block.getId().getPath())
                .parent(models.get(steep ? FiligreeRailingSlopeBlock.Part.FULL : FiligreeRailingSlopeBlock.Part.LOWER))
                .transforms()
                    .transform(ItemDisplayContext.GUI)
                        .rotation(30, 225, 0).translation(0, steep ? 4.25f : -3.75f, 0).scale(0.5f).end()
                    .transform(ItemDisplayContext.GROUND)
                        .rotation(0, 0, 0).translation(0, steep ? 6 : 1.2f, 0).scale(0.2f).end()
                    .transform(ItemDisplayContext.FIXED)
                        .rotation(0, 90, 0).translation(0, steep ? 4.25f : -3.55f, 0).scale(0.45f).end()
                    .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                        .rotation(75, 45, 0).translation(0, steep ? 4.5f : 0.3f, 0).scale(0.3f).end()
                    .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                        .rotation(75, 45, 0).translation(0, steep ? 4.5f : 0.3f, 0).scale(0.3f).end()
                    .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                        .rotation(0, 45, 0).translation(0, steep ? 5 : -1.3f, 0).scale(0.32f).end()
                    .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                        .rotation(0, 225, 0).translation(0, steep ? 5 : -1.3f, 0).scale(0.32f).end()
                .end();
    }

    // OBJ models, all sloping up toward the south (so FACING south is unrotated), one set per
    // HALF. Right corners use their FACING; a left corner is the right corner of the facing
    // counter-clockwise from it, as with vanilla stairs.
    private void solarShinglesBlock(Block block, String name) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            Direction facing = state.getValue(SolarShinglesBlock.FACING);
            StairsShape shape = state.getValue(SolarShinglesBlock.SHAPE);
            String half = state.getValue(SolarShinglesBlock.HALF) == Half.TOP ? "top" : "bottom";
            String suffix = switch (shape) {
                case STRAIGHT -> "";
                case INNER_LEFT, INNER_RIGHT -> "_inner_corner";
                case OUTER_LEFT, OUTER_RIGHT -> "_outer_corner";
            };
            if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) facing = facing.getCounterClockWise();
            return ConfiguredModel.builder()
                    .modelFile(new UncheckedModelFile(modLoc("block/" + name + "_" + half + "_slope" + suffix)))
                    .rotationY((int) facing.toYRot())
                    .uvLock(false)
                    .build();
        }, SolarShinglesBlock.WATERLOGGED);
        simpleBlockItem(block, new UncheckedModelFile(modLoc("block/" + name + "_bottom_slope")));
    }

    // Hand-made models: "trellis" with vines, "trellis_no_vines" without (also the item).
    // Every flower shares trellis_flowers; its grey flower is tinted per flower at runtime.
    private void trellisBlock() {
        ModelFile vines   = new UncheckedModelFile(modLoc("block/trellis"));
        ModelFile noVines = new UncheckedModelFile(modLoc("block/trellis_no_vines"));
        ModelFile glow    = new UncheckedModelFile(modLoc("block/trellis_glow"));
        ModelFile flowers = new UncheckedModelFile(modLoc("block/trellis_flowers"));
        getVariantBuilder(ModBlocks.TRELLIS.get()).forAllStatesExcept(state -> {
            TrellisPlant plant = state.getValue(TrellisBlock.PLANT);
            ModelFile model = switch (plant) {
                case NONE -> noVines;
                case VINES -> vines;
                case GLOW_BERRIES -> glow;
                default -> flowers;
            };
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(horizontalYRot(state.getValue(TrellisBlock.FACING)))
                    .build();
        }, TrellisBlock.WATERLOGGED);
        simpleBlockItem(ModBlocks.TRELLIS.get(), noVines);
    }

    // For blocks whose models are hand-crafted (Blockbench), with a "_lit" variant.
    // Generates only the blockstate JSON; the model files themselves stay in src/main/resources.
    private void litCustomModelBlock(DeferredBlock<? extends Block> block, boolean generateItem) {
        String path     = block.getId().getPath();
        ModelFile unlit = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile lit   = new UncheckedModelFile(modLoc("block/" + path + "_lit"));

        getVariantBuilder(block.get()).forAllStates(state ->
                ConfiguredModel.builder()
                        .modelFile(state.getValue(BlockStateProperties.LIT) ? lit : unlit)
                        .build());

        if (generateItem) simpleBlockItem(block.get(), unlit);
    }

    // Like litCustomModelBlock but also rotates the model on Y for HORIZONTAL_FACING.
    private void litFacingCustomModelBlock(DeferredBlock<? extends Block> block, boolean generateItem) {
        String path     = block.getId().getPath();
        ModelFile unlit = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile lit   = new UncheckedModelFile(modLoc("block/" + path + "_lit"));

        getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(BlockStateProperties.LIT) ? lit : unlit)
                .rotationY(horizontalYRot(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .build());

        if (generateItem) simpleBlockItem(block.get(), unlit);
    }

    // A hand-crafted model with no lit variant, rotated on Y for HORIZONTAL_FACING.
    private void facingCustomModelBlock(DeferredBlock<? extends Block> block, boolean generateItem) {
        ModelFile model = new UncheckedModelFile(modLoc("block/" + block.getId().getPath()));
        getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(model)
                .rotationY(horizontalYRot(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .build());
        if (generateItem) simpleBlockItem(block.get(), model);
    }

    // The pipe item shows the shape it's set to: the "shape" item property is the
    // PipeShape's ordinal (registered in ModClientEvents), and each override swaps in
    // that shape's block model.
    private void acceleratorPipeItemModel() {
        ItemModelBuilder builder = itemModels().getBuilder(ModBlocks.PARTICLE_ACCELERATOR_PIPE.getId().getPath())
                .parent(new UncheckedModelFile(modLoc("block/particle_accelerator_pipe")));
        for (PipeShape shape : PipeShape.values()) {
            if (shape == PipeShape.STRAIGHT) continue;
            builder.override()
                    .predicate(modLoc("shape"), shape.ordinal())
                    .model(new UncheckedModelFile(modLoc("block/" + BuiltInRegistries.BLOCK.getKey(shape.block()).getPath())))
                    .end();
        }
    }

    // Like litCustomModelBlock but also rotates the model for an AXIS property (X/Y/Z).
    // No item model: these blocks' items are hand-crafted.
    private void litAxisModelBlock(DeferredBlock<? extends Block> block) {
        String path     = block.getId().getPath();
        ModelFile unlit = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile lit   = new UncheckedModelFile(modLoc("block/" + path + "_lit"));

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
            return ConfiguredModel.builder()
                    .modelFile(state.getValue(BlockStateProperties.LIT) ? lit : unlit)
                    .rotationX(axis == Direction.Axis.Y ? 0 : 90)
                    .rotationY(axis == Direction.Axis.X ? 90 : 0)
                    .build();
        });
    }

    // Turbine blades can sit on any of the 3 rotor axes. The model is thin along Y and
    // extends along X. For axis=Y, FACING spins it on Y; for X/Z it gets the same
    // rotation as the rotor's own model and a single fixed idle orientation, since
    // FACING only matters for the idle look and TurbineRotorRenderer handles spinning.
    private void turbineBladeBlock(DeferredBlock<TurbineBladeBlock> block) {
        String path = block.getId().getPath();
        ModelFile visible = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile hidden  = new UncheckedModelFile(modLoc("block/turbine_blade_hidden"));

        getVariantBuilder(block.get()).forAllStates(state -> {
            if (state.getValue(TurbineBladeBlock.HIDDEN))
                return ConfiguredModel.builder().modelFile(hidden).build();

            Direction.Axis axis = state.getValue(TurbineBladeBlock.AXIS);
            int xRot, yRot;
            if (axis == Direction.Axis.X) {
                xRot = 90; yRot = 90;
            } else if (axis == Direction.Axis.Z) {
                xRot = 90; yRot = 0;
            } else {
                xRot = 0;
                yRot = switch (state.getValue(TurbineBladeBlock.FACING)) {
                    case SOUTH -> 90;
                    case WEST -> 180;
                    case NORTH -> 270;
                    default -> 0; // EAST, and the unreachable UP/DOWN facings
                };
            }

            return ConfiguredModel.builder().modelFile(visible).rotationX(xRot).rotationY(yRot).build();
        });
    }

    // Furnace-style: an oriented block (vanilla's block/orientable template) whose front
    // face swaps texture when lit.
    private void furnaceStyleBlock(DeferredBlock<? extends Block> block, ResourceLocation side,
                                    ResourceLocation front, ResourceLocation frontOn, ResourceLocation top) {
        String path = block.getId().getPath();
        ModelFile unlit = models().orientable(path, side, front, top);
        ModelFile lit   = models().orientable(path + "_lit", side, frontOn, top);

        getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(BlockStateProperties.LIT) ? lit : unlit)
                .rotationY(horizontalYRot(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .build());

        simpleBlockItem(block.get(), unlit);
    }

    // For the heat battery: 3 custom models keyed by heat=0/1/2.
    private void heatStateModelBlock(DeferredBlock<? extends Block> block) {
        String path = block.getId().getPath();
        ModelFile off         = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile lit         = new UncheckedModelFile(modLoc("block/" + path + "_lit"));
        ModelFile superheated = new UncheckedModelFile(modLoc("block/" + path + "_superheated"));

        getVariantBuilder(block.get()).forAllStates(state -> {
            int heat = state.getValue(HeatBatteryBlock.HEAT);
            return ConfiguredModel.builder()
                    .modelFile(heat == 2 ? superheated : heat == 1 ? lit : off)
                    .build();
        });
        simpleBlockItem(block.get(), off);
    }

    private void solarMirrorBlock() {
        // The lower half shows the static base, the plate is drawn by SolarMirrorRenderer,
        // and the upper half is an invisible placeholder (see SolarMirrorBlock). The item
        // uses the full, statically tilted assembly for a nicer icon.
        ModelFile model = new UncheckedModelFile(modLoc("block/solar_mirror_block"));
        ModelFile topModel = new UncheckedModelFile(modLoc("block/solar_mirror_top"));
        ModelFile itemModel = new UncheckedModelFile(modLoc("block/solar_mirror"));
        getVariantBuilder(ModBlocks.SOLAR_MIRROR.get()).forAllStates(state -> {
            if (state.getValue(SolarMirrorBlock.HALF) == DoubleBlockHalf.UPPER)
                return ConfiguredModel.builder().modelFile(topModel).build();

            Direction facing = state.getValue(SolarMirrorBlock.FACING);
            // Only FACING=UP can be placed now, but old saves may still have wall/ceiling
            // mirrors, so rotate the model's base against the surface it was attached to.
            int xRot = switch (facing) {
                case UP    -> 0;   // floor: base sits on the ground, no rotation
                case DOWN  -> 180; // ceiling: base against ceiling, upside down
                default    -> 270; // walls: X=270 puts the base (bottom) against the surface
            };
            int yRot = switch (facing) {
                case NORTH -> 180; // base -> south, block placed north of wall
                case EAST  -> 270; // base -> west
                case WEST  -> 90;  // base -> east
                default    -> 0;   // SOUTH / DOWN / UP
            };
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });
        simpleBlockItem(ModBlocks.SOLAR_MIRROR.get(), itemModel);

        // Ruined copy: the lower half draws the whole static mirror, turned by FACING; the
        // upper half is invisible, like the Solar Mirror's
        ModelFile ruined = new UncheckedModelFile(modLoc("block/ruined_solar_mirror"));
        ModelFile ruinedTop = new UncheckedModelFile(modLoc("block/ruined_solar_mirror_top"));
        getVariantBuilder(ModBlocks.RUINED_SOLAR_MIRROR.get()).forAllStates(state ->
                state.getValue(RuinedSolarMirrorBlock.HALF) == DoubleBlockHalf.UPPER
                        ? ConfiguredModel.builder().modelFile(ruinedTop).build()
                        : ConfiguredModel.builder().modelFile(ruined)
                                .rotationY(horizontalYRot(state.getValue(RuinedSolarMirrorBlock.FACING))).build());
        simpleBlockItem(ModBlocks.RUINED_SOLAR_MIRROR.get(), ruined);
        facingCustomModelBlock(ModBlocks.RUINED_SOLAR_HEATER, true);
    }

    // The lower half draws the whole two-block lamp; the upper half is an invisible
    // placeholder (see StreetlampBlock).
    private void streetlampBlock() {
        ModelFile unlit = new UncheckedModelFile(modLoc("block/streetlamp"));
        ModelFile lit = new UncheckedModelFile(modLoc("block/streetlamp_lit"));
        ModelFile top = new UncheckedModelFile(modLoc("block/streetlamp_top"));
        getVariantBuilder(ModBlocks.STREETLAMP.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(StreetlampBlock.HALF) == DoubleBlockHalf.UPPER ? top
                        : state.getValue(StreetlampBlock.LIT) ? lit : unlit)
                .build());
        simpleBlockItem(ModBlocks.STREETLAMP.get(), unlit);

        ModelFile broken = new UncheckedModelFile(modLoc("block/broken_streetlamp"));
        ModelFile brokenTop = new UncheckedModelFile(modLoc("block/broken_streetlamp_top"));
        getVariantBuilder(ModBlocks.BROKEN_STREETLAMP.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(BrokenStreetlampBlock.HALF) == DoubleBlockHalf.UPPER ? brokenTop : broken)
                .build());
        simpleBlockItem(ModBlocks.BROKEN_STREETLAMP.get(), broken);
    }

    // Pointing away from the block it's on, rotated like an End Rod (the model points up).
    private void wireConnectorBlock() {
        ModelFile model = new UncheckedModelFile(modLoc("block/wire_connector"));
        getVariantBuilder(ModBlocks.WIRE_CONNECTOR.get()).forAllStates(state -> {
            Direction facing = state.getValue(WireConnectorBlock.FACING);
            int x = switch (facing) {
                case UP -> 0;
                case DOWN -> 180;
                default -> 90;
            };
            int y = facing.getAxis().isHorizontal() ? (int) facing.toYRot() + 180 : 0;
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y % 360).build();
        });
        simpleBlockItem(ModBlocks.WIRE_CONNECTOR.get(), model);
    }

    // The hand-made model faces north (ports and rod ends out); any of the six directions. Each
    // roll is a hand-written composite (end_plate_roll90/180/270) turning it around its facing,
    // since blockstate rotations can't.
    private void endPlateBlock() {
        ModelFile model = new UncheckedModelFile(modLoc("block/end_plate"));
        getVariantBuilder(ModBlocks.END_PLATE.get()).forAllStates(state -> {
            Direction facing = state.getValue(EndPlateBlock.FACING);
            int roll = state.getValue(EndPlateBlock.ROLL);
            int x = facing == Direction.DOWN ? 90 : facing == Direction.UP ? 270 : 0;
            int y = facing.getAxis().isHorizontal() ? horizontalYRot(facing) : 0;
            ModelFile rolled = roll == 0 ? model : new UncheckedModelFile(modLoc("block/end_plate_roll" + roll * 90));
            return ConfiguredModel.builder().modelFile(rolled).rotationX(x).rotationY(y).build();
        });
        simpleBlockItem(ModBlocks.END_PLATE.get(), model);
    }

    // Face-attached like a Lever, with the same rotations (WireSwitchBlock.xRot/yRot, which also
    // turn its shape and wire posts). The models are hand-made: <model> closed, <model>_open open.
    private void wireSwitchBlock(Block block, String model) {
        ModelFile closed = new UncheckedModelFile(modLoc("block/" + model));
        ModelFile open = new UncheckedModelFile(modLoc("block/" + model + "_open"));
        getVariantBuilder(block).forAllStatesExcept(state -> {
            AttachFace face = state.getValue(WireSwitchBlock.FACE);
            return ConfiguredModel.builder()
                    .modelFile(state.getValue(WireSwitchBlock.CLOSED) ? closed : open)
                    .rotationX(WireSwitchBlock.xRot(face))
                    .rotationY(WireSwitchBlock.yRot(face, state.getValue(WireSwitchBlock.FACING)))
                    .build();
        }, RedstoneWireRelayBlock.INVERTED);
        // The hand-made model's own display transforms are used.
        itemModels().getBuilder(BuiltInRegistries.BLOCK.getKey(block).getPath()).parent(closed);
    }

    // Hung under a ceiling (grow_lamp_top) or from a wall or another lamp at an end, which shows
    // a mounting rail (grow_lamp_side). The models run along Z.
    private void growLampBlock() {
        getVariantBuilder(ModBlocks.GROW_LAMP.get()).forAllStates(state -> {
            String name = "block/grow_lamp_" + (state.getValue(GrowLampBlock.CEILING) ? "top" : "side")
                    + (state.getValue(GrowLampBlock.LIT) ? "_lit" : "");
            return ConfiguredModel.builder()
                    .modelFile(new UncheckedModelFile(modLoc(name)))
                    .rotationY(state.getValue(GrowLampBlock.AXIS) == Direction.Axis.X ? 90 : 0)
                    .build();
        });
        simpleBlockItem(ModBlocks.GROW_LAMP.get(), new UncheckedModelFile(modLoc("block/grow_lamp_side_lit")));
    }

    private void kineticSprinklerBlock() {
        // The rusted copy is a static OBJ model, so it needs no GeckoLib item renderer
        ModelFile rusted = new UncheckedModelFile(modLoc("block/rusted_kinetic_sprinkler"));
        simpleBlock(ModBlocks.RUSTED_KINETIC_SPRINKLER.get(), rusted);
        simpleBlockItem(ModBlocks.RUSTED_KINETIC_SPRINKLER.get(), rusted);

        ModelFile model = new UncheckedModelFile(modLoc("block/kinetic_sprinkler"));
        getVariantBuilder(ModBlocks.KINETIC_SPRINKLER.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(model).build());
        itemModels().getBuilder("kinetic_sprinkler")
                .parent(new UncheckedModelFile("minecraft:builtin/entity"))
                .transforms()
                    .transform(ItemDisplayContext.GUI)
                        .rotation(30, 225, 0).translation(0, 0, 0).scale(0.625f).end()
                    .transform(ItemDisplayContext.GROUND)
                        .rotation(0, 0, 0).translation(0, 3, 0).scale(0.25f).end()
                    .transform(ItemDisplayContext.FIXED)
                        .rotation(0, 0, 0).translation(0, 0, 0).scale(0.5f).end()
                    .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                        .rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f).end()
                    .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                        .rotation(0, 45, 0).translation(0, 0, 0).scale(0.4f).end()
                    .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                        .rotation(0, 225, 0).translation(0, 0, 0).scale(0.4f).end()
                .end();
    }

    private void deadGrassPlantBlock() {
        ModelFile crossModel = new UncheckedModelFile(modLoc("block/dead_grass"));
        simpleBlock(ModBlocks.DEAD_GRASS.get(), crossModel);
        itemModels().withExistingParent("dead_grass", "minecraft:item/generated")
                .texture("layer0", modLoc("block/dead_grass"));
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void solarPowerTowerBlock() {
        String base = "block/solar_power_tower/";
        getVariantBuilder(ModBlocks.SOLAR_POWER_TOWER.get()).forAllStates(state -> {
            String pos = state.getValue(SolarPowerTowerBlock.POSITION).getSerializedName();
            return ConfiguredModel.builder()
                    .modelFile(new UncheckedModelFile(modLoc(base + "block_" + pos)))
                    .build();
        });
        simpleBlockItem(ModBlocks.SOLAR_POWER_TOWER.get(),
                new UncheckedModelFile(modLoc(base + "block_single")));
    }

    // The glass tank uses hand-made models per layer (models/block/bioreactor/), like the
    // Fermentation Vat; the Base is a plain cube. Both get connected textures in ModClientEvents.
    private void bioreactorBlocks() {
        String base = "block/bioreactor/";
        getVariantBuilder(ModBlocks.BIOREACTOR.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(new UncheckedModelFile(modLoc(base + "block_" + state.getValue(BioreactorBlock.POSITION).getSerializedName())))
                .build());
        simpleBlockItem(ModBlocks.BIOREACTOR.get(), new UncheckedModelFile(modLoc(base + "block_single")));

        ModelFile baseModel = models().cubeBottomTop("bioreactor_base", modLoc(base + "bioreactor_base"),
                modLoc(base + "bioreactor_base_bottom"), modLoc(base + "bioreactor_base_top"));
        simpleBlockWithItem(ModBlocks.BIOREACTOR_BASE.get(), baseModel);
    }

    private void fermentationVatBlock() {
        String base = "block/fermentation_vat/";

        getVariantBuilder(ModBlocks.FERMENTATION_VAT.get()).forAllStates(state -> {
            String vStr = state.getValue(FermentationVatBlock.POSITION).getSerializedName();
            return ConfiguredModel.builder()
                    .modelFile(new UncheckedModelFile(modLoc(base + "block_" + vStr)))
                    .build();
        });

        simpleBlockItem(ModBlocks.FERMENTATION_VAT.get(),
                new UncheckedModelFile(modLoc(base + "block_single")));
    }

    // Eight ages share 4 lower and 4 upper textures: lower hemp_stage0 (age 0), 1 (1), 2 (2-3),
    // 3 (4-7, full height so it meets the top); upper hemp_top_stage0-3 (ages 4-7). The top
    // appears at age 4.
    private void hempCropBlock() {
        getVariantBuilder(ModBlocks.HEMP.get()).forAllStates(state -> {
            int age = state.getValue(HempCropBlock.AGE);
            String name = state.getValue(HempCropBlock.HALF) == DoubleBlockHalf.UPPER
                    ? "hemp_top_stage" + Math.max(0, age - HempCropBlock.TOP_AGE)
                    : "hemp_stage" + (age >= HempCropBlock.TOP_AGE ? 3 : age >= 2 ? 2 : age);
            return ConfiguredModel.builder()
                    .modelFile(models().crop(name, modLoc("block/" + name)).renderType("minecraft:cutout"))
                    .build();
        });
    }

    private void fluidBlock(DeferredBlock<LiquidBlock> block, ResourceLocation particleTexture) {
        ModelFile model = models().getBuilder("block/" + block.getId().getPath())
                .texture("particle", particleTexture);
        getVariantBuilder(block.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(model).build());
    }
}
