package com.fintech.api.dto;

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

    
}
