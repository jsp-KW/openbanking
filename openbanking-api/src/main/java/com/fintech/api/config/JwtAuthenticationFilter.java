// src/main/java/com/fintech/api/config/JwtAuthenticationFilter.java
package com.fintech.api.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fintech.api.jwt.JwtUtil;
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
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token =  header.substring(7);


            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getUsername(token);
                String role = jwtUtil.getUserRole(token); // 사용자인지 관리자인지 역할을 추출!!

                UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(email).password("").authorities("ROLE_"+role).build();

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}