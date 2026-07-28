package net.succ.solar_punk.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;

public class ModTriggers {

    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, SolarPunk.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> HEAT_STORED =
            TRIGGERS.register("heat_stored", SolarpunkTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> GEYSER_ACTIVE =
            TRIGGERS.register("geyser_active", SolarpunkTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> GASIFIER_RUNNING =
            TRIGGERS.register("gasifier_running", SolarpunkTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> BIOFUEL_ENGINE_ON =
            TRIGGERS.register("biofuel_engine_on", SolarpunkTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> FERMENTATION_DONE =
            TRIGGERS.register("fermentation_done", SolarpunkTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> TOWER_BUILT =
            TRIGGERS.register("tower_built", SolarpunkTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> TURBINE_BUILT =
            TRIGGERS.register("turbine_built", SolarpunkTrigger::new);

    public static void register(IEventBus bus) {
        TRIGGERS.register(bus);
    }

    public static void fireNearby(Level level, BlockPos pos,
            DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> trigger) {
        if (level.isClientSide()) return;
        Vec3 center = Vec3.atCenterOf(pos);
        ((ServerLevel) level).getPlayers(p -> p.distanceToSqr(center) < 256.0)
                .forEach(p -> trigger.get().trigger(p));
    }
}
