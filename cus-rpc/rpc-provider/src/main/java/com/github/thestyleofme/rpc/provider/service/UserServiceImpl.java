package com.github.thestyleofme.rpc.provider.service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.github.thestyleofme.rpc.common.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 0:52
 * @since 1.0.0
 */
@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {

    @Override
    public String sayHello(String msg) {
        try {
            TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(4));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.debug("are you ok? {}", msg);
        return "are you ok? " + msg;
    }

}
