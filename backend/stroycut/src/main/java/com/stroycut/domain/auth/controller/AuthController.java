package com.stroycut.domain.auth.controller;

import com.stroycut.domain.auth.model.CustomUserDetails;
import com.stroycut.domain.auth.model.dto.GoogleLoginRequest;
import com.stroycut.domain.auth.model.dto.TokenDto;
import com.stroycut.domain.auth.service.AuthService;
import com.stroycut.domain.auth.service.MobileAuthService;
import com.stroycut.domain.auth.util.JWTUtil;
import com.stroycut.global.common.model.dto.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MobileAuthService mobileAuthService;
    private final JWTUtil jwtUtil;

    // 앱 로그인 엔드포인트 - 앱에서 ID 토큰으로 로그인
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenDto>> appLogin(@RequestBody GoogleLoginRequest request) {
        log.info("앱 로그인 요청 - 클라이언트: 모바일, 인증 방식: 구글");

        // ID 토큰 검증 및 JWT 토큰 발급
        TokenDto tokenDto = mobileAuthService.processGoogleLogin(request.getIdToken());
        return ResponseEntity.ok(new BaseResponse<>(tokenDto));
    }

    // 웹 로그인 메인 엔드포인트 ( 구글 OAuth 리다이렉트 )
    @GetMapping("/web/login")
    public void webLogin(HttpServletResponse response) throws IOException {
        log.info("웹 로그인 요청 - 리다이렉트: 구글 OAuth");
        response.sendRedirect("/oauth2/authorization/google");
    }

    // refresh 토큰 있을 시 access 토큰 갱신 요청
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenDto>> refreshToken(@RequestBody TokenDto tokenDto) {
        log.info("토큰 갱신 요청 - 리프레시 토큰 길이: {}", tokenDto.getRefreshToken());
        TokenDto newTokenDto = authService.refreshAccessToken(tokenDto.getRefreshToken());
        return ResponseEntity.ok(new BaseResponse<>(newTokenDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@RequestHeader("Authorization") String accessToken) {
        // Bearer 접두사 제거 (공백 포함)
        accessToken = accessToken.replace("Bearer ", "");
        log.info("로그아웃 요청 - 사용자 ID: {}", jwtUtil.getMemberId(accessToken));

        authService.logout(accessToken);
        return ResponseEntity.ok(new BaseResponse<>());
    }
}