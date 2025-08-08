package com.fintech.api.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 예금가입 엔티티
// 예금가입시 이체할 계좌, 이자를 받을 계좌, 가입 사용자를 이어주기 위해
// user와 일대다, 예금상품과 일대다,  계좌와 일대 다
public class UserDeposit {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) // 기본키 PK
    private Long id; 

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user; // FK

    @ManyToOne
    @JoinColumn(name = "product_id")
    private DepositProduct  product; //FK

    @ManyToOne
    @JoinColumn(name ="account_id")
    private Account account;// FK


    private LocalDateTime joinedAt;// 상품 가입일

    private LocalDateTime maturityDate; // 상품 만기일

    private BigDecimal amount; // 예치 금액

    @Enumerated(EnumType.STRING)
    private DepositStatus status; // 상품 상태
    
    
    private LocalDateTime interestPaidAt;
    private BigDecimal  interestAmount;
    

}
