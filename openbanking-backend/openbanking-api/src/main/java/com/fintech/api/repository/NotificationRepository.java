package com.fintech.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
}
