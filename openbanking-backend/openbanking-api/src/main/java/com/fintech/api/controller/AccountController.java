package com.fintech.api.controller;

// Todo
// DTO 적용전의 엔티티 직접 반환 방식 
// DTO 설계로 무한 순환 참조 가능성 방지 및 프론트엔드단에서 과하게 많은 정보 받는것 방지
// 출력 포맷 커스터마이징 어려움 방지
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.fintech.api.domain.Account;
import com.fintech.api.service.AccountService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.fintech.api.dto.AccountDto;
import com.fintech.api.dto.AccountRequestDto;
import com.fintech.api.dto.MessageResponse;
import com.fintech.api.dto.TransferRequestDto;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 해당 사용자의 계좌를 생성
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountRequestDto accountDto, @AuthenticationPrincipal UserDetails userDetails) {
        
        System.out.println("bankId = " + accountDto.getBankId());
        System.out.println("accountType = " + accountDto.getAccountType());
        System.out.println("balance = " + accountDto.getBalance());
        String email = userDetails.getUsername();
        Account created = accountService.createAccount(accountDto,email);
        return ResponseEntity.ok(AccountDto.from(created));
    }
 
    // 사용자별로 계좌 목록을 조회
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    public ResponseEntity<List<AccountDto>> getUserAccounts (@AuthenticationPrincipal UserDetails userDetails) {
       
        if (userDetails == null) {
        System.out.println(" userDetails is null - 인증 정보 없음");
        return ResponseEntity.status(403).build();
    }
        String email = userDetails.getUsername();
        System.out.println("인증된 사용자 이메일" +  email);
        List<Account> accounts = accountService.getAccountsByEmail(email);
        List<AccountDto> dtos = accounts.stream().map(AccountDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    // 특정 계좌 조회 ( 사용자거나, 관리자인 경우) by 계좌 id로 
    // frontend에서 ui 드롭박스로 선택하면 백엔드에 id가 넘어오기 때문임

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(
        @PathVariable Long accountId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return accountService.getAccountById(accountId)
            .filter(account ->
                account.getUser().getEmail().equals(userDetails.getUsername()) ||
                userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            )
            .map(AccountDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(403).build());
    }


    // 계좌 삭제!
    
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
        @PathVariable Long accountId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        accountService.deleteAccountWithAuth(accountId, email);
        return ResponseEntity.noContent().build();
    }


    // 계좌번호로 조회하기
    // 사용자 or 관리자 가능으로 로직 설계
    //GET /accounts/number/{accountNumber}
    
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/number/{accountNumber}/bank/{bankId}")
    public ResponseEntity<AccountDto> getAccountByNumber(
        @PathVariable String accountNumber,
        @PathVariable Long bankId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return accountService.getAccountByNumber(accountNumber,bankId)
            .filter(account ->
                account.getUser().getEmail().equals(userDetails.getUsername()) || userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ) .map(AccountDto::from) .map(ResponseEntity::ok) .orElse(ResponseEntity.status(403).build());
    }


    // 오픈 뱅킹 핵심 api
    // 입출금 이체 기능!!!
    // POST 방식 /accounts/transfer
    // 권한 인증 방식 Bearer {token}
    // fromAccountId 하고 toAccountId로?

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/transfer")
    public ResponseEntity<MessageResponse> transfer(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody TransferRequestDto requestDto
    ) {
        accountService.transfer(
            userDetails.getUsername(),
            requestDto.getFromBankId(),
            requestDto.getToBankId(),
            requestDto.getFromAccountNumber(),
            requestDto.getToAccountNumber(),
            requestDto.getAmount()
        );
        return ResponseEntity.ok(new MessageResponse("이체 완료"));
    }

     @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<?> getAccountBalance(
        @PathVariable Long accountId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return accountService.getAccountById(accountId)
            .filter(account ->
                account.getUser().getEmail().equals(userDetails.getUsername()) ||
                userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            )
            .map(account -> ResponseEntity.ok(Map.of(
                "accountNumber", account.getAccountNumber(),
                "balance", account.getBalance() != null ? account.getBalance() : 0L,
                "bankName", account.getBank().getBankName()
            )))
            .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "해당 계좌에 접근할 권한이 없습니다.")));
    }
    
}
