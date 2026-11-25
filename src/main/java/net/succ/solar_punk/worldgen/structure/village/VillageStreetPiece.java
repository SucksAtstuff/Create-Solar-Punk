package net.succ.solar_punk.worldgen.structure.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import org.jetbrains.annotations.Nullable;

/**
 * A stretch of street: a rectangle whose top block, wherever it lands on the terrain, becomes the
 * street block. Over water it uses the bridge block, or leaves a gap if there is none. It doesn't
 * reshape the terrain, so streets run up and down hills.
 */
public class VillageStreetPiece extends StructurePiece implements PieceBeardifierModifier {
    private final BlockStateProvider street;
    @Nullable
    private final BlockStateProvider bridge;

    public VillageStreetPiece(BoundingBox box, BlockStateProvider street, @Nullable BlockStateProvider bridge) {
        super(ModStructures.VILLAGE_STREET.get(), 0, box);
        this.street = street;
        this.bridge = bridge;
    }

    public VillageStreetPiece(CompoundTag tag) {
        super(ModStructures.VILLAGE_STREET.get(), tag);
        this.street = BlockStateProvider.CODEC.parse(NbtOps.INSTANCE, tag.get("Street")).getOrThrow();
        this.bridge = tag.contains("Bridge")
                ? BlockStateProvider.CODEC.parse(NbtOps.INSTANCE, tag.get("Bridge")).getOrThrow() : null;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.put("Street", BlockStateProvider.CODEC.encodeStart(NbtOps.INSTANCE, street).getOrThrow());
        if (bridge != null) tag.put("Bridge", BlockStateProvider.CODEC.encodeStart(NbtOps.INSTANCE, bridge).getOrThrow());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(boundingBox.minX(), chunkBox.minX()), maxX = Math.min(boundingBox.maxX(), chunkBox.maxX());
        int minZ = Math.max(boundingBox.minZ(), chunkBox.minZ()), maxZ = Math.min(boundingBox.maxZ(), chunkBox.maxZ());
        // During worldgen the WG heightmap is the bare terrain. A finished chunk (/place) no longer
        // has it, so use the one that skips leaves, and clear plants off the street there.
        Heightmap.Types surface = level.getChunk(chunkPos.x, chunkPos.z).hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG)
                ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                pos.set(x, level.getHeight(surface, x, z) - 1, z);
                BlockState top = level.getBlockState(pos);
                if (!top.getFluidState().isEmpty()) {
                    if (bridge != null && top.getFluidState().is(FluidTags.WATER)) {
                        level.setBlock(pos, bridge.getState(random, pos), 2);
                    }
                } else if (top.isSolidRender(level, pos)) {
                    level.setBlock(pos, street.getState(random, pos), 2);
                    BlockPos above = pos.above();
                    BlockState plant = level.getBlockState(above);
                    if (!plant.isAir() && plant.canBeReplaced() && plant.getFluidState().isEmpty()) {
                        level.setBlock(above, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    // Streets follow the ground instead of flattening it
    @Override
    public BoundingBox getBeardifierBox() {
        return boundingBox;
    }

    @Override
    public TerrainAdjustment getTerrainAdjustment() {
        return TerrainAdjustment.NONE;
    }

    @Override
    public int getGroundLevelDelta() {
        return 0;
    }
}
