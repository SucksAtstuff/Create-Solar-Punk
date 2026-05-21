package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.CTModel;
import net.succ.solar_punk.block.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FermentationVatModel extends CTModel {

    private static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

    public FermentationVatModel(BakedModel originalModel) {
        super(originalModel, new FermentationVatCTBehaviour(
            ModSpriteShifts.FERMENTATION_VAT,
            ModSpriteShifts.FERMENTATION_VAT_TOP,
            ModSpriteShifts.FERMENTATION_VAT_INNER
        ));
    }

    @Override
    protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world,
            BlockPos pos, BlockState state, ModelData blockEntityData) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        CullData cullData = new CullData();
        for (Direction d : Direction.Plane.HORIZONTAL)
            cullData.setCulled(d, world.getBlockState(pos.relative(d)).is(ModBlocks.FERMENTATION_VAT.get()));
        return builder.with(CULL_PROPERTY, cullData);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand,
            ModelData extraData, RenderType renderType) {
        if (side != null)
            return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (extraData.has(CULL_PROPERTY) && extraData.get(CULL_PROPERTY).isCulled(d))
                continue;
            quads.addAll(super.getQuads(state, d, rand, extraData, renderType));
        }
        quads.addAll(super.getQuads(state, null, rand, extraData, renderType));
        return quads;
    }

    private static class CullData {
        final boolean[] culledFaces = new boolean[4];

        void setCulled(Direction face, boolean cull) {
            if (face.getAxis().isVertical()) return;
            culledFaces[face.get2DDataValue()] = cull;
        }

        boolean isCulled(Direction face) {
            if (face.getAxis().isVertical()) return false;
            return culledFaces[face.get2DDataValue()];
        }
    }
}
