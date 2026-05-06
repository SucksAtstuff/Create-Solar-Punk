package net.succ.create_solar_powered.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.block.custom.HeatPipeBlock;
import net.succ.create_solar_powered.block.custom.SolarHeaterBlock;
import net.succ.create_solar_powered.block.custom.SolarPanelBlock;
import net.succ.create_solar_powered.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Create_solar_powered.MODID);

    public static final DeferredBlock<HeatPipeBlock> HEAT_PIPE = registerBlock("heat_pipe",
            () -> new HeatPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)));

    public static final DeferredBlock<SolarHeaterBlock> SOLAR_HEATER = registerBlock("solar_heater",
            () -> new SolarHeaterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)));

    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL = registerBlock("solar_panel",
            () -> new SolarPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}