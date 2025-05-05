package com.storycut.domain.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTUtil {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Access 토큰 생성 - memberId 사용
    public String createAccessToken(Long memberId) {
        return createToken(String.valueOf(memberId), ACCESS_TOKEN_EXPIRE_TIME);
    }

    // Refresh 토큰 생성 - memberId 사용
    public String createRefreshToken(Long memberId) {
        return createToken(String.valueOf(memberId), REFRESH_TOKEN_EXPIRE_TIME);
    }

    // 토큰 생성 공통 메소드
    private String createToken(String subject, long expireTime) {
        Claims claims = Jwts.claims().subject(subject).build();
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiredDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // Request 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // 디버깅용 로그 추가
        System.out.println("Authorization 헤더: " + bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token", e);
            return false;
        }
    }

    // 토큰에서 멤버 ID 추출
    public Long getMemberId(String token) {
        String memberId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        
        return Long.parseLong(memberId);
    }

    // 인증 객체 생성
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getMemberId(token).toString());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    
    // 토큰 만료 시간 반환
    public long getExpirationTime(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getExpiration().getTime();
    }
}