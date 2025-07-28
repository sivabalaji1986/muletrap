package com.hbs.muletrap.service;

import com.hbs.muletrap.config.DetectionConfig;
import com.hbs.muletrap.dto.TransactionDirection;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudDetectionServiceTest {

    private TransactionRepository transactionRepository;
    private DetectionConfig detectionConfig;
    private FraudDetectionService service;
    private final String customerId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        DetectionConfig.FraudConfig fraudConfig = new DetectionConfig.FraudConfig();
        // set up fraud thresholds
        fraudConfig.setSimilarityThreshold(0.8);
        DetectionConfig.FraudConfig.Inflow inflow = new DetectionConfig.FraudConfig.Inflow();
        inflow.setCount(2);
        inflow.setMaxAmount(100.0);
        fraudConfig.setInflow(inflow);
        DetectionConfig.FraudConfig.Outflow outflow = new DetectionConfig.FraudConfig.Outflow();
        outflow.setCount(1);
        outflow.setMinAmount(50.0);
        fraudConfig.setOutflow(outflow);
        detectionConfig = new DetectionConfig();
        detectionConfig.setFraud(fraudConfig);

        service = new FraudDetectionService(transactionRepository, detectionConfig);
    }

    @Test
    void testSimilarToMule() {
        // create a dummy embedding
        float[] emb = new float[] {1f, 2f, 3f};
        // create a “known mule” with an identical embedding
        TransactionEntity mule = new TransactionEntity();
        mule.setId(UUID.randomUUID());
        mule.setCustomerId(customerId);
        mule.setMule(true);
        mule.setEmbedding(new float[] {1f, 2f, 3f});

        // mock the repo to return that mule when fetching the last 10
        when(transactionRepository.findTop10ByCustomerIdAndIsMuleTrueOrderByCreatedAtDesc(
                eq(customerId), any(PageRequest.class)))
                .thenReturn(List.of(mule));

        // call service
        boolean result = service.isSimilarToKnownMule(customerId, emb);

        assertTrue(result, "Should detect similarity to the identical mule embedding");

        // verify we passed correct PageRequest
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(transactionRepository).findTop10ByCustomerIdAndIsMuleTrueOrderByCreatedAtDesc(eq(customerId), captor.capture());
        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    void testSuspiciousFlow() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);

        // create 2 small inbound txns and 1 large outbound txn
        TransactionEntity in1 = new TransactionEntity();
        in1.setCustomerId(customerId);
        in1.setDirection(TransactionDirection.INBOUND);
        in1.setAmount(BigDecimal.valueOf(10));

        TransactionEntity in2 = new TransactionEntity();
        in2.setCustomerId(customerId);
        in2.setDirection(TransactionDirection.INBOUND);
        in2.setAmount(BigDecimal.valueOf(20));

        TransactionEntity out1 = new TransactionEntity();
        out1.setCustomerId(customerId);
        out1.setDirection(TransactionDirection.OUTBOUND);
        out1.setAmount(BigDecimal.valueOf(100));

        // mock repo to return these three
        when(transactionRepository.findByCustomerIdAndCreatedAtAfter(eq(customerId), any(LocalDateTime.class)))
                .thenReturn(List.of(in1, in2, out1));

        // call service
        boolean flag = service.isSuspiciousInflowOutflowPattern(customerId);

        assertTrue(flag, "Should detect suspicious inflow/outflow pattern (2 inbounds, 1 outbound)");
    }
}
