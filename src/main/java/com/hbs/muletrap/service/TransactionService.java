package com.hbs.muletrap.service;

import com.hbs.muletrap.dto.TransactionInput;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TransactionService {
    private final PromptGeneratorService promptGen;
    private final EmbeddingService embedSvc;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionRepository repo;

    public TransactionService(PromptGeneratorService promptGen, EmbeddingService embedSvc,
                              FraudDetectionService fraudDetectionService, TransactionRepository repo) {
        this.promptGen = promptGen;
        this.embedSvc = embedSvc;
        this.fraudDetectionService = fraudDetectionService;
        this.repo = repo;
    }

    public Mono<TransactionEntity> process(TransactionInput input) {
        String prompt = promptGen.generatePrompt(input, /* inject RiskConfig */ null);
        return embedSvc.generateEmbedding(prompt)
                .map(vector -> {
                    TransactionEntity e = new TransactionEntity();
                    e.setAmount(input.getAmount());
                    e.setChannel(input.getChannel());
                    e.setTime(input.getTime());
                    e.setCountry(input.getCountry());
                    e.setAccountAgeDays(input.getAccountAgeDays());
                    e.setActivitySummary(input.getActivitySummary());
                    e.setEmbedding(vector);
                    boolean mule = fraudDetectionService.isSimilarToKnownMule(vector, 0.9f) || fraudDetectionService.isSuspiciousInflowOutflowPattern(input.getAmount());
                    e.setMule(mule);
                    return repo.save(e);
                });
    }

    public List<TransactionEntity> listMules() {
        return repo.findTop10ByIsMuleTrueOrderByCreatedAtDesc();
    }
}
