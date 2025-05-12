package com.storycut.domain.auth.controller;

import com.storycut.domain.auth.model.CustomUserDetails;
import com.storycut.domain.auth.model.dto.GoogleLoginRequest;
import com.storycut.domain.auth.model.dto.TokenDto;
import com.storycut.domain.auth.model.dto.YouTubeAuthResponse;
import com.storycut.domain.auth.service.GoogleAuthService;
import com.storycut.domain.auth.service.TokenService;
import com.storycut.domain.auth.util.JWTUtil;
import com.storycut.global.model.dto.BaseResponse;
import com.storycut.global.model.dto.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "로그인, 토큰 관리, 로그아웃 API")
public class AuthController {

    private final TokenService tokenService;
    private final GoogleAuthService googleAuthService;
    private final JWTUtil jwtUtil;
    
    @Value("${app.dev-mode:false}")
    private boolean devMode;
    
    @Value("${app.baseUrl}")
    private String baseUrl;

    //==================================
    // 구글 로그인 관련 API
    //==================================

    @Operation(summary = "구글 로그인 (모바일)", description = "모바일 앱에서 구글 ID 토큰으로 로그인합니다")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenDto>> googleLogin(
        @RequestBody GoogleLoginRequest request) {
        log.info("[구글 로그인] 모바일 앱 요청");

        TokenDto tokenDto = googleAuthService.processGoogleLogin(request.getIdToken());
        return ResponseEntity.ok(new BaseResponse<>(tokenDto));
    }

    @Operation(summary = "웹 로그인", description = "웹 환경에서 구글 로그인 페이지로 리다이렉트합니다")
    @GetMapping("/web/login")
    public void googleWebLogin(
        HttpServletResponse response) throws IOException {
        log.info("[구글 로그인] 웹 요청 - OAuth 리다이렉트");
        response.sendRedirect("/oauth2/authorization/google");
    }

    @Operation(summary = "테스트 로그인", description = "개발 환경에서만 사용 가능한 테스트 로그인")
    @GetMapping("/test-login")
    public ResponseEntity<BaseResponse<TokenDto>> testLogin() {
        if (!devMode) {
            return ResponseEntity.badRequest().body(new BaseResponse<>(BaseResponseStatus.UNAUTHORIZED));
        }

        log.info("[테스트 로그인] 개발 모드에서 테스트 계정 로그인");
        TokenDto tokenDto = googleAuthService.processTestLogin();
        return ResponseEntity.ok(new BaseResponse<>(tokenDto));
    }

    @Operation(summary = "OAuth2 콜백 처리", description = "구글 인증 후 리다이렉트되는 콜백을 처리합니다")
    @GetMapping("/oauth2/callback")
    public RedirectView handleOAuth2Callback(
        @RequestParam("code") String code,
        @RequestParam("state") String state) {
        log.info("[OAuth2 콜백] 인증 코드 수신 - state: {}", state);
        
        // 상태 토큰 검증 및 사용자 ID 획득
        Long memberId = tokenService.validateAuthState(state);
        if (memberId == null) {
            log.warn("[OAuth2 콜백] 유효하지 않은 상태 토큰: {}", state);
            return new RedirectView(baseUrl + "/auth/error?error=invalid_state");
        }
        
        try {
            // 인증 코드로 토큰 교환
            TokenDto tokenDto = googleAuthService.exchangeAuthCodeForTokens(code, memberId);
            
            // 토큰 교환 성공 후 상태 토큰 삭제
            tokenService.deleteAuthState(state);
            log.info("[OAuth2 콜백] 토큰 교환 성공 - 사용자 ID: {}", memberId);
            
            // 딥링크를 통해 앱으로 리다이렉트
            return new RedirectView("storycut://auth/success?token=" + tokenDto.getGoogleAccessToken());
        } catch (Exception e) {
            log.error("[OAuth2 콜백] 토큰 교환 실패", e);
            return new RedirectView(baseUrl + "/auth/error?error=token_exchange_failed");
        }
    }

    //==================================
    // 유튜브 권한 관련 API
    //==================================

    @Operation(summary = "유튜브 권한 URL 생성", description = "유튜브 업로드 권한 획득을 위한 인증 URL을 생성합니다")
    @GetMapping("/youtube/auth")
    public ResponseEntity<BaseResponse<YouTubeAuthResponse>> getYouTubeAuthUrl(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        log.info("[유튜브 권한] URL 생성 요청 - 사용자 ID: {}", memberId);
        
        YouTubeAuthResponse response = googleAuthService.generateYouTubeAuthUrl(memberId);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

    @Operation(summary = "유튜브 권한 확인", description = "사용자가 유튜브 업로드 권한을 가지고 있는지 확인합니다")
    @GetMapping("/youtube/status")
    public ResponseEntity<BaseResponse<Boolean>> checkYouTubeAccess(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        log.info("[유튜브 권한] 상태 확인 - 사용자 ID: {}", memberId);
        
        boolean hasAccess = googleAuthService.hasYouTubeAccess(memberId);
        return ResponseEntity.ok(new BaseResponse<>(hasAccess));
    }

    //==================================
    // 토큰 관리 API
    //==================================

    @Operation(summary = "구글 토큰 갱신", description = "만료된 구글 액세스 토큰을 갱신합니다")
    @PostMapping("/google-refresh")
    public ResponseEntity<BaseResponse<TokenDto>> refreshGoogleToken(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        log.info("[토큰 갱신] 구글 액세스 토큰 갱신 - 사용자 ID: {}", memberId);
        
        TokenDto newGoogleTokenDto = googleAuthService.refreshGoogleAccessToken(memberId);
        return ResponseEntity.ok(new BaseResponse<>(newGoogleTokenDto));
    }

    @Operation(summary = "JWT 토큰 갱신", description = "만료된 JWT 액세스 토큰을 리프레시 토큰으로 갱신합니다")
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenDto>> refreshJwtToken(
        @RequestBody TokenDto tokenDto) {
        log.info("[토큰 갱신] JWT 액세스 토큰 갱신 요청");
        
        TokenDto newTokenDto = tokenService.refreshAccessToken(tokenDto.getRefreshToken());
        return ResponseEntity.ok(new BaseResponse<>(newTokenDto));
    }

    //==================================
    // 로그아웃 API
    //==================================

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리하고 토큰을 무효화합니다")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        log.info("[로그아웃] 요청 - 사용자 ID: {}", memberId);

        tokenService.logout(memberId);
        return ResponseEntity.ok(new BaseResponse<>());
    }
}