package com.fintech.api.domain;

// DepositProduct 엔티티에 @Enumerated(EnumType.STRING) 으로 매핑, 
// DB에는 SIMPLE, COMPOUND 로 저장됨

// InterestCalculatorFactory 를 생성하여 InterestType에 따른 적절한 이자 계산 전략 주입
public enum InterestType {
    SIMPLE("단리"),
    COMPOUND("복리");
    
    private final String description;
    
    InterestType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
