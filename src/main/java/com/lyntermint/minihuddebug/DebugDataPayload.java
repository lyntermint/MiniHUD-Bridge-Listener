package com.lyntermint.minihuddebug;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DebugDataPayload(byte[] data) implements CustomPayload {
    public static final Identifier ID = Identifier.of("minihuddebug", "data");
    public static final CustomPayload.Id<DebugDataPayload> PAYLOAD_ID = new CustomPayload.Id<>(ID);
    public static final PacketCodec<PacketByteBuf, DebugDataPayload> CODEC = new PacketCodec<>() {
        @Override
        public DebugDataPayload decode(PacketByteBuf buf) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return new DebugDataPayload(bytes);
        }

        @Override
        public void encode(PacketByteBuf buf, DebugDataPayload payload) {
            buf.writeBytes(payload.data());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
}
