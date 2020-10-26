package com.github.thestyleofme.rpc.provider.boot;

import com.alibaba.fastjson.JSON;
import com.github.thestyleofme.rpc.common.codec.JsonSerializer;
import com.github.thestyleofme.rpc.common.codec.RpcDecoder;
import com.github.thestyleofme.rpc.common.pojo.RpcRequest;
import com.github.thestyleofme.rpc.common.pojo.ServerInfo;
import com.github.thestyleofme.rpc.provider.handler.UserServiceHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
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
@Slf4j
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
        server1Start();
        // server2Start();
    }

    private void server2Start() throws Exception {
        // 启动服务器
        String ip = "127.0.0.1";
        int port = 9998;
        startServer(ip, port);
        // 将IP及端口信息注册到Zookeeper
        registerZkNode(ip, port, "server2");
    }

    private void server1Start() throws Exception {
        // 启动服务器
        String ip = "127.0.0.1";
        int port = 9999;
        startServer(ip, port);
        // 将IP及端口信息注册到Zookeeper
        registerZkNode(ip, port, "server1");
    }

    private void registerZkNode(String ip, int port, String serverName) throws Exception {
        RetryPolicy backoffRetry = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(backoffRetry)
                // 设置独立的命名空间 /base 所有节点都是/base开头的，数据隔离
                .namespace("base")
                .build();
        client.start();

        // 创建临时节点 127.0.0.1:9999
        String serverZkPath = "/cus-rpc/" + serverName;
        String ipAndPort = String.format("%s:%d", ip, port);
        ServerInfo serverInfo = ServerInfo.builder()
                .ipAndPort(ipAndPort)
                .serverName(serverName)
                .build();
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(serverZkPath, JSON.toJSONString(serverInfo).getBytes());
        log.debug("register to zk[{}] success, {}", serverZkPath, ipAndPort);
    }


}
