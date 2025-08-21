package com.fintech.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
        @RequestHeader("Idempotency-Key") String idempotencyKey, // FRONT END에서 전달 받은 키
        @RequestParam Long accountId,
        @RequestParam Long amount,
        @RequestParam String type
     
    ) { 
        String email = userDetails.getUsername();
        String requestId = idempotencyKey;
        Transaction created = transactionService.createTransaction(email,accountId, amount,requestId, type);
        return ResponseEntity.ok(TransactionWithAccountDto.from(created));
    }


    // 특정 계좌에 전체 거래내역가져오기
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @SecurityRequirement(name= "bearerAuth")
    public ResponseEntity<List<TransactionWithAccountDto>>  getTransactionsByAccountId(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable("accountId") Long accountId) {
        String email = userDetails.getUsername(); // 본인 인증단계
        
          var dtos = transactionService.getTransactionsByAccountId(email, accountId);
         return ResponseEntity.ok(dtos);
     
    }


    //단일  거래내역 조회
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionWithAccountDto> getTransactionById(
    @AuthenticationPrincipal UserDetails userDetails,    
    @PathVariable ("transactionId")Long transactionId) {

        String email = userDetails.getUsername();

        return transactionService.getTransactionByIdWithAuth(email,transactionId).map(TransactionWithAccountDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
