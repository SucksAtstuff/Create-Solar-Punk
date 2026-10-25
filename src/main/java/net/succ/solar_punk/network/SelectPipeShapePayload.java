package net.succ.solar_punk.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.PipeShape;
import net.succ.solar_punk.item.ParticleAcceleratorPipeItem;

// Client -> server: the player picked a shape in PipeShapeMenu for the pipe in their main hand.
public record SelectPipeShapePayload(PipeShape shape) implements CustomPacketPayload {

    public static final Type<SelectPipeShapePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "select_pipe_shape"));
    public static final StreamCodec<ByteBuf, SelectPipeShapePayload> STREAM_CODEC =
            StreamCodec.composite(PipeShape.STREAM_CODEC, SelectPipeShapePayload::shape, SelectPipeShapePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, SelectPipeShapePayload::handle);
    }

    private static void handle(SelectPipeShapePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack held = context.player().getMainHandItem();
            if (held.getItem() instanceof ParticleAcceleratorPipeItem)
                ParticleAcceleratorPipeItem.setShape(held, payload.shape());
        });
    }
}
