package com.fintech.api.dto;

import java.math.BigDecimal;

import com.fintech.api.domain.DepositProduct;
import com.fintech.api.domain.DepositType;
import com.fintech.api.domain.InterestType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositProductResponseDto {
    private Long id;// pk
    private String name;// 상품명
    private BigDecimal interestRate; //이자율
    private Integer periodMonths; // 예치한 기간 (개월 단위)
    private BigDecimal minAmount;// 최소 가입 금액
    private DepositType type; // 정기예금인지 적금인지 ENUM TYPE
    private InterestType interestType;
    private String interestTypeDescription;

    public static DepositProductResponseDto from(DepositProduct p) {
        return DepositProductResponseDto.builder()
            .id(p.getId())
            .name(p.getName())
            .interestRate(p.getInterestRate())
            .periodMonths(p.getPeriodMonths())
            .minAmount(p.getMinAmount())
            .interestType(p.getInterestType())
            .interestTypeDescription(p.getInterestType().getDescription())
            .type(p.getType())
            .build();
    }
}