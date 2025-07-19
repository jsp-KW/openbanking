package com.fintech.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.ScheduledTransfer;

public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, Long> {
    // 예약 이체 대상 조회를 위한 함수 
    // 조회 기준 시점 (현재 시점 이전이면서 대기 상태인 것들)
    List <ScheduledTransfer> findByStatusAndScheduledAtBefore(String status, LocalDateTime now);
    
    // 개인 예약이체 조회용
    // select * from ScheduledTransfer where user_id = userId; 이렇게 jpa가 처리
    List <ScheduledTransfer> findByUserId(Long userId);
}
