package com.hbs.muletrap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fraud")
@Data
public class FraudConfig {
    private double similarityThreshold;
    private Inflow inflow;
    private Outflow outflow;

    @Data public static class Inflow { private int count; private double maxAmount; }
    @Data public static class Outflow { private int count; private double minAmount; }
}