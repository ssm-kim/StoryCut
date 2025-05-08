package com.storycut.domain.auth.controller;

import com.storycut.domain.auth.model.dto.AuthCodeRequest;
import com.storycut.domain.auth.model.dto.GoogleLoginRequest;
import com.storycut.domain.auth.model.dto.TokenDto;
import com.storycut.domain.auth.model.dto.YouTubeAuthResponse;
import com.storycut.domain.auth.service.AuthService;
import com.storycut.domain.auth.service.MobileAuthService;
import com.storycut.domain.auth.util.JWTUtil;
import com.storycut.global.model.dto.BaseResponse;
import com.storycut.global.model.dto.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final MobileAuthService mobileAuthService;
    private final JWTUtil jwtUtil;
    
    @Value("${app.dev-mode:false}")
    private boolean devMode;
    
    @Value("${app.baseUrl}")
    private String baseUrl;

    /**
     * 앱 로그인 엔드포인트 - 앱에서 ID 토큰으로 로그인
     * Credential Manager를 통해 획득한 ID 토큰 사용
     */
    @Operation(summary = "구글 ID 토큰 로그인", description = "안드로이드 앱에서 Credential Manager로 획득한 구글 ID 토큰으로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenDto>> appLogin(@RequestBody GoogleLoginRequest request) {
        log.info("앱 로그인 요청 - 클라이언트: 모바일, 인증 방식: 구글");

        // ID 토큰 검증 및 JWT 토큰 발급
        TokenDto tokenDto = mobileAuthService.processGoogleLogin(request.getIdToken());
        return ResponseEntity.ok(new BaseResponse<>(tokenDto));
    }

    /**
     * 유튜브 권한 요청 URL 생성 엔드포인트
     * 앱에서 유튜브 업로드 권한이 필요할 때 호출
     */
    @Operation(summary = "유튜브 권한 요청 URL 생성", description = "유튜브 업로드 권한을 위한 인증 URL을 생성합니다.")
    @GetMapping("/youtube/auth")
    public ResponseEntity<BaseResponse<YouTubeAuthResponse>> getYouTubeAuthUrl(@RequestHeader("Authorization") String accessToken) {
        // Bearer 접두사 제거
        accessToken = accessToken.replace("Bearer ", "");
        Long memberId = jwtUtil.getMemberId(accessToken);
        
        log.info("유튜브 권한 요청 URL 생성 - 사용자 ID: {}", memberId);
        
        // 유튜브 권한 인증 URL 생성
        YouTubeAuthResponse response = mobileAuthService.generateYouTubeAuthUrl(memberId);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }
    
    /**
     * 구글 OAuth2 콜백 처리 엔드포인트
     * 유튜브 권한 동의 후 리다이렉트되는 엔드포인트
     */
    @Operation(summary = "구글 OAuth2 콜백 처리", description = "구글 OAuth2 인증 후 콜백을 처리합니다.")
    @GetMapping("/oauth2/callback")
    public RedirectView handleOAuth2Callback(@RequestParam("code") String code, 
                                           @RequestParam("state") String state) {
        log.info("구글 OAuth2 콜백 처리 - 인증 코드 수신");
        
        // 상태 토큰 검증 및 사용자 ID 획득
        Long memberId = authService.validateAuthState(state);
        if (memberId == null) {
            log.error("인증 상태 검증 실패 - 잘못된 state 값");
            return new RedirectView(baseUrl + "/auth/error?error=invalid_state");
        }
        
        try {
            // 인증 코드로 토큰 교환
            TokenDto tokenDto = mobileAuthService.exchangeAuthCodeForTokens(code, memberId);
            log.info("구글 토큰 전체 : {}", tokenDto.toString());
            log.info("구글 OAuth2 토큰 교환 성공 - 사용자 ID: {} 구글 액세스 토큰 : {}", memberId, tokenDto.getGoogleAccessToken());
            
            // 토큰 교환 성공 후 상태 토큰 삭제
            authService.deleteAuthState(state);
            
            // 로컬용
            // return new RedirectView(baseUrl + "/auth/success?token=" + tokenDto.getGoogleAccessToken());

            // 운영용
            return new RedirectView("storycut://auth/success?token=" + tokenDto.getGoogleAccessToken());
        } catch (Exception e) {
            log.error("구글 OAuth2 토큰 교환 실패", e);
            return new RedirectView(baseUrl + "/auth/error?error=token_exchange_failed");
        }
    }

    /**
     * 유튜브 권한 확인 엔드포인트
     * 사용자가 유튜브 권한을 가지고 있는지 확인
     */
    @Operation(summary = "유튜브 권한 확인", description = "사용자가 유튜브 업로드 권한을 가지고 있는지 확인합니다.")
    @GetMapping("/youtube/status")
    public ResponseEntity<BaseResponse<Boolean>> checkYouTubeAccess(@RequestHeader("Authorization") String accessToken) {
        // Bearer 접두사 제거
        accessToken = accessToken.replace("Bearer ", "");
        Long memberId = jwtUtil.getMemberId(accessToken);
        
        log.info("유튜브 권한 확인 - 사용자 ID: {}", memberId);
        
        // 유튜브 권한 확인
        boolean hasAccess = mobileAuthService.hasYouTubeAccess(memberId);
        return ResponseEntity.ok(new BaseResponse<>(hasAccess));
    }

    /**
     * 구글 액세스 토큰 갱신 엔드포인트
     */
    @Operation(summary = "구글 액세스 토큰 갱신", description = "구글 액세스 토큰이 만료되었을 때 갱신합니다.")
    @PostMapping("/google-refresh")
    public ResponseEntity<BaseResponse<TokenDto>> refreshGoogleToken(@RequestHeader("Authorization") String accessToken) {
        // Bearer 접두사 제거
        accessToken = accessToken.replace("Bearer ", "");
        Long memberId = jwtUtil.getMemberId(accessToken);
        log.info("구글 액세스 토큰 갱신 요청 - 사용자 ID: {}", memberId);
        
        TokenDto newGoogleTokenDto = mobileAuthService.refreshGoogleAccessToken(memberId);
        return ResponseEntity.ok(new BaseResponse<>(newGoogleTokenDto));
    }

    /**
     * JWT 토큰 갱신 엔드포인트
     */
    @Operation(summary = "JWT 토큰 갱신", description = "JWT 액세스 토큰이 만료되었을 때 리프레시 토큰으로 갱신합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenDto>> refreshToken(@RequestBody TokenDto tokenDto) {
        log.info("토큰 갱신 요청 - 리프레시 토큰 길이: {}", tokenDto.getRefreshToken().length());
        TokenDto newTokenDto = authService.refreshAccessToken(tokenDto.getRefreshToken());
        return ResponseEntity.ok(new BaseResponse<>(newTokenDto));
    }

    /**
     * 로그아웃 엔드포인트
     */
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@RequestHeader("Authorization") String accessToken) {
        // Bearer 접두사 제거
        accessToken = accessToken.replace("Bearer ", "");
        log.info("로그아웃 요청 - 사용자 ID: {}", jwtUtil.getMemberId(accessToken));

        authService.logout(accessToken);
        return ResponseEntity.ok(new BaseResponse<>());
    }

    /**
     * 웹 로그인 메인 엔드포인트 (구글 OAuth 리다이렉트)
     */
    @Operation(summary = "웹 로그인 리다이렉트", description = "웹 환경에서 구글 OAuth 로그인으로 리다이렉트합니다.")
    @GetMapping("/web/login")
    public void webLogin(HttpServletResponse response) throws IOException {
        log.info("웹 로그인 요청 - 리다이렉트: 구글 OAuth");
        response.sendRedirect("/oauth2/authorization/google");
    }
    
    /**
     * 테스트 로그인 엔드포인트 (개발 환경 전용)
     */
    @Operation(summary = "테스트 로그인", description = "개발 환경에서 테스트용 로그인을 처리합니다. (개발 모드에서만 사용 가능)")
    @GetMapping("/test-login")
    public ResponseEntity<BaseResponse<TokenDto>> testLogin() {
        if (!devMode) {
            return ResponseEntity.badRequest().body(new BaseResponse<>(BaseResponseStatus.UNAUTHORIZED));
        }
        
        log.info("테스트 로그인 요청");
        TokenDto tokenDto = mobileAuthService.processTestLogin();
        return ResponseEntity.ok(new BaseResponse<>(tokenDto));
    }
}