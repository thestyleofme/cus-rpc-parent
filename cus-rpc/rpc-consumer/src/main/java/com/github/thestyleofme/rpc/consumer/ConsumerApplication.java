package com.github.thestyleofme.rpc.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 11:03
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan("com.github.thestyleofme.rpc")
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
