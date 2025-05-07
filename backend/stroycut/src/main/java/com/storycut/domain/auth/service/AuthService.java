package com.storycut.domain.auth.service;

import com.storycut.domain.auth.model.dto.TokenDto;
import com.storycut.domain.auth.util.JWTUtil;
import com.storycut.domain.member.model.entity.Member;
import com.storycut.domain.member.repository.MemberRepository;
import com.storycut.global.exception.BusinessException;
import com.storycut.global.model.dto.BaseResponseStatus;
import com.storycut.global.util.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final AesEncryptionUtil encryptionUtil;
    
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60; // 7일 (초 단위)
    private static final long PKCE_VERIFIER_EXPIRE_TIME = 10 * 60; // 10분 (초 단위)
    private static final long AUTH_STATE_EXPIRE_TIME = 10 * 60; // 10분 (초 단위)
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String GOOGLE_CLIENT_ID;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;
    
    @Value("${app.dev-mode:false}")
    private boolean devMode;

    @Transactional
    public void saveRefreshToken(Long memberId, String refreshToken) {
        // Redis에 리프레시 토큰 저장 (키: RT:memberId, 값: refreshToken)
        redisTemplate.opsForValue().set(
                "RT:" + memberId,
                refreshToken,
                REFRESH_TOKEN_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        log.info("리프레시 토큰 저장 완료 - 사용자 ID: {}", memberId);
    }
    
    @Transactional
    public void saveGoogleRefreshToken(Long memberId, String encryptedGoogleRefreshToken) {
        // Redis에 암호화된 구글 리프레시 토큰 저장 (키: G_RT:memberId, 값: encryptedGoogleRefreshToken)
        // 구글 리프레시 토큰은 만료 시간이 없으므로 영구 저장
        redisTemplate.opsForValue().set("G_RT:" + memberId, encryptedGoogleRefreshToken);
        log.info("암호화된 구글 리프레시 토큰 저장 완료 - 사용자 ID: {}", memberId);
    }
    
    /**
     * PKCE 코드 검증기 저장
     */
    @Transactional
    public void savePkceVerifier(Long memberId, String codeVerifier) {
        // Redis에 PKCE 코드 검증기 저장 (키: PKCE:memberId, 값: codeVerifier)
        redisTemplate.opsForValue().set(
                "PKCE:" + memberId,
                codeVerifier,
                PKCE_VERIFIER_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        log.info("PKCE 코드 검증기 저장 완료 - 사용자 ID: {}", memberId);
    }
    
    /**
     * CSRF 방지를 위한 인증 상태 저장
     */
    @Transactional
    public void saveAuthState(String state, Long memberId) {
        // Redis에 인증 상태 토큰 저장 (키: STATE:state, 값: memberId)
        redisTemplate.opsForValue().set(
                "STATE:" + state,
                String.valueOf(memberId),
                AUTH_STATE_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        log.info("인증 상태 토큰 저장 완료 - 상태: {}, 사용자 ID: {}", state, memberId);
    }
    
    /**
     * 인증 상태 검증 및 사용자 ID 반환
     */
    @Transactional
    public Long validateAuthState(String state) {
        String memberIdStr = redisTemplate.opsForValue().get("STATE:" + state);
        if (memberIdStr == null) {
            log.warn("인증 상태 토큰을 찾을 수 없음 - 상태: {}", state);
            return null;
        }
        
        // 상태 토큰 사용 후 삭제 (재사용 방지)
        redisTemplate.delete("STATE:" + state);
        
        return Long.valueOf(memberIdStr);
    }
    
    /**
     * PKCE 코드 검증기 조회
     */
    @Transactional(readOnly = true)
    public String getPkceVerifier(Long memberId) {
        // Redis에서 PKCE 코드 검증기 조회
        String codeVerifier = redisTemplate.opsForValue().get("PKCE:" + memberId);
        if (codeVerifier == null) {
            log.warn("PKCE 코드 검증기를 찾을 수 없음 - 사용자 ID: {}", memberId);
        }
        
        // 검증기 사용 후 삭제 (재사용 방지)
        redisTemplate.delete("PKCE:" + memberId);
        
        return codeVerifier;
    }
    
    /**
     * 구글 리프레시 토큰 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasGoogleRefreshToken(Long memberId) {
        return redisTemplate.hasKey("G_RT:" + memberId);
    }

    @Transactional
    public TokenDto refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(BaseResponseStatus.INVALID_ID_TOKEN);
        }

        // 리프레시 토큰에서 사용자 ID 추출
        Long memberId = jwtUtil.getMemberId(refreshToken);
        
        // Redis에서 저장된 리프레시 토큰 가져오기
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + memberId);
        
        // 저장된 리프레시 토큰과 일치하는지 확인
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(BaseResponseStatus.REFRESH_TOKEN_FAILED);
        }

        // 사용자 존재 여부 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.USER_NOT_FOUND));

        // 새로운 액세스 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(memberId);
        
        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 리프레시 토큰 유지
                .build();
    }
    
    @Transactional
    public String refreshGoogleAccessToken(Long memberId) {
        log.info("구글 액세스 토큰 갱신 시작 - 사용자 ID: {}", memberId);
        
        // 개발 모드에서는 더미 데이터 반환
        if (devMode) {
            log.info("개발 모드: 더미 구글 액세스 토큰 생성");
            String dummyToken = "refreshed_dummy_google_access_token_" + System.currentTimeMillis();
            
            // 멤버 엔티티 업데이트
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(BaseResponseStatus.USER_NOT_FOUND));
            member.updateGoogleAccessToken(dummyToken);
            memberRepository.save(member);
            
            return dummyToken;
        }
        
        // Redis에서 저장된 암호화된 구글 리프레시 토큰 가져오기
        String encryptedGoogleRefreshToken = redisTemplate.opsForValue().get("G_RT:" + memberId);
        
        if (encryptedGoogleRefreshToken == null || encryptedGoogleRefreshToken.isEmpty()) {
            log.error("구글 리프레시 토큰을 찾을 수 없음 - 사용자 ID: {}", memberId);
            throw new BusinessException(BaseResponseStatus.REFRESH_TOKEN_FAILED);
        }
        
        try {
            // 암호화된 리프레시 토큰 복호화
            String googleRefreshToken = encryptionUtil.decrypt(encryptedGoogleRefreshToken);
            
            // Google Token API 호출을 위한 파라미터 준비
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", GOOGLE_CLIENT_ID);
            formData.add("client_secret", GOOGLE_CLIENT_SECRET);
            formData.add("refresh_token", googleRefreshToken);
            formData.add("grant_type", "refresh_token");
            
            log.info("구글 토큰 갱신 파라미터 - client_id: {}", GOOGLE_CLIENT_ID);
            
            // WebClient를 사용하여 Google Token API 호출
            WebClient webClient = WebClient.builder().build();
            Map<String, Object> response = webClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response == null || !response.containsKey("access_token")) {
                log.error("구글 액세스 토큰 갱신 실패 - 응답 없음");
                throw new BusinessException(BaseResponseStatus.TOKEN_GENERATION_FAILED);
            }
            
            String newGoogleAccessToken = (String) response.get("access_token");
            log.info("구글 액세스 토큰 갱신 성공 - 사용자 ID: {}", memberId);
            
            // 새 액세스 토큰을 Member 엔티티에 저장
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(BaseResponseStatus.USER_NOT_FOUND));
            member.updateGoogleAccessToken(newGoogleAccessToken);
            memberRepository.save(member);
            
            return newGoogleAccessToken;
        } catch (Exception e) {
            log.error("구글 액세스 토큰 갱신 중 오류 발생", e);
            throw new BusinessException(BaseResponseStatus.TOKEN_GENERATION_FAILED);
        }
    }

    @Transactional
    public void logout(String accessToken) {
        // 액세스 토큰 유효성 검증
        if (!jwtUtil.validateToken(accessToken)) {
            throw new BusinessException(BaseResponseStatus.INVALID_ID_TOKEN);
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
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.USER_NOT_FOUND));
    }
}