package com.fintech.api.dto;

import com.fintech.api.domain.Bank;
import lombok.AllArgsConstructor;
import lombok.Getter;

// getter -> 단일 객체 변환 1:1
// .map() 리스트 전체 변환  1:N , Stream 에서만 쓰는 method
@Getter
@AllArgsConstructor
public class BankDto {
    private Long id;
    private String code;
    private String bankName;
    private String website_url; 
    public static BankDto from(Bank bank) {
        return new BankDto(
            bank.getId(),
            bank.getCode(),
            bank.getBankName(),
            bank.getWebsite_url()
        );
    }
}
