package com.fintech.api.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequestDto {
    @NotNull
    private Long bankId;
    
    @PositiveOrZero
    private Long balance;

    private String accountType;

    @NotNull(message = "계좌 비밀번호는 필수")
    @Size(min =4, max= 4, message = "계좌 비밀번호는 정확히 4자리")
    private String password;
}
