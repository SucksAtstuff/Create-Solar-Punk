package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.client.resources.model.BakedModel;

public class TurbineCasingGlassModel extends CTModel {
    public TurbineCasingGlassModel(BakedModel originalModel) {
        super(originalModel, new TurbineCasingCTBehaviour(ModSpriteShifts.TURBINE_CASING_GLASS));
    }
}