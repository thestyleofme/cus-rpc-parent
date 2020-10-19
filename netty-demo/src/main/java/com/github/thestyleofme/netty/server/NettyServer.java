package com.github.thestyleofme.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * <p>
 * 接收客户端请求，打印在控制台
 * </p>
 *
 * @author isaac 2020/10/19 23:56
 * @since 1.0.0
 */
public class NettyServer {

    public static void main(String[] args) throws InterruptedException {
        // 创建2个线程池对象
        // 当前这两个实例代表两个线程池，默认线程数为CPU核心数乘2
        // bossGroup 接收客户端传过来的请求 负责接收用户连接
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        // workerGroup处理请求 负责处理用户的io读写操作
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        // 创建启动引导类
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        // 设置启动引导类
        // 添加到组中，两个线程池，第一个负责接收，第二个负责读写
        serverBootstrap.group(boosGroup, workerGroup)
                // 给我们当前设置一个通道类型
                .channel(NioServerSocketChannel.class)
                // 绑定一个初始化监听
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    // 事件监听channel通道
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 获取pipeline
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        // 绑定编码 解码
                        pipeline.addFirst(new StringEncoder());
                        pipeline.addLast(new StringDecoder());
                        // 绑定业务逻辑
                        pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                // 获取入栈信息
                                System.out.println(msg);
                            }
                        });
                    }
                });

        // 启动引导类绑定端口
        ChannelFuture channelFuture = serverBootstrap.bind(9999).sync();
        // 关闭通道 会阻塞等待直到服务器的channel关闭
        channelFuture.channel().closeFuture().sync();
    }

}
