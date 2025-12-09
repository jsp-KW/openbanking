package com.fintech.api.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import com.fintech.api.domain.NotificationType;
import com.fintech.api.domain.ScheduledTransfer;
import com.fintech.api.dto.CreateNotificationRequestDto;
import com.fintech.api.repository.ScheduledTransferRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor

public class ScheduledTransferProcessor {
    
    private final AccountService accountService;
    private final ScheduledTransferRepository scheduledTransferRepository;
    private final NotificationService notificationService;

    // 퍼사드 복잡한 인터페이스를 단순한 인터페이스로 제공하는 패턴을 적용


    public void process (ScheduledTransfer s) {

        try {
             // 비관락  + 이체
             String requestId = "SCHEDULED-" + s.getId();
             accountService.transferForSystem(s.getFromAccount().getId(),s.getToAccount().getId(), s.getAmount(), requestId);


             // 성공 status 

             s.setStatus("완료");
             scheduledTransferRepository.save(s);

             //  성공 알림

             notificationService.createNotification(CreateNotificationRequestDto.builder()
                .userId(s.getUser().getId())
                .message("예약 이체 완료: " + s.getAmount() + "원")
                .type(NotificationType.SCHEDULED_TRANSFER).build()
            
            );
        
        
        
            } catch (Exception e) {
                log.error ("예약 이체 실패 ID: {} ", s.getId(), e);
                s.setStatus ("실패");
                scheduledTransferRepository.save(s); //  예외를 가지고 상태만 저장하는 방식이 배치에선 유리

                // 실패알림 dto 생성
                notificationService.createNotification(CreateNotificationRequestDto.builder()
                .userId(s.getUser().getId()).message("예약이체 실패: " + e.getMessage())
                .type(NotificationType.INSUFFICIENT_BALANCE).build() );
            }

    }
}
