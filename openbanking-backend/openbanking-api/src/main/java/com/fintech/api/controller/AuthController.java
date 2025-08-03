package com.fintech.api.controller;

import com.fintech.api.config.JwtTokenProvider;
import com.fintech.api.domain.User; 
import com.fintech.api.dto.LoginRequest;
import com.fintech.api.dto.LoginResponse;
import com.fintech.api.dto.SignupRequest;
import com.fintech.api.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fintech.api.config.RedisConfig;
import java.time.Duration;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {


    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtUtil;

    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String accesstoken = jwtUtil.createToken(request.getEmail(), role);
        String refreshToken = jwtUtil.createRefreshToken(request.getEmail(), role);

        redisTemplate.opsForValue().set("refresh:" + request.getEmail(),
        
            refreshToken, Duration.ofDays(1)
        );
        
        return ResponseEntity.ok(new LoginResponse(accesstoken, refreshToken));
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


    @PostMapping("/refresh")
    public ResponseEntity <?> refreshToken (HttpServletRequest request) {
        String header = request.getHeader("Authorization"); //이때는 AccessToken이 아닌 RefreshToken임 

        // header 가 없거나, Bearer 로 시작하지 않은 경우 예외처리
        if (header == null  || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("RefreshToken이 필요합니다.");
        }

        String refreshToken = header.substring(7); // Bearer  이후 문자열 추출

        if (!jwtUtil.validateToken(refreshToken)) { // 추출한 토큰이 유효한지 검증 ->만료했는지 , 서명 무결성 확인
            return ResponseEntity.status(401).body("refresh token 유효하지 x");

        }

        String email = jwtUtil.getUsername(refreshToken);// refreshtoken에서 사용자 email 추출

        String redisToken = redisTemplate.opsForValue().get("refresh:" + email); //redis에 저장된 refreshToken 가져오기

        if (redisToken == null || !redisToken.equals(refreshToken)) {// redis에 없거나, 일치하지 않으면 탈취/만료된 토큰-> 인증 실패
            return ResponseEntity.status(401).body("RefreshToken이 만료되었거나 일치하지 않음.");
        }

        String role = jwtUtil.getUserRole(refreshToken); // refreshToken이 정상인 경우, 새로운 accessToken 생성
        String newAccessToken = jwtUtil.createToken(email, role); //추출된 권한으로 만들기
        
        // 새로운 accessToken과 refreshToken return
        return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken));
    }

    // 1. client logout 요청
    // 2. server accessToken parsing -> email 추출
    // 3. redis에서 refresh: <email> key 삭제
    // 4. return 200 ok

    // refresh token은 redis에서 지워지는데, accesstoken자체는 만료시간 30분 이내에는 살아있다는문제
    // 이를 위해 블랙리스트 도입
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("AccessToken이 필요합니다.");
        }

        String accessToken = header.substring(7);

        if (!jwtUtil.validateToken(accessToken)) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }

        String email = jwtUtil.getUsername(accessToken);

        redisTemplate.delete("refresh:" + email); //refreshToken redis에서 삭제

        // todo
        // accessToken 을 블랙리스트 등록 TTL: 남은 유효시간

        long remain_time = jwtUtil.getExpiration(accessToken);
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", Duration.ofMillis(remain_time));


        return ResponseEntity.ok("logout 완료(accessToken 은 blacklist 처리)");
    }
}
