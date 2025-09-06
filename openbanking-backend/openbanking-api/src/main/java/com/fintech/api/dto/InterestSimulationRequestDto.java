package com.fintech.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class InterestSimulationRequestDto {
    
    @NotNull(message = "금액을 입력해주세요")
    @DecimalMin(value = "10000", message = "최소 1만원 이상 입력해주세요")
    private BigDecimal amount;
    
    @NotNull(message = "이자율을 입력해주세요")
    @DecimalMin(value = "0.1", message = "이자율은 0.1% 이상이어야 합니다")
    @DecimalMax(value = "20.0", message = "이자율은 20% 이하여야 합니다")
    private BigDecimal interestRate;
    
    @NotNull(message = "기간을 입력해주세요")
    @Min(value = 1, message = "최소 1개월 이상이어야 합니다")
    @Max(value = 60, message = "최대 60개월까지 가능합니다")
    private Integer periodMonths;
}
