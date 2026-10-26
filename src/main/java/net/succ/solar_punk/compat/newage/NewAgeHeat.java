package net.succ.solar_punk.compat.newage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Soft compat for Create: New Age (gitlab.com/antarcticgardens/create-new-age). This class
// never touches New Age classes, so it's safe to load without the mod. When New Age is
// installed, Heat Batteries are created as NewAgeHeatBatteryBlockEntity instead, which
// implements New Age's heat interface; that class is only ever loaded from createBattery().
public final class NewAgeHeat {

    private static final Logger LOGGER = LoggerFactory.getLogger("solarpunk/newage");

    public static final boolean LOADED = ModList.get().isLoaded("create_new_age");

    private static boolean disabled = false;

    /** The Heat Battery's block entity factory: the New Age-aware one when the mod is there. */
    public static HeatBatteryBlockEntity createBattery(BlockPos pos, BlockState state) {
        if (LOADED && !disabled) {
            try {
                return NewAgeHeatBatteryBlockEntity.create(pos, state);
            } catch (Throwable t) {
                disable(t);
            }
        }
        return new HeatBatteryBlockEntity(ModBlockEntities.HEAT_BATTERY.get(), pos, state);
    }

    /** New Age has no public API, so if their heat interface changes, the compat turns itself off. */
    static void disable(Throwable t) {
        if (disabled) return;
        disabled = true;
        LOGGER.warn("[SolarPunk] Create: New Age heat compat disabled: {}", t.toString());
    }

    static boolean isDisabled() {
        return disabled;
    }

    /** The battery's temperature on New Age's scale, from its charge. */
    public static float temperature(HeatBatteryBlockEntity battery) {
        return (float) battery.getHeatStored() / Config.heatBatteryMaxHeat * Config.heatBatteryNewAgeMaxTemperature;
    }

    private NewAgeHeat() {}
}
