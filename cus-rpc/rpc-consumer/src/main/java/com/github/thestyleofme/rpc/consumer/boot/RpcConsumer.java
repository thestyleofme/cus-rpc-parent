package com.github.thestyleofme.rpc.consumer.boot;

import java.lang.reflect.Proxy;
import java.util.concurrent.*;

import com.github.thestyleofme.rpc.consumer.handler.UserClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 1:22
 * @since 1.0.0
 */
public class RpcConsumer {

    private RpcConsumer() {

    }

    private static final ExecutorService EXECUTOR_SERVICE =
            new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2,
                    50,
                    100L,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(50),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    private static UserClientHandler userClientHandler;

    public static void initClient(String ip, int port) throws InterruptedException {
        // 初始化UserClientHandler
        userClientHandler = new UserClientHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                // 设置请求协议为TCP
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new StringDecoder());
                        // 设置自定义事件处理器
                        pipeline.addLast(userClientHandler);
                    }
                });
        bootstrap.connect(ip, port).sync();
    }

    /**
     * 创建代理
     *
     * @param clazz         接口类型
     * @param providerParam UserService#sayHello#
     * @return proxy
     */
    public static Object createProxy(Class<?> clazz, final String providerParam) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{clazz},
                (proxy, method, args) -> {
                    // 初始化客户端
                    if (userClientHandler == null) {
                        initClient("127.0.0.1", 9999);
                    }
                    // 给UserClientHandler 设置param参数
                    userClientHandler.setParam(providerParam + args[0]);
                    // 使用线程池开启一个线程处理call()操作，并返回结果
                    return EXECUTOR_SERVICE.submit(userClientHandler).get();
                });
    }
}
