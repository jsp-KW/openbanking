package com.fintech.api.dto;

import java.time.LocalDateTime;

import com.fintech.api.domain.Transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 거래내역 응답시에 필요한 정보만 전달하기 위해 설계된 DTO
@Getter
@AllArgsConstructor
public class TransactionWithAccountDto {
    private Long id;
    private Long amount;
    private String type;
    private String description;
    private LocalDateTime transactionDate;
    private Long balanceAfter;

    private String accountNumber; // 계좌번호까지 포함하고 싶어서
    public static TransactionWithAccountDto from (Transaction tx) {
        return new TransactionWithAccountDto(tx.getId(),
         tx.getAmount(), tx.getType(),tx.getDescription(), tx.getTransactionDate(), tx.getBalanceAfter(),
         tx.getAccount() != null ? tx.getAccount().getAccountNumber(): null);
    }

}
