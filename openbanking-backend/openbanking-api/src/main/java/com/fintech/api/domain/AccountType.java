package com.fintech.api.domain;
    
import java.util.Arrays;

// 계좌유형 enum으로 관리

public enum AccountType{

    CHECKING("입출금"),
    SAVINGS("예적금"),
    SUBSCRIPTION("청약");


    private final String label;
    AccountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AccountType fromLabel (String label) {
        return Arrays.stream(values()).
            filter(t->t.label.equals(label))
            .findFirst()
                .orElseThrow(()->new IllegalArgumentException("유효하지 않은 계좌 유형"));
    }
}
