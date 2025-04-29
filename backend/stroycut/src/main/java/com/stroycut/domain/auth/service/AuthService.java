package com.stroycut.domain.auth.service;

import com.stroycut.domain.auth.model.dto.TokenDto;
import com.stroycut.domain.auth.util.JWTUtil;
import com.stroycut.domain.member.model.entity.Member;
import com.stroycut.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60; // 7일 (초 단위)

    @Transactional
    public void saveRefreshToken(Long memberId, String refreshToken) {
        // Redis에 리프레시 토큰 저장 (키: memberId, 값: refreshToken)
        redisTemplate.opsForValue().set(
                "RT:" + memberId,
                refreshToken,
                REFRESH_TOKEN_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        log.info("리프레시 토큰 저장 완료 - 사용자 ID: {}", memberId);
    }

    @Transactional
    public TokenDto refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 리프레시 토큰에서 사용자 ID 추출
        Long memberId = jwtUtil.getMemberId(refreshToken);
        
        // Redis에서 저장된 리프레시 토큰 가져오기
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + memberId);
        
        // 저장된 리프레시 토큰과 일치하는지 확인
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh token not found or not matched");
        }

        // 사용자 존재 여부 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + memberId));

        // 새로운 액세스 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(memberId);
        
        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 리프레시 토큰 유지
                .build();
    }

    @Transactional
    public void logout(String accessToken) {
        // 액세스 토큰 유효성 검증
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("Invalid access token");
        }

        // 토큰에서 사용자 ID 추출
        Long memberId = jwtUtil.getMemberId(accessToken);
        
        // Redis에서 리프레시 토큰 삭제
        redisTemplate.delete("RT:" + memberId);
        
        // 액세스 토큰 블랙리스트에 추가 (남은 유효 시간동안)
        // 토큰의 만료 시간 계산
        long expiration = jwtUtil.getExpirationTime(accessToken) - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                    "BL:" + accessToken,
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
        }
        
        log.info("로그아웃 처리 완료 - 사용자 ID: {}", memberId);
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + memberId));
    }
}