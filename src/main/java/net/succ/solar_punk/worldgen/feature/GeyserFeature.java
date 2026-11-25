package net.succ.solar_punk.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.RustedGeyserCapBlock;
import net.succ.solar_punk.block.entity.custom.GeyserVentBlockEntity;
import net.minecraft.core.Direction;
import net.succ.solar_punk.SolarPunk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.succ.solar_punk.block.ModBlocks;

import java.util.List;

public class GeyserFeature extends Feature<NoneFeatureConfiguration> {

    private static final List<Block> PALETTE = List.of(
            Blocks.STONE, Blocks.ANDESITE, Blocks.ANDESITE,
            Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE
    );
    private static final List<Block> NETHER_PALETTE = List.of(
            Blocks.BASALT, Blocks.BASALT, Blocks.SMOOTH_BASALT,
            Blocks.BLACKSTONE, Blocks.BLACKSTONE, Blocks.MAGMA_BLOCK
    );
    // Where a Nether vent starts looking for a floor (this far above the placement height)
    // and how many blocks down it looks
    private static final int NETHER_FLOOR_SEARCH_UP = 16;
    private static final int NETHER_FLOOR_SEARCH = 64;

    private static final ResourceLocation FRAME = ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "geyser_frame");
    // The vent column in geyser_frame.nbt
    private static final BlockPos FRAME_CENTER = new BlockPos(4, 0, 4);
    private static final int FRAME_VENT_Y = 5;

    private final boolean nether;

    /** @param nether the Basalt Deltas version: a basalt and magma dome around a Nether Geyser Vent */
    public GeyserFeature(Codec<NoneFeatureConfiguration> codec, boolean nether) {
        super(codec);
        this.nether = nether;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();

        // Per-attempt spawn chance from config (0 = never, 100 = always). The biome
        // this attempt is even happening in is already filtered by geyser_biomes via
        // ConfigurableGeyserBiomeModifier, so this only thins out placement frequency.
        if (random.nextInt(100) >= (nether ? Config.netherGeyserSpawnChance : Config.geyserSpawnChance)) return false;

        int r = 3 + random.nextInt(2); // dome radius: 3 or 4
        // Now and then an Overworld vent comes with an old wooden frame over it (geyser_frame.nbt)
        // instead of the plain dome. The frame's mound is 9x9, so check the wider footprint.
        boolean frame = !nether && random.nextInt(100) < Config.geyserFrameChance;
        if (frame) r = 4;

        // getHeightmapPos returns the first air position above the surface; .below() is the surface block.
        // The Nether has a roof, so there the floor is found by scanning down from the placement height.
        BlockPos surface = nether
                ? findNetherFloor(level, origin, r)
                : level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin).below();
        if (surface == null) return false;
        List<Block> palette = nether ? NETHER_PALETTE : PALETTE;

        // Never generate on or in water (or any other fluid). WORLD_SURFACE_WG counts water
        // as surface, so the surface block itself is water on oceans/lakes/rivers. Also scan
        // the dome footprint so geysers don't spill into adjacent shorelines or ponds.
        // Nether vents skip this: Basalt Deltas are full of lava pools, and lava around the
        // dome is part of the look (the dome fills in any lava inside its own shape).
        if (!nether) for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                if (x * x + z * z > r * r) continue;
                for (int y = -1; y <= 1; y++) {
                    if (!level.getFluidState(surface.offset(x, y, z)).isEmpty()) return false;
                }
            }
        }

        if (frame && placeFrame(level, surface, random)) return true;

        for (int x = -r; x <= r; x++) {
            for (int y = -1; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    // Hollow chimney in the center, but leave the top block (y==r) solid so the vent has a seat
                    if (x == 0 && z == 0 && y >= 1 && y < r) continue;

                    float noise = random.nextFloat() * 0.6f;
                    if (x * x + y * y + z * z <= (r + noise) * (r + noise)) {
                        BlockPos p = surface.offset(x, y, z);
                        BlockState existing = level.getBlockState(p);
                        // Above ground: only fill air (and lava, for a Nether vent). At/below ground:
                        // replace anything except bedrock.
                        boolean fillable = existing.isAir() || (nether && !existing.getFluidState().isEmpty());
                        if (fillable || (y <= 0 && !existing.is(Blocks.BEDROCK))) {
                            level.setBlock(p, palette.get(random.nextInt(palette.size())).defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Place the vent flush with the dome peak (overwrites the stone cap placed by the loop)
        level.setBlock(surface.above(r),
                (nether ? ModBlocks.NETHER_GEYSER_VENT : ModBlocks.GEYSER_VENT).get().defaultBlockState(), 3);
        rollStrength(level, surface.above(r), random);
        // Some Overworld vents still wear an old cap someone left behind
        if (!nether && random.nextInt(100) < Config.rustedGeyserCapChance)
            level.setBlock(surface.above(r + 1), ModBlocks.RUSTED_GEYSER_CAP.get().defaultBlockState()
                    .setValue(RustedGeyserCapBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random)), 3);
        return true;
    }

    /**
     * Places the geyser frame centred on the vent column, its bottom layer one below the surface
     * like the dome's, turned a random way. False if the template is missing (a datapack removed
     * it), so the plain dome goes in instead.
     */
    private static boolean placeFrame(WorldGenLevel level, BlockPos surface, RandomSource random) {
        StructureTemplate template = level.getLevel().getServer().getStructureManager().get(FRAME).orElse(null);
        if (template == null) return false;
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.getRandom(random))
                .setRotationPivot(FRAME_CENTER)
                .setIgnoreEntities(true);
        BlockPos corner = surface.offset(-FRAME_CENTER.getX(), -1, -FRAME_CENTER.getZ());
        template.placeInWorld(level, corner, corner, settings, random, Block.UPDATE_CLIENTS);
        // The pivot keeps the vent column in place whichever way the frame turned
        rollStrength(level, corner.offset(FRAME_CENTER.getX(), FRAME_VENT_Y, FRAME_CENTER.getZ()), random);
        return true;
    }

    /** Gives a freshly placed vent a random strength (weak, normal or strong). */
    private static void rollStrength(WorldGenLevel level, BlockPos vent, RandomSource random) {
        if (level.getBlockEntity(vent) instanceof GeyserVentBlockEntity be)
            be.setStrength(GeyserVentBlockEntity.Strength.roll(random));
    }

    /**
     * The first floor near {@code origin} with room for the vent, so it never ends up buried in a
     * wall or stuck to the ceiling. Starts a little above the origin, since an origin inside the
     * ground would otherwise miss the floor just above it. Solid ground wins; lava (which the dome
     * fills in) is only used when there's none, since Basalt Deltas floors are mostly lava pools
     * and the lava sea would otherwise get most of the vents. Null if there is neither in reach.
     */
    private static BlockPos findNetherFloor(WorldGenLevel level, BlockPos origin, int r) {
        BlockPos lavaFloor = null;
        BlockPos.MutableBlockPos pos = origin.above(NETHER_FLOOR_SEARCH_UP).mutable();
        for (int i = 0; i < NETHER_FLOOR_SEARCH && pos.getY() > level.getMinBuildHeight() + 1; i++, pos.move(0, -1, 0)) {
            if (!level.getBlockState(pos).isAir()) continue;
            BlockPos floor = pos.below();
            BlockState below = level.getBlockState(floor);
            if (!below.isSolidRender(level, floor) && !below.getFluidState().is(FluidTags.LAVA)) continue;
            // The vent sits r above the floor: it and the block over it must be open. Anything
            // between is either filled by the dome or already solid.
            if (!level.getBlockState(floor.above(r)).isAir() || !level.getBlockState(floor.above(r + 1)).isAir()) continue;
            if (below.isSolidRender(level, floor)) return floor.immutable();
            if (lavaFloor == null) lavaFloor = floor.immutable();
        }
        return lavaFloor;
    }
}
