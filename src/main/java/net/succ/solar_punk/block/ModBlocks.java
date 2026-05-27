package net.succ.solar_punk.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.block.custom.BiomassGasifierBlock;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.custom.FermentationVatItem;
import net.succ.solar_punk.block.custom.GeyserCapBlock;
import net.succ.solar_punk.block.custom.GeyserVentBlock;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.block.custom.KineticBatteryBlock;
import net.succ.solar_punk.block.custom.SolarHeaterBlock;
import net.succ.solar_punk.block.custom.SolarMirrorBlock;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;
import net.succ.solar_punk.block.custom.SolarPowerTowerItem;
import net.succ.solar_punk.block.custom.BiofilterBlock;
import net.succ.solar_punk.block.custom.KineticSprinklerBlock;
import net.succ.solar_punk.block.custom.KineticSprinklerItem;
import net.succ.solar_punk.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SolarPunk.MODID);

    public static final DeferredBlock<FermentationVatBlock> FERMENTATION_VAT = registerBlockCustomItem("fermentation_vat",
            () -> new FermentationVatBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .noOcclusion()),
            block -> new FermentationVatItem(block, new Item.Properties()));

    public static final DeferredBlock<BiomassGasifierBlock> BIOMASS_GASIFIER = registerBlock("biomass_gasifier",
            () -> new BiomassGasifierBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<BiofuelEngineBlock> BIOFUEL_ENGINE = registerBlock("biofuel_engine",
            () -> new BiofuelEngineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

public static final DeferredBlock<GeyserCapBlock> GEYSER_CAP = registerBlock("geyser_cap",
            () -> new GeyserCapBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

public static final DeferredBlock<GeyserVentBlock> GEYSER_VENT = registerBlock("geyser_vent",
            () -> new GeyserVentBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1, 3600000f)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.BLOCK)));

public static final DeferredBlock<Block> SALT_BLOCK = registerBlock("salt_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 3.0f)));

    public static final DeferredBlock<Block> DEAD_GRASS_BLOCK = registerBlock("dead_grass_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5f)
                    .sound(net.minecraft.world.level.block.SoundType.GRASS)));

    public static final DeferredBlock<Block> RUINED_DIRT = registerBlock("ruined_dirt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(net.minecraft.world.level.block.SoundType.GRAVEL)));

    public static final DeferredBlock<Block> ASH_BLOCK = registerBlock("ash_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(0.3f)
                    .sound(net.minecraft.world.level.block.SoundType.SAND)));

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
                    .lightLevel(state -> state.getValue(HeatBatteryBlock.HEAT) * 4)));

    public static final DeferredBlock<SolarPowerTowerBlock> SOLAR_POWER_TOWER = registerBlockCustomItem("solar_power_tower",
            () -> new SolarPowerTowerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()),
            block -> new SolarPowerTowerItem(block, new Item.Properties()));

    public static final DeferredBlock<SolarMirrorBlock> SOLAR_MIRROR = registerBlock("solar_mirror",
            () -> new SolarMirrorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .strength(2.0f, 4.0f)));

    public static final DeferredBlock<BiofilterBlock> BIOFILTER = registerBlock("biofilter",
            () -> new BiofilterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 4.0f)));

    public static final DeferredBlock<KineticSprinklerBlock> KINETIC_SPRINKLER = registerBlockCustomItem("kinetic_sprinkler",
            () -> new KineticSprinklerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(2.5f, 4.0f)
                    .noOcclusion()),
            block -> new KineticSprinklerItem(block, new Item.Properties()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockCustomItem(String name, Supplier<T> block,
            java.util.function.Function<T, ? extends BlockItem> itemFactory) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> itemFactory.apply(toReturn.get()));
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}