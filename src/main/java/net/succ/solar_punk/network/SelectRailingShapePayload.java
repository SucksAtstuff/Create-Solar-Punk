package net.succ.solar_punk.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.RailingShape;
import net.succ.solar_punk.item.FiligreeRailingItem;

// Client -> server: the player picked a shape in RadialShapeMenu for the filigree railing in their main hand.
public record SelectRailingShapePayload(RailingShape shape) implements CustomPacketPayload {

    public static final Type<SelectRailingShapePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "select_railing_shape"));
    public static final StreamCodec<ByteBuf, SelectRailingShapePayload> STREAM_CODEC =
            StreamCodec.composite(RailingShape.STREAM_CODEC, SelectRailingShapePayload::shape, SelectRailingShapePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, SelectRailingShapePayload::handle);
    }

    private static void handle(SelectRailingShapePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack held = context.player().getMainHandItem();
            if (held.getItem() instanceof FiligreeRailingItem)
                FiligreeRailingItem.setShape(held, payload.shape());
        });
    }
}
