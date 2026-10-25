package net.succ.solar_punk.block.custom;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.succ.solar_punk.block.ModBlocks;

import java.util.function.Supplier;

// The shapes a Particle Accelerator Pipe item can place. There's one pipe item, and the
// player picks the shape from a radial menu (see PipeShapeMenu); the choice is stored on
// the item stack.
public enum PipeShape implements StringRepresentable {
    STRAIGHT("straight", ModBlocks.PARTICLE_ACCELERATOR_PIPE::get),
    BEND("bend", ModBlocks.PARTICLE_ACCELERATOR_PIPE_BEND::get),
    DIAGONAL("diagonal", ModBlocks.PARTICLE_ACCELERATOR_PIPE_DIAGONAL::get),
    BEND_MIRRORED("bend_mirrored", ModBlocks.PARTICLE_ACCELERATOR_PIPE_BEND_MIRRORED::get);

    public static final Codec<PipeShape> CODEC = StringRepresentable.fromEnum(PipeShape::values);
    public static final StreamCodec<ByteBuf, PipeShape> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(i -> values()[i], PipeShape::ordinal);

    private final String name;
    private final Supplier<? extends Block> block;

    PipeShape(String name, Supplier<? extends Block> block) {
        this.name = name;
        this.block = block;
    }

    public Block block() {
        return block.get();
    }

    /** The shape that places this block, or null if it isn't a pipe shape (e.g. the Input). */
    public static PipeShape of(Block block) {
        for (PipeShape shape : values())
            if (shape.block() == block) return shape;
        return null;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
