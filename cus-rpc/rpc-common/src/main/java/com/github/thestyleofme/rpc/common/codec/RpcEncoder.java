package com.github.thestyleofme.rpc.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 10:04
 * @since 1.0.0
 */
public class RpcEncoder extends MessageToByteEncoder<Object> {

    private final Class<?> clazz;
    private final Serializer serializer;

    public RpcEncoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf byteBuf) throws Exception {
        if (clazz != null && clazz.isInstance(msg)) {
            byte[] bytes = serializer.serialize(msg);
            // byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
