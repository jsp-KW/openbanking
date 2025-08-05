package com.fintech.api.dto;

import com.fintech.api.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 계좌정보 -> 클라이언트  (필요한 정보만 응답하도록)
// 엔티티에서 직접 반환시, user와 bank 까지 끌고 와 무한 loop 위험 존재해서

@Getter
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String accountNumber;
    private String bankName;
    private Long balance;
    private Long bankId;

    // 은행이름 -> getBank().getBankName() 으로 접근!!!
    public static AccountDto from (Account account) {
        return new AccountDto(account.getId(), account.getAccountNumber(), account.getBank().getBankName(), account.getBalance(),account.getBank().getId());
    }
}
