package com.fintech.api.dto;

import java.time.LocalDateTime;

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
}
