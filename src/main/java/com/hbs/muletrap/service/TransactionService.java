package com.hbs.muletrap.service;

import com.hbs.muletrap.config.DetectionConfig;
import com.hbs.muletrap.dto.TransactionInput;
import com.hbs.muletrap.dto.TransactionResponse;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final PromptGeneratorService promptGeneratorService;
    private final EmbeddingService embeddingService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionRepository transactionRepository;
    private final DetectionConfig detectionConfig;

    private static final Pageable TOP10 = PageRequest.of(0,10);

    public TransactionService(
            PromptGeneratorService promptGeneratorService,
            EmbeddingService embeddingService,
            FraudDetectionService fraudDetectionService,
            TransactionRepository transactionRepository,
            DetectionConfig detectionConfig
    ) {
        this.promptGeneratorService = promptGeneratorService;
        this.embeddingService = embeddingService;
        this.fraudDetectionService = fraudDetectionService;
        this.transactionRepository = transactionRepository;
        this.detectionConfig = detectionConfig;
    }

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionResponse process(TransactionInput input) {
        logger.info("Processing transaction: {} & RiskConfig {}", input, detectionConfig.getRisk());
        // Generate prompt and embedding synchronously
        String prompt = promptGeneratorService.generatePrompt(input, detectionConfig.getRisk());
        logger.info("Generated prompt: {}", prompt);
        float[] vector = embeddingService.generateEmbedding(prompt);
        logger.info("Generated embedding vector: {}", vector);

        // Build entity
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setCustomerId(input.getCustomerId());
        transactionEntity.setAmount(input.getAmount());
        transactionEntity.setChannel(input.getChannel());
        transactionEntity.setTime(input.getTime());
        transactionEntity.setCountry(input.getCountry());
        transactionEntity.setAccountAgeDays(input.getAccountAgeDays());
        transactionEntity.setDirection(input.getDirection());
        transactionEntity.setActivitySummary(input.getActivitySummary());
        transactionEntity.setEmbedding(vector);

        // Fraud checks
        boolean isMuleCandidate = fraudDetectionService.isMuleCandidate(
                input.getCountry());
        logger.info("Is mule candidate: {}", isMuleCandidate);
        if (isMuleCandidate) {
            boolean isMule = fraudDetectionService.isSimilarToKnownMules(input.getCustomerId(), vector)
                    || fraudDetectionService.isSuspiciousInflowOutflowPattern(input.getCustomerId(), input.getAmount(), input.getDirection());
            logger.info("Transaction is flagged as mule: {}", isMule);
            transactionEntity.setMule(isMule);
        }
        logger.info("Transaction entity: {}", transactionEntity);

        // Persist
        TransactionEntity transactionEntityResponse = transactionRepository.save(transactionEntity);
        return toTransactionResponse(transactionEntityResponse);
    }

    public List<TransactionResponse> listMules(String country) {
        List<TransactionEntity> transactionEntities;
        logger.info("Listing mules with country filter: {}", country);
        if (country != null && !country.isBlank()) {
            transactionEntities = transactionRepository.findTop10ByIsMuleTrueAndCountryOrderByCreatedAtDesc(country, TOP10);
        } else {
            transactionEntities = transactionRepository.findTop10ByIsMuleTrueOrderByCreatedAtDesc();
        }

        logger.info("Found {} mules in the database", transactionEntities.size());
        List<TransactionResponse> transactionResponseList = transactionEntities.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
        logger.info("Converted {} mules to response DTOs", transactionResponseList.size());
        return transactionResponseList;
    }

    private TransactionResponse toTransactionResponse(TransactionEntity transactionEntity) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setId(transactionEntity.getId());
        transactionResponse.setCustomerId(transactionEntity.getCustomerId());
        transactionResponse.setAmount(transactionEntity.getAmount());
        transactionResponse.setChannel(transactionEntity.getChannel());
        transactionResponse.setTime(transactionEntity.getTime());
        transactionResponse.setCountry(transactionEntity.getCountry());
        transactionResponse.setAccountAgeDays(transactionEntity.getAccountAgeDays());
        transactionResponse.setDirection(transactionEntity.getDirection());
        transactionResponse.setActivitySummary(transactionEntity.getActivitySummary());
        transactionResponse.setMule(transactionEntity.isMule());
        transactionResponse.setCreatedAt(transactionEntity.getCreatedAt());
        return transactionResponse;
    }
}
