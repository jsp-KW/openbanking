package com.fintech.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.User;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// 가독성 : 유저조회 설정, 저장
// 유지보수성 : 모든 계좌 생성 로직이 한 곳에서 관리
// 테스트 용이성
// 스프링 규칙 +통일성 -> 계층 분리, DI, 예외처리 

@Service
@RequiredArgsConstructor // spring DI  의존성 주입
public class AccountService {
    // JPA REPOSITORY 사용 -> 기본적인 CRUD 제공
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // findById () JPA 기본 메서드
    // User 엔티티의 ID를 가져오는 함수

    // setUser 
    // Account 하고 User와 ManyToOne 관계로 묶여있어서
    // 계좌 생성시 어떤 사용자에게 소속되는지 명시해야 하므로

    // JPA 의 save()
    // INSERT / UPDATE 를 알아서 처리해줌
    // save 이후 자동으로 트랜잭션이 반영
    // 트랜잭션이 묶여있고 -> 내부적으로 영속성 컨텍스트에 의해 관리

    // Optional & 예외 처리 부분
    // null-safe 
    @Transactional  // db 쓰기 작업 -> 트랜잭션 보장 명시 
    // 계좌 생성 (사용자와 연결)
    public Account createAccount (Account account, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()->
        new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        account.setUser(user);
        return accountRepository.save(account);
    }
    // 특정 사용자의 모든 계좌 리스트 조회 함수
    public List<Account> getAccountsByUserId (Long userId) {
        return accountRepository.findByUserId(userId);
        
    }
    // 특정 계좌 조회
    public Optional<Account> getAccountById (Long id) {
        return accountRepository.findById(id);
    }
    // 특정 계좌 삭제
    public void deleteAccount (Long id) {
        accountRepository.deleteById(id);
    }
}
