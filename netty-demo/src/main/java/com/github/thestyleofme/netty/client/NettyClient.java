package com.github.thestyleofme.netty.client;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

/**
 * <p>
 * 客户端给服务端发送数据
 * </p>
 *
 * @author isaac 2020/10/19 23:57
 * @since 1.0.0
 */
public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        // 创建连接池对象
        NioEventLoopGroup group = new NioEventLoopGroup();
        // 创建客户端启动引导类
        Bootstrap bootstrap = new Bootstrap();
        // 配置启动引导类
        bootstrap.group(group)
                // 设置通道为NIO
                .channel(NioSocketChannel.class)
                // 设置channel初始化监听
                .handler(new ChannelInitializer<Channel>() {
                    // 当前该方法监听channel是否初始化
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        // 设置编码
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new StringEncoder());
                    }
                });
        // 使用启动引导类连接服务器，获取一个channel
        Channel channel = bootstrap.connect("127.0.0.1", 9999).channel();
        // 循环写数据给服务器
        while (true) {
            channel.writeAndFlush("hello server, this is client");
            TimeUnit.SECONDS.sleep(2L);
        }
    }
}
