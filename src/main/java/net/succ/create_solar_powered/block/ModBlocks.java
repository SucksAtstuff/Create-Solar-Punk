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
import net.succ.create_solar_powered.block.custom.AndesiteSolarPanelBlock;
import net.succ.create_solar_powered.block.custom.BrassSolarPanelBlock;
import net.succ.create_solar_powered.block.custom.HeatBatteryBlock;
import net.succ.create_solar_powered.block.custom.KineticBatteryBlock;
import net.succ.create_solar_powered.block.custom.SolarHeaterBlock;
import net.succ.create_solar_powered.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Create_solar_powered.MODID);

public static final DeferredBlock<Block> SALT_BLOCK = registerBlock("salt_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 3.0f)));

    public static final DeferredBlock<SolarHeaterBlock> SOLAR_HEATER = registerBlock("solar_heater",
            () -> new SolarHeaterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<KineticBatteryBlock> KINETIC_BATTERY = registerBlock("kinetic_battery",
            () -> new KineticBatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<AndesiteSolarPanelBlock> ANDESITE_SOLAR_PANEL = registerBlock("andesite_solar_panel",
            () -> new AndesiteSolarPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<BrassSolarPanelBlock> BRASS_SOLAR_PANEL = registerBlock("brass_solar_panel",
            () -> new BrassSolarPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<HeatBatteryBlock> HEAT_BATTERY = registerBlock("heat_battery",
            () -> new HeatBatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .lightLevel(state -> state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) ? 7 : 0)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}