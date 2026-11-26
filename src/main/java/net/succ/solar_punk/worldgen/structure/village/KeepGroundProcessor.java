package net.succ.solar_punk.worldgen.structure.village;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Skips the template's air at and below its floor, so open spots in a building's bottom layer
 * (a porch, the gaps around a tower's base) keep the ground instead of becoming holes. Air above
 * the floor is still placed, clearing the inside.
 */
public class KeepGroundProcessor extends StructureProcessor {
    public static final MapCodec<KeepGroundProcessor> CODEC =
            Codec.INT.fieldOf("floor").xmap(KeepGroundProcessor::new, p -> p.floor);

    private final int floor;

    public KeepGroundProcessor(int floor) {
        this.floor = floor;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos,
                                                             StructureTemplate.StructureBlockInfo raw,
                                                             StructureTemplate.StructureBlockInfo placed,
                                                             StructurePlaceSettings settings) {
        return raw.pos().getY() <= floor && raw.state().isAir() ? null : placed;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructures.KEEP_GROUND.get();
    }
}
