package com.fintech.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.fintech.api.domain.InterestType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InterestCalculationService {
    
    /**
     * 이자 계산 메서드
     * @param principal 원금
     * @param annualRate 연이율 (예: 3.5 = 3.5%)
     * @param days 예치일수
     * @param interestType 이자 계산 방식
     * @return 계산된 이자 (원 단위)
     */
    public BigDecimal calculateInterest(BigDecimal principal, BigDecimal annualRate, 
                                      int days, InterestType interestType) {
        
        if (principal == null || annualRate == null || days <= 0) {
            log.warn("잘못된 이자 계산 파라미터: principal={}, rate={}, days={}", principal, annualRate, days);
            return BigDecimal.ZERO;
        }
        
        BigDecimal result;
        switch (interestType) {
            case SIMPLE: // 단리
                result = calculateSimpleInterest(principal, annualRate, days);
                break;
            case COMPOUND: // 복리
                result = calculateCompoundInterest(principal, annualRate, days);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 이자 계산 방식: " + interestType);
        }
        
        log.debug("이자 계산 완료: 원금={}, 이율={}, 일수={}, 방식={}, 이자={}", 
                 principal, annualRate, days, interestType, result);
        
        return result;
    }
    
    /**
     * 단리 계산: 원금 × 연이율 × (일수/365)
     */
    private BigDecimal calculateSimpleInterest(BigDecimal principal, BigDecimal annualRate, int days) {
        // 연이율을 소수로 변환 (3.5% → 0.035)
        BigDecimal rate = annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        
        // 일수를 년 단위로 변환
        BigDecimal yearFraction = BigDecimal.valueOf(days).divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);
        
        // 단리 계산: 원금 × 이율 × 기간
        return principal
                .multiply(rate)
                .multiply(yearFraction)
                .setScale(0, RoundingMode.HALF_UP); // 원 단위로 반올림
    }
    
    /**
     * 복리 계산: 원금 × (1 + 일이율)^일수 - 원금
     * 실제로는 복리 계산이 매우 복잡하므로 근사치 계산 사용
     */
    private BigDecimal calculateCompoundInterest(BigDecimal principal, BigDecimal annualRate, int days) {
        // 연이율을 소수로 변환
        BigDecimal rate = annualRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        
        // 일이율 계산
        BigDecimal dailyRate = rate.divide(BigDecimal.valueOf(365), 8, RoundingMode.HALF_UP);
        
        // 복리 계산: (1 + 일이율)^일수
        // Math.pow를 사용한 근사 계산 (정확한 계산을 위해서는 더 복잡한 로직 필요)
        double compoundFactor = Math.pow(1 + dailyRate.doubleValue(), days);
        BigDecimal compoundMultiplier = BigDecimal.valueOf(compoundFactor);
        
        BigDecimal finalAmount = principal.multiply(compoundMultiplier);
        BigDecimal interest = finalAmount.subtract(principal);
        
        return interest.setScale(0, RoundingMode.HALF_UP);
    }
    
    /**
     * 월복리 계산 (나중에 추가할 때 사용)
     */
    private BigDecimal calculateMonthlyCompoundInterest(BigDecimal principal, BigDecimal annualRate, int days) {
        BigDecimal rate = annualRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
        
        int months = days / 30; // 근사치
        double compoundFactor = Math.pow(1 + monthlyRate.doubleValue(), months);
        
        BigDecimal finalAmount = principal.multiply(BigDecimal.valueOf(compoundFactor));
        return finalAmount.subtract(principal).setScale(0, RoundingMode.HALF_UP);
    }
    
    /**
     * 이자 시뮬레이션 (가입 전 미리 계산해보기)
     */
    public BigDecimal simulateInterest(BigDecimal amount, BigDecimal rate, int months, InterestType type) {
        int days = months * 30; // 근사치 (실제로는 정확한 일수 계산 필요)
        return calculateInterest(amount, rate, days, type);
    }
}