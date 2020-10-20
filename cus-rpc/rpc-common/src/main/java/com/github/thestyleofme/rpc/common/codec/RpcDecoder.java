package com.github.thestyleofme.rpc.common.codec;

import java.nio.charset.StandardCharsets;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 10:04
 * @since 1.0.0
 */
public class RpcDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final Class<?> clazz;
    private final Serializer serializer;

    public RpcDecoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        Object object = serializer.deserialize(clazz, msg.toString(StandardCharsets.UTF_8).getBytes());
        out.add(object);
    }
}
