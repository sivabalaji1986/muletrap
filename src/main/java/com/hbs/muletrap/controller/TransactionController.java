package com.hbs.muletrap.controller;

import com.hbs.muletrap.dto.TransactionInput;
import com.hbs.muletrap.dto.TransactionResponse;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.service.TransactionService;
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

    @PostMapping
    public ResponseEntity<TransactionResponse> submit(@Valid @RequestBody TransactionInput input) {
        TransactionResponse saved = txnService.process(input);
        if(saved == null) {
            return ResponseEntity.badRequest().build();
        } else{
            return ResponseEntity.ok(saved);
        }
    }

    @GetMapping("/similar")
    public ResponseEntity<List<TransactionResponse>> similar() {
        List<TransactionResponse> mules = txnService.listMules();
        if(mules == null || mules.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else{
            return ResponseEntity.ok(mules);
        }
    }
}