package net.succ.solar_punk.client.model;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
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

// Connected-texture model for the Create-style multiblock tanks (Fermentation Vat,
// Solar Power Tower). Uses Create's fluid tank CT layout, connects only to the same
// block of the same formed multiblock (not just any neighbour of the same type, or two
// separate tanks side by side would join), and culls side faces inside the multiblock.
public class TankModel extends CTModel {

    private static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

    public TankModel(BakedModel originalModel, CTSpriteShiftEntry layerShift,
                     CTSpriteShiftEntry topShift, CTSpriteShiftEntry innerShift) {
        super(originalModel, new FluidTankCTBehaviour(layerShift, topShift, innerShift) {
            @Override
            public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader,
                                      BlockPos pos, BlockPos otherPos, Direction face) {
                return state.getBlock() == other.getBlock()
                        && ConnectivityHandler.isConnected(reader, pos, otherPos);
            }
        });
    }

    @Override
    protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world,
            BlockPos pos, BlockState state, ModelData blockEntityData) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        CullData cullData = new CullData();
        for (Direction d : Direction.Plane.HORIZONTAL)
            cullData.setCulled(d, ConnectivityHandler.isConnected(world, pos, pos.relative(d)));
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
