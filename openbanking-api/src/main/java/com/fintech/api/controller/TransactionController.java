package com.fintech.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fintech.api.domain.Transaction;
import com.fintech.api.service.TransactionService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import com.fintech.api.dto.TransactionWithAccountDto;


@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;

    // 거래내역 생성 
    @PostMapping
    public ResponseEntity<TransactionWithAccountDto> createTransaction (
        @RequestParam Long accountId,
        @RequestParam Long amount,
        @RequestParam String type
    ) {
        Transaction created = transactionService.createTransaction(accountId, amount, type);
        return ResponseEntity.ok(TransactionWithAccountDto.from(created));
    }


    // 특정 계좌에 전체 거래내역가져오기
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionWithAccountDto>>  getTransactionsByAccountId(@PathVariable Long accountId) {
        List<Transaction> tx_list = transactionService.getTransactionsByAccountId(accountId);     
        List<TransactionWithAccountDto> dto_lists = tx_list.stream().map(TransactionWithAccountDto::from).toList();
        return ResponseEntity.ok(dto_lists);
    }


    //단일  거래내역 조회
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionWithAccountDto> getTransactionById(@PathVariable Long transactionId) {
        return transactionService.getTransactionById(transactionId).map(TransactionWithAccountDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
