package com.hbs.muletrap.service;

import com.hbs.muletrap.config.FraudConfig;
import com.hbs.muletrap.dto.TransactionDirection;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;

    private final FraudConfig fraudConfig;

    public FraudDetectionService(TransactionRepository transactionRepository, FraudConfig fraudConfig) {
        this.transactionRepository = transactionRepository;
        this.fraudConfig = fraudConfig;
    }

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);

    public boolean isSimilarToKnownMule(float[] emb) {
        logger.info("Checking similarity to known mules with embedding: {}", emb);
        logger.info("FraudConfig: {}", fraudConfig);
        double threshold = fraudConfig.getSimilarityThreshold();
        for (TransactionEntity mule : transactionRepository.findTop10ByIsMuleTrueOrderByCreatedAtDesc()) {
            if (mule.getEmbedding() != null && cosineSimilarity(emb, mule.getEmbedding()) > threshold) {
                logger.info("Found similar mule: {} with similarity score above threshold: {}", mule.getId(), threshold);
                return true;
            }
        }
        logger.info("No similar mules found for embedding: {}", emb);
        return false;
    }

    public boolean isSuspiciousInflowOutflowPattern(BigDecimal amount) {
        List<TransactionEntity> recent = transactionRepository.findByCreatedAtAfter(LocalDateTime.now().minusHours(1));
        logger.info("Checking inflow/outflow pattern for recent transactions: {}", recent.size());
        long inflows = recent.stream()
                .filter(t -> t.getDirection() == TransactionDirection.INBOUND
                        && t.getAmount().compareTo(
                        BigDecimal.valueOf(fraudConfig.getInflow().getMaxAmount()))
                        < 0)
                .count();
        logger.info("Number of inflows: {}", inflows);
        long outflows = recent.stream()
                .filter(t -> t.getDirection() == TransactionDirection.OUTBOUND
                        && t.getAmount().compareTo(
                        BigDecimal.valueOf(fraudConfig.getOutflow().getMinAmount()))
                        > 0)
                .count();
        logger.info("Number of outflows: {}", outflows);
        return inflows >= fraudConfig.getInflow().getCount() && outflows >= fraudConfig.getOutflow().getCount();
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0f, normA = 0f, normB = 0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i]*a[i];
            normB += b[i]*b[i];
        }
        logger.info("Cosine similarity calculation: dot={}, normA={}, normB={}", dot, normA, normB);
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}