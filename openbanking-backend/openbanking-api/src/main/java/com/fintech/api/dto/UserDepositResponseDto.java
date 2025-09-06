package com.fintech.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fintech.api.domain.DepositStatus;
import com.fintech.api.domain.UserDeposit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 사용자 예금 가입 응답 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDepositResponseDto {

    private Long id;
    private String productName;     // 상품명
    private BigDecimal amount;      // 예치 금액
    private BigDecimal interestAmount; // 현재까지 지급된 이자
    private LocalDateTime joinedAt; // 가입일
    private LocalDateTime maturityDate; // 만기일
    private DepositStatus status;   // 상태 (ACTIVE, CLOSED 등)

    // Entity -> DTO 변환 메서드
    public static UserDepositResponseDto from(UserDeposit entity) {
        return UserDepositResponseDto.builder()
                .id(entity.getId())
                .productName(entity.getProduct().getName()) // 연결된 상품 이름
                .amount(entity.getAmount())
                .interestAmount(entity.getInterestAmount())
                .joinedAt(entity.getJoinedAt())
                .maturityDate(entity.getMaturityDate())
                .status(entity.getStatus())
                .build();
    }
}
