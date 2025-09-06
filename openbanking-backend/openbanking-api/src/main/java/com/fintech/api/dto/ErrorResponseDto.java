package com.fintech.api.dto;


import java.time.LocalDateTime;

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
public class ErrorResponseDto {
    
    private String code;
    private String message;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
