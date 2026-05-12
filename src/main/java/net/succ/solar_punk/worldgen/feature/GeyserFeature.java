package net.succ.solar_punk.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.succ.solar_punk.block.ModBlocks;

import java.util.List;

public class GeyserFeature extends Feature<NoneFeatureConfiguration> {

    private static final List<Block> PALETTE = List.of(
            Blocks.STONE, Blocks.ANDESITE, Blocks.ANDESITE,
            Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE
    );

    public GeyserFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();

        // getHeightmapPos returns the first air position above the surface; .below() is the surface block
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin).below();

        int r = 3 + random.nextInt(2); // dome radius: 3 or 4

        for (int x = -r; x <= r; x++) {
            for (int y = -1; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    // Hollow chimney in center — but leave the top block (y==r) solid so the vent has a seat
                    if (x == 0 && z == 0 && y >= 1 && y < r) continue;

                    float noise = random.nextFloat() * 0.6f;
                    if (x * x + y * y + z * z <= (r + noise) * (r + noise)) {
                        BlockPos p = surface.offset(x, y, z);
                        BlockState existing = level.getBlockState(p);
                        // Above ground: only fill air. At/below ground: replace anything except bedrock.
                        if (existing.isAir() || (y <= 0 && !existing.is(Blocks.BEDROCK))) {
                            level.setBlock(p, PALETTE.get(random.nextInt(PALETTE.size())).defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Place the vent flush with the dome peak (overwrites the stone cap placed by the loop)
        level.setBlock(surface.above(r), ModBlocks.GEYSER_VENT.get().defaultBlockState(), 3);
        return true;
    }
}
