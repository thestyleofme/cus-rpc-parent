package com.github.thestyleofme.rpc.provider.boot;

import com.github.thestyleofme.rpc.common.codec.JsonSerializer;
import com.github.thestyleofme.rpc.common.codec.RpcDecoder;
import com.github.thestyleofme.rpc.common.pojo.RpcRequest;
import com.github.thestyleofme.rpc.provider.handler.UserServiceHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 1:12
 * @since 1.0.0
 */
@Component
public class ServerBoot implements ApplicationRunner {

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
                        // 绑定出参为String格式 输出需编码
                        pipeline.addLast(new StringEncoder());
                        // 绑定入参为RpcRequest格式 输入需解码
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JsonSerializer()));
                        // 设置自定义ChannelHandler添加到管道中
                        pipeline.addLast(new UserServiceHandler());
                    }
                });
        serverBootstrap.bind(ip, port).sync();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动服务器
        startServer("127.0.0.1", 9999);
    }
}
