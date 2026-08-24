package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.client.resources.model.BakedModel;

public class FusionReactorCasingGlassModel extends CTModel {
    public FusionReactorCasingGlassModel(BakedModel originalModel) {
        super(originalModel, new FusionReactorCasingCTBehaviour(ModSpriteShifts.FUSION_REACTOR_CASING_GLASS));
    }
}
