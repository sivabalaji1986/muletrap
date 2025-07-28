package com.hbs.muletrap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix="detection")
@Data
public class DetectionConfig {
    private RiskConfig risk;
    private FraudConfig fraud;

    @Data
    public static class RiskConfig {
        private AmountThreshold amount;
        private List<String> highRiskCountries;
        private int newAccountDays;
        @Data
        public static class AmountThreshold {
            private double low, medium, high;
        }
    }

    @Data
    public static class FraudConfig {
        private double similarityThreshold;
        private Inflow inflow;
        private Outflow outflow;
        @Data public static class Inflow  { private int count; private double maxAmount; }
        @Data public static class Outflow { private int count; private double minAmount; }
    }
}