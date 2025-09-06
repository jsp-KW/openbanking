package com.fintech.api.domain;

// 진행중, 만기, 해지 

public enum DepositStatus {
    ACTIVE("활성"),
    MATURED("만료"),
    EARLY_TERMINATED("조기해지"),
    PENDING("대기중");
    
    private final String description;
    
    DepositStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}