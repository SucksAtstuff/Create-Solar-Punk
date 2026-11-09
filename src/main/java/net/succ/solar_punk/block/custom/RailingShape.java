package net.succ.solar_punk.block.custom;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

// The shapes a filigree railing item can place. There's one railing item per material, and
// the player picks the shape from a radial menu (see RadialShapeMenu); the choice is stored on
// the item stack. FiligreeRailingItem maps each shape to its block.
public enum RailingShape implements StringRepresentable {
    FLAT("flat"),
    SLOPE("slope"),
    STEEP_SLOPE("steep_slope");

    public static final Codec<RailingShape> CODEC = StringRepresentable.fromEnum(RailingShape::values);
    public static final StreamCodec<ByteBuf, RailingShape> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(i -> values()[i], RailingShape::ordinal);

    private final String name;

    RailingShape(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
