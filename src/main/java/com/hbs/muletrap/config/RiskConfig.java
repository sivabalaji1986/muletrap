package com.hbs.muletrap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "risk")
@Data
public class RiskConfig {
    private AmountThreshold amount;
    private List<String> highRiskCountries;
    private int newAccountDays;

    @Data
    public static class AmountThreshold {
        private double low;
        private double medium;
        private double high;
    }

}