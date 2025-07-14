package com.fintech.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.api.domain.Notification;
import com.fintech.api.domain.User;
import com.fintech.api.dto.CreateNotificationRequestDto;
import com.fintech.api.dto.NotificationResponseDto;
import com.fintech.api.repository.NotificationRepository;
import com.fintech.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 내 알림 전체 조회
    public List<NotificationResponseDto> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList(); //유저 아이디를 기반으로 알림 전체 가져와서 List형으로 반환
    }

    // 알림 읽음 처리
    @Transactional
    public boolean isReaded(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("알림이 존재하지 않습니다")); // 알림이 없는경우 예외처리

        // 본인 알림인지 (본인의 알람만 체크 가능 -> userId 기반 체크)
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인 알림만 읽음 처리 가능합니다.");
        }

        notification.setIsRead(true); // 읽음 처리를 위한 true값 설정
        return true; // frontend에 반환(쓰일수 있을거같아서)
    }

    // 알림 생성 (알람생성 dto 인자로 받아서 저장)
    public void createNotification(CreateNotificationRequestDto dto) { 
        User user = userRepository.findById(dto.getUserId()) .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다.")); // 사용자 존재 x경우 예외처리

        Notification notification = Notification.builder().user(user).message(dto.getMessage()).type(dto.getType()).isRead(false).build();

        notificationRepository.save(notification); // 알람 생성후 저장
    }

    // private: Entity → DTO 변환
    private NotificationResponseDto toDto(Notification n) {
        return NotificationResponseDto.builder().id(n.getId()).message(n.getMessage()).type(n.getType()).isRead(n.getIsRead()).createdAt(n.getCreatedAt()).build();
    }
}

