package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.world.level.block.Block;

// Containment shell for the Fusion Reactor - Netherite Ingot applied to a Copper Block,
// one tier past the Steam Turbine's industrial iron casing. No block entity: unlike
// TurbineCasingBlock, this doesn't proxy a fluid handler through to anything (the
// reactor's steam output is its own tank, not piped through the shell walls).
public class FusionReactorCasingBlock extends Block implements IWrenchable {
    public FusionReactorCasingBlock(Properties properties) {
        super(properties);
    }
}
