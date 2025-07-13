package com.fintech.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.Transaction;
import com.fintech.api.domain.User;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.TransactionRepository;
import com.fintech.api.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/*
 * TransactionService 역할
 * 1. 거래 등록
 * 2. 계좌 잔액 갱신
 * 3. 거래 내역 조회
 * 4. 단일 거래 내역 조회
 * 5. 거래 취소 및 롤백 (이상거래)
 */
@Service
@RequiredArgsConstructor
public class TransactionService {
    //DI 의존성 주입
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;



    @Transactional // DB 변경작업 모두 성공 또는 모두 실패하도록 보장
    // 입출금 실패 -> 잔액 및 거래내역 모두 roll back!
    public Transaction createTransaction (String email,Long accountId, Long amount, String type) {
       
        // 사용자 조회 
         User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 계좌 조회 + 본인 소유인지 여부 확인 -> 계좌가 있긴 해도 해당 이메일의 user가 소유한 계좌인지
        // 다른 사람 계좌에 입출금 가능해서 보안상 위험한 것 방지

        Account account = accountRepository.findById(accountId).filter(a-> a.getUser().getId().equals(user.getId()))
            .orElseThrow(()-> new IllegalArgumentException("해당 계좌가 존재하기 않거나 본인의 계좌가 아닙니다."));
        
        // 예외처리 -> HTTP RESPONSE  400 BAD REQUERST ERROR
        if ("WITHDRAWAL".equalsIgnoreCase(type)) {
            if (account.getBalance() < amount) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }
            account.setBalance(account.getBalance() - amount);
        }else  if ("DEPOSIT".equalsIgnoreCase(type)) {
            account.setBalance(account.getBalance()+amount);
        }else {
            throw new IllegalArgumentException("알 수 없는 거래입니다.");
        }


        Transaction transaction = Transaction.builder().account(account).amount(amount).type(type).build();

        return transactionRepository.save(transaction); // 객체간 연관관계를 수동으로 명시 JPA 핵심 개념
    }

   

    public Optional<Transaction> getTransactionById (Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactionsByAccountId(String email, Long accountId) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Account account = accountRepository.findById(accountId)
        .filter(a -> a.getUser().getId().equals(user.getId()))
        .orElseThrow(() -> new IllegalArgumentException("본인의 계좌가 아닙니다."));

    return transactionRepository.findByAccountId(accountId);
    
    }

    public Optional<Transaction> getTransactionByIdWithAuth(String email, Long transactionId) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    return transactionRepository.findById(transactionId)
        .filter(tx -> tx.getAccount().getUser().getId().equals(user.getId()));
    }


}
