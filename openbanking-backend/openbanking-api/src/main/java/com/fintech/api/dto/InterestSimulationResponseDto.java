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
public class InterestSimulationResponseDto {
    
    private BigDecimal amount;              // 원금
    private BigDecimal interestRate;        // 이자율
    private Integer periodMonths;           // 기간(개월)
    
    private BigDecimal simpleInterest;      // 단리 이자
    private BigDecimal compoundInterest;    // 복리 이자
    
    private BigDecimal totalAmountWithSimple;   // 단리 총액
    private BigDecimal totalAmountWithCompound; // 복리 총액
    
    private BigDecimal interestDifference;  // 단리/복리 차이
    
    // 추가 정보
    private String recommendation;          // 추천 메시지
    
    public String getRecommendation() {
        if (interestDifference.compareTo(BigDecimal.ZERO) > 0) {
            return "복리 상품이 " + interestDifference + "원 더 유리합니다!";
        } else {
            return "단리와 복리의 차이가 거의 없습니다.";
        }
    }
}