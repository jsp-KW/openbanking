package com.fintech.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.api.dto.CreateNotificationRequestDto;
import com.fintech.api.dto.NotificationResponseDto;
import com.fintech.api.service.NotificationService;
import com.fintech.api.service.UserService;

import lombok.RequiredArgsConstructor;


@RequestMapping("/api/notifications")
@RestController
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService  notificationService;
    private final UserService userService;

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponseDto>>getMy(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Long userId = userService.getUserIdByEmail(email);
        List<NotificationResponseDto> results = notificationService.getMyNotifications(userId);
        return ResponseEntity.ok(results);
    }


    @PostMapping("/read/{id}")
    public ResponseEntity<String> markAsRead (@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByEmail(userDetails.getUsername());
        notificationService.isReaded(id, userId);
        return ResponseEntity.ok("알람 읽음 처리 완료!");

    }


    @PostMapping("") 
    public ResponseEntity<String> createNotification (@RequestBody CreateNotificationRequestDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(r-> r.getAuthority().equals(
            "ROLE_ADMIN"
        ));

        if(!isAdmin) return ResponseEntity.status(403).body("관리자만 접근할 수 있습니다.");

        notificationService.createNotification(dto);
        return ResponseEntity.ok("알림을 생성하였습니다.");
    }
    
}
