package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.client.resources.model.BakedModel;

public class FusionReactorCasingModel extends CTModel {
    public FusionReactorCasingModel(BakedModel originalModel) {
        super(originalModel, new FusionReactorCasingCTBehaviour(ModSpriteShifts.FUSION_REACTOR_CASING));
    }
}
