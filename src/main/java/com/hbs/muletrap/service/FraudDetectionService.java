package com.hbs.muletrap.service;

import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;

    public FraudDetectionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public boolean isSimilarToKnownMule(float[] embedding, float threshold) {
        List<TransactionEntity> knownMules = transactionRepository.findTop10ByIsMuleTrueOrderByCreatedAtDesc();
        for (TransactionEntity mule : knownMules) {
            if (mule.getEmbedding() == null) continue;
            if (cosineSimilarity(embedding, mule.getEmbedding()) > threshold) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuspiciousInflowOutflowPattern(BigDecimal amount, String accountId) {
        List<TransactionEntity> recentTxns = transactionRepository.findByCreatedAtAfter(LocalDateTime.now().minusHours(1));

        long inflows = recentTxns.stream()
                .filter(t -> t.getAmount().compareTo(new BigDecimal("500")) < 0)
                .count();
        long outflows = recentTxns.stream()
                .filter(t -> t.getAmount().compareTo(new BigDecimal("1000")) > 0)
                .count();

        return inflows >= 3 && outflows >= 1;
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0f, normA = 0f, normB = 0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += Math.pow(a[i], 2);
            normB += Math.pow(b[i], 2);
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}