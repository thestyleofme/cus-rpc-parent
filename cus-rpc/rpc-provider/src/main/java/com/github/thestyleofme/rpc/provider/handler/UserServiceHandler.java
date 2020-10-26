package com.github.thestyleofme.rpc.provider.handler;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.alibaba.fastjson.JSON;
import com.github.thestyleofme.rpc.common.pojo.RpcRequest;
import com.github.thestyleofme.rpc.common.pojo.ServerInfo;
import com.github.thestyleofme.rpc.common.utils.ApplicationContextHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationContext;

/**
 * <p>
 * 自定义业务处理器
 * </p>
 *
 * @author isaac 2020/10/20 1:02
 * @since 1.0.0
 */
@Slf4j
public class UserServiceHandler extends ChannelInboundHandlerAdapter {

    private static final CuratorFramework CLIENT;

    static {
        // 遍历zk获取ip port并connect 注册监听 最后启动
        RetryPolicy backoffRetry = new ExponentialBackoffRetry(1000, 3);
        CLIENT = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(backoffRetry)
                // 设置独立的命名空间 /base 所有节点都是/base开头的，数据隔离
                .namespace("base")
                .build();
        CLIENT.start();
    }

    /**
     * 当客户端读取数据时，该方法被调用
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 注意：客户端将来发送请求时会传递一个参数：UserService#sayHello#are you ok
        // 判断当前请求是否符合规则
        if (msg instanceof RpcRequest) {
            // 获取消息对象
            RpcRequest request = (RpcRequest) msg;
            // 向zk写响应信息 执行之前
            String server = request.getServer();
            String serverPath = String.format("%s/%s", "/cus-rpc", server);
            String data = new String(CLIENT.getData().forPath(serverPath));
            ServerInfo serverInfo = JSON.parseObject(data, ServerInfo.class);
            LocalDateTime start = LocalDateTime.now();
            serverInfo.setLastDateTime(start
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            // 执行
            Class<?> clazz = request.getClazz();
            String methodName = request.getMethodName();
            // 获取Bean对象
            ApplicationContext context = Optional.ofNullable(ApplicationContextHelper.getContext())
                    .orElseThrow(() -> new IllegalStateException("not spring env, cannot get ApplicationContext"));
            Object serviceBean = context.getBean(clazz);
            // 获取方法并执行
            Method method = serviceBean.getClass().getMethod(methodName, request.getParameterTypes());
            Object result = method.invoke(serviceBean, request.getParameters());
            // 执行之后更新zk
            LocalDateTime end = LocalDateTime.now();
            long seconds = Duration.between(start, end).getSeconds();
            serverInfo.setCost(seconds == -1L ? 0 : seconds);
            CLIENT.create().orSetData()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(serverPath, JSON.toJSONString(serverInfo).getBytes());
            // 向客户端输出结果
            ctx.writeAndFlush(result);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
