package net.succ.solar_punk.block.custom;

import net.minecraft.world.level.block.Block;

// Fusion Reactor blanket module - the "devour fuel, maximize steam" build. Placed in
// the reactor's blanket band (see FusionReactorCoreBlockEntity); no unique behaviour of
// its own, the blanket-ratio math lives entirely in the core's scan. See
// plan_for_fusion.md.
public class BerylliumReflectorModuleBlock extends Block {
    public BerylliumReflectorModuleBlock(Properties properties) {
        super(properties);
    }
}
