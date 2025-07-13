package com.fintech.api.controller;

import com.fintech.api.domain.User; 
import com.fintech.api.dto.LoginRequest;
import com.fintech.api.dto.LoginResponse;
import com.fintech.api.dto.SignupRequest;
import com.fintech.api.jwt.JwtUtil;
import com.fintech.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = jwtUtil.createToken(request.getEmail(), role);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        if (userService.isEmailExists(request.getEmail())) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // createUser() 안에서 암호화 
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        userService.createUser(user);
        return ResponseEntity.ok("회원가입 성공!");
    }
}
