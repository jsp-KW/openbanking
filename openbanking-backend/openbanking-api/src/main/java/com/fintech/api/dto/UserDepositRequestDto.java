package com.fintech.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDepositRequestDto { // 고객 상품 가입 요청시 DTO
    private Long productId;// 가입할 상품
    private BigDecimal amount;// 예치 금액
    private String fromAccountNumber; // 출금 계좌번호
}
