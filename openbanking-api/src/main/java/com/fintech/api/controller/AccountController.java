package com.fintech.api.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.fintech.api.domain.Account;
import com.fintech.api.service.AccountService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 해당 사용자의 계좌를 생성
    @PostMapping("/users/{userId}") 
    public ResponseEntity <Account> createAccount (@RequestBody Account account, @PathVariable Long userId) {
        Account created_account = accountService.createAccount(account, userId);
        return ResponseEntity.ok(created_account);
    }

    // 사용자별로 계좌 목록을 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Account>> getUserAccounts (@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    // 특정 계좌 조회
    @GetMapping ("/{accountId}")
    public ResponseEntity<Account> getAccount (@PathVariable Long accountId) {
        return accountService.getAccountById(accountId).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    }


    // 계좌 삭제!
    @DeleteMapping("/{accountId}") 
    public ResponseEntity <Void> deleteAccount (@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
