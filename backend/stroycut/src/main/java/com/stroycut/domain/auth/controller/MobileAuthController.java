package com.stroycut.domain.auth.controller;

import com.stroycut.domain.auth.model.dto.GoogleLoginRequest;
import com.stroycut.domain.auth.model.dto.TokenDto;
import com.stroycut.domain.auth.service.MobileAuthService;
import com.stroycut.global.common.model.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 모바일 앱 인증 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MobileAuthController {

    private final MobileAuthService mobileAuthService;

    /**
     * 안드로이드 앱에서 전송한 구글 ID 토큰으로 로그인/회원가입 처리
     * @param request 구글 ID 토큰이 포함된 요청 객체
     * @return JWT 액세스 토큰과 리프레시 토큰
     */
    @PostMapping("/google/mobile")
    public ResponseEntity<BaseResponse<TokenDto>> googleLoginMobile(@RequestBody GoogleLoginRequest request) {
        log.info("모바일 구글 로그인 요청 - ID 토큰 길이: {}", request.getIdToken().length());
        
        // ID 토큰 검증 및 JWT 토큰 발급
        TokenDto tokenDto = mobileAuthService.processGoogleLogin(request.getIdToken());
        log.info("토큰이요",tokenDto.getAccessToken());
        System.out.println("토큰ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ"+  tokenDto.getAccessToken());
        return ResponseEntity.ok(new BaseResponse<>(tokenDto));
    }
}