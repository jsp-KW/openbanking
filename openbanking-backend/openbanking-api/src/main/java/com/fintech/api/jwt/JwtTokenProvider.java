package com.fintech.api.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

// token 만드는 기계
// 로그인 성공시 JWT TOKEN CREATE
// 서버는 세션을 만들지 않고, JWT TOKEN만 확인

@Component
public class JwtTokenProvider {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // TOKEN을 발급하는 함수 -? 사용자 이름 + 역할 (관리자인지 아닌지)
    public String createToken(String username, String role) {
        return Jwts.builder().setSubject(username)
        .claim("role", role)
        .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+ 3600000)) //1 시간
        .signWith(key).compact();
    }
    
    // TOKEN 검증하는 함수
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch(JwtException e) {
            return false;
        }
    }


    // 토큰에서 현재 사용자 이름을 추출하는 함수
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }


    // 관리자/ 사용자 역할 꺼내기
    // 역할 정보를 추출하는 함수
    public String getUserRole(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("role", String.class);
}
}
