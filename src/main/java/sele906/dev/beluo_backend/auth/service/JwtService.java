package sele906.dev.beluo_backend.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.token.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.token.expiration}")
    private long refreshExpiration;

    // 비밀키 객체로 변환
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 토큰 생성
    public String generateAccessToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId) // 토큰 안에 userId 저장
                .claim("role", role) //role 가져오기
                .issuedAt(new Date()) // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + accessExpiration)) // 만료 시간
                .signWith(getKey()) // 비밀키로 서명
                .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .subject(userId) // 토큰 안에 userId 저장
                .issuedAt(new Date()) // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration)) // 만료 시간
                .signWith(getKey()) // 비밀키로 서명
                .compact();
    }

    // 토큰에서 userId 꺼내기
    public String extractUserId(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 토큰에서 role 꺼내기
    public String extractRole(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }

    //토큰이 유효한지 확인
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
