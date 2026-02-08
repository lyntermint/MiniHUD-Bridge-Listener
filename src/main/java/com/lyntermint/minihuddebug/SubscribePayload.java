package com.lyntermint.minihuddebug;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SubscribePayload(byte mode) implements CustomPayload {
    public static final Identifier ID = Identifier.of("minihuddebug", "subscribe");
    public static final CustomPayload.Id<SubscribePayload> PAYLOAD_ID = new CustomPayload.Id<>(ID);
    public static final PacketCodec<PacketByteBuf, SubscribePayload> CODEC =
            PacketCodec.tuple(PacketCodecs.BYTE, SubscribePayload::mode, SubscribePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
}
