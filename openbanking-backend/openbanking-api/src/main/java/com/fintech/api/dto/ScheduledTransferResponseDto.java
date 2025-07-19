package com.fintech.api.dto;

import java.time.LocalDateTime;

import com.fintech.api.domain.ScheduledTransfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO 책임 분리 -> 클라이언트에 반환할 데이터만 담고, DTO 책임 분리(프론트 응답 전용)
// from () -> Entity -> DTO 변환 담당하는 함수
// 유지보수, db 안정화 
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledTransferResponseDto {
    
    private Long scheduledTransferId;
    private String message;
    private Long amount;
    private LocalDateTime scheduledAt;
    private String status;
    

    // Entity -> DTO 
    public static ScheduledTransferResponseDto from(ScheduledTransfer st) {

        return ScheduledTransferResponseDto.builder()
            .scheduledTransferId(st.getId())
            .message("예약이체가 성공적으로 등록되었습니다.").amount(st.getAmount())
                .scheduledAt(st.getScheduledAt())
                .status(st.getStatus()).build();

    }
}