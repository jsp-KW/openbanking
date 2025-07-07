package com.fintech.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.api.domain.Transaction;
import com.fintech.api.service.TransactionService;
import java.util.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;

    // 거래내역 생성 
    @PostMapping
    public ResponseEntity<Transaction> createTransaction (
        @RequestParam Long accountId,
        @RequestParam Long amount,
        @RequestParam String type
    ) {
        Transaction created = transactionService.createTransaction(accountId, amount, type);
        return ResponseEntity.ok(created);
    }


    // 특정 계좌에 전체 거래내역가져오기
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>>  getTransactionsByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId));
    }


    //단일  거래내역 조회
    @GetMapping("/{transactionId}")
    public ResponseEntity <Transaction> getTransactionById(@PathVariable Long transactionId) {
        return transactionService.getTransactionById(transactionId).map(ResponseEntity::ok).orElse(
            ResponseEntity.notFound().build()
        );
    }
}
