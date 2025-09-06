package com.fintech.api.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
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
// 예적금 상품정보 엔티티
// 이율, 기간 최소금액 등
public class DepositProduct {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id; //PK

    @ManyToOne
    @JoinColumn(name ="bank_id")
    private Bank bank; // 은행 1개 여러개 상품존재가능 -> 1대 다
    
    private String name;// 상품명 (정기예금 1년 등)

    @Column(nullable = false, precision = 5, scale = 4) //최대 값 9.9999 
    private BigDecimal interestRate; // 연 이자율(3.5454->3.5%)
    @Column(nullable = false)
    private int periodMonths;// 예치기간 (개월 단위)

    private BigDecimal minAmount; // 최소 가입금액

    @Enumerated(EnumType.STRING)
    private DepositType  type; // 정기예금/적금  구분 

    private LocalDateTime createdAt;// 상품 등록일

    @Column(length =2000)
    private String description;
    
    // 수정 일시
    private LocalDateTime updatedAt;

    // 이자 계산 방식을 위한 컬럼
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InterestType interestType = InterestType.SIMPLE; // 기본값: 단리
    
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    @OneToMany(mappedBy= "product")
    private List<UserDeposit> userDeposits= new ArrayList<>();
    

}
