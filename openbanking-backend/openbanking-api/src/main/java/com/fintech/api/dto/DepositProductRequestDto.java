package com.fintech.api.dto;

import java.math.BigDecimal;

import com.fintech.api.domain.DepositType;
import com.fintech.api.domain.InterestType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositProductRequestDto {
    private String name;            // 상품명
    private BigDecimal interestRate;    // 이율 (예: 3.5)
    private Integer periodMonths;   // 기간 (개월)
    private BigDecimal minAmount;         // 최소 납입금액
    private DepositType type;       // 예금/적금
    private InterestType interestType;
}