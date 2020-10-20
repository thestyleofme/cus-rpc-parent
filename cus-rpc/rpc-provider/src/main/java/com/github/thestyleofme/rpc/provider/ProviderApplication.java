package com.github.thestyleofme.rpc.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/20 9:38
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan("com.github.thestyleofme.rpc")
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
