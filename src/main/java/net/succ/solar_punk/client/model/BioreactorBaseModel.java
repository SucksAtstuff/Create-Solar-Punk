package net.succ.solar_punk.client.model;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// Connected textures for the Bioreactor Base: its own sheets for the sides, top and bottom,
// separate from the glass tank. Base blocks join only when they're under the same formed
// tank, so two Bioreactors built side by side keep separate bases.
public class BioreactorBaseModel extends CTModel {

    public BioreactorBaseModel(BakedModel originalModel, CTSpriteShiftEntry side,
                               CTSpriteShiftEntry top, CTSpriteShiftEntry bottom) {
        super(originalModel, new HorizontalCTBehaviour(side, top) {
            @Override
            public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
                return direction == Direction.DOWN ? bottom : super.getShift(state, direction, sprite);
            }

            @Override
            public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader,
                                      BlockPos pos, BlockPos otherPos, Direction face) {
                return state.getBlock() == other.getBlock()
                        && pos.getY() == otherPos.getY()
                        && ConnectivityHandler.isConnected(reader, pos.above(), otherPos.above());
            }
        });
    }
}
