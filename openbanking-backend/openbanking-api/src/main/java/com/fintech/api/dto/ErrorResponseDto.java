package com.fintech.api.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
// @AllArgsConstructor, @NoArgsConstructor는 Builder를 사용할 경우 생략 가능하거나 필수는 아닙니다.

@Getter
@Builder // 이 클래스가 빌더 패턴으로 생성될 수 있도록 합니다.
public class ErrorResponseDto {

    private final String code;
    private final String message;
    
    @Builder.Default // 빌더를 통해 생성 시, 값을 넣지 않으면 기본값 사용
    private final LocalDateTime timestamp = LocalDateTime.now();

}