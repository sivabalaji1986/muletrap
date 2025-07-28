package com.hbs.muletrap.controller;

import com.hbs.muletrap.dto.TransactionInput;
import com.hbs.muletrap.dto.TransactionResponse;
import com.hbs.muletrap.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @PostMapping
    public ResponseEntity<TransactionResponse> submit(@Valid @RequestBody TransactionInput input) {
        logger.info("TransactionController - Received transaction input: {}", input);
        TransactionResponse transactionResponse = transactionService.process(input);
        logger.info("TransactionController - Processed transaction response: {}", transactionResponse);
        if(transactionResponse == null) {
            return ResponseEntity.badRequest().build();
        } else{
            return ResponseEntity.ok(transactionResponse);
        }
    }

    @GetMapping("/similar")
    public ResponseEntity<List<TransactionResponse>> similar(@RequestParam(name="country", required=false) String country) {
        logger.info("TransactionController - Fetching mule transactions, country filter={}", country);
        List<TransactionResponse> listedMules = transactionService.listMules(country);
        logger.info("TransactionController - Found similar transactions: {}", listedMules);

        if(listedMules == null || listedMules.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else{
            return ResponseEntity.ok(listedMules);
        }
    }
}