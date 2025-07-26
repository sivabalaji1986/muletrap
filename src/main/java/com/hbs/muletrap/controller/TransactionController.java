package com.hbs.muletrap.controller;

import com.hbs.muletrap.dto.TransactionInput;
import com.hbs.muletrap.entity.TransactionEntity;
import com.hbs.muletrap.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService txnService;
    public TransactionController(TransactionService txnService) {
        this.txnService = txnService;
    }

    @PostMapping
    public Mono<ResponseEntity<TransactionEntity>> submit(@RequestBody TransactionInput input) {
        return txnService.process(input)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/similar")
    public ResponseEntity<List<TransactionEntity>> similar() {
        return ResponseEntity.ok(txnService.listMules());
    }
}
