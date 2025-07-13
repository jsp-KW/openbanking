package com.fintech.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    // 다른 유저정보 같이 response 받고싶은 경우 필드 추가하면 됨.
}
