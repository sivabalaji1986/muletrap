package com.hbs.muletrap.service;

import com.hbs.muletrap.config.DetectionConfig;
import com.hbs.muletrap.dto.TransactionInput;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import static com.hbs.muletrap.constants.MuleTrapConstants.*;

@Service
public class PromptGeneratorService {

    private static final String PROMPT_TEMPLATE = """
            Transaction of $%.2f via %s at %s from %s.
            This is considered a %s amount from a %s country.
            The account is %s (age: %d days).
            Prior activity: %s.
            """;

    public String generatePrompt(TransactionInput txn, DetectionConfig.RiskConfig config) {
        String amountCategory = categorizeAmount(txn.getAmount(), config);
        String countryRisk    = config.getHighRiskCountries().contains(txn.getCountry())
                ? HIGH_RISK_LABEL
                : LOW_RISK_LABEL;
        String ageCategory    = txn.getAccountAgeDays() < config.getNewAccountDays()
                ? NEWLY_OPENED_LABEL
                : ESTABLISHED_LABEL;

        return String.format(PROMPT_TEMPLATE,
                txn.getAmount(),
                txn.getChannel(),
                txn.getTime(),
                txn.getCountry(),
                amountCategory,
                countryRisk,
                ageCategory,
                txn.getAccountAgeDays(),
                txn.getActivitySummary()
        );
    }

    private String categorizeAmount(BigDecimal amount, DetectionConfig.RiskConfig config) {
        double value = amount.doubleValue();
        if (value >= config.getAmount().getHigh()) {
            return HIGH_VALUE_LABEL;
        } else if (value >= config.getAmount().getMedium()) {
            return MEDIUM_VALUE_LABEL;
        } else {
            return LOW_VALUE_LABEL;
        }
    }
}
