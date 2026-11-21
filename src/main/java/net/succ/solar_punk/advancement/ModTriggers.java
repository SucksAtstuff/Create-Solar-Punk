package net.succ.solar_punk.advancement;

import studio.offbyone.lib.advancement.SimpleTrigger;
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

    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> HEAT_STORED =
            TRIGGERS.register("heat_stored", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> GEYSER_ACTIVE =
            TRIGGERS.register("geyser_active", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> GEYSER_SCALDED =
            TRIGGERS.register("geyser_scalded", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> GASIFIER_RUNNING =
            TRIGGERS.register("gasifier_running", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> BIOFUEL_ENGINE_ON =
            TRIGGERS.register("biofuel_engine_on", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> GREENHOUSE_SEALED =
            TRIGGERS.register("greenhouse_sealed", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> FERMENTATION_DONE =
            TRIGGERS.register("fermentation_done", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> BIOREACTOR_BIOMASS =
            TRIGGERS.register("bioreactor_biomass", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> TOWER_BUILT =
            TRIGGERS.register("tower_built", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> TURBINE_BUILT =
            TRIGGERS.register("turbine_built", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> TURBINE_FLOODED =
            TRIGGERS.register("turbine_flooded", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> REACTOR_BUILT =
            TRIGGERS.register("reactor_built", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> TRITIUM_BRED =
            TRIGGERS.register("tritium_bred", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> ACCELERATOR_BUILT =
            TRIGGERS.register("accelerator_built", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> ACCELERATOR_COLLISION =
            TRIGGERS.register("accelerator_collision", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> ACCELERATOR_BIG_RING =
            TRIGGERS.register("accelerator_big_ring", SimpleTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> DISPENSED_WIRE_CONNECTOR =
            TRIGGERS.register("dispensed_wire_connector", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> DISPENSED_GROW_LAMP =
            TRIGGERS.register("dispensed_grow_lamp", SimpleTrigger::new);

    public static void register(IEventBus bus) {
        TRIGGERS.register(bus);
    }

    public static void fireNearby(Level level, BlockPos pos,
            DeferredHolder<CriterionTrigger<?>, SimpleTrigger> trigger) {
        fireNearby(level, pos, trigger, 16);
    }

    public static void fireNearby(Level level, BlockPos pos,
            DeferredHolder<CriterionTrigger<?>, SimpleTrigger> trigger, double radius) {
        if (level.isClientSide()) return;
        Vec3 center = Vec3.atCenterOf(pos);
        ((ServerLevel) level).getPlayers(p -> p.distanceToSqr(center) < radius * radius)
                .forEach(p -> trigger.get().trigger(p));
    }
}
