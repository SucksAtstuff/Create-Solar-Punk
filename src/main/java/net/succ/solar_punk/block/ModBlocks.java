package net.succ.solar_punk.block;

import net.succ.solar_punk.wire.ModWires;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.item.ModDataComponents;
import net.succ.offbyonelib.multiblock.MultiBlockLayerItem;
import net.succ.offbyonelib.registry.BlockRegistrar;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;
import net.succ.solar_punk.block.custom.BiofilterBlock;
import net.succ.solar_punk.block.custom.FiligreeRailingBlock;
import net.succ.solar_punk.block.custom.FiligreeRailingSlopeBlock;
import net.succ.solar_punk.block.custom.FiligreeRailingSteepSlopeBlock;
import net.succ.solar_punk.block.custom.SaltLampBlock;
import net.succ.solar_punk.block.custom.GardenLightBlock;
import net.succ.solar_punk.block.custom.StreetlampBlock;
import net.succ.solar_punk.block.custom.GrowLampBlock;
import net.succ.solar_punk.block.custom.HempCropBlock;
import net.succ.solar_punk.block.custom.SolarPlantBedBlock;
import net.succ.offbyonelib.wire.WireConnectorBlock;
import net.succ.offbyonelib.wire.WireSwitchBlock;
import net.succ.solar_punk.block.custom.ElectricMotorBlock;
import net.succ.solar_punk.block.custom.EndPlateBlock;
import net.succ.solar_punk.block.custom.FuelCellBlock;
import net.succ.offbyonelib.wire.RedstoneWireRelayBlock;
import net.succ.solar_punk.block.custom.GreenhouseControllerBlock;
import net.succ.solar_punk.block.custom.TrellisBlock;
import net.succ.solar_punk.block.custom.TrellisPlant;
import net.succ.solar_punk.block.custom.SolarShinglesBlock;
import net.succ.solar_punk.block.custom.RammedEarthColor;
import net.succ.solar_punk.block.custom.SolarGlassBlock;
import net.succ.solar_punk.block.custom.SolarGlassPaneBlock;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.block.custom.BiomassGasifierBlock;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.custom.CrystallizerBlock;
import net.succ.solar_punk.block.custom.ElectrolyzerBlock;
import net.succ.solar_punk.block.custom.DeadGrassBlock;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.custom.BioreactorBlock;
import net.succ.solar_punk.block.custom.BioreactorBaseBlock;
import net.succ.solar_punk.block.custom.FireboxBoilerBlock;
import net.succ.solar_punk.block.custom.FusionReactorCasingBlock;
import net.succ.solar_punk.block.custom.FusionReactorCasingGlassBlock;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.block.custom.GeyserCapBlock;
import net.succ.solar_punk.block.custom.GeyserVentBlock;
import net.succ.solar_punk.block.custom.GeyserWellBlock;
import net.succ.solar_punk.block.custom.RustedGeyserCapBlock;
import net.succ.solar_punk.block.custom.HeatBatteryBlock;
import net.succ.solar_punk.block.custom.KineticBatteryBlock;
import net.succ.solar_punk.block.custom.ParticleAcceleratorInputBlock;
import net.succ.solar_punk.block.custom.ParticleAcceleratorPipeBlock;
import net.succ.solar_punk.item.ParticleAcceleratorPipeItem;
import net.succ.solar_punk.block.custom.KineticSprinklerBlock;
import net.succ.solar_punk.block.custom.KineticSprinklerItem;
import net.succ.solar_punk.block.custom.SolarHeaterBlock;
import net.succ.solar_punk.block.custom.SolarMirrorBlock;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;
import net.succ.solar_punk.block.custom.TurbineBladeBlock;
import net.succ.solar_punk.block.custom.TurbineCasingBlock;
import net.succ.solar_punk.block.custom.TurbineCasingGlassBlock;
import net.succ.solar_punk.block.custom.TurbineRotorBlock;
import net.succ.solar_punk.item.FiligreeRailingItem;
import net.succ.solar_punk.item.ModItems;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SolarPunk.MODID);

    // Registers each block with its item, and tracks decorative ones for the Building Blocks
    // tab. Declared before the blocks below, which register through it.
    private static final BlockRegistrar REG = new BlockRegistrar(BLOCKS, ModItems.ITEMS);

    // Solar

    public static final DeferredBlock<AndesiteSolarPanelBlock> ANDESITE_SOLAR_PANEL = REG.block("andesite_solar_panel",
            () -> new AndesiteSolarPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<BrassSolarPanelBlock> BRASS_SOLAR_PANEL = REG.block("brass_solar_panel",
            () -> new BrassSolarPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    // Strung together with spools of wire to carry FE; see WireConnectorBlockEntity.
    public static final DeferredBlock<WireConnectorBlock> WIRE_CONNECTOR = REG.block("wire_connector",
            () -> new WireConnectorBlock(ModWires.SYSTEM, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    // Cuts a wire network in two at its posts; the relay is thrown by redstone. See WireSwitchBlockEntity.
    public static final DeferredBlock<WireSwitchBlock> WIRE_SWITCH = REG.block("wire_switch",
            () -> new WireSwitchBlock(ModWires.SYSTEM, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<RedstoneWireRelayBlock> REDSTONE_WIRE_RELAY = REG.block("redstone_wire_relay",
            () -> new RedstoneWireRelayBlock(ModWires.SYSTEM, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    // FE back into rotation; see ElectricMotorBlockEntity.
    public static final DeferredBlock<ElectricMotorBlock> ELECTRIC_MOTOR = REG.block("electric_motor",
            () -> new ElectricMotorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    // Water into Hydrogen and Oxygen with FE; see ElectrolyzerBlockEntity.
    public static final DeferredBlock<ElectrolyzerBlock> ELECTROLYZER = REG.block("electrolyzer",
            () -> new ElectrolyzerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    // Hydrogen and Oxygen back into FE: a row of Fuel Cells clamped between two End Plates.
    // The plates run the stack (see EndPlateBlockEntity); cells pass goggles and FE to them.
    public static final DeferredBlock<FuelCellBlock> FUEL_CELL = REG.block("fuel_cell",
            () -> new FuelCellBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final DeferredBlock<EndPlateBlock> END_PLATE = REG.block("end_plate",
            () -> new EndPlateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()));

    public static final DeferredBlock<KineticBatteryBlock> KINETIC_BATTERY = REG.block("kinetic_battery",
            () -> new KineticBatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<SolarHeaterBlock> SOLAR_HEATER = REG.block("solar_heater",
            () -> new SolarHeaterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<HeatBatteryBlock> HEAT_BATTERY = REG.block("heat_battery",
            () -> new HeatBatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .lightLevel(state -> state.getValue(HeatBatteryBlock.HEAT) * 4)));

    public static final DeferredBlock<CrystallizerBlock> CRYSTALLIZER = REG.block("crystallizer",
            () -> new CrystallizerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)));

    public static final DeferredBlock<SolarPowerTowerBlock> SOLAR_POWER_TOWER = REG.blockWithItem("solar_power_tower",
            () -> new SolarPowerTowerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()),
            block -> new MultiBlockLayerItem(block, ModBlockEntities.SOLAR_POWER_TOWER, new Item.Properties()));

    public static final DeferredBlock<SolarMirrorBlock> SOLAR_MIRROR = REG.block("solar_mirror",
            () -> new SolarMirrorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .strength(2.0f, 4.0f)));

    // Steam

    public static final DeferredBlock<FireboxBoilerBlock> FIREBOX_BOILER = REG.block("firebox_boiler",
            () -> new FireboxBoilerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .lightLevel(state -> state.getValue(FireboxBoilerBlock.LIT) ? 13 : 0)));

    public static final DeferredBlock<TurbineRotorBlock> TURBINE_ROTOR = REG.block("turbine_rotor",
            () -> new TurbineRotorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<TurbineCasingBlock> TURBINE_CASING = REG.block("turbine_casing",
            () -> new TurbineCasingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(4.0f, 8.0f)
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final DeferredBlock<TurbineCasingGlassBlock> TURBINE_CASING_GLASS = REG.block("turbine_casing_glass",
            () -> new TurbineCasingGlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .noOcclusion()
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final DeferredBlock<TurbineBladeBlock> ANDESITE_TURBINE_BLADE = REG.block("andesite_turbine_blade",
            () -> new TurbineBladeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<TurbineBladeBlock> BRASS_TURBINE_BLADE = REG.block("brass_turbine_blade",
            () -> new TurbineBladeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    // Biofuel and farming

    // Copper tier: turns a glass-roofed space in front of it into a greenhouse.
    public static final DeferredBlock<GreenhouseControllerBlock> GREENHOUSE_CONTROLLER = REG.block("greenhouse_controller",
            () -> new GreenhouseControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.COPPER)));

    // FE-powered grow light; see GrowLampBlockEntity.
    public static final DeferredBlock<GrowLampBlock> GROW_LAMP = REG.block("grow_lamp",
            () -> new GrowLampBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0f, 3.0f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(GrowLampBlock.LIT) ? 15 : 0)));

    // Raised bed with its own panel and grow light; see SolarPlantBedBlockEntity.
    public static final DeferredBlock<SolarPlantBedBlock> SOLAR_PLANT_BED = REG.block("solar_plant_bed",
            () -> new SolarPlantBedBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(SolarPlantBedBlock.LIT) ? 13 : 0)));

    public static final DeferredBlock<BiomassGasifierBlock> BIOMASS_GASIFIER = REG.block("biomass_gasifier",
            () -> new BiomassGasifierBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<FermentationVatBlock> FERMENTATION_VAT = REG.blockWithItem("fermentation_vat",
            () -> new FermentationVatBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .noOcclusion()),
            block -> new MultiBlockLayerItem(block, ModBlockEntities.FERMENTATION_VAT, new Item.Properties()));

    public static final DeferredBlock<BioreactorBlock> BIOREACTOR = REG.blockWithItem("bioreactor",
            () -> new BioreactorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn(Blocks::never)
                    .isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)),
            block -> new MultiBlockLayerItem(block, ModBlockEntities.BIOREACTOR, new Item.Properties()));

    public static final DeferredBlock<BioreactorBaseBlock> BIOREACTOR_BASE = REG.block("bioreactor_base",
            () -> new BioreactorBaseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)));

    public static final DeferredBlock<BiofuelEngineBlock> BIOFUEL_ENGINE = REG.block("biofuel_engine",
            () -> new BiofuelEngineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<KineticSprinklerBlock> KINETIC_SPRINKLER = REG.blockWithItem("kinetic_sprinkler",
            () -> new KineticSprinklerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(2.5f, 4.0f)
                    .noOcclusion()),
            block -> new KineticSprinklerItem(block, new Item.Properties()));

    // Two blocks tall; no item of its own, Hemp Seeds (ModItems) plant it.
    public static final DeferredBlock<HempCropBlock> HEMP = BLOCKS.register("hemp",
            () -> new HempCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));

    // Geothermal

    public static final DeferredBlock<GeyserCapBlock> GEYSER_CAP = REG.block("geyser_cap",
            () -> new GeyserCapBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<GeyserWellBlock> GEYSER_WELL = REG.block("geyser_well",
            () -> new GeyserWellBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    // Found on some natural vents; an axe scrapes it into a working Geyser Cap
    public static final DeferredBlock<RustedGeyserCapBlock> RUSTED_GEYSER_CAP = REG.block("rusted_geyser_cap",
            () -> new RustedGeyserCapBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<GeyserVentBlock> GEYSER_VENT = REG.block("geyser_vent",
            () -> new GeyserVentBlock(false, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1, 3600000f)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.BLOCK)));

    // Basalt Deltas variant: a stronger vent (Config.netherGeyserMultiplier) that erupts fire
    public static final DeferredBlock<GeyserVentBlock> NETHER_GEYSER_VENT = REG.block("nether_geyser_vent",
            () -> new GeyserVentBlock(true, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1, 3600000f)
                    .lightLevel(state -> 3)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.BLOCK)));

    // Fusion

    public static final DeferredBlock<FusionReactorCoreBlock> FUSION_REACTOR_CORE = REG.block("fusion_reactor_core",
            () -> new FusionReactorCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()
                    .strength(6.0f, 1200.0f)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)));

    // Fusion Reactor containment shell, one tier above the Turbine's industrial iron casing.
    public static final DeferredBlock<FusionReactorCasingBlock> FUSION_REACTOR_CASING = REG.block("fusion_reactor_casing",
            () -> new FusionReactorCasingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(6.0f, 12.0f)
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final DeferredBlock<FusionReactorCasingGlassBlock> FUSION_REACTOR_CASING_GLASS = REG.block("fusion_reactor_casing_glass",
            () -> new FusionReactorCasingGlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 9.0f)
                    .noOcclusion()
                    .sound(SoundType.NETHERITE_BLOCK)));

    // Blanket modules fill the band between the Core and the Casing shell. Plain blocks:
    // FusionReactorCoreBlockEntity's scan counts them and does all the ratio math.
    // Lithium is the fuel-efficient build, Beryllium burns more fuel for more steam.
    public static final DeferredBlock<Block> LITHIUM_BREEDER_MODULE = REG.block("lithium_breeder_module",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0f, 8.0f)));

    public static final DeferredBlock<Block> BERYLLIUM_REFLECTOR_MODULE = REG.block("beryllium_reflector_module",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(4.0f, 8.0f)));

    // Particle Accelerator ring pieces. Models only for now, no ring logic yet.
    // One item places every pipe shape (picked from a radial menu, see PipeShape), so the
    // bends and diagonal are registered without items of their own.
    public static final DeferredBlock<ParticleAcceleratorPipeBlock> PARTICLE_ACCELERATOR_PIPE = REG.blockWithItem("particle_accelerator_pipe",
            () -> new ParticleAcceleratorPipeBlock(acceleratorPipeProperties(), ParticleAcceleratorPipeBlock.Kind.STRAIGHT),
            block -> new ParticleAcceleratorPipeItem(block, new Item.Properties()));

    public static final DeferredBlock<ParticleAcceleratorPipeBlock> PARTICLE_ACCELERATOR_PIPE_BEND = BLOCKS.register("particle_accelerator_pipe_bend",
            () -> new ParticleAcceleratorPipeBlock(acceleratorPipeProperties(), ParticleAcceleratorPipeBlock.Kind.ANGLED));

    // The same bend curving the other way (block models can rotate but not mirror).
    public static final DeferredBlock<ParticleAcceleratorPipeBlock> PARTICLE_ACCELERATOR_PIPE_BEND_MIRRORED = BLOCKS.register("particle_accelerator_pipe_bend_mirrored",
            () -> new ParticleAcceleratorPipeBlock(acceleratorPipeProperties(), ParticleAcceleratorPipeBlock.Kind.ANGLED));

    public static final DeferredBlock<ParticleAcceleratorPipeBlock> PARTICLE_ACCELERATOR_PIPE_DIAGONAL = BLOCKS.register("particle_accelerator_pipe_diagonal",
            () -> new ParticleAcceleratorPipeBlock(acceleratorPipeProperties(), ParticleAcceleratorPipeBlock.Kind.ANGLED));

    // Where shaft power enters the ring (the shaft hookup comes with the ring logic).
    public static final DeferredBlock<ParticleAcceleratorInputBlock> PARTICLE_ACCELERATOR_INPUT = REG.block("particle_accelerator_input",
            () -> new ParticleAcceleratorInputBlock(acceleratorPipeProperties()));

    // Global warming

    public static final DeferredBlock<BiofilterBlock> BIOFILTER = REG.block("biofilter",
            () -> new BiofilterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 4.0f)));

    public static final DeferredBlock<Block> DEAD_GRASS_BLOCK = REG.block("dead_grass_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5f)
                    .sound(SoundType.GRASS)));

    public static final DeferredBlock<DeadGrassBlock> DEAD_GRASS = REG.block("dead_grass",
            () -> new DeadGrassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .replaceable()));

    public static final DeferredBlock<Block> RUINED_DIRT = REG.block("ruined_dirt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL)));

    public static final DeferredBlock<Block> ASH_BLOCK = REG.block("ash_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .requiresCorrectToolForDrops()
                    .strength(0.3f)
                    .sound(SoundType.SAND)));

    // Resources

    public static final DeferredBlock<Block> SALT_BLOCK = REG.block("salt_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 3.0f)));

    // Generates underground everywhere, with no biome gate. Drops Raw Lithium.
    public static final DeferredBlock<Block> LITHIUM_ORE = REG.block("lithium_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f)));

    // Same hardness and sound bump vanilla gives its deepslate ores. Generated wherever
    // the Lithium Ore feature hits deepslate.
    public static final DeferredBlock<Block> DEEPSLATE_LITHIUM_ORE = REG.block("deepslate_lithium_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 3.0f)
                    .sound(SoundType.DEEPSLATE)));

    // Storage blocks, named and tuned like vanilla's metal blocks (iron_block -> "Block of Iron").
    public static final DeferredBlock<Block> LITHIUM_BLOCK = REG.block("lithium_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)));

    public static final DeferredBlock<Block> BERYLLIUM_BLOCK = REG.block("beryllium_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)));

    // Building blocks: these go in the Building Blocks creative tab. Register them with
    // REG.decorative (stairs, slabs and walls already are), and add stairs/slab/wall/
    // chiseled sets to a family in datagen/ModBlockFamilies.

    public static final DeferredBlock<StairBlock> SALT_STAIRS = REG.stairs("salt_stairs", SALT_BLOCK);
    public static final DeferredBlock<SlabBlock> SALT_SLAB = REG.slab("salt_slab", SALT_BLOCK);

    public static final DeferredBlock<Block> POLISHED_SALT = REG.decorative("polished_salt",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(SALT_BLOCK.get())));
    public static final DeferredBlock<StairBlock> POLISHED_SALT_STAIRS = REG.stairs("polished_salt_stairs", POLISHED_SALT);
    public static final DeferredBlock<SlabBlock> POLISHED_SALT_SLAB = REG.slab("polished_salt_slab", POLISHED_SALT);
    public static final DeferredBlock<WallBlock> POLISHED_SALT_WALL = REG.wall("polished_salt_wall", POLISHED_SALT);

    public static final DeferredBlock<Block> SALT_BRICKS = REG.decorative("salt_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(SALT_BLOCK.get())));
    public static final DeferredBlock<StairBlock> SALT_BRICK_STAIRS = REG.stairs("salt_brick_stairs", SALT_BRICKS);
    public static final DeferredBlock<SlabBlock> SALT_BRICK_SLAB = REG.slab("salt_brick_slab", SALT_BRICKS);
    public static final DeferredBlock<WallBlock> SALT_BRICK_WALL = REG.wall("salt_brick_wall", SALT_BRICKS);

    public static final DeferredBlock<Block> CHISELED_SALT_BRICKS = REG.decorative("chiseled_salt_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(SALT_BRICKS.get())));

    // Pressed from soft Ash Blocks into stone-like blocks: pickaxe, not shovel.
    public static final DeferredBlock<Block> POLISHED_ASH = REG.decorative("polished_ash",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.POLISHED_TUFF)));
    public static final DeferredBlock<StairBlock> POLISHED_ASH_STAIRS = REG.stairs("polished_ash_stairs", POLISHED_ASH);
    public static final DeferredBlock<SlabBlock> POLISHED_ASH_SLAB = REG.slab("polished_ash_slab", POLISHED_ASH);
    public static final DeferredBlock<WallBlock> POLISHED_ASH_WALL = REG.wall("polished_ash_wall", POLISHED_ASH);

    public static final DeferredBlock<Block> ASH_BRICKS = REG.decorative("ash_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(POLISHED_ASH.get()).sound(SoundType.TUFF_BRICKS)));
    public static final DeferredBlock<StairBlock> ASH_BRICK_STAIRS = REG.stairs("ash_brick_stairs", ASH_BRICKS);
    public static final DeferredBlock<SlabBlock> ASH_BRICK_SLAB = REG.slab("ash_brick_slab", ASH_BRICKS);
    public static final DeferredBlock<WallBlock> ASH_BRICK_WALL = REG.wall("ash_brick_wall", ASH_BRICKS);

    public static final DeferredBlock<Block> CHISELED_ASH_BRICKS = REG.decorative("chiseled_ash_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(ASH_BRICKS.get())));

    // Hempcrete: Hemp Stalks set in lime. Light and soft for a stone (less than half the
    // blast resistance of Polished Ash), so it's a building block, not a bunker wall.
    public static final DeferredBlock<Block> HEMPCRETE = REG.decorative("hempcrete",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .requiresCorrectToolForDrops()
                    .strength(1.0f, 2.5f)
                    .sound(SoundType.PACKED_MUD)));
    public static final DeferredBlock<StairBlock> HEMPCRETE_STAIRS = REG.stairs("hempcrete_stairs", HEMPCRETE);
    public static final DeferredBlock<SlabBlock> HEMPCRETE_SLAB = REG.slab("hempcrete_slab", HEMPCRETE);
    public static final DeferredBlock<WallBlock> HEMPCRETE_WALL = REG.wall("hempcrete_wall", HEMPCRETE);

    public static final DeferredBlock<Block> POLISHED_HEMPCRETE = REG.decorative("polished_hempcrete",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(HEMPCRETE.get())));
    public static final DeferredBlock<StairBlock> POLISHED_HEMPCRETE_STAIRS = REG.stairs("polished_hempcrete_stairs", POLISHED_HEMPCRETE);
    public static final DeferredBlock<SlabBlock> POLISHED_HEMPCRETE_SLAB = REG.slab("polished_hempcrete_slab", POLISHED_HEMPCRETE);
    public static final DeferredBlock<WallBlock> POLISHED_HEMPCRETE_WALL = REG.wall("polished_hempcrete_wall", POLISHED_HEMPCRETE);

    public static final DeferredBlock<Block> CHISELED_POLISHED_HEMPCRETE = REG.decorative("chiseled_polished_hempcrete",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(POLISHED_HEMPCRETE.get())));

    public static final DeferredBlock<Block> POLISHED_HEMPCRETE_BRICKS = REG.decorative("polished_hempcrete_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(HEMPCRETE.get()).sound(SoundType.MUD_BRICKS)));
    public static final DeferredBlock<StairBlock> POLISHED_HEMPCRETE_BRICK_STAIRS = REG.stairs("polished_hempcrete_brick_stairs", POLISHED_HEMPCRETE_BRICKS);
    public static final DeferredBlock<SlabBlock> POLISHED_HEMPCRETE_BRICK_SLAB = REG.slab("polished_hempcrete_brick_slab", POLISHED_HEMPCRETE_BRICKS);
    public static final DeferredBlock<WallBlock> POLISHED_HEMPCRETE_BRICK_WALL = REG.wall("polished_hempcrete_brick_wall", POLISHED_HEMPCRETE_BRICKS);

    // Rammed Earth: per RammedEarthColor a block, bricks and chiseled bricks, the block and the
    // bricks each with stairs, slab and wall. Each shape shares one grey texture tinted per colour
    // (see ModClientEvents). Families are in datagen/ModBlockFamilies.
    public record RammedEarthSet(DeferredBlock<Block> block, DeferredBlock<StairBlock> stairs,
                                 DeferredBlock<SlabBlock> slab, DeferredBlock<WallBlock> wall,
                                 DeferredBlock<Block> bricks, DeferredBlock<StairBlock> brickStairs,
                                 DeferredBlock<SlabBlock> brickSlab, DeferredBlock<WallBlock> brickWall,
                                 DeferredBlock<Block> chiseledBricks) {
        public List<DeferredBlock<? extends Block>> all() {
            return List.of(block, stairs, slab, wall, bricks, brickStairs, brickSlab, brickWall, chiseledBricks);
        }
    }

    public static final Map<RammedEarthColor, RammedEarthSet> RAMMED_EARTH = registerRammedEarth();

    private static Map<RammedEarthColor, RammedEarthSet> registerRammedEarth() {
        Map<RammedEarthColor, RammedEarthSet> sets = new EnumMap<>(RammedEarthColor.class);
        for (RammedEarthColor color : RammedEarthColor.values()) {
            DeferredBlock<Block> block = REG.decorative(color.id,
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(color.mapColor)
                            .requiresCorrectToolForDrops()
                            .strength(1.5f, 6.0f)
                            .sound(SoundType.PACKED_MUD)));
            DeferredBlock<StairBlock> stairs = REG.stairs(color.id + "_stairs", block);
            DeferredBlock<SlabBlock> slab = REG.slab(color.id + "_slab", block);
            DeferredBlock<WallBlock> wall = REG.wall(color.id + "_wall", block);
            DeferredBlock<Block> bricks = REG.decorative(color.id + "_bricks",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(block.get()).sound(SoundType.MUD_BRICKS)));
            sets.put(color, new RammedEarthSet(block, stairs, slab, wall, bricks,
                    REG.stairs(color.id + "_brick_stairs", bricks),
                    REG.slab(color.id + "_brick_slab", bricks),
                    REG.wall(color.id + "_brick_wall", bricks),
                    REG.decorative("chiseled_" + color.id + "_bricks",
                            () -> new Block(BlockBehaviour.Properties.ofFullCopy(bricks.get())))));
        }
        return Collections.unmodifiableMap(sets);
    }

    // Filigree railings: one item per material places every shape (flat, slope, steep slope,
    // picked from a radial menu), so the slope blocks have no items of their own.
    public static final DeferredBlock<FiligreeRailingBlock> BRASS_FILIGREE_RAILING = REG.decorativeWithItem("brass_filigree_railing",
            () -> new FiligreeRailingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                    .mapColor(MapColor.TERRACOTTA_YELLOW)),
            block -> new FiligreeRailingItem(block, ModBlocks.BRASS_FILIGREE_RAILING_SLOPE,
                    ModBlocks.BRASS_FILIGREE_RAILING_STEEP_SLOPE, ModDataComponents.RAILING_SHAPE, new Item.Properties()));
    public static final DeferredBlock<FiligreeRailingSlopeBlock> BRASS_FILIGREE_RAILING_SLOPE = BLOCKS.register("brass_filigree_railing_slope",
            () -> new FiligreeRailingSlopeBlock(BlockBehaviour.Properties.ofFullCopy(BRASS_FILIGREE_RAILING.get())));
    public static final DeferredBlock<FiligreeRailingSteepSlopeBlock> BRASS_FILIGREE_RAILING_STEEP_SLOPE = BLOCKS.register("brass_filigree_railing_steep_slope",
            () -> new FiligreeRailingSteepSlopeBlock(BlockBehaviour.Properties.ofFullCopy(BRASS_FILIGREE_RAILING.get())));

    public static final DeferredBlock<FiligreeRailingBlock> ANDESITE_FILIGREE_RAILING = REG.decorativeWithItem("andesite_filigree_railing",
            () -> new FiligreeRailingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                    .mapColor(MapColor.STONE)),
            block -> new FiligreeRailingItem(block, ModBlocks.ANDESITE_FILIGREE_RAILING_SLOPE,
                    ModBlocks.ANDESITE_FILIGREE_RAILING_STEEP_SLOPE, ModDataComponents.RAILING_SHAPE, new Item.Properties()));
    public static final DeferredBlock<FiligreeRailingSlopeBlock> ANDESITE_FILIGREE_RAILING_SLOPE = BLOCKS.register("andesite_filigree_railing_slope",
            () -> new FiligreeRailingSlopeBlock(BlockBehaviour.Properties.ofFullCopy(ANDESITE_FILIGREE_RAILING.get())));
    public static final DeferredBlock<FiligreeRailingSteepSlopeBlock> ANDESITE_FILIGREE_RAILING_STEEP_SLOPE = BLOCKS.register("andesite_filigree_railing_steep_slope",
            () -> new FiligreeRailingSteepSlopeBlock(BlockBehaviour.Properties.ofFullCopy(ANDESITE_FILIGREE_RAILING.get())));

    public static final DeferredBlock<FiligreeRailingBlock> COPPER_FILIGREE_RAILING = REG.decorativeWithItem("copper_filigree_railing",
            () -> new FiligreeRailingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                    .mapColor(MapColor.COLOR_ORANGE)),
            block -> new FiligreeRailingItem(block, ModBlocks.COPPER_FILIGREE_RAILING_SLOPE,
                    ModBlocks.COPPER_FILIGREE_RAILING_STEEP_SLOPE, ModDataComponents.RAILING_SHAPE, new Item.Properties()));
    public static final DeferredBlock<FiligreeRailingSlopeBlock> COPPER_FILIGREE_RAILING_SLOPE = BLOCKS.register("copper_filigree_railing_slope",
            () -> new FiligreeRailingSlopeBlock(BlockBehaviour.Properties.ofFullCopy(COPPER_FILIGREE_RAILING.get())));
    public static final DeferredBlock<FiligreeRailingSteepSlopeBlock> COPPER_FILIGREE_RAILING_STEEP_SLOPE = BLOCKS.register("copper_filigree_railing_steep_slope",
            () -> new FiligreeRailingSteepSlopeBlock(BlockBehaviour.Properties.ofFullCopy(COPPER_FILIGREE_RAILING.get())));

    public static final DeferredBlock<FiligreeRailingBlock> IRON_FILIGREE_RAILING = REG.decorativeWithItem("iron_filigree_railing",
            () -> new FiligreeRailingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                    .mapColor(MapColor.METAL)),
            block -> new FiligreeRailingItem(block, ModBlocks.IRON_FILIGREE_RAILING_SLOPE,
                    ModBlocks.IRON_FILIGREE_RAILING_STEEP_SLOPE, ModDataComponents.RAILING_SHAPE, new Item.Properties()));
    public static final DeferredBlock<FiligreeRailingSlopeBlock> IRON_FILIGREE_RAILING_SLOPE = BLOCKS.register("iron_filigree_railing_slope",
            () -> new FiligreeRailingSlopeBlock(BlockBehaviour.Properties.ofFullCopy(IRON_FILIGREE_RAILING.get())));
    public static final DeferredBlock<FiligreeRailingSteepSlopeBlock> IRON_FILIGREE_RAILING_STEEP_SLOPE = BLOCKS.register("iron_filigree_railing_steep_slope",
            () -> new FiligreeRailingSteepSlopeBlock(BlockBehaviour.Properties.ofFullCopy(IRON_FILIGREE_RAILING.get())));

    // Glass that makes a little FE under open sky; see SolarGlassBlockEntity.
    public static final DeferredBlock<SolarGlassBlock> SOLAR_GLASS = REG.decorative("solar_glass",
            () -> new SolarGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final DeferredBlock<SolarGlassPaneBlock> SOLAR_GLASS_PANE = REG.decorative("solar_glass_pane",
            () -> new SolarGlassPaneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)));
    // Greenhouse glass: decorative for now; meant to be what seals a future greenhouse (see
    // docs/IDEAS.md). The panels are frosted: opaque-looking but they still let light through.
    public static final DeferredBlock<TransparentBlock> GREENHOUSE_GLASS = REG.decorative("greenhouse_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final DeferredBlock<IronBarsBlock> GREENHOUSE_GLASS_PANE = REG.decorative("greenhouse_glass_pane",
            () -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)));
    public static final DeferredBlock<TransparentBlock> CLEAR_GREENHOUSE_GLASS = REG.decorative("clear_greenhouse_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final DeferredBlock<IronBarsBlock> CLEAR_GREENHOUSE_GLASS_PANE = REG.decorative("clear_greenhouse_glass_pane",
            () -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)));
    public static final DeferredBlock<TransparentBlock> GREENHOUSE_PANEL = REG.decorative("greenhouse_panel",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final DeferredBlock<TransparentBlock> LARGE_GREENHOUSE_PANEL = REG.decorative("large_greenhouse_panel",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    // Decorative stepped roofing that bends into corners like stairs; see SolarShinglesBlock.
    public static final DeferredBlock<SolarShinglesBlock> SOLAR_SHINGLES = REG.decorative("solar_shingles",
            () -> new SolarShinglesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));
    // Solar Shingles made of Solar Glass: same block, glass models and Solar Glass output.
    public static final DeferredBlock<SolarShinglesBlock> SOLAR_GLASS_SHINGLES = REG.decorative("solar_glass_shingles",
            () -> new SolarShinglesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .mapColor(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<StairBlock> TURBINE_CASING_STAIRS = REG.stairs("turbine_casing_stairs", TURBINE_CASING);
    public static final DeferredBlock<SlabBlock> TURBINE_CASING_SLAB = REG.slab("turbine_casing_slab", TURBINE_CASING);
    public static final DeferredBlock<WallBlock> TURBINE_CASING_WALL = REG.wall("turbine_casing_wall", TURBINE_CASING);

    public static final DeferredBlock<StairBlock> FUSION_REACTOR_CASING_STAIRS = REG.stairs("fusion_reactor_casing_stairs", FUSION_REACTOR_CASING);
    public static final DeferredBlock<SlabBlock> FUSION_REACTOR_CASING_SLAB = REG.slab("fusion_reactor_casing_slab", FUSION_REACTOR_CASING);
    public static final DeferredBlock<WallBlock> FUSION_REACTOR_CASING_WALL = REG.wall("fusion_reactor_casing_wall", FUSION_REACTOR_CASING);

    public static final DeferredBlock<SaltLampBlock> SALT_LAMP = REG.decorative("salt_lamp",
            () -> new SaltLampBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.5f)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(SaltLampBlock.LIT) ? 12 : 0)));

    public static final DeferredBlock<GardenLightBlock> GARDEN_LIGHT = REG.decorative("garden_light",
            () -> new GardenLightBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.5f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(GardenLightBlock.LIT) ? 10 : 0)));

    // Two cells tall; only the upper half (the lamp head) gives off light.
    public static final DeferredBlock<StreetlampBlock> STREETLAMP = REG.decorative("streetlamp",
            () -> new StreetlampBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(2.0f, 4.0f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(StreetlampBlock.LIT)
                            && state.getValue(StreetlampBlock.HALF) == DoubleBlockHalf.UPPER ? 15 : 0)));

    // Scorched planks, made by applying Biochar to planks. Charring makes wood fire resistant,
    // so unlike planks it doesn't burn.
    public static final DeferredBlock<Block> CHARRED_TIMBER = REG.decorative("charred_timber",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)));
    public static final DeferredBlock<StairBlock> CHARRED_TIMBER_STAIRS = REG.stairs("charred_timber_stairs", CHARRED_TIMBER);
    public static final DeferredBlock<SlabBlock> CHARRED_TIMBER_SLAB = REG.slab("charred_timber_slab", CHARRED_TIMBER);

    // Green roofing from vanilla's Moss Block. With global warming on it absorbs pollution like leaves.
    public static final DeferredBlock<StairBlock> MOSS_STAIRS = REG.stairs("moss_stairs", () -> Blocks.MOSS_BLOCK);
    public static final DeferredBlock<SlabBlock> MOSS_SLAB = REG.slab("moss_slab", () -> Blocks.MOSS_BLOCK);

    public static final DeferredBlock<TrellisBlock> TRELLIS = REG.decorative("trellis",
            () -> new TrellisBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(0.4f)
                    .sound(SoundType.LADDER)
                    .lightLevel(state -> state.getValue(TrellisBlock.PLANT) == TrellisPlant.GLOW_BERRIES ? 14 : 0)
                    .noOcclusion()
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)));

    private static BlockBehaviour.Properties acceleratorPipeProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(4.0f, 8.0f)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    public static List<DeferredBlock<? extends Block>> decorativeBlocks() {
        return REG.decorativeBlocks();
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}