package com.fintech.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequestDto {
    @NotNull
    private Long bankId;

    @NotBlank
    private String accountNumber;
    
    @PositiveOrZero
    private Long balance;
}
