package com.hbs.muletrap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fraud")
@Data
public class FraudConfig {
    private double similarityThreshold;
    private Inflow inflow = new Inflow();
    private Outflow outflow = new Outflow();

    @Data
    public static class Inflow {
        private int count = 0;
        private double maxAmount = 0.0;
    }

    @Data
    public static class Outflow {
        private int count = 0;
        private double minAmount = 0.0;
    }
}