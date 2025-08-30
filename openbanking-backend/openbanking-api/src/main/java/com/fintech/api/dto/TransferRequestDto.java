package com.fintech.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequestDto {
    private String fromAccountNumber;
    private String toAccountNumber;
    private Long amount;
    private Long fromBankId;
    private Long toBankId;
    @NotNull(message = "계좌 비밀번호는 필수")
    private String password;
    

    
}
