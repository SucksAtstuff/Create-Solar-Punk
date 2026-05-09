package net.succ.create_solar_powered.datagen;

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
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.block.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Create_solar_powered.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        litCustomModelBlock(ModBlocks.ANDESITE_SOLAR_PANEL, false);
        litCustomModelBlock(ModBlocks.BRASS_SOLAR_PANEL, true);
        litAxisModelBlock(ModBlocks.KINETIC_BATTERY, false);
        litCubeAllBlock(ModBlocks.SOLAR_HEATER, mcLoc("block/iron_block"), mcLoc("block/gold_block"));
        cubeAllBlock(ModBlocks.SALT_BLOCK, mcLoc("block/calcite"));
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
}
