package com.fintech.api.config;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.*;
import java.util.Base64;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

// token 만드는 기계
// 로그인 성공시 JWT TOKEN CREATE
// 서버는 세션을 만들지 않고, JWT TOKEN만 확인

@Component
public class JwtTokenProvider {
   
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    // 개인키 불러오기

    public JwtTokenProvider() throws Exception {
        InputStream priStrem = getClass().getClassLoader().getResourceAsStream("keys/private_converted.pem");
        String priKeyContent = new String(priStrem.readAllBytes(), StandardCharsets.UTF_8)
        .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");

        
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priKeyContent));
        this.privateKey = keyFactory.generatePrivate(keySpec);


        // 공개키 불러오기
           InputStream pubStream = getClass().getClassLoader().getResourceAsStream("keys/public.pem");
        String pubKeyContent = new String(pubStream.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pubKeyContent));
        this.publicKey = keyFactory.generatePublic(pubKeySpec);
    }   
   
    // jwt token 발급-> 서버
    // 서버의 개인키 privateKey로 서명하고, RS256 서명알고리즘 사용
    // 만료시간 1시간짜리->30분으로 대체
    // TOKEN을 발급하는 함수 -? 사용자 이름 + 역할 (관리자인지 아닌지)
    public String createToken(String username, String role) {
        return Jwts.builder().setSubject(username)
        .claim("role", role)
        .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+ 1800000)) //30분
        .signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }
    
    // TOKEN 검증하는 함수
    // 서명이 유효하면 true, 유효하지 않으면 false
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
            return true;
        } catch(JwtException e) {
            return false;
        }
    }


    // 토큰에서 현재 사용자 이름을 추출하는 함수
    // 공개키를 사용
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody().getSubject();
    }


    // 관리자/ 사용자 역할 꺼내기
    // 공개키 사용
    // 역할 정보를 추출하는 함수
    public String getUserRole(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("role", String.class);
}
    // redis 도입으로 인한 refreshToken 생성 함수
    public String createRefreshToken(String email, String role) {
       
        return Jwts.builder().setSubject(email).
        claim("role",role)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 604800000))// 7일동안 유효
        .signWith(privateKey, SignatureAlgorithm.RS256).compact();    
    
    }

    // 만료시간 30분자리 토큰이 있는데, 로그아웃을 5분만에 하면?
    // 이 토큰은 25분동안 메모리 차지... -> 이 경우의 토큰을 블랙리스트로 !

    public long getExpiration(String accessToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(accessToken)
        .getBody();
    
        Date expiration = claims.getExpiration(); // 토큰에 들어있는 만료시각을 추출해서 저장하고
        return expiration.getTime()-System.currentTimeMillis(); // 만료시각 - 현재시각 : 얼마나 남았는지 check
    }   //이 시간을 통해서 redis에 blacklist로 등록할 TTL 로 활용
}
