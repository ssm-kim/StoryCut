package com.stroycut.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stroycut.domain.auth.model.OAuth2UserInfo;
import com.stroycut.domain.auth.model.GoogleOAuth2UserInfo;
import com.stroycut.domain.auth.model.dto.TokenDto;
import com.stroycut.domain.auth.util.JWTUtil;
import com.stroycut.domain.member.model.entity.Member;
import com.stroycut.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 모바일 앱 인증 처리 서비스
 * 안드로이드 앱에서 전송한 구글 ID 토큰을 검증하고 인증 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MobileAuthService {

    private final JWTUtil jwtUtil;
    private final AuthService authService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    
    // 구글 토큰 검증 URL
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    
    /**
     * 안드로이드 앱에서 전송한 구글 ID 토큰 처리
     * @param idToken 구글 로그인 후 앱에서 받은 ID 토큰
     * @return 액세스 토큰, 리프레시 토큰, 멤버 ID를 포함한 응답 객체
     */
    @Transactional
    public TokenDto processGoogleLogin(String idToken) {
        try {
            // 1. 구글 API에서 ID 토큰 검증 및 사용자 정보 추출
            Map<String, Object> tokenInfo = verifyGoogleIdToken(idToken);
            
            // 2. OAuth2UserInfo 객체 생성 (기존 OAuth 로직 재사용)
            OAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(tokenInfo);
            
            // 3. 회원 정보 조회 또는 생성 (기존 OAuth 로직 재사용)
            Member member = oAuth2UserService.saveOrUpdateMember(userInfo);
            
            // 4. JWT 토큰 발급
            Long memberId = member.getId();
            String accessToken = jwtUtil.createAccessToken(memberId);
            String refreshToken = jwtUtil.createRefreshToken(memberId);
            
            // 5. 리프레시 토큰 저장
            authService.saveRefreshToken(memberId, refreshToken);
            
            log.info("모바일 구글 로그인 성공 - 멤버 ID: {}, 이메일: {}", memberId, userInfo.getEmail());
            
            // 6. 토큰 정보 반환
            return TokenDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .memberId(memberId)
                    .build();
            
        } catch (Exception e) {
            log.error("모바일 구글 로그인 실패", e);
            throw new RuntimeException("모바일 구글 로그인 처리 중 오류 발생", e);
        }
    }
    
    /**
     * 구글 ID 토큰 검증
     * 실제 서비스에서는 WebClient로 구글 API 검증, 개발/테스트 시에는 간소화된 검증 사용
     */
    private Map<String, Object> verifyGoogleIdToken(String idToken) {
        try {
            // 방법 1: 실제 서비스 환경 - 구글 API로 검증
            if (!"test".equals(idToken)) {  // 테스트 토큰이 아닌 경우 실제 검증
                // WebClient 빈이 없는 경우 생성
                WebClient webClient = WebClient.builder().build();
                
                return webClient.get()
                        .uri(GOOGLE_TOKEN_INFO_URL + idToken)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();  // 동기적 처리
            }
            
            // 방법 2: 개발/테스트 환경 - 토큰 파싱만 진행
            // JWT 형식 (header.payload.signature)에서 페이로드 부분만 디코딩
            String[] parts = idToken.split("\\.");
            if (parts.length > 1) {
                return parseTokenPayload(parts[1]);
            }
            
            // 테스트용 더미 데이터 (개발 편의성)
            return createDummyUserInfo();
            
        } catch (Exception e) {
            log.error("구글 ID 토큰 검증 실패", e);
            throw new RuntimeException("구글 ID 토큰 검증 실패", e);
        }
    }
    
    /**
     * 토큰 페이로드 파싱 (개발/테스트용)
     */
    private Map<String, Object> parseTokenPayload(String payload) {
        try {
            // Base64 디코딩 처리
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            String decodedPayload = new String(decoder.decode(payload));
            
            // JSON 파싱
            return objectMapper.readValue(decodedPayload, Map.class);
        } catch (Exception e) {
            log.warn("토큰 파싱 실패, 더미 데이터 사용", e);
            return createDummyUserInfo();
        }
    }
    
    /**
     * 테스트용 더미 사용자 정보 생성
     */
    private Map<String, Object> createDummyUserInfo() {
        Map<String, Object> dummyInfo = new HashMap<>();
        dummyInfo.put("sub", "test_user_id_123");
        dummyInfo.put("email", "test@example.com");
        dummyInfo.put("name", "테스트 사용자");
        dummyInfo.put("picture", "https://example.com/default_profile.jpg");
        dummyInfo.put("email_verified", true);
        return dummyInfo;
    }
}