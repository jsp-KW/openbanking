package com.fintech.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.api.domain.Transaction;
import com.fintech.api.dto.TransactionWithAccountDto;
import com.fintech.api.service.TransactionService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;

    // 거래내역 생성 
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<TransactionWithAccountDto> createTransaction (
        @AuthenticationPrincipal UserDetails userDetails, // 현재 로그인하고 있는 사용자 정보
        @RequestParam Long accountId,
        @RequestParam Long amount,
        @RequestParam String type
    ) {
        String email = userDetails.getUsername();
        Transaction created = transactionService.createTransaction(email,accountId, amount, type);
        return ResponseEntity.ok(TransactionWithAccountDto.from(created));
    }


    // 특정 계좌에 전체 거래내역가져오기
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionWithAccountDto>>  getTransactionsByAccountId(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable Long accountId) {
        String email = userDetails.getUsername(); // 본인 인증단계
        List<Transaction> tx_list = transactionService.getTransactionsByAccountId(email, accountId);     
        List<TransactionWithAccountDto> dto_lists = tx_list.stream().map(TransactionWithAccountDto::from).toList();
        return ResponseEntity.ok(dto_lists);
    }


    //단일  거래내역 조회
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionWithAccountDto> getTransactionById(
    @AuthenticationPrincipal UserDetails userDetails,    
    @PathVariable Long transactionId) {

        String email = userDetails.getUsername();

        return transactionService.getTransactionByIdWithAuth(email,transactionId).map(TransactionWithAccountDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
