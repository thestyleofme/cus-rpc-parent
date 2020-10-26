package com.github.thestyleofme.rpc.consumer.boot;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSON;
import com.github.thestyleofme.rpc.common.codec.JsonSerializer;
import com.github.thestyleofme.rpc.common.codec.RpcEncoder;
import com.github.thestyleofme.rpc.common.pojo.RpcRequest;
import com.github.thestyleofme.rpc.common.pojo.ServerInfo;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 1:22
 * @since 1.0.0
 */
@Slf4j
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

    // private static UserClientHandler userClientHandler;

    public static UserClientHandler initClient(String server, CuratorFramework client) throws Exception {
        // 初始化UserClientHandler
        UserClientHandler userClientHandler = new UserClientHandler();
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
                        // 绑定出参格式为RpcRequest 输出需编码
                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new JsonSerializer()));
                        // 绑定入参为String 输入需解码
                        pipeline.addLast(new StringDecoder());
                        // 设置自定义事件处理器
                        pipeline.addLast(userClientHandler);
                    }
                });
        String data = new String(client.getData().forPath("/cus-rpc/" + server));
        ServerInfo serverInfo = JSON.parseObject(data, ServerInfo.class);
        String ipAndPort = serverInfo.getIpAndPort();
        String[] split = ipAndPort.split(":");
        bootstrap.connect(split[0], Integer.parseInt(split[1])).sync();
        log.debug("connect to server success, {}", ipAndPort);
        return userClientHandler;
    }

    /**
     * 创建代理
     *
     * @param clazz 接口类型
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> clazz, String server, CuratorFramework client) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{clazz},
                (proxy, method, args) -> {
                    // 初始化客户端
                    UserClientHandler userClientHandler = initClient(server, client);
                    // 给UserClientHandler 设置param参数
                    RpcRequest request = new RpcRequest();
                    request.setRequestId(UUID.randomUUID().toString());
                    request.setClassName(clazz.getSimpleName());
                    request.setClazz(clazz);
                    request.setMethodName(method.getName());
                    request.setParameters(args);
                    request.setParameterTypes(new Class[]{String.class});
                    request.setServer(server);
                    userClientHandler.setParam(request);
                    // 使用线程池开启一个线程处理call()操作，并返回结果
                    return EXECUTOR_SERVICE.submit(userClientHandler).get();
                });
    }
}
