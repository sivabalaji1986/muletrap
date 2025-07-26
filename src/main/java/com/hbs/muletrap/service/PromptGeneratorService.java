package com.hbs.muletrap.service;

import com.hbs.muletrap.config.RiskConfig;
import com.hbs.muletrap.constants.MuleTrapConstants;
import com.hbs.muletrap.dto.TransactionInput;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromptGeneratorService {

    private static final String PROMPT_TEMPLATE = """
            Transaction of $%.2f via %s at %s from %s.
            This is considered a %s amount from a %s country.
            The account is %s (age: %d days).
            Prior activity: %s.
            """;

    public String generatePrompt(TransactionInput txn, RiskConfig config) {
        String amountCategory = categorizeAmount(txn.getAmount(), config);
        String countryRisk    = config.getHighRiskCountries().contains(txn.getCountry())
                ? MuleTrapConstants.HIGH_RISK_LABEL
                : MuleTrapConstants.LOW_RISK_LABEL;
        String ageCategory    = txn.getAccountAgeDays() < config.getNewAccountDays()
                ? MuleTrapConstants.NEWLY_OPENED_LABEL
                : MuleTrapConstants.ESTABLISHED_LABEL;

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

    private String categorizeAmount(BigDecimal amount, RiskConfig config) {
        double value = amount.doubleValue();
        if (value >= config.getAmount().getHigh()) {
            return MuleTrapConstants.HIGH_VALUE_LABEL;
        } else if (value >= config.getAmount().getMedium()) {
            return MuleTrapConstants.MEDIUM_VALUE_LABEL;
        } else {
            return MuleTrapConstants.LOW_VALUE_LABEL;
        }
    }
}
