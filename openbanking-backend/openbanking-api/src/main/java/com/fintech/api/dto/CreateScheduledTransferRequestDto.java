package com.fintech.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 예약 이체 기능을 위한 dto
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateScheduledTransferRequestDto {
    private String fromAccountNumber;
    private String toAccountNumber;
    private Long amount;
    private LocalDateTime scheduledAt;
    @NotNull(message = "계좌 비밀번호는 필수")
    @Size(min =4, max= 4, message = "계좌 비밀번호는 정확히 4자리")
    private String password;

     @NotNull(message = "입금 은행 ID는 필수")
    private Long toBankId;    
}
