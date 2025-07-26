// src/main/java/com/fintech/api/config/JwtAuthenticationFilter.java
package com.fintech.api.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fintech.api.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// request header에서 jwt token을 꺼내고, 유효한 경우 -> SecruityContext에 등록-> 인증된 사용자로 인식


@Component // Bean으로 자동 등록되게 만듬
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtUtil;// jwt 토큰 클래스 (생성,검증,추출)
    private final CustomUserDetailsService customUserDetailsService;// 필요한 경우 사용자 정보를 조회할때 사용하는 객체

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getServletPath();
        String method = request.getMethod();
        
        // 회원가입에 대한 부분은 인증이 필요하지 않은 경우이므로
        // if 문 처리로 이 경로에 대해서는 필터를 통과시키고
       if (
        path.startsWith("/auth") ||
        (method.equals("POST") && path.equals("/users")) ||
        (method.equals("POST") && path.equals("/users/"))
    ) {
        filterChain.doFilter(request, response);
        return;
    }
        // Authorization header 에서 jwt token 추출
        String header = request.getHeader("Authorization");

        // Bearer " 이후  문자열을 추출
        if (header != null && header.startsWith("Bearer ")) {
            String token =  header.substring(7);

            //추출한 문자열을 토큰 검증해서,
            // 유효한 토큰이라면?

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getUsername(token); 
                String role = jwtUtil.getUserRole(token); // 사용자인지 관리자인지 역할을 추출!! ROLE_USER/ ROLE_ADMIN

                // SPRING SECURIY 에서 사용할 USERDETAILS 객체 생성 
                // PASSWORD 는 비워두고
                UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(email).password("").authorities("ROLE_"+role).build();

                // 인증 객체 생성 및 SecurityContext에 등록
                // 이후 controller에서 @AuthenticationPrincipal 이나 SecurityContextHolder로 사용자 정보에 대해 access가 가능
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        
        filterChain.doFilter(request, response); // 다음 filter로 요청 전달
    }
}