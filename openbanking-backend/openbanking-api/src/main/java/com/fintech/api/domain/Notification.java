package com.fintech.api.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 3가지 기능에 대한 api 구현을 위한 알람(notifications 엔티티)
// GET /api/notifications/my	내 알림 조회
// POST /api/notifications/read/{id}	특정 알림 읽음 처리
// POST /api/notifications	(관리자 or 시스템) 알림 발송
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String message;

  
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private NotificationType type; // DEPOSIT, WITHDRAWAL, TRANSFER, SCHEDULED_TRANSFER

    @Builder.Default
    private Boolean isRead = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void setCreatedAt() {
        if (createdAt == null) 
        this.createdAt = LocalDateTime.now();
    }
}
