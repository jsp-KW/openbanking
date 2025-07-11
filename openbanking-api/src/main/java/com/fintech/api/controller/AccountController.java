package com.fintech.api.controller;

// Todo
// DTO 적용전의 엔티티 직접 반환 방식 
// DTO 설계로 무한 순환 참조 가능성 방지 및 프론트엔드단에서 과하게 많은 정보 받는것 방지
// 출력 포맷 커스터마이징 어려움 방지
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.fintech.api.domain.Account;
import com.fintech.api.service.AccountService;

import lombok.RequiredArgsConstructor;
import com.fintech.api.dto.AccountDto;

import java.util.List;


@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 해당 사용자의 계좌를 생성
    // @PostMapping("/users/{userId}") 
    // public ResponseEntity <AccountDto> createAccount (@RequestBody Account account, @PathVariable Long userId) {
    //     Account created_account = accountService.createAccount(account, userId);
    //     return ResponseEntity.ok(AccountDto.from(created_account));
    // }

    @PostMapping("")
    public ResponseEntity<AccountDto> createAccount(@RequestBody Account account, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Account created = accountService.createAccount(account,email);
        return ResponseEntity.ok(AccountDto.from(created));
    }
    // public String postMethodName(@RequestBody String entity) {
    //     //TODO: process POST request
        
    //     return entity;
    // }
    
    // 사용자별로 계좌 목록을 조회
    @GetMapping("/my")
    public ResponseEntity<List<AccountDto>> getUserAccounts (@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<Account> accounts = accountService.getAccountsByEmail(email);
        List<AccountDto> dtos = accounts.stream().map(AccountDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    // 특정 계좌 조회
    @GetMapping ("/{accountId}")
    public ResponseEntity<AccountDto> getAccount (@PathVariable Long accountId) {
        return accountService.getAccountById(accountId).map(AccountDto::from).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 계좌 삭제!
    
    @DeleteMapping("/{accountId}") 
    public ResponseEntity<Void> deleteAccount(
        @PathVariable Long accountId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        accountService.deleteAccountWithAuth(accountId, email);
        return ResponseEntity.noContent().build();
    }

}
