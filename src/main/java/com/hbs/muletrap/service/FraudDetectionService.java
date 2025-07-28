package com.hbs.muletrap.service;

import com.hbs.muletrap.config.FraudConfig;
import com.hbs.muletrap.dto.TransactionDirection;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /**
     * Check if a new embedding is similar to any of the last 10 mule embeddings for the same customer.
     */
    public boolean isSimilarToKnownMule(String customerId, float[] emb) {
        logger.info("Checking similarity to known mules for customer {} with embedding length {}", customerId, emb.length);
        double threshold = fraudConfig.getSimilarityThreshold();
        Pageable limit10 = PageRequest.of(0, 10);

        List<TransactionEntity> knownMules = transactionRepository
                .findTop10ByCustomerIdAndIsMuleTrueOrderByCreatedAtDesc(customerId, limit10);

        for (TransactionEntity mule : knownMules) {
            float[] muleEmb = mule.getEmbedding();
            if (muleEmb != null) {
                float sim = cosineSimilarity(emb, muleEmb);
                logger.info("Comparing to mule {}: similarity = {}", mule.getId(), sim);
                if (sim > threshold) {
                    logger.info("Found similar mule {} with similarity {} > threshold {}", mule.getId(), sim, threshold);
                    return true;
                }
            }
        }
        logger.info("No similar mules found for customer {}", customerId);
        return false;
    }

    /**
     * Check for suspicious pattern: N small INBOUND and M large OUTBOUND within the past hour for the same customer.
     */
    public boolean isSuspiciousInflowOutflowPattern(String customerId) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        List<TransactionEntity> recent = transactionRepository
                .findByCustomerIdAndCreatedAtAfter(customerId, cutoff);

        logger.info("{} recent transactions for customer {} since {}", recent.size(), customerId, cutoff);

        long inflows = recent.stream()
                .filter(t -> t.getDirection() == TransactionDirection.INBOUND
                        && t.getAmount().compareTo(BigDecimal.valueOf(fraudConfig.getInflow().getMaxAmount())) < 0)
                .count();
        logger.info("Number of small inflows: {}", inflows);

        long outflows = recent.stream()
                .filter(t -> t.getDirection() == TransactionDirection.OUTBOUND
                        && t.getAmount().compareTo(BigDecimal.valueOf(fraudConfig.getOutflow().getMinAmount())) > 0)
                .count();
        logger.info("Number of large outflows: {}", outflows);

        boolean flag = inflows >= fraudConfig.getInflow().getCount()
                && outflows >= fraudConfig.getOutflow().getCount();
        logger.info("Suspicious flow pattern for customer {}: {}", customerId, flag);
        return flag;
    }

    /**
     * Compute cosine similarity between two float vectors.
     */
    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0f, normA = 0f, normB = 0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        logger.info("Cosine similarity calculation: dot={}, normA={}, normB={}", dot, normA, normB);
        return (float)(dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}