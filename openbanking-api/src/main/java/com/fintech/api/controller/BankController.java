package com.fintech.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.api.domain.Bank;
import com.fintech.api.service.BankService;

import lombok.RequiredArgsConstructor;
import com.fintech.api.dto.BankDto;
@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {
    private final BankService bankService; // 컨트롤러는 실제로 db 조회x 서비스를 통해

    // 모든 은행 목록 가져오기
    @GetMapping
    public ResponseEntity <List<BankDto>> getAllBanks() {
        List<Bank> banks = bankService.getAllBanks();
        List<BankDto> dtos = banks.stream().map(BankDto::from).toList();
        return ResponseEntity.ok(dtos);

    } 

    // 특정 은행 조회하기
    @GetMapping ("/{id}")
    public ResponseEntity<BankDto> getBankId(@PathVariable Long id) {
        return bankService.getBankById(id).map(BankDto::from).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 추가로 이름으로 은행 조회
    @GetMapping("/search")
    public ResponseEntity<BankDto> getBankByNmae(@RequestParam String name) {
          return bankService.getBankByName(name).map(BankDto::from).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
   
    }

    // 새 은행 등록 -> 바꿀지 말지
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Bank> createBank(@RequestBody Bank bank) {
       
        return ResponseEntity.ok(bankService.createBank(bank));
    }
    // 특정 은행 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.ok().build();
    }
    
}
