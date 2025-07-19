package com.fintech.api.dto;

import java.time.LocalDateTime;

import com.fintech.api.domain.ScheduledTransfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//사용자가 예약한 예약이체 내역을 조회할 때 반환할 DTO
//Entity 를 그대로 반환x, 필요한 정보 선별 
// api/scheduled-transfer/my 같은 api에 사용할 예정

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTransferListResponseDto {

    private Long id; // primary key

    private String fromAccountNumber; // 출금 계좌 번호

    private String toAccountNumber; // 입금 계좌 번호

    private Long amount;// 예약 이체 금액

    private LocalDateTime scheduledAt; // 예약 이체 예정시각

    private String status; // 예약이체 현재 상태 -> 대기/완료/실패
    

    // Service 로직에서 Entity 를 받은 후 from 호출하여 dto 생성하는 from 함수
    public static ScheduledTransferListResponseDto from(ScheduledTransfer st) {
        return ScheduledTransferListResponseDto.builder()
                .id(st.getId())
                .fromAccountNumber(st.getFromAccount().getAccountNumber())
                .toAccountNumber(st.getToAccount().getAccountNumber())
                .amount(st.getAmount())
                .scheduledAt(st.getScheduledAt())
                .status(st.getStatus())
                .build();
    }
}
