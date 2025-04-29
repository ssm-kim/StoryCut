package com.stroycut.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stroycut.domain.auth.model.dto.TokenDto;
import com.stroycut.domain.auth.service.AuthService;
import com.stroycut.domain.auth.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(email);
        String refreshToken = jwtUtil.createRefreshToken(email);
        
        // 리프레시 토큰 저장
        authService.saveRefreshToken(email, refreshToken);
        
        // 모바일 앱으로 리다이렉트 또는 토큰 응답
        // 앱에서는 URL 스키마 또는 딥링크를 사용할 수 있습니다.
        String targetUrl = UriComponentsBuilder.fromUriString("stroycut://oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
                
        // 또는 JSON 응답으로 처리할 수도 있습니다.
        TokenDto tokenDto = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(tokenDto));
        
        log.info("OAuth2 로그인 성공 - 사용자 이메일: {}", email);
    }
}
