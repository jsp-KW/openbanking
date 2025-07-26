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
    // 만료시간 1시간짜리
    // TOKEN을 발급하는 함수 -? 사용자 이름 + 역할 (관리자인지 아닌지)
    public String createToken(String username, String role) {
        return Jwts.builder().setSubject(username)
        .claim("role", role)
        .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+ 3600000)) //1 시간
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
}
