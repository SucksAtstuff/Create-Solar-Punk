package net.succ.solar_punk.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.Config;

/**
 * Agrivoltaics: a solar panel raised over a plant is cooled by it and makes a little more.
 * The panel looks straight down through air for the first block within range; if that is a
 * plant (crop, sapling, flower...), the panel gets the bonus. Panels don't occlude light, so
 * the plants underneath keep growing at their normal speed.
 */
public final class Agrivoltaics {

    public static boolean hasPlantBelow(Level level, BlockPos panel) {
        if (Config.agrivoltaicsBonus <= 0) return false;
        BlockPos.MutableBlockPos cursor = panel.mutable();
        for (int i = 0; i < Config.agrivoltaicsRange; i++) {
            cursor.move(0, -1, 0);
            if (!level.isLoaded(cursor)) return false;
            BlockState state = level.getBlockState(cursor);
            if (state.isAir()) continue;
            return PlantGrowth.isPlant(state);
        }
        return false;
    }

    public static float multiplier(boolean plantBelow) {
        return plantBelow ? 1 + (float) Config.agrivoltaicsBonus : 1;
    }

    private Agrivoltaics() {}
}
