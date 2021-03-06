package com.github.thestyleofme.rpc.consumer.handler;

import java.util.concurrent.Callable;

import com.github.thestyleofme.rpc.common.pojo.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 1:30
 * @since 1.0.0
 */
@Slf4j
public class UserClientHandler extends ChannelInboundHandlerAdapter implements Callable<Object> {

    /**
     * 事件处理上下文（储存handler信息，写操作）
     */
    private ChannelHandlerContext context;
    /**
     * 记录服务器返回的数据
     */
    private String result;
    /**
     * 记录将要发送给服务器的数据
     */
    private RpcRequest rpcRequest;

    /**
     * 客户端和服务器连接时，该方法就自动执行
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    /**
     * 当读到服务器数据，该方法自动执行
     * 收到服务端数据，唤醒等待线程
     */
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.result = msg.toString();
        notify();
    }

    /**
     * 将客户端数据写到服务器，开始等待唤醒
     */
    @Override
    public synchronized Object call() throws Exception {
        context.writeAndFlush(rpcRequest);
        wait();
        return result;
    }

    public void setParam(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }

}
