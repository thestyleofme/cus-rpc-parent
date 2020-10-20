package com.github.thestyleofme.rpc.consumer.boot;

import java.util.concurrent.TimeUnit;

import com.github.thestyleofme.rpc.common.service.UserService;
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
public class ConsumerBoot implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 创建代理对象
        UserService proxy = RpcConsumer.createProxy(UserService.class);
        // 循环给服务器发送数据
        while (true) {
            System.out.println(proxy.sayHello("i am ok!!"));
            TimeUnit.SECONDS.sleep(2L);
        }
    }
}
