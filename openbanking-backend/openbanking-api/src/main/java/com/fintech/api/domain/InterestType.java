package com.fintech.api.domain;

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
