package net.succ.create_solar_powered.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.block.ModBlocks;
import net.succ.create_solar_powered.block.entity.custom.HeatPipeBlockEntity;
import net.succ.create_solar_powered.block.entity.custom.SolarHeaterBlockEntity;
import net.succ.create_solar_powered.block.entity.custom.SolarPanelBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Create_solar_powered.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatPipeBlockEntity>> HEAT_PIPE =
            BLOCK_ENTITIES.register("heat_pipe", () -> BlockEntityType.Builder
                    .of((pos, state) -> new HeatPipeBlockEntity(ModBlockEntities.HEAT_PIPE.get(), pos, state), ModBlocks.HEAT_PIPE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarHeaterBlockEntity>> SOLAR_HEATER =
            BLOCK_ENTITIES.register("solar_heater", () -> BlockEntityType.Builder
                    .of((pos, state) -> new SolarHeaterBlockEntity(ModBlockEntities.SOLAR_HEATER.get(), pos, state), ModBlocks.SOLAR_HEATER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL =
            BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder
                    .of((pos, state) -> new SolarPanelBlockEntity(ModBlockEntities.SOLAR_PANEL.get(), pos, state), ModBlocks.SOLAR_PANEL.get())
                    .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
