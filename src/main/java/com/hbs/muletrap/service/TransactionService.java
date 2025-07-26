package com.hbs.muletrap.service;

import com.hbs.muletrap.config.RiskConfig;
import com.hbs.muletrap.dto.TransactionInput;
import com.hbs.muletrap.dto.TransactionResponse;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final PromptGeneratorService promptGen;
    private final EmbeddingService embedSvc;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionRepository repo;
    private final RiskConfig riskConfig;

    public TransactionService(
            PromptGeneratorService promptGen,
            EmbeddingService embedSvc,
            FraudDetectionService fraudDetectionService,
            TransactionRepository repo,
            RiskConfig riskConfig
    ) {
        this.promptGen = promptGen;
        this.embedSvc = embedSvc;
        this.fraudDetectionService = fraudDetectionService;
        this.repo = repo;
        this.riskConfig = riskConfig;
    }

    public TransactionResponse process(TransactionInput input) {
        // Generate prompt and embedding synchronously
        String prompt = promptGen.generatePrompt(input, riskConfig);
        float[] vector = embedSvc.generateEmbedding(prompt);

        // Build entity
        TransactionEntity entity = new TransactionEntity();
        entity.setAmount(input.getAmount());
        entity.setChannel(input.getChannel());
        entity.setTime(input.getTime());
        entity.setCountry(input.getCountry());
        entity.setAccountAgeDays(input.getAccountAgeDays());
        entity.setActivitySummary(input.getActivitySummary());
        entity.setEmbedding(vector);

        // Fraud checks
        boolean isMule = fraudDetectionService.isSimilarToKnownMule(vector)
                || fraudDetectionService.isSuspiciousInflowOutflowPattern(input.getAmount());
        entity.setMule(isMule);

        // Persist
        TransactionEntity transactionEntity = repo.save(entity);
        return toResponse(transactionEntity);
    }

    public List<TransactionResponse> listMules() {
        List<TransactionEntity> transactionEntities = repo.findTop10ByIsMuleTrueOrderByCreatedAtDesc();
        List<TransactionResponse> dtoList = transactionEntities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return dtoList;
    }

    private TransactionResponse toResponse(TransactionEntity e) {
        TransactionResponse resp = new TransactionResponse();
        resp.setId(e.getId());
        resp.setAmount(e.getAmount());
        resp.setChannel(e.getChannel());
        resp.setTime(e.getTime());
        resp.setCountry(e.getCountry());
        resp.setAccountAgeDays(e.getAccountAgeDays());
        resp.setActivitySummary(e.getActivitySummary());
        resp.setMule(e.isMule());
        resp.setCreatedAt(e.getCreatedAt());
        return resp;
    }
}
