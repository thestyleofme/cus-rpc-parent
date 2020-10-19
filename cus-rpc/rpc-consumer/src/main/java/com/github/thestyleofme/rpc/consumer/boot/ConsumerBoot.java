package com.github.thestyleofme.rpc.consumer.boot;

import java.util.concurrent.TimeUnit;

import com.github.thestyleofme.rpc.common.service.UserService;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 2:00
 * @since 1.0.0
 */
public class ConsumerBoot {

    private static final String PROVIDER_NAME = "UserService#sayHello#";

    public static void main(String[] args) throws InterruptedException {
        // 创建代理对象
        UserService proxy = (UserService) RpcConsumer.createProxy(UserService.class, PROVIDER_NAME);
        // 循环给服务器发送数据
        while (true) {
            System.out.println(proxy.sayHello("i am ok!!"));
            TimeUnit.SECONDS.sleep(2L);
        }
    }
}
