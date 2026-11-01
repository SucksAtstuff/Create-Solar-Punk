package net.succ.solar_punk.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;
import net.succ.solar_punk.block.custom.BiofilterBlock;
import net.succ.solar_punk.block.custom.SaltLampBlock;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.block.custom.BiomassGasifierBlock;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.custom.CrystallizerBlock;
import net.succ.solar_punk.block.custom.DeadGrassBlock;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.custom.FermentationVatItem;
import net.succ.solar_punk.block.custom.FireboxBoilerBlock;
import net.succ.solar_punk.block.custom.FusionReactorCasingBlock;
import net.succ.solar_punk.block.custom.FusionReactorCasingGlassBlock;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.block.custom.GeyserCapBlock;
import net.succ.solar_punk.block.custom.GeyserVentBlock;
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
import net.succ.solar_punk.block.custom.SolarPowerTowerItem;
import net.succ.solar_punk.block.custom.TurbineBladeBlock;
import net.succ.solar_punk.block.custom.TurbineCasingBlock;
import net.succ.solar_punk.block.custom.TurbineCasingGlassBlock;
import net.succ.solar_punk.block.custom.TurbineRotorBlock;
import net.succ.solar_punk.item.ModItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SolarPunk.MODID);

    // Filled by registerDecorative (and the stairs/slab/wall helpers) as the blocks below
    // register, so it has to be declared first.
    private static final List<DeferredBlock<? extends Block>> DECORATIVE_BLOCKS = new ArrayList<>();

    // Solar

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

    public static final DeferredBlock<KineticBatteryBlock> KINETIC_BATTERY = registerBlock("kinetic_battery",
            () -> new KineticBatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<SolarHeaterBlock> SOLAR_HEATER = registerBlock("solar_heater",
            () -> new SolarHeaterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<HeatBatteryBlock> HEAT_BATTERY = registerBlock("heat_battery",
            () -> new HeatBatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .lightLevel(state -> state.getValue(HeatBatteryBlock.HEAT) * 4)));

    public static final DeferredBlock<CrystallizerBlock> CRYSTALLIZER = registerBlock("crystallizer",
            () -> new CrystallizerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)));

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

    // Steam

    public static final DeferredBlock<FireboxBoilerBlock> FIREBOX_BOILER = registerBlock("firebox_boiler",
            () -> new FireboxBoilerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .lightLevel(state -> state.getValue(FireboxBoilerBlock.LIT) ? 13 : 0)));

    public static final DeferredBlock<TurbineRotorBlock> TURBINE_ROTOR = registerBlock("turbine_rotor",
            () -> new TurbineRotorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<TurbineCasingBlock> TURBINE_CASING = registerBlock("turbine_casing",
            () -> new TurbineCasingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(4.0f, 8.0f)
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final DeferredBlock<TurbineCasingGlassBlock> TURBINE_CASING_GLASS = registerBlock("turbine_casing_glass",
            () -> new TurbineCasingGlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .noOcclusion()
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final DeferredBlock<TurbineBladeBlock> ANDESITE_TURBINE_BLADE = registerBlock("andesite_turbine_blade",
            () -> new TurbineBladeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<TurbineBladeBlock> BRASS_TURBINE_BLADE = registerBlock("brass_turbine_blade",
            () -> new TurbineBladeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    // Biofuel and farming

    public static final DeferredBlock<BiomassGasifierBlock> BIOMASS_GASIFIER = registerBlock("biomass_gasifier",
            () -> new BiomassGasifierBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<FermentationVatBlock> FERMENTATION_VAT = registerBlockCustomItem("fermentation_vat",
            () -> new FermentationVatBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f)
                    .noOcclusion()),
            block -> new FermentationVatItem(block, new Item.Properties()));

    public static final DeferredBlock<BiofuelEngineBlock> BIOFUEL_ENGINE = registerBlock("biofuel_engine",
            () -> new BiofuelEngineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f)
                    .noOcclusion()));

    public static final DeferredBlock<KineticSprinklerBlock> KINETIC_SPRINKLER = registerBlockCustomItem("kinetic_sprinkler",
            () -> new KineticSprinklerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(2.5f, 4.0f)
                    .noOcclusion()),
            block -> new KineticSprinklerItem(block, new Item.Properties()));

    // Geothermal

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

    // Fusion

    public static final DeferredBlock<FusionReactorCoreBlock> FUSION_REACTOR_CORE = registerBlock("fusion_reactor_core",
            () -> new FusionReactorCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()
                    .strength(6.0f, 1200.0f)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)));

    // Fusion Reactor containment shell, one tier above the Turbine's industrial iron casing.
    public static final DeferredBlock<FusionReactorCasingBlock> FUSION_REACTOR_CASING = registerBlock("fusion_reactor_casing",
            () -> new FusionReactorCasingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(6.0f, 12.0f)
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final DeferredBlock<FusionReactorCasingGlassBlock> FUSION_REACTOR_CASING_GLASS = registerBlock("fusion_reactor_casing_glass",
            () -> new FusionReactorCasingGlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 9.0f)
                    .noOcclusion()
                    .sound(SoundType.NETHERITE_BLOCK)));

    // Blanket modules fill the band between the Core and the Casing shell. Plain blocks:
    // FusionReactorCoreBlockEntity's scan counts them and does all the ratio math.
    // Lithium is the fuel-efficient build, Beryllium burns more fuel for more steam.
    public static final DeferredBlock<Block> LITHIUM_BREEDER_MODULE = registerBlock("lithium_breeder_module",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
                    .strength(4.0f, 8.0f)));

    public static final DeferredBlock<Block> BERYLLIUM_REFLECTOR_MODULE = registerBlock("beryllium_reflector_module",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .strength(4.0f, 8.0f)));

    // Particle Accelerator ring pieces. Models only for now, no ring logic yet.
    // One item places every pipe shape (picked from a radial menu, see PipeShape), so the
    // bends and diagonal are registered without items of their own.
    public static final DeferredBlock<ParticleAcceleratorPipeBlock> PARTICLE_ACCELERATOR_PIPE = registerBlockCustomItem("particle_accelerator_pipe",
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
    public static final DeferredBlock<ParticleAcceleratorInputBlock> PARTICLE_ACCELERATOR_INPUT = registerBlock("particle_accelerator_input",
            () -> new ParticleAcceleratorInputBlock(acceleratorPipeProperties()));

    // Global warming

    public static final DeferredBlock<BiofilterBlock> BIOFILTER = registerBlock("biofilter",
            () -> new BiofilterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 4.0f)));

    public static final DeferredBlock<Block> DEAD_GRASS_BLOCK = registerBlock("dead_grass_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5f)
                    .sound(SoundType.GRASS)));

    public static final DeferredBlock<DeadGrassBlock> DEAD_GRASS = registerBlock("dead_grass",
            () -> new DeadGrassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .replaceable()));

    public static final DeferredBlock<Block> RUINED_DIRT = registerBlock("ruined_dirt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL)));

    public static final DeferredBlock<Block> ASH_BLOCK = registerBlock("ash_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(0.3f)
                    .sound(SoundType.SAND)));

    // Resources

    public static final DeferredBlock<Block> SALT_BLOCK = registerBlock("salt_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .requiresCorrectToolForDrops()
                    .strength(1.5f, 3.0f)));

    // Generates underground everywhere, with no biome gate. Drops Raw Lithium.
    public static final DeferredBlock<Block> LITHIUM_ORE = registerBlock("lithium_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f)));

    // Same hardness and sound bump vanilla gives its deepslate ores. Not generated yet:
    // the ore feature still places LITHIUM_ORE in deepslate too.
    public static final DeferredBlock<Block> DEEPSLATE_LITHIUM_ORE = registerBlock("deepslate_lithium_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 3.0f)
                    .sound(SoundType.DEEPSLATE)));

    // Storage blocks, named and tuned like vanilla's metal blocks (iron_block -> "Block of Iron").
    public static final DeferredBlock<Block> LITHIUM_BLOCK = registerBlock("lithium_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)));

    public static final DeferredBlock<Block> BERYLLIUM_BLOCK = registerBlock("beryllium_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)));

    // Building blocks: these go in the Building Blocks creative tab. Register them with
    // registerDecorative (stairs, slabs and walls already are), and add stairs/slab/wall/
    // chiseled sets to a family in datagen/ModBlockFamilies.

    public static final DeferredBlock<StairBlock> SALT_STAIRS = registerStairs("salt_stairs", SALT_BLOCK);
    public static final DeferredBlock<SlabBlock> SALT_SLAB = registerSlab("salt_slab", SALT_BLOCK);

    public static final DeferredBlock<Block> POLISHED_SALT = registerDecorative("polished_salt",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(SALT_BLOCK.get())));
    public static final DeferredBlock<StairBlock> POLISHED_SALT_STAIRS = registerStairs("polished_salt_stairs", POLISHED_SALT);
    public static final DeferredBlock<SlabBlock> POLISHED_SALT_SLAB = registerSlab("polished_salt_slab", POLISHED_SALT);
    public static final DeferredBlock<WallBlock> POLISHED_SALT_WALL = registerWall("polished_salt_wall", POLISHED_SALT);

    public static final DeferredBlock<Block> SALT_BRICKS = registerDecorative("salt_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(SALT_BLOCK.get())));
    public static final DeferredBlock<StairBlock> SALT_BRICK_STAIRS = registerStairs("salt_brick_stairs", SALT_BRICKS);
    public static final DeferredBlock<SlabBlock> SALT_BRICK_SLAB = registerSlab("salt_brick_slab", SALT_BRICKS);
    public static final DeferredBlock<WallBlock> SALT_BRICK_WALL = registerWall("salt_brick_wall", SALT_BRICKS);

    public static final DeferredBlock<Block> CHISELED_SALT_BRICKS = registerDecorative("chiseled_salt_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(SALT_BRICKS.get())));

    public static final DeferredBlock<StairBlock> TURBINE_CASING_STAIRS = registerStairs("turbine_casing_stairs", TURBINE_CASING);
    public static final DeferredBlock<SlabBlock> TURBINE_CASING_SLAB = registerSlab("turbine_casing_slab", TURBINE_CASING);
    public static final DeferredBlock<WallBlock> TURBINE_CASING_WALL = registerWall("turbine_casing_wall", TURBINE_CASING);

    public static final DeferredBlock<StairBlock> FUSION_REACTOR_CASING_STAIRS = registerStairs("fusion_reactor_casing_stairs", FUSION_REACTOR_CASING);
    public static final DeferredBlock<SlabBlock> FUSION_REACTOR_CASING_SLAB = registerSlab("fusion_reactor_casing_slab", FUSION_REACTOR_CASING);
    public static final DeferredBlock<WallBlock> FUSION_REACTOR_CASING_WALL = registerWall("fusion_reactor_casing_wall", FUSION_REACTOR_CASING);

    public static final DeferredBlock<SaltLampBlock> SALT_LAMP = registerDecorative("salt_lamp",
            () -> new SaltLampBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.5f)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(SaltLampBlock.LIT) ? 12 : 0)));

    private static BlockBehaviour.Properties acceleratorPipeProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(4.0f, 8.0f)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    // Blocks shown in the Building Blocks creative tab instead of the main one.
    private static <T extends Block> DeferredBlock<T> registerDecorative(String name, Supplier<T> block) {
        DeferredBlock<T> registered = registerBlock(name, block);
        DECORATIVE_BLOCKS.add(registered);
        return registered;
    }

    public static List<DeferredBlock<? extends Block>> decorativeBlocks() {
        return Collections.unmodifiableList(DECORATIVE_BLOCKS);
    }

    // Stairs, slabs and walls copy every property of the block they're made from.
    private static DeferredBlock<StairBlock> registerStairs(String name, DeferredBlock<? extends Block> base) {
        return registerDecorative(name, () -> new StairBlock(base.get().defaultBlockState(),
                BlockBehaviour.Properties.ofFullCopy(base.get())));
    }

    private static DeferredBlock<SlabBlock> registerSlab(String name, DeferredBlock<? extends Block> base) {
        return registerDecorative(name, () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(base.get())));
    }

    private static DeferredBlock<WallBlock> registerWall(String name, DeferredBlock<? extends Block> base) {
        return registerDecorative(name, () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(base.get()).forceSolidOn()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlockCustomItem(String name, Supplier<T> block,
            Function<T, ? extends BlockItem> itemFactory) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> itemFactory.apply(toReturn.get()));
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}