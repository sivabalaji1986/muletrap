package com.hbs.muletrap.service;

import com.hbs.muletrap.config.FraudConfig;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FraudDetectionServiceTest {

    @Test
    void testSimilarToMule() {
        // 1) Mock the repository
        TransactionRepository mockRepo = Mockito.mock(TransactionRepository.class);
        // 2) Create a FraudConfig with a low threshold
        FraudConfig cfg = new FraudConfig();
        cfg.setSimilarityThreshold(0.5);
        // 3) Wire up the service with both repo and config
        FraudDetectionService svc = new FraudDetectionService(mockRepo, cfg);

        // 4) Prepare a “known mule” embedding
        TransactionEntity mule = new TransactionEntity();
        mule.setEmbedding(new float[]{1f, 0f});
        when(mockRepo.findTop10ByIsMuleTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(mule));

        // 5) Call the new method signature
        assertTrue(svc.isSimilarToKnownMule(new float[]{1f, 0f}));
    }

    @Test
    void testSuspiciousFlow() {
        TransactionRepository mockRepo = Mockito.mock(TransactionRepository.class);
        FraudConfig cfg = new FraudConfig();
        // configure inflow/outflow from application.yml values
        cfg.setInflow(new FraudConfig.Inflow());
        cfg.getInflow().setCount(2);
        cfg.getInflow().setMaxAmount(100);
        cfg.setOutflow(new FraudConfig.Outflow());
        cfg.getOutflow().setCount(1);
        cfg.getOutflow().setMinAmount(50);

        FraudDetectionService svc = new FraudDetectionService(mockRepo, cfg);

        // Create three transactions: two small (inflow) and one large (outflow)
        TransactionEntity t1 = new TransactionEntity(); t1.setAmount(BigDecimal.valueOf(50));
        TransactionEntity t2 = new TransactionEntity(); t2.setAmount(BigDecimal.valueOf(40));
        TransactionEntity t3 = new TransactionEntity(); t3.setAmount(BigDecimal.valueOf(60));

        // Mock the repo to return these when looking at the last hour
        when(mockRepo.findByCreatedAtAfter(any(LocalDateTime.class)))
                .thenReturn(List.of(t1, t2, t3));

        // Since 2 inflows <100 and 1 outflow >50, it should be flagged
        assertTrue(svc.isSuspiciousInflowOutflowPattern(BigDecimal.ZERO));
    }
}