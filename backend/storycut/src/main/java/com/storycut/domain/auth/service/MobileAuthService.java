package com.storycut.domain.auth.service;

import com.storycut.domain.auth.model.GoogleOAuth2UserInfo;
import com.storycut.domain.auth.model.OAuth2UserInfo;
import com.storycut.domain.auth.model.dto.TokenDto;
import com.storycut.domain.auth.model.dto.YouTubeAuthResponse;
import com.storycut.domain.auth.util.JWTUtil;
import com.storycut.domain.member.model.entity.Member;
import com.storycut.global.exception.BusinessException;
import com.storycut.global.model.dto.BaseResponseStatus;
import com.storycut.global.util.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

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
    private final AesEncryptionUtil encryptionUtil;

    // 구글 토큰 검증 URL
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    
    // 유튜브 업로드 권한
    private static final String YOUTUBE_UPLOAD_SCOPE = "https://www.googleapis.com/auth/youtube.upload";
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String GOOGLE_CLIENT_ID;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;
    
    @Value("${app.baseUrl}")
    private String BASE_URL;
    
    @Value("${app.dev-mode:false}")
    private boolean devMode;
    
    /**
     * 구글 ID 토큰으로 로그인 처리
     * Credential Manager로 획득한 ID 토큰만 사용
     */
    @Transactional
    public TokenDto processGoogleLogin(String idToken) {
        try {
            // 1. 구글 API에서 ID 토큰 검증 및 사용자 정보 추출
            Map<String, Object> tokenInfo = verifyGoogleIdToken(idToken);
            
            // 2. OAuth2UserInfo 객체 생성
            OAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(tokenInfo);
            
            // 3. 회원 정보 조회 또는 생성
            Member member = oAuth2UserService.saveOrUpdateMember(userInfo);
            Long memberId = member.getId();
            
            // 4. 서버 JWT 토큰 발급
            String accessToken = jwtUtil.createAccessToken(memberId);
            String refreshToken = jwtUtil.createRefreshToken(memberId);
            
            // 5. 리프레시 토큰 저장
            authService.saveRefreshToken(memberId, refreshToken);
            
            log.info("구글 로그인 성공 - 멤버 ID: {}, 이메일: {}", memberId, userInfo.getEmail());
            
            // 6. 서버 JWT 토큰만 반환 (구글 API 토큰은 별도 API로 처리)
            return TokenDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {
            log.error("구글 로그인 실패", e);
            throw new BusinessException(BaseResponseStatus.GOOGLE_LOGIN_ERROR);
        }
    }

    /**
     * ID 토큰 검증
     */
    private Map<String, Object> verifyGoogleIdToken(String idToken) {
        try {
            // 개발 모드에서는 더미 사용자 정보 반환
            if (devMode && "test".equals(idToken)) {
                return createDummyUserInfo();
            }
            
            // WebClient 빈이 없는 경우 생성
            WebClient webClient = WebClient.builder().build();

            return webClient.get()
                .uri(GOOGLE_TOKEN_INFO_URL + idToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();  // 동기적 처리
        } catch (Exception e) {
            log.error("구글 ID 토큰 검증 실패", e);
            throw new BusinessException(BaseResponseStatus.TOKEN_VERIFICATION_FAILED);
        }
    }
    
    /**
     * 유튜브 권한 인증 URL 생성
     */
    @Transactional
    public YouTubeAuthResponse generateYouTubeAuthUrl(Long memberId) {
        // 1. PKCE 코드 검증기 및 도전 생성
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        
        // 2. CSRF 방지를 위한 state 생성
        String state = UUID.randomUUID().toString();
        
        // 3. 상태 및 PKCE 검증기 저장
        authService.saveAuthState(state, memberId);
        authService.savePkceVerifier(memberId, codeVerifier);
        
        // 4. 인증 URL 생성
        String authUrl = buildAuthorizationUrl(codeChallenge, state);
        
        return YouTubeAuthResponse.builder()
                .authUrl(authUrl)
                .state(state)
                .build();
    }
    
    /**
     * OAuth2 인증 코드로 구글 토큰 교환
     */
    @Transactional
    public TokenDto exchangeAuthCodeForTokens(String code, Long memberId) {
        try {
            log.info("구글 인증 코드로 토큰 교환 시작 - 사용자 ID: {}", memberId);
            
            // 1. PKCE 코드 검증기 가져오기
            String codeVerifier = authService.getPkceVerifier(memberId);
            if (codeVerifier == null || codeVerifier.isEmpty()) {
                log.error("PKCE 코드 검증기를 찾을 수 없음 - 사용자 ID: {}", memberId);
                throw new BusinessException(BaseResponseStatus.UNAUTHORIZED);
            }
            
            // 개발 모드에서는 더미 토큰 사용
            if (devMode) {
                String googleAccessToken = "dummy_google_access_token_" + System.currentTimeMillis();
                String googleRefreshToken = "dummy_google_refresh_token_" + System.currentTimeMillis();
                
                // 사용자 정보 업데이트
                Member member = authService.getMemberById(memberId);
                member.updateGoogleAccessToken(googleAccessToken);
                
                // 리프레시 토큰 암호화 저장
                authService.saveGoogleRefreshToken(memberId, encryptionUtil.encrypt(googleRefreshToken));
                
                log.info("개발 모드: 더미 구글 토큰 생성 완료 - 사용자 ID: {}", memberId);
                
                return TokenDto.builder()
                        .googleAccessToken(googleAccessToken)
                        .build();
            }
            
            // 2. Google Token API 호출을 위한 파라미터 준비
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", GOOGLE_CLIENT_ID);
            formData.add("client_secret", GOOGLE_CLIENT_SECRET);
            formData.add("code", code);
            formData.add("code_verifier", codeVerifier);
            formData.add("grant_type", "authorization_code");
            formData.add("redirect_uri", BASE_URL + "/api/auth/oauth2/callback");
            
            // 3. WebClient를 사 용하여 GoogleToken API 호출
            WebClient webClient = WebClient.builder().build();
            Map<String, Object> response = webClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response == null || !response.containsKey("access_token") || !response.containsKey("refresh_token")) {
                log.error("구글 토큰 교환 실패 - 완전한 응답을 받지 못함: {}", response);
                throw new BusinessException(BaseResponseStatus.TOKEN_GENERATION_FAILED);
            }
            
            // 4. 토큰 추출
            String googleAccessToken = (String) response.get("access_token");
            String googleRefreshToken = (String) response.get("refresh_token");
            
            // 5. 사용자 정보 업데이트 및 토큰 저장
            Member member = authService.getMemberById(memberId);
            member.updateGoogleAccessToken(googleAccessToken);
            
            // 리프레시 토큰은 암호화하여 저장
            authService.saveGoogleRefreshToken(memberId, encryptionUtil.encrypt(googleRefreshToken));
            
            log.info("구글 토큰 교환 성공 - 액세스 토큰과 리프레시 토큰 발급됨 - 사용자 ID: {}", memberId);
            
            // 6. 액세스 토큰만 반환 (리프레시 토큰은 서버에 안전하게 저장)
            return TokenDto.builder()
                    .googleAccessToken(googleAccessToken)
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("구글 인증 코드로 토큰 교환 중 오류 발생", e);
            throw new BusinessException(BaseResponseStatus.TOKEN_GENERATION_FAILED);
        }
    }
    
    /**
     * 구글 액세스 토큰 갱신
     */
    @Transactional
    public TokenDto refreshGoogleAccessToken(Long memberId) {
        log.info("구글 액세스 토큰 갱신 요청 - 사용자 ID: {}", memberId);
        
        // 개발 모드에서는 더미 액세스 토큰 생성
        if (devMode) {
            String newDummyAccessToken = "refreshed_dummy_google_access_token_" + System.currentTimeMillis();
            
            // 사용자 정보 업데이트
            Member member = authService.getMemberById(memberId);
            member.updateGoogleAccessToken(newDummyAccessToken);
            
            return TokenDto.builder()
                    .googleAccessToken(newDummyAccessToken)
                    .build();
        }
        
        // 실제 모드: 구글 API 호출
        // 구글 액세스 토큰 갱신
        String newGoogleAccessToken = authService.refreshGoogleAccessToken(memberId);
        
        // 응답 생성
        return TokenDto.builder()
                .googleAccessToken(newGoogleAccessToken)
                .build();
    }
    
    /**
     * 사용자가 유튜브 권한을 가지고 있는지 확인
     */
    public boolean hasYouTubeAccess(Long memberId) {
        // Redis에서 구글 리프레시 토큰 존재 여부 확인
        return authService.hasGoogleRefreshToken(memberId);
    }
    
    /**
     * PKCE 방식의 OAuth2 인증 URL 생성
     */
    private String buildAuthorizationUrl(String codeChallenge, String state) {
        return GOOGLE_AUTH_URL + "?" +
                "client_id=" + GOOGLE_CLIENT_ID + "&" +
                "redirect_uri=" + BASE_URL + "/api/auth/oauth2/callback" + "&" +
                "response_type=code&" +
                "scope=openid%20email%20profile%20" + YOUTUBE_UPLOAD_SCOPE + "&" +
                "access_type=offline&" +
                "prompt=consent&" +
                "state=" + state + "&" +
                "code_challenge=" + codeChallenge + "&" +
                "code_challenge_method=S256";
    }
    
    /**
     * PKCE 코드 검증기 생성 (랜덤 문자열)
     */
    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }
    
    /**
     * PKCE 코드 도전 생성 (검증기의 해시값)
     */
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘이 지원되지 않습니다.", e);
        }
    }
    
    /**
     * 테스트용 더미 사용자 정보 생성
     */
    private Map<String, Object> createDummyUserInfo() {
        Map<String, Object> dummyInfo = new HashMap<>();
        dummyInfo.put("sub", "test_user_id_" + UUID.randomUUID().toString().substring(0, 8));
        dummyInfo.put("email", "test@example.com");
        dummyInfo.put("name", "테스트 사용자");
        dummyInfo.put("picture", "https://example.com/default_profile.jpg");
        dummyInfo.put("email_verified", true);
        return dummyInfo;
    }
    
    /**
     * 테스트용 간단 로그인 처리 (개발 환경 전용)
     */
    @Transactional
    public TokenDto processTestLogin() {
        if (!devMode) {
            throw new BusinessException(BaseResponseStatus.UNAUTHORIZED);
        }
        
        log.info("테스트 로그인 처리 중...");
        return processGoogleLogin("test");
    }
}