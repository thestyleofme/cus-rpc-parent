package com.github.thestyleofme.rpc.provider.boot;

import com.github.thestyleofme.rpc.provider.handler.UserServiceHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 1:12
 * @since 1.0.0
 */
public class ServerBoot {

    public static void startServer(String ip, int port) throws InterruptedException {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boosGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new StringDecoder());
                        // 设置自定义ChannelHandler添加到管道中
                        pipeline.addLast(new UserServiceHandler());
                    }
                });
        serverBootstrap.bind(ip, port).sync();
    }

    public static void main(String[] args) throws InterruptedException {
        // 启动服务器
        startServer("127.0.0.1", 9999);
    }
}
