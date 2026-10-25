package net.succ.solar_punk.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.succ.solar_punk.block.custom.PipeShape;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.block.custom.SolarMirrorBlock;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;
import net.succ.solar_punk.block.custom.TurbineBladeBlock;
import net.succ.solar_punk.fluid.ModFluids;

public class ModBlockStateProvider extends BlockStateProvider {
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
        fermentationVatBlock();
        blockWithItem(ModBlocks.SALT_BLOCK);
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

        litAxisModelBlock(ModBlocks.TURBINE_ROTOR);
        turbineBladeBlock(ModBlocks.ANDESITE_TURBINE_BLADE);
        turbineBladeBlock(ModBlocks.BRASS_TURBINE_BLADE);

        // Fluid blocks: particle texture only, the fluid renderer handles the actual surface.
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

    // Y rotation for a model authored facing NORTH.
    private static int horizontalYRot(Direction facing) {
        return switch (facing) {
            case EAST  ->  90;
            case SOUTH -> 180;
            case WEST  -> 270;
            default    ->   0; // NORTH
        };
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

    private void fluidBlock(DeferredBlock<LiquidBlock> block, ResourceLocation particleTexture) {
        ModelFile model = models().getBuilder("block/" + block.getId().getPath())
                .texture("particle", particleTexture);
        getVariantBuilder(block.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(model).build());
    }
}
