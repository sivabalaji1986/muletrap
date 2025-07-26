package com.hbs.muletrap.service;

import com.hbs.muletrap.config.RiskConfig;
import com.hbs.muletrap.dto.TransactionInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;

class PromptGeneratorServiceTest {
    @Test
    void testGeneratePrompt() {
        RiskConfig config = new RiskConfig();
        config.setAmount(new RiskConfig.AmountThreshold());
        config.getAmount().setLow(0);
        config.getAmount().setMedium(1000);
        config.getAmount().setHigh(5000);
        config.setHighRiskCountries(List.of("Mordor"));
        config.setNewAccountDays(30);

        TransactionInput txn = new TransactionInput();
        txn.setAmount(BigDecimal.valueOf(6000));
        txn.setChannel("ATM");
        txn.setTime("02:00");
        txn.setCountry("Mordor");
        txn.setAccountAgeDays(10);
        txn.setActivitySummary("first txn");

        PromptGeneratorService svc = new PromptGeneratorService();
        String prompt = svc.generatePrompt(txn, config);
        assertTrue(prompt.contains("high-value amount"));
        assertTrue(prompt.contains("high-risk country"));
        assertTrue(prompt.contains("newly opened"));
    }
}