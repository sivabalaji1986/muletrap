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
    private final TransactionService txnService;

    public TransactionController(TransactionService txnService) {
        this.txnService = txnService;
    }

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @PostMapping
    public ResponseEntity<TransactionResponse> submit(@Valid @RequestBody TransactionInput input) {
        logger.info("TransactionController - Received transaction input: {}", input);
        TransactionResponse saved = txnService.process(input);
        logger.info("TransactionController - Processed transaction response: {}", saved);
        if(saved == null) {
            return ResponseEntity.badRequest().build();
        } else{
            return ResponseEntity.ok(saved);
        }
    }

    @GetMapping("/similar")
    public ResponseEntity<List<TransactionResponse>> similar() {
        logger.info("TransactionController - Fetching similar transactions");
        List<TransactionResponse> mules = txnService.listMules();
        logger.info("TransactionController - Found similar transactions: {}", mules);
        if(mules == null || mules.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else{
            return ResponseEntity.ok(mules);
        }
    }
}