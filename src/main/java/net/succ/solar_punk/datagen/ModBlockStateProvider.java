package net.succ.solar_punk.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
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
import net.succ.solar_punk.block.custom.HeatBatteryBlock;

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
        blockWithItem(ModBlocks.SALT_BLOCK);
        heatStateModelBlock(ModBlocks.HEAT_BATTERY);
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

    private void blockWithItem(DeferredBlock<?> deferredBlock){
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<Block> deferredBlock){
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("succsessentials:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItemOther(DeferredBlock<Block> deferredBlock, String appendix){
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("succsessentials:block/" + deferredBlock.getId().getPath() + appendix));
    }
}
