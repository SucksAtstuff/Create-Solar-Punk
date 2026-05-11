package net.succ.create_solar_powered.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.block.ModBlocks;
import net.succ.create_solar_powered.block.entity.custom.AndesiteSolarPanelBlockEntity;
import net.succ.create_solar_powered.block.entity.custom.BrassSolarPanelBlockEntity;
import net.succ.create_solar_powered.block.entity.custom.HeatBatteryBlockEntity;
import net.succ.create_solar_powered.block.entity.custom.KineticBatteryBlockEntity;
import net.succ.create_solar_powered.block.entity.custom.SolarHeaterBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Create_solar_powered.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarHeaterBlockEntity>> SOLAR_HEATER =
            BLOCK_ENTITIES.register("solar_heater", () -> BlockEntityType.Builder
                    .of((pos, state) -> new SolarHeaterBlockEntity(ModBlockEntities.SOLAR_HEATER.get(), pos, state), ModBlocks.SOLAR_HEATER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KineticBatteryBlockEntity>> KINETIC_BATTERY =
            BLOCK_ENTITIES.register("kinetic_battery", () -> BlockEntityType.Builder
                    .of((pos, state) -> new KineticBatteryBlockEntity(ModBlockEntities.KINETIC_BATTERY.get(), pos, state), ModBlocks.KINETIC_BATTERY.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AndesiteSolarPanelBlockEntity>> ANDESITE_SOLAR_PANEL =
            BLOCK_ENTITIES.register("andesite_solar_panel", () -> BlockEntityType.Builder
                    .of((pos, state) -> new AndesiteSolarPanelBlockEntity(ModBlockEntities.ANDESITE_SOLAR_PANEL.get(), pos, state), ModBlocks.ANDESITE_SOLAR_PANEL.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrassSolarPanelBlockEntity>> BRASS_SOLAR_PANEL =
            BLOCK_ENTITIES.register("brass_solar_panel", () -> BlockEntityType.Builder
                    .of((pos, state) -> new BrassSolarPanelBlockEntity(ModBlockEntities.BRASS_SOLAR_PANEL.get(), pos, state), ModBlocks.BRASS_SOLAR_PANEL.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatBatteryBlockEntity>> HEAT_BATTERY =
            BLOCK_ENTITIES.register("heat_battery", () -> BlockEntityType.Builder
                    .of((pos, state) -> new HeatBatteryBlockEntity(ModBlockEntities.HEAT_BATTERY.get(), pos, state), ModBlocks.HEAT_BATTERY.get())
                    .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}