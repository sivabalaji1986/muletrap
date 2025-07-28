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
    private final PromptGeneratorService promptGeneratorService;
    private final EmbeddingService embeddingService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionRepository transactionRepository;
    private final RiskConfig riskConfig;

    public TransactionService(
            PromptGeneratorService promptGeneratorService,
            EmbeddingService embeddingService,
            FraudDetectionService fraudDetectionService,
            TransactionRepository transactionRepository,
            RiskConfig riskConfig
    ) {
        this.promptGeneratorService = promptGeneratorService;
        this.embeddingService = embeddingService;
        this.fraudDetectionService = fraudDetectionService;
        this.transactionRepository = transactionRepository;
        this.riskConfig = riskConfig;
    }

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionResponse process(TransactionInput input) {
        logger.info("Processing transaction: {} & RiskConfig {}", input, riskConfig);
        // Generate prompt and embedding synchronously
        String prompt = promptGeneratorService.generatePrompt(input, riskConfig);
        logger.info("Generated prompt: {}", prompt);
        float[] vector = embeddingService.generateEmbedding(prompt);
        logger.info("Generated embedding vector: {}", vector);

        // Build entity
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount(input.getAmount());
        transactionEntity.setChannel(input.getChannel());
        transactionEntity.setTime(input.getTime());
        transactionEntity.setCountry(input.getCountry());
        transactionEntity.setAccountAgeDays(input.getAccountAgeDays());
        transactionEntity.setDirection(input.getDirection());
        transactionEntity.setActivitySummary(input.getActivitySummary());
        transactionEntity.setEmbedding(vector);

        // Fraud checks
        boolean isMule = fraudDetectionService.isSimilarToKnownMule(vector)
                || fraudDetectionService.isSuspiciousInflowOutflowPattern(input.getAmount());
        transactionEntity.setMule(isMule);
        logger.info("Transaction entity: {}", transactionEntity);

        // Persist
        TransactionEntity transactionEntityResponse = transactionRepository.save(transactionEntity);
        return toTransactionResponse(transactionEntityResponse);
    }

    public List<TransactionResponse> listMules() {
        List<TransactionEntity> transactionEntities = transactionRepository.findTop10ByIsMuleTrueOrderByCreatedAtDesc();
        logger.info("Found {} mules in the database", transactionEntities.size());
        List<TransactionResponse> transactionResponseList = transactionEntities.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
        logger.info("Converted {} mules to response DTOs", transactionResponseList.size());
        return transactionResponseList;
    }

    private TransactionResponse toTransactionResponse(TransactionEntity e) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setId(e.getId());
        transactionResponse.setAmount(e.getAmount());
        transactionResponse.setChannel(e.getChannel());
        transactionResponse.setTime(e.getTime());
        transactionResponse.setCountry(e.getCountry());
        transactionResponse.setAccountAgeDays(e.getAccountAgeDays());
        transactionResponse.setDirection(e.getDirection());
        transactionResponse.setActivitySummary(e.getActivitySummary());
        transactionResponse.setMule(e.isMule());
        transactionResponse.setCreatedAt(e.getCreatedAt());
        return transactionResponse;
    }
}
