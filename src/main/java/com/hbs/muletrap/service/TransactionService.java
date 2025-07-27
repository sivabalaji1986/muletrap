package com.hbs.muletrap.service;

import com.hbs.muletrap.config.RiskConfig;
import com.hbs.muletrap.dto.TransactionInput;
import com.hbs.muletrap.dto.TransactionResponse;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionResponse process(TransactionInput input) {
        logger.info("Processing transaction: {} & RiskConfig {}", input, riskConfig);
        // Generate prompt and embedding synchronously
        String prompt = promptGen.generatePrompt(input, riskConfig);
        logger.info("Generated prompt: {}", prompt);
        float[] vector = embedSvc.generateEmbedding(prompt);
        logger.info("Generated embedding vector: {}", vector);

        // Build entity
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount(input.getAmount());
        transactionEntity.setChannel(input.getChannel());
        transactionEntity.setTime(input.getTime());
        transactionEntity.setCountry(input.getCountry());
        transactionEntity.setAccountAgeDays(input.getAccountAgeDays());
        transactionEntity.setActivitySummary(input.getActivitySummary());
        transactionEntity.setEmbedding(vector);

        // Fraud checks
        boolean isMule = fraudDetectionService.isSimilarToKnownMule(vector)
                || fraudDetectionService.isSuspiciousInflowOutflowPattern(input.getAmount());
        transactionEntity.setMule(isMule);
        logger.info("Transaction entity: {}", transactionEntity);

        // Persist
        TransactionEntity transactionEntityResponse = repo.save(transactionEntity);
        return toTransactionResponse(transactionEntityResponse);
    }

    public List<TransactionResponse> listMules() {
        List<TransactionEntity> transactionEntities = repo.findTop10ByIsMuleTrueOrderByCreatedAtDesc();
        logger.info("Found {} mules in the database", transactionEntities.size());
        List<TransactionResponse> transactionResponseList = transactionEntities.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
        logger.info("Converted {} mules to response DTOs", transactionResponseList.size());
        return transactionResponseList;
    }

    private TransactionResponse toTransactionResponse(TransactionEntity e) {
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
