package net.succ.solar_punk.worldgen.structure.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;

/**
 * One house (or the centre piece) of a {@link SimpleVillageStructure}: a whole template, turned.
 * The terrain is built up under it and cleared above its floor, like a vanilla village house.
 */
public class VillageHousePiece extends TemplateStructurePiece implements PieceBeardifierModifier {
    private final int floor;

    public VillageHousePiece(StructureTemplateManager manager, ResourceLocation template, BlockPos origin, Rotation rotation, int floor) {
        super(ModStructures.VILLAGE_HOUSE.get(), 0, manager, template, template.toString(), settings(rotation), origin);
        this.floor = floor;
    }

    public VillageHousePiece(StructureTemplateManager manager, CompoundTag tag) {
        super(ModStructures.VILLAGE_HOUSE.get(), tag, manager, id -> settings(Rotation.valueOf(tag.getString("Rot"))));
        this.floor = tag.getInt("Floor");
    }

    private static StructurePlaceSettings settings(Rotation rotation) {
        return new StructurePlaceSettings().setRotation(rotation).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Rot", placeSettings.getRotation().name());
        tag.putInt("Floor", floor);
    }

    @Override
    protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {}

    @Override
    public BoundingBox getBeardifierBox() {
        return boundingBox;
    }

    @Override
    public TerrainAdjustment getTerrainAdjustment() {
        return TerrainAdjustment.BEARD_THIN;
    }

    @Override
    public int getGroundLevelDelta() {
        return floor;
    }
}
