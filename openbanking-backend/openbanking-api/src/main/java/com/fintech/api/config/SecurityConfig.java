package com.fintech.api.config;

import java.util.List;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) //cors 허용 설정
        .csrf(AbstractHttpConfigurer::disable) // csrf 보안 비활성화 
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// 세션 저장 x
        .authorizeHttpRequests(auth -> auth // 경로별 접근 권한 설정
             .requestMatchers("/", "/hello").permitAll() 
             .requestMatchers("/redis/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
           
            .requestMatchers(
    "/", "/hello",                      // 루트 테스트용
    "/swagger-ui/**",                  // Swagger UI HTML 파일
    "/v3/api-docs/**",                 // OpenAPI JSON 명세
    "/swagger-resources/**",          // Swagger 리소스
    "/webjars/**"                      // Swagger 정적 리소스
).permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // 회원가입은 누구나 가능
            .requestMatchers(HttpMethod.POST, "/api/banks").hasRole("ADMIN") // 은행 등록 / 삭제는 관리자만 가능
            .requestMatchers(HttpMethod.DELETE, "/api/banks/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN") // 모든 사용자 정보 조회는 관리자만 가능
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN") // 모든 사용자 정보 삭제는 관리자만 가능하게
            .requestMatchers(HttpMethod.GET, "/api/accounts/my").hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(JwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // jwt 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
        //UsernamePasswordAuthenticationFilter는 spring  이 기본적으로 제공하는 로그인 처리 필터
        // 이 필터앞에 JwtAuthenticationFilter를 삽입하여 컨트롤러 진입  전 사용자 인증 정보를 setting
       return http.build();
    }
    // 패스워드 인코더 등록
    // 비밀번호 암호화를 위하여

    //react frontend 단 cors 에러 해결을 위해   
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // React 주소 허용을 위해서
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));// 모든 헤더 허용
        config.setAllowCredentials(true); // Authorization 헤더 포함 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 적용
        return source;
    }

    // 비밀번호 암호화용 Encoder 등록
    // spring security -> 암호화된 비밀번호 요구함
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 매니저 등록
    // spring security 의 인증 로직 담당
    // AuthenticationManagerBuilder 를 자동ㅈ 구성
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
