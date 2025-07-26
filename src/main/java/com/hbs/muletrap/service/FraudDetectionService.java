package com.hbs.muletrap.service;

import com.hbs.muletrap.config.FraudConfig;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
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

    public boolean isSimilarToKnownMule(float[] emb) {
        double threshold = fraudConfig.getSimilarityThreshold();
        for (TransactionEntity mule : transactionRepository.findTop10ByIsMuleTrueOrderByCreatedAtDesc()) {
            if (mule.getEmbedding() != null && cosineSimilarity(emb, mule.getEmbedding()) > threshold) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuspiciousInflowOutflowPattern(BigDecimal amount) {
        List<TransactionEntity> recent = transactionRepository.findByCreatedAtAfter(LocalDateTime.now().minusHours(1));
        long inflows = recent.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.valueOf(fraudConfig.getInflow().getMaxAmount())) < 0)
                .count();
        long outflows = recent.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.valueOf(fraudConfig.getOutflow().getMinAmount())) > 0)
                .count();
        return inflows >= fraudConfig.getInflow().getCount() && outflows >= fraudConfig.getOutflow().getCount();
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0f, normA = 0f, normB = 0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i]*a[i];
            normB += b[i]*b[i];
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}