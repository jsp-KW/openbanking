package com.fintech.api.controller;

import com.fintech.api.dto.LoginRequest;
import com.fintech.api.dto.LoginResponse;
import com.fintech.api.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        // 1. 사용자 인증 시도
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = jwtUtil.createToken(request.getEmail(),role);

        // 3. 토큰 응답
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
