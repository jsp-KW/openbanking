package com.fintech.api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Transaction {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amount; // 거래 금액
    private String type; // 거래 종류 
    private String description; // 거래 내역 설명


    private LocalDateTime transactionDate; // 거래 시간
    private Long balanceAfter; // 거래 후 잔액


    // 계좌와 거래내역 관계
    // 계좌 하나에 여러 거래 내역이 가능하므로,
    // 거래내역 N 계좌 1
    // Many to One 관계
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name= "account_id")
    @JsonBackReference
    private Account account;


    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
    }

}
