package com.fintech.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.Transaction;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.TransactionRepository;

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


    @Transactional // DB 변경작업 모두 성공 또는 모두 실패하도록 보장
    // 입출금 실패 -> 잔액 및 거래내역 모두 roll back!
    public Transaction createTransaction (Long accountId, Long amount, String type) {
        Account account = accountRepository.findById(accountId).orElseThrow(()->
        new IllegalArgumentException("계좌를 찾을 수 없습니다!!"));

        // 예외처리 -> HTTP RESPONSE  400 BAD REQUERST ERROR
        if ("WITHDRAWAL".equals (type)) {
            if (account.getBalance() < amount) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }
            account.setBalance(account.getBalance() - amount);
        }else  if ("DEPOSIT".equals(type)) {
            account.setBalance(account.getBalance()+amount);
        }else {
            throw new IllegalArgumentException("알 수 없는 거래입니다.");
        }


        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setType(type);


        return transactionRepository.save(transaction); // 객체간 연관관계를 수동으로 명시 JPA 핵심 개념
    }

    public List <Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    public Optional<Transaction> getTransactionById (Long id) {
        return transactionRepository.findById(id);
    }
}
