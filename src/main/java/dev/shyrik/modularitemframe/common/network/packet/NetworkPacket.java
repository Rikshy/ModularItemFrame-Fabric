package dev.shyrik.modularitemframe.common.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public abstract class NetworkPacket {

    public abstract Identifier getId();

    protected abstract void encode(PacketByteBuf buf);

    public PacketByteBuf toBuffer() {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        encode(passedData);
        return passedData;
    }
}
