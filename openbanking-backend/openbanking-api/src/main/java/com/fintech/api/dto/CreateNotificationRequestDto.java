package com.fintech.api.dto;

import com.fintech.api.domain.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 알람관련 처리 dto

public class CreateNotificationRequestDto {
    
    private Long userId;
    private String message;
    private NotificationType type;

}
