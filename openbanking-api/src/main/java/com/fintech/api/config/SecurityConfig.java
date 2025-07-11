package com.fintech.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

// JWT ->TOKEN 기반 -> STATELESS (세션을 만들지 X), 매 요청마다 토큰으로 인증

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // @PreAuthorize 동작하기 위해
public class SecurityConfig { 
    private final JwtAuthenticationFilter JwtAuthenticationFilter;

    @Bean
    // 모든 인증/ 인가 규칙 정해지는
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // csrf 비활성화 -> csrf란 웹브라우저 기반 세션 보안을 위한 기능
        // JWT 기반 REST API에서는 불필요 하므로 비활성화
        // /auth/** -> 로그인 회원가입과 같은 url 누구나 접근 가능
        // 나머지는 인증이 필요


    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
             .requestMatchers("/", "/hello").permitAll() 
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers(
    "/", "/hello",                      // 루트 테스트용
    "/swagger-ui/**",                  // Swagger UI HTML 파일
    "/v3/api-docs/**",                 // OpenAPI JSON 명세
    "/swagger-resources/**",          // Swagger 리소스
    "/webjars/**"                      // Swagger 정적 리소스
).permitAll()
            .requestMatchers(HttpMethod.POST, "/banks").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/banks/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(JwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

       return http.build();
    }
    // 패스워드 인코더 등록
    // 비밀번호 암호화를 위하여

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
