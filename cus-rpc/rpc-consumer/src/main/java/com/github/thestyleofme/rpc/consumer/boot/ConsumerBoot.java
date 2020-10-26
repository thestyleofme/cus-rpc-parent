package com.github.thestyleofme.rpc.consumer.boot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSON;
import com.github.thestyleofme.rpc.common.pojo.ServerInfo;
import com.github.thestyleofme.rpc.common.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
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
 * @author isaac 2020/10/20 2:00
 * @since 1.0.0
 */
@Component
@Slf4j
public class ConsumerBoot implements ApplicationRunner {

    private static final Map<String, UserService> PROXY_MAP = new ConcurrentHashMap<>();
    public static final String PARENT_PATH = "/cus-rpc";
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
        // 设置监听
        CuratorCache cache = CuratorCache.build(CLIENT, PARENT_PATH);
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(ConsumerBoot::doNodeCreate)
                .forDeletes(ConsumerBoot::doNodeDelete)
                .build();
        cache.listenable().addListener(listener);
        cache.start();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        while (true) {
            // 连接所有服务器
            doWithAllServers();
            // 负载均衡 选择响应最少的服务器
            doWithMinimalCostServer();
        }
    }

    private void doWithMinimalCostServer() throws Exception {
        String minimalCostServer = fetchMinimalCostServer();
        log.debug("minimal cost server: {}", minimalCostServer);
        try {
            log.debug(PROXY_MAP.get(minimalCostServer).sayHello("i am ok!!"));
            // 开启一个定时线程做心跳检测
            // 若当前时间距离服务端最后一次响应的时间超过5秒，则判定该服务端失效
            doHeartbeatDetection();
            TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(3));
        } catch (Exception e) {
            log.error("some error happened", e);
        }
    }

    private String fetchMinimalCostServer() throws Exception {
        Optional<ServerInfo> min = CLIENT.getChildren().forPath(PARENT_PATH).stream().map(s -> {
            try {
                String data = new String(CLIENT.getData().forPath(PARENT_PATH + "/" + s));
                return JSON.parseObject(data, ServerInfo.class);
            } catch (Exception e) {
                // ignore
                return null;
            }
        })
                .filter(Objects::nonNull)
                .min((o1, o2) -> (int) ((o1.getCost() == null ? 0 : o1.getCost()) -
                        (o2.getCost() == null ? 0 : o2.getCost())));
        if (min.isPresent()) {
            return min.get().getServerName();
        }
        throw new IllegalStateException("not find server");
    }

    private void doWithAllServers() {
        PROXY_MAP.forEach((s, proxy) -> {
            try {
                log.debug(proxy.sayHello("i am ok!!"));
                // 开启一个定时线程做心跳检测
                // 若当前时间距离服务端最后一次响应的时间超过5秒，则判定该服务端失效
                doHeartbeatDetection();
                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(3));
            } catch (Exception e) {
                log.error("error", e);
            }
        });
    }

    private void doHeartbeatDetection() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() ->
                        PROXY_MAP.forEach((server, userService) -> {
                            // 更新该服务端节点的值
                            try {
                                LocalDateTime now = LocalDateTime.now();
                                String currentDate = now
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                String serverPath = String.format("%s/%s", PARENT_PATH, server);
                                String data = new String(CLIENT.getData().forPath(serverPath));
                                ServerInfo serverInfo = JSON.parseObject(data, ServerInfo.class);
                                // 日期比较 是否超过5秒
                                String lastDate = serverInfo.getLastDateTime();
                                LocalDateTime last = LocalDateTime.parse(lastDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                long seconds = Duration.between(now, last).getSeconds();
                                if (seconds > 5L) {
                                    // 清除该节点
                                    CLIENT.delete().forPath(serverPath);
                                } else {
                                    serverInfo.setLastDateTime(currentDate);
                                }
                                CLIENT.create().orSetData()
                                        .creatingParentsIfNeeded()
                                        .withMode(CreateMode.EPHEMERAL)
                                        .forPath(serverPath, JSON.toJSONString(serverInfo).getBytes());
                                log.debug("refresh server[{}]", server);
                            } catch (Exception e) {
                                log.error("error", e);
                            }
                        }),
                10L, 5L, TimeUnit.SECONDS);
    }

    private static void doNodeDelete(ChildData oldNode) {
        String nodePath = oldNode.getPath();
        log.debug("Node deleted. Old value: [{}]", nodePath);
        String server = nodePath.substring(nodePath.lastIndexOf('/') + 1);
        PROXY_MAP.remove(server);
    }

    private static void doNodeCreate(ChildData node) {
        String nodePath = node.getPath();
        // 初始 跳过
        if (PARENT_PATH.equals(nodePath)) {
            return;
        }
        log.debug("Node created: [{}]", nodePath);
        String server = nodePath.substring(nodePath.lastIndexOf('/') + 1);
        PROXY_MAP.put(server, RpcConsumer.createProxy(UserService.class, server, CLIENT));
    }

}
