package net.succ.solar_punk.datagen;

import net.succ.offbyonelib.datagen.BlockFamilies;
import net.succ.offbyonelib.datagen.AddonBlockTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.succ.offbyonelib.datagen.DatagenUtil.blockTag;

public class ModBlockTagProvider extends AddonBlockTagsProvider {

    private static final TagKey<Block> STORAGE_BLOCKS_SALT = blockTag("c", "storage_blocks/salt");
    private static final TagKey<Block> ORES_LITHIUM = blockTag("c", "ores/lithium");
    private static final TagKey<Block> STORAGE_BLOCKS_LITHIUM = blockTag("c", "storage_blocks/lithium");
    private static final TagKey<Block> STORAGE_BLOCKS_BERYLLIUM = blockTag("c", "storage_blocks/beryllium");

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Casing stairs, slabs and walls come off with a Wrench, like the casings themselves.
        for (BlockFamily family : List.of(ModBlockFamilies.TURBINE_CASING, ModBlockFamilies.FUSION_REACTOR_CASING))
            BlockFamilies.variants(family).values().forEach(block -> tag(blockTag("create", "wrench_pickup")).add(block));

        // Building block families: shape tags, and every variant is pickaxe-mineable (axe
        // for wooden ones, whose wooden_stairs/wooden_slabs tags are already in stairs/slabs,
        // and hoe for moss).
        ModBlockFamilies.ALL.forEach(family -> {
            boolean wooden = ModBlockFamilies.isWooden(family);
            familyTags(family, wooden, wooden ? BlockTags.MINEABLE_WITH_AXE
                    : ModBlockFamilies.isMoss(family) ? BlockTags.MINEABLE_WITH_HOE
                    : BlockTags.MINEABLE_WITH_PICKAXE);
        });

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.ANDESITE_SOLAR_PANEL.get())
                .add(ModBlocks.BRASS_SOLAR_PANEL.get())
                .add(ModBlocks.SOLAR_HEATER.get())
                .add(ModBlocks.CRYSTALLIZER.get())
                .add(ModBlocks.FIREBOX_BOILER.get())
                .add(ModBlocks.SALT_BLOCK.get())
                .add(ModBlocks.SALT_BRICKS.get())
                .add(ModBlocks.ASH_BRICKS.get())
                .add(ModBlocks.HEMPCRETE.get())
                .add(ModBlocks.POLISHED_HEMPCRETE_BRICKS.get())
                .add(ModBlocks.BRASS_FILIGREE_RAILING.get())
                .add(ModBlocks.BRASS_FILIGREE_RAILING_SLOPE.get())
                .add(ModBlocks.ANDESITE_FILIGREE_RAILING.get())
                .add(ModBlocks.ANDESITE_FILIGREE_RAILING_SLOPE.get())
                .add(ModBlocks.BRASS_FILIGREE_RAILING_STEEP_SLOPE.get())
                .add(ModBlocks.ANDESITE_FILIGREE_RAILING_STEEP_SLOPE.get())
                .add(ModBlocks.COPPER_FILIGREE_RAILING.get())
                .add(ModBlocks.COPPER_FILIGREE_RAILING_SLOPE.get())
                .add(ModBlocks.COPPER_FILIGREE_RAILING_STEEP_SLOPE.get())
                .add(ModBlocks.IRON_FILIGREE_RAILING.get())
                .add(ModBlocks.IRON_FILIGREE_RAILING_SLOPE.get())
                .add(ModBlocks.IRON_FILIGREE_RAILING_STEEP_SLOPE.get())
                .add(ModBlocks.SALT_LAMP.get())
                .add(ModBlocks.GARDEN_LIGHT.get())
                .add(ModBlocks.STREETLAMP.get())
                .add(ModBlocks.WIRE_CONNECTOR.get())
                .add(ModBlocks.WIRE_SWITCH.get())
                .add(ModBlocks.REDSTONE_WIRE_RELAY.get())
                .add(ModBlocks.ELECTRIC_MOTOR.get())
                .add(ModBlocks.ELECTROLYZER.get())
                .add(ModBlocks.FUEL_CELL.get())
                .add(ModBlocks.END_PLATE.get())
                .add(ModBlocks.GROW_LAMP.get())
                .add(ModBlocks.SOLAR_PLANT_BED.get())
                .add(ModBlocks.SOLAR_SHINGLES.get())
                .add(ModBlocks.GREENHOUSE_CONTROLLER.get())
                .add(ModBlocks.LITHIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get())
                .add(ModBlocks.LITHIUM_BLOCK.get())
                .add(ModBlocks.BERYLLIUM_BLOCK.get())
                .add(ModBlocks.KINETIC_BATTERY.get())
                .add(ModBlocks.HEAT_BATTERY.get())
                .add(ModBlocks.FERMENTATION_VAT.get())
                .add(ModBlocks.BIOREACTOR.get())
                .add(ModBlocks.BIOREACTOR_BASE.get())
                .add(ModBlocks.BIOMASS_GASIFIER.get())
                .add(ModBlocks.BIOFUEL_ENGINE.get())
                .add(ModBlocks.GEYSER_CAP.get())
                .add(ModBlocks.SOLAR_POWER_TOWER.get())
                .add(ModBlocks.SOLAR_MIRROR.get())
                .add(ModBlocks.RUINED_SOLAR_MIRROR.get())
                .add(ModBlocks.RUINED_SOLAR_HEATER.get())
                .add(ModBlocks.GEYSER_VENT.get())
                .add(ModBlocks.NETHER_GEYSER_VENT.get())
                .add(ModBlocks.GEYSER_WELL.get())
                .add(ModBlocks.RUSTED_GEYSER_CAP.get())
                .add(ModBlocks.BIOFILTER.get())
                .add(ModBlocks.KINETIC_SPRINKLER.get())
                .add(ModBlocks.TURBINE_CASING.get())
                .add(ModBlocks.TURBINE_CASING_GLASS.get())
                .add(ModBlocks.TURBINE_ROTOR.get())
                .add(ModBlocks.ANDESITE_TURBINE_BLADE.get())
                .add(ModBlocks.BRASS_TURBINE_BLADE.get())
                .add(ModBlocks.FUSION_REACTOR_CASING.get())
                .add(ModBlocks.FUSION_REACTOR_CASING_GLASS.get())
                .add(ModBlocks.LITHIUM_BREEDER_MODULE.get())
                .add(ModBlocks.BERYLLIUM_REFLECTOR_MODULE.get())
                .add(ModBlocks.FUSION_REACTOR_CORE.get())
                .add(ModBlocks.PARTICLE_ACCELERATOR_PIPE.get())
                .add(ModBlocks.PARTICLE_ACCELERATOR_PIPE_BEND.get())
                .add(ModBlocks.PARTICLE_ACCELERATOR_PIPE_BEND_MIRRORED.get())
                .add(ModBlocks.PARTICLE_ACCELERATOR_PIPE_DIAGONAL.get())
                .add(ModBlocks.PARTICLE_ACCELERATOR_INPUT.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.ASH_BLOCK.get())
                .add(ModBlocks.DEAD_GRASS_BLOCK.get())
                .add(ModBlocks.RUINED_DIRT.get());

        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.TRELLIS.get(), ModBlocks.CHARRED_TIMBER.get());
        tag(BlockTags.PLANKS).add(ModBlocks.CHARRED_TIMBER.get());
        tag(BlockTags.CLIMBABLE).add(ModBlocks.TRELLIS.get());
        // Not BlockTags.CROPS: bees grow crops with CropBlock.getStateForAge(), which would put
        // a lower half where the hemp's top is.
        tag(BlockTags.MAINTAINS_FARMLAND).add(ModBlocks.HEMP.get());

        tag(Tags.Blocks.GLASS_BLOCKS).add(ModBlocks.SOLAR_GLASS.get());
        tag(BlockTags.IMPERMEABLE).add(ModBlocks.SOLAR_GLASS.get());
        tag(Tags.Blocks.GLASS_PANES).add(ModBlocks.SOLAR_GLASS_PANE.get());
        tag(Tags.Blocks.GLASS_BLOCKS).add(ModBlocks.GREENHOUSE_GLASS.get(), ModBlocks.CLEAR_GREENHOUSE_GLASS.get(),
                ModBlocks.GREENHOUSE_PANEL.get(), ModBlocks.LARGE_GREENHOUSE_PANEL.get());
        tag(BlockTags.IMPERMEABLE).add(ModBlocks.GREENHOUSE_GLASS.get(), ModBlocks.CLEAR_GREENHOUSE_GLASS.get(),
                ModBlocks.GREENHOUSE_PANEL.get(), ModBlocks.LARGE_GREENHOUSE_PANEL.get());
        tag(Tags.Blocks.GLASS_PANES).add(ModBlocks.GREENHOUSE_GLASS_PANE.get(), ModBlocks.CLEAR_GREENHOUSE_GLASS_PANE.get());
        // Roofs a greenhouse's plants grow faster under: every greenhouse glass block, and Solar Glass.
        tag(ModTags.Blocks.SOLAR_PLANT_BED_BLACKLIST)
                .add(Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM, Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM,
                        Blocks.PITCHER_CROP, ModBlocks.HEMP.get())
                .addTag(BlockTags.TALL_FLOWERS);

        tag(ModTags.Blocks.GREENHOUSE_GLASS).add(ModBlocks.GREENHOUSE_GLASS.get(), ModBlocks.GREENHOUSE_GLASS_PANE.get(),
                ModBlocks.CLEAR_GREENHOUSE_GLASS.get(), ModBlocks.CLEAR_GREENHOUSE_GLASS_PANE.get(),
                ModBlocks.GREENHOUSE_PANEL.get(), ModBlocks.LARGE_GREENHOUSE_PANEL.get(),
                ModBlocks.SOLAR_GLASS.get(), ModBlocks.SOLAR_GLASS_PANE.get(), ModBlocks.SOLAR_GLASS_SHINGLES.get());
        // Serene Seasons: crops under greenhouse glass grow whatever the season. Harmless
        // without Serene Seasons installed, the tag is just never read.
        tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sereneseasons", "greenhouse_glass")))
                .addTag(ModTags.Blocks.GREENHOUSE_GLASS);

        sableWeights();

        tag(Tags.Blocks.STORAGE_BLOCKS).add(ModBlocks.SALT_BLOCK.get())
                .add(ModBlocks.LITHIUM_BLOCK.get())
                .add(ModBlocks.BERYLLIUM_BLOCK.get());
        tag(STORAGE_BLOCKS_SALT).add(ModBlocks.SALT_BLOCK.get());
        tag(STORAGE_BLOCKS_LITHIUM).add(ModBlocks.LITHIUM_BLOCK.get());
        tag(STORAGE_BLOCKS_BERYLLIUM).add(ModBlocks.BERYLLIUM_BLOCK.get());

        // Mid-late-game ore, gated behind iron tools.
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.LITHIUM_ORE.get()).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());
        tag(Tags.Blocks.ORES).add(ModBlocks.LITHIUM_ORE.get()).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());
        tag(ORES_LITHIUM).add(ModBlocks.LITHIUM_ORE.get()).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());
        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(ModBlocks.LITHIUM_ORE.get());
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());
        tag(Tags.Blocks.ORE_RATES_SINGULAR).add(ModBlocks.LITHIUM_ORE.get()).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());

        // Fusion Reactor multiblock membership, see ModTags.
        tag(ModTags.Blocks.REACTOR_CASING)
                .add(ModBlocks.FUSION_REACTOR_CASING.get())
                .add(ModBlocks.FUSION_REACTOR_CASING_GLASS.get());
        tag(ModTags.Blocks.REACTOR_BLANKET_LITHIUM).add(ModBlocks.LITHIUM_BREEDER_MODULE.get());
        tag(ModTags.Blocks.REACTOR_BLANKET_BERYLLIUM).add(ModBlocks.BERYLLIUM_REFLECTOR_MODULE.get());

        var pollutionSources = tag(ModTags.Blocks.POLLUTION_SOURCES)
                .add(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
                     ModBlocks.BIOFUEL_ENGINE.get(), ModBlocks.BIOMASS_GASIFIER.get(), ModBlocks.FIREBOX_BOILER.get())
                .addOptional(ResourceLocation.fromNamespaceAndPath("create", "lit_blaze_burner"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("create", "steam_engine"))
                // Create Diesel Generators
                .addOptional(ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "diesel_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "large_diesel_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "huge_diesel_engine"));
        // Create Aeronautics portable engines, one per dye colour
        for (DyeColor colour : DyeColor.values())
            pollutionSources.addOptional(ResourceLocation.fromNamespaceAndPath("simulated", colour.getName() + "_portable_engine"));
    }

    /**
     * How much our blocks weigh on a Sable sub-level (Create Aeronautics contraptions). Sable reads
     * its own block tags: normal blocks weigh 1, {@code sable:light} 0.5, {@code super_light} 0.25,
     * {@code heavy} 2 (stone) and {@code super_heavy} 4 (metal storage blocks, Create's flywheel);
     * {@code quarter_volume} makes thin blocks float like iron bars. These are only tag files in
     * Sable's namespace listing our own blocks, so without Sable nothing reads them. Slabs and
     * stairs are already light and half volume through #minecraft:slabs and #minecraft:stairs,
     * and glass panes super light through #c:glass_panes.
     */
    private void sableWeights() {
        // Lithium and Beryllium blocks are super heavy through #c:storage_blocks; salt is about as
        // dense as stone, so the Salt Block is only heavy
        tag(sable("super_heavy")).add(ModBlocks.KINETIC_BATTERY.get(), ModBlocks.HEAT_BATTERY.get(),
                        ModBlocks.FUSION_REACTOR_CORE.get())
                .remove(ModBlocks.SALT_BLOCK.get());

        var heavy = tag(sable("heavy")).add(ModBlocks.SALT_BLOCK.get(), ModBlocks.POLISHED_SALT.get(),
                ModBlocks.POLISHED_SALT_WALL.get(), ModBlocks.SALT_BRICKS.get(), ModBlocks.SALT_BRICK_WALL.get(),
                ModBlocks.CHISELED_SALT_BRICKS.get(),
                ModBlocks.TURBINE_CASING.get(), ModBlocks.TURBINE_CASING_GLASS.get(), ModBlocks.TURBINE_CASING_WALL.get(),
                ModBlocks.TURBINE_ROTOR.get(),
                ModBlocks.FUSION_REACTOR_CASING.get(), ModBlocks.FUSION_REACTOR_CASING_GLASS.get(),
                ModBlocks.FUSION_REACTOR_CASING_WALL.get(),
                ModBlocks.LITHIUM_BREEDER_MODULE.get(), ModBlocks.BERYLLIUM_REFLECTOR_MODULE.get(),
                ModBlocks.FIREBOX_BOILER.get(),
                ModBlocks.GEYSER_CAP.get(), ModBlocks.GEYSER_WELL.get(), ModBlocks.RUSTED_GEYSER_CAP.get());
        // Slabs of the heavy materials weigh what a full normal block does, like Sable's stone slabs
        var normal = tag(sable("normal")).add(ModBlocks.SALT_SLAB.get(), ModBlocks.POLISHED_SALT_SLAB.get(),
                ModBlocks.SALT_BRICK_SLAB.get(), ModBlocks.TURBINE_CASING_SLAB.get(),
                ModBlocks.FUSION_REACTOR_CASING_SLAB.get());
        ModBlocks.RAMMED_EARTH.values().forEach(set -> {
            heavy.add(set.block().get(), set.wall().get(), set.bricks().get(), set.brickWall().get(),
                    set.chiseledBricks().get());
            normal.add(set.slab().get(), set.brickSlab().get());
        });

        // Hempcrete and Ash are far lighter than stone
        tag(sable("light")).add(ModBlocks.ANDESITE_SOLAR_PANEL.get(), ModBlocks.BRASS_SOLAR_PANEL.get(),
                ModBlocks.SOLAR_MIRROR.get(), ModBlocks.RUINED_SOLAR_MIRROR.get(),
                ModBlocks.SOLAR_HEATER.get(), ModBlocks.RUINED_SOLAR_HEATER.get(),
                ModBlocks.SOLAR_SHINGLES.get(), ModBlocks.SOLAR_GLASS_SHINGLES.get(),
                ModBlocks.GREENHOUSE_PANEL.get(), ModBlocks.LARGE_GREENHOUSE_PANEL.get(),
                ModBlocks.KINETIC_SPRINKLER.get(), ModBlocks.STREETLAMP.get(), ModBlocks.SALT_LAMP.get(),
                ModBlocks.WIRE_SWITCH.get(), ModBlocks.REDSTONE_WIRE_RELAY.get(),
                ModBlocks.HEMPCRETE.get(), ModBlocks.HEMPCRETE_WALL.get(), ModBlocks.POLISHED_HEMPCRETE.get(),
                ModBlocks.POLISHED_HEMPCRETE_WALL.get(), ModBlocks.CHISELED_POLISHED_HEMPCRETE.get(),
                ModBlocks.POLISHED_HEMPCRETE_BRICKS.get(), ModBlocks.POLISHED_HEMPCRETE_BRICK_WALL.get(),
                ModBlocks.ASH_BLOCK.get(), ModBlocks.POLISHED_ASH.get(), ModBlocks.POLISHED_ASH_WALL.get(),
                ModBlocks.ASH_BRICKS.get(), ModBlocks.ASH_BRICK_WALL.get(), ModBlocks.CHISELED_ASH_BRICKS.get());

        // Thin and small, like Sable's iron bars and lanterns
        Block[] thin = {ModBlocks.WIRE_CONNECTOR.get(), ModBlocks.GROW_LAMP.get(), ModBlocks.GARDEN_LIGHT.get(),
                ModBlocks.TRELLIS.get(), ModBlocks.WIRE_SWITCH.get(), ModBlocks.REDSTONE_WIRE_RELAY.get(),
                ModBlocks.BRASS_FILIGREE_RAILING.get(), ModBlocks.BRASS_FILIGREE_RAILING_SLOPE.get(),
                ModBlocks.BRASS_FILIGREE_RAILING_STEEP_SLOPE.get(),
                ModBlocks.ANDESITE_FILIGREE_RAILING.get(), ModBlocks.ANDESITE_FILIGREE_RAILING_SLOPE.get(),
                ModBlocks.ANDESITE_FILIGREE_RAILING_STEEP_SLOPE.get(),
                ModBlocks.COPPER_FILIGREE_RAILING.get(), ModBlocks.COPPER_FILIGREE_RAILING_SLOPE.get(),
                ModBlocks.COPPER_FILIGREE_RAILING_STEEP_SLOPE.get(),
                ModBlocks.IRON_FILIGREE_RAILING.get(), ModBlocks.IRON_FILIGREE_RAILING_SLOPE.get(),
                ModBlocks.IRON_FILIGREE_RAILING_STEEP_SLOPE.get()};
        tag(sable("quarter_volume")).add(thin);
        tag(sable("super_light")).add(ModBlocks.WIRE_CONNECTOR.get(), ModBlocks.GROW_LAMP.get(),
                ModBlocks.GARDEN_LIGHT.get(), ModBlocks.TRELLIS.get(),
                ModBlocks.BRASS_FILIGREE_RAILING.get(), ModBlocks.BRASS_FILIGREE_RAILING_SLOPE.get(),
                ModBlocks.BRASS_FILIGREE_RAILING_STEEP_SLOPE.get(),
                ModBlocks.ANDESITE_FILIGREE_RAILING.get(), ModBlocks.ANDESITE_FILIGREE_RAILING_SLOPE.get(),
                ModBlocks.ANDESITE_FILIGREE_RAILING_STEEP_SLOPE.get(),
                ModBlocks.COPPER_FILIGREE_RAILING.get(), ModBlocks.COPPER_FILIGREE_RAILING_SLOPE.get(),
                ModBlocks.COPPER_FILIGREE_RAILING_STEEP_SLOPE.get(),
                ModBlocks.IRON_FILIGREE_RAILING.get(), ModBlocks.IRON_FILIGREE_RAILING_SLOPE.get(),
                ModBlocks.IRON_FILIGREE_RAILING_STEEP_SLOPE.get());
    }

    private static TagKey<Block> sable(String name) {
        return blockTag("sable", name);
    }
}
