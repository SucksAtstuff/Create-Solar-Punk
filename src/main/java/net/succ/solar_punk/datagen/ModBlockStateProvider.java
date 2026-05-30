package net.succ.solar_punk.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
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
        litFacingCustomModelBlock(ModBlocks.BIOMASS_GASIFIER, true);
        litFacingCustomModelBlock(ModBlocks.BIOFUEL_ENGINE, true);
        fermentationVatBlock();
        blockWithItem(ModBlocks.SALT_BLOCK);
        simpleBlockWithItem(ModBlocks.DEAD_GRASS_BLOCK.get(), new ModelFile.UncheckedModelFile(modLoc("block/dead_grass_block")));
        simpleBlockWithItem(ModBlocks.RUINED_DIRT.get(), new ModelFile.UncheckedModelFile(modLoc("block/ruined_dirt")));
        simpleBlockWithItem(ModBlocks.ASH_BLOCK.get(), new ModelFile.UncheckedModelFile(modLoc("block/ash_block")));
        deadGrassPlantBlock();
        heatStateModelBlock(ModBlocks.HEAT_BATTERY);
        solarPowerTowerBlock();
        solarMirrorBlock();
        kineticSprinklerBlock();

        ResourceLocation casingTex = modLoc("block/industrial_iron_casing/industrial_iron_casing");
        simpleBlockWithItem(ModBlocks.TURBINE_CASING.get(),
                models().cubeAll("turbine_casing", casingTex));
        simpleBlockWithItem(ModBlocks.TURBINE_CASING_GLASS.get(),
                models().cubeAll("turbine_casing_glass", casingTex));
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

    // For simple cube-all blocks. Generates the block model, blockstate, and item model.
    private void cubeAllBlock(DeferredBlock<? extends Block> block, ResourceLocation texture) {
        simpleBlockWithItem(block.get(), models().cubeAll(block.getId().getPath(), texture));
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

    // For cube-all blocks with a LIT property, generating two models (unlit and lit).
    private void litCubeAllBlock(DeferredBlock<? extends Block> block, ResourceLocation unlitTex, ResourceLocation litTex) {
        String path = block.getId().getPath();
        ModelFile unlit = models().cubeAll(path, unlitTex);
        ModelFile lit = models().cubeAll(path + "_lit", litTex);
        getVariantBuilder(block.get()).forAllStates(state ->
                ConfiguredModel.builder()
                        .modelFile(state.getValue(BlockStateProperties.LIT) ? lit : unlit)
                        .build());
        simpleBlockItem(block.get(), unlit);
    }

    private void solarMirrorBlock() {
        ModelFile model = new UncheckedModelFile(modLoc("block/solar_mirror"));
        getVariantBuilder(ModBlocks.SOLAR_MIRROR.get()).forAllStates(state -> {
            Direction facing = state.getValue(SolarMirrorBlock.FACING);
            // X/Y rotations so the model's base (bottom face) attaches to the clicked surface.
            // rotationX=90 tips the model so its base points toward local-North; Y then spins that.
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
        simpleBlockItem(ModBlocks.SOLAR_MIRROR.get(), model);
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
}
