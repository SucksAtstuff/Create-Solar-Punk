package net.succ.solar_punk.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.block.custom.AndesiteTurbineBladeBlock;
import net.succ.solar_punk.block.custom.BrassTurbineBladeBlock;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.block.custom.SolarMirrorBlock;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // No geometry — GeckoLib renders everything. The hand-crafted model only supplies the particle texture.
        ModelFile geyserCapBlock = new UncheckedModelFile(modLoc("block/geyser_cap"));
        getVariantBuilder(ModBlocks.GEYSER_CAP.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(geyserCapBlock).build());
        itemModels().withExistingParent("geyser_cap", modLoc("block/geyser_cap_display"));

        blockWithItem(ModBlocks.GEYSER_VENT);

        litCustomModelBlock(ModBlocks.ANDESITE_SOLAR_PANEL, false);
        litCustomModelBlock(ModBlocks.BRASS_SOLAR_PANEL, true);
        litAxisModelBlock(ModBlocks.KINETIC_BATTERY, false);
        litFacingCustomModelBlock(ModBlocks.SOLAR_HEATER, true);
        furnaceStyleBlock(ModBlocks.CRYSTALLIZER,
                modLoc("block/crystallizer/crystallizer_side"),
                modLoc("block/crystallizer/crystallizer_front"),
                modLoc("block/crystallizer/crystallizer_front_on"),
                modLoc("block/crystallizer/crystallizer_top"));
        furnaceStyleBlock(ModBlocks.DEUTERIUM_EXTRACTOR,
                modLoc("block/deuterium_extractor/deuterium_extractor_side"),
                modLoc("block/deuterium_extractor/deuterium_extractor_front"),
                modLoc("block/deuterium_extractor/deuterium_extractor_front_on"),
                modLoc("block/deuterium_extractor/deuterium_extractor_top"));
        furnaceStyleBlock(ModBlocks.LITHIUM_BRINE_EXTRACTOR,
                modLoc("block/lithium_brine_extractor/lithium_brine_extractor_side"),
                modLoc("block/lithium_brine_extractor/lithium_brine_extractor_front"),
                modLoc("block/lithium_brine_extractor/lithium_brine_extractor_front_on"),
                modLoc("block/lithium_brine_extractor/lithium_brine_extractor_top"));
        litFacingCustomModelBlock(ModBlocks.FIREBOX_BOILER, true);
        litFacingCustomModelBlock(ModBlocks.BIOMASS_GASIFIER, true);
        litFacingCustomModelBlock(ModBlocks.BIOFUEL_ENGINE, true);
        fermentationVatBlock();
        blockWithItem(ModBlocks.SALT_BLOCK);
        blockWithItem(ModBlocks.LITHIUM_ORE);
        blockWithItem(ModBlocks.DEEPSLATE_LITHIUM_ORE);
        blockWithItem(ModBlocks.LITHIUM_BLOCK);
        blockWithItem(ModBlocks.BERYLLIUM_BLOCK);
        simpleBlockWithItem(ModBlocks.DEAD_GRASS_BLOCK.get(), new ModelFile.UncheckedModelFile(modLoc("block/dead_grass_block")));
        simpleBlockWithItem(ModBlocks.RUINED_DIRT.get(), new ModelFile.UncheckedModelFile(modLoc("block/ruined_dirt")));
        simpleBlockWithItem(ModBlocks.ASH_BLOCK.get(), new ModelFile.UncheckedModelFile(modLoc("block/ash_block")));
        deadGrassPlantBlock();
        heatStateModelBlock(ModBlocks.HEAT_BATTERY);
        solarPowerTowerBlock();
        solarMirrorBlock();
        kineticSprinklerBlock();

        litAxisCustomModelBlock(ModBlocks.TURBINE_ROTOR, false);
        turbineBladeBlock(ModBlocks.ANDESITE_TURBINE_BLADE,
                AndesiteTurbineBladeBlock.FACING, AndesiteTurbineBladeBlock.AXIS, AndesiteTurbineBladeBlock.HIDDEN);
        turbineBladeBlock(ModBlocks.BRASS_TURBINE_BLADE,
                BrassTurbineBladeBlock.FACING, BrassTurbineBladeBlock.AXIS, BrassTurbineBladeBlock.HIDDEN);

        // Fluid blocks — particle texture only; the fluid renderer handles the actual surface.
        fluidBlock(ModFluids.MOLTEN_SALT_BLOCK, modLoc("block/molten_salt_still"));
        fluidBlock(ModFluids.BIOFUEL_BLOCK,     mcLoc("block/water_still"));
        fluidBlock(ModFluids.FERTILIZER_BLOCK,  mcLoc("block/water_still"));
        fluidBlock(ModFluids.STEAM_BLOCK,       mcLoc("block/water_still"));

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

        // Blanket modules - fill the band between the Core and the Casing shell. Hand-crafted
        // Blockbench models (custom per-face UV swatches out of the 64x64 texture), not a
        // generated cube_all - the model files live in src/main/resources, not src/generated.
        simpleBlockWithItem(ModBlocks.LITHIUM_BREEDER_MODULE.get(),
                new UncheckedModelFile(modLoc("block/lithium_breeder_module")));
        simpleBlockWithItem(ModBlocks.BERYLLIUM_REFLECTOR_MODULE.get(),
                new UncheckedModelFile(modLoc("block/beryllium_reflector_module")));

        // Two states, driven by FusionReactorCoreBlock.FORMED: a plain visible cube
        // while the structure is incomplete (formed=false, generated cube_all, same as
        // any other placeholder block), and no geometry at all once formed=true (same
        // trick as GeyserCap above - minecraft:block/block has no elements, hand-crafted
        // model only supplies the particle texture) so the block doesn't entomb
        // FusionReactorCoreRenderer's glowing sphere + rings inside a solid box once
        // they start rendering. The item still gets its own flat icon (temp placeholder
        // art) instead of inheriting either block render - see dead_grass above for the
        // same "distinct item texture" pattern.
        ModelFile fusionReactorCoreCube = cubeAll(ModBlocks.FUSION_REACTOR_CORE.get());
        ModelFile fusionReactorCoreFormed = new UncheckedModelFile(modLoc("block/fusion_reactor_core_formed"));
        getVariantBuilder(ModBlocks.FUSION_REACTOR_CORE.get()).forAllStates(state ->
                ConfiguredModel.builder()
                        .modelFile(state.getValue(FusionReactorCoreBlock.FORMED) ? fusionReactorCoreFormed : fusionReactorCoreCube)
                        .build());
        itemModels().withExistingParent("fusion_reactor_core", "minecraft:item/generated")
                .texture("layer0", modLoc("item/fusion_reactor_core"));
    }

    // For blocks whose models are hand-crafted (Blockbench).
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

    // Like litCustomModelBlock but also handles a HORIZONTAL_FACING property by rotating the model on Y.
    private void litFacingCustomModelBlock(DeferredBlock<? extends Block> block, boolean generateItem) {
        String path     = block.getId().getPath();
        ModelFile unlit = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile lit   = new UncheckedModelFile(modLoc("block/" + path + "_lit"));

        getVariantBuilder(block.get()).forAllStates(state -> {
            boolean isLit = state.getValue(BlockStateProperties.LIT);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            int yRot = switch (facing) {
                case EAST  ->  90;
                case SOUTH -> 180;
                case WEST  -> 270;
                default    ->   0; // NORTH
            };
            return ConfiguredModel.builder()
                    .modelFile(isLit ? lit : unlit)
                    .rotationY(yRot)
                    .build();
        });

        if (generateItem) simpleBlockItem(block.get(), unlit);
    }

    // Like litCustomModelBlock but also handles an AXIS property (X/Y/Z) by rotating the model.
    private void litAxisModelBlock(DeferredBlock<? extends Block> block, boolean generateItem) {
        String path     = block.getId().getPath();
        ModelFile unlit = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile lit   = new UncheckedModelFile(modLoc("block/" + path + "_lit"));

        getVariantBuilder(block.get()).forAllStates(state -> {
            boolean isLit = state.getValue(BlockStateProperties.LIT);
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);

            int xRot = 0, yRot = 0;
            if (axis == Direction.Axis.X) { xRot = 90; yRot = 90; }
            else if (axis == Direction.Axis.Z) { xRot = 90; }

            return ConfiguredModel.builder()
                    .modelFile(isLit ? lit : unlit)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });

        if (generateItem) simpleBlockItem(block.get(), unlit);
    }

    // Like litCustomModelBlock but also handles an AXIS property (X/Y/Z) by rotating
    // the model - same x/y rotation values as litAxisModelBlock, just combined with a
    // separate LIT model swap (used by the Turbine Rotor, which needs both).
    private void litAxisCustomModelBlock(DeferredBlock<? extends Block> block, boolean generateItem) {
        String path     = block.getId().getPath();
        ModelFile unlit = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile lit   = new UncheckedModelFile(modLoc("block/" + path + "_lit"));

        getVariantBuilder(block.get()).forAllStates(state -> {
            boolean isLit = state.getValue(BlockStateProperties.LIT);
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);

            int xRot = 0, yRot = 0;
            if (axis == Direction.Axis.X) { xRot = 90; yRot = 90; }
            else if (axis == Direction.Axis.Z) { xRot = 90; }

            return ConfiguredModel.builder()
                    .modelFile(isLit ? lit : unlit)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });

        if (generateItem) simpleBlockItem(block.get(), unlit);
    }

    // Turbine blades can sit on any of the 3 rotor axes. The blade model is authored
    // thin along Y, extending along X (the original, still-shipped vertical-turbine
    // orientation): for axis=Y we keep the exact original per-facing spin (yRot only).
    // For axis=X/Z we reuse the same x/y values already proven for the rotor's own
    // cube model (litAxisModelBlock) to correctly reorient the thin axis - this
    // deliberately does not attempt a fully independent 4-way idle facing rotation
    // for the X/Z cases (a single fixed idle orientation per axis instead): the
    // structure scan never checks FACING for validity, and the visually-important
    // case (the blades actively spinning) is handled separately and correctly by
    // TurbineRotorRenderer, which reorients+spins the same base mesh at render time.
    private void turbineBladeBlock(DeferredBlock<? extends Block> block, DirectionProperty facingProp,
                                    EnumProperty<Direction.Axis> axisProp, BooleanProperty hiddenProp) {
        String path = block.getId().getPath();
        ModelFile visible = new UncheckedModelFile(modLoc("block/" + path));
        ModelFile hidden  = new UncheckedModelFile(modLoc("block/turbine_blade_hidden"));

        getVariantBuilder(block.get()).forAllStates(state -> {
            if (state.getValue(hiddenProp))
                return ConfiguredModel.builder().modelFile(hidden).build();

            Direction.Axis axis = state.getValue(axisProp);
            int xRot, yRot;
            if (axis == Direction.Axis.X) {
                xRot = 90; yRot = 90;
            } else if (axis == Direction.Axis.Z) {
                xRot = 90; yRot = 0;
            } else {
                xRot = 0;
                yRot = switch (state.getValue(facingProp)) {
                    case SOUTH -> 90;
                    case WEST -> 180;
                    case NORTH -> 270;
                    default -> 0; // EAST, and the unreachable UP/DOWN facings
                };
            }

            return ConfiguredModel.builder().modelFile(visible).rotationX(xRot).rotationY(yRot).build();
        });
    }

    // For simple cube-all blocks. Generates the block model, blockstate, and item model.
    private void cubeAllBlock(DeferredBlock<? extends Block> block, ResourceLocation texture) {
        simpleBlockWithItem(block.get(), models().cubeAll(block.getId().getPath(), texture));
    }

    // Furnace-style: an oriented block whose front face swaps texture when lit, same
    // template vanilla's own Furnace uses (block/orientable - top/bottom share one
    // texture, front is distinct, the other 3 sides share "side").
    private void furnaceStyleBlock(DeferredBlock<? extends Block> block, ResourceLocation side,
                                    ResourceLocation front, ResourceLocation frontOn, ResourceLocation top) {
        String path = block.getId().getPath();
        ModelFile unlit = models().orientable(path, side, front, top);
        ModelFile lit   = models().orientable(path + "_lit", side, frontOn, top);

        getVariantBuilder(block.get()).forAllStates(state -> {
            boolean isLit = state.getValue(BlockStateProperties.LIT);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            int yRot = switch (facing) {
                case EAST  ->  90;
                case SOUTH -> 180;
                case WEST  -> 270;
                default    ->   0; // NORTH
            };
            return ConfiguredModel.builder()
                    .modelFile(isLit ? lit : unlit)
                    .rotationY(yRot)
                    .build();
        });

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
        // The lower half shows the static base/mount - the reflective plate is rendered
        // separately (SolarMirrorRenderer) so it can continuously track the sun. The post
        // and plate poke up into the cell above, so the upper half is a bare, invisible
        // placeholder that exists only to reserve that space (see SolarMirrorBlock).
        // The held item still uses the full, statically-tilted assembly for a nicer icon.
        ModelFile model = new UncheckedModelFile(modLoc("block/solar_mirror_block"));
        ModelFile topModel = new UncheckedModelFile(modLoc("block/solar_mirror_top"));
        ModelFile itemModel = new UncheckedModelFile(modLoc("block/solar_mirror"));
        getVariantBuilder(ModBlocks.SOLAR_MIRROR.get()).forAllStates(state -> {
            if (state.getValue(SolarMirrorBlock.HALF) == DoubleBlockHalf.UPPER)
                return ConfiguredModel.builder().modelFile(topModel).build();

            Direction facing = state.getValue(SolarMirrorBlock.FACING);
            // X/Y rotations so the model's base (bottom face) attaches to the clicked surface.
            // rotationX=90 tips the model so its base points toward local-North; Y then spins that.
            // Only FACING=UP is placeable going forward, but old saves may still have
            // wall/ceiling-mounted mirrors from before that restriction - keep those working.
            int xRot = switch (facing) {
                case UP    -> 0;   // floor: base sits on the ground, no rotation
                case DOWN  -> 180; // ceiling: base against ceiling, upside down
                default    -> 270; // walls: X=270 puts the base (bottom) against the surface
            };
            int yRot = switch (facing) {
                case NORTH -> 180; // base → south, block placed north of wall
                case EAST  -> 270; // base → west
                case WEST  -> 90;  // base → east
                default    -> 0;   // SOUTH / DOWN / UP
            };
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });
        simpleBlockItem(ModBlocks.SOLAR_MIRROR.get(), itemModel);
    }

    // For blocks whose model is hand-crafted with no state variants.
    private void simpleCustomModelBlock(DeferredBlock<? extends Block> block) {
        ModelFile model = new UncheckedModelFile(modLoc("block/" + block.getId().getPath()));
        getVariantBuilder(block.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(model).build());
        simpleBlockItem(block.get(), model);
    }

    private void kineticSprinklerBlock() {
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

    private void blockWithItem(DeferredBlock<?> deferredBlock){
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<Block> deferredBlock){
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("succsessentials:block/" + deferredBlock.getId().getPath()));
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

    private void blockItemOther(DeferredBlock<Block> deferredBlock, String appendix){
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("succsessentials:block/" + deferredBlock.getId().getPath() + appendix));
    }

    private void fluidBlock(DeferredBlock<LiquidBlock> block, ResourceLocation particleTexture) {
        ModelFile model = models().getBuilder("block/" + block.getId().getPath())
                .texture("particle", particleTexture);
        getVariantBuilder(block.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(model).build());
    }
}
