package com.hbs.muletrap;

import com.hbs.muletrap.config.FraudConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FraudConfig.class)
public class MuleTrapApplication {
    public static void main(String[] args) {
        SpringApplication.run(MuleTrapApplication.class, args);
    }
}
