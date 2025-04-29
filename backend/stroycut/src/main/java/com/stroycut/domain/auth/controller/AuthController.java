package com.stroycut.domain.auth.controller;

import com.stroycut.domain.auth.model.dto.TokenDto;
import com.stroycut.domain.auth.service.AuthService;
import com.stroycut.global.common.model.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenDto>> refreshToken(@RequestBody TokenDto tokenDto) {
        log.info("토큰 갱신 요청");
        TokenDto newTokenDto = authService.refreshAccessToken(tokenDto.getRefreshToken());
        return ResponseEntity.ok(new BaseResponse<>(newTokenDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@RequestHeader("Authorization") String accessToken) {
        log.info("로그아웃 요청");
        // Bearer 접두사 제거
        accessToken = accessToken.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.ok(new BaseResponse<>(null));
    }

    // 테스트용 엔드포인트
    @GetMapping("/test")
    public ResponseEntity<BaseResponse<String>> testAuth() {
        return ResponseEntity.ok(new BaseResponse<>("인증 테스트 성공!"));
    }
}