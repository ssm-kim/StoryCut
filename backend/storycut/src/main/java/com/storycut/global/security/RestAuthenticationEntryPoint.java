package com.storycut.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 인증되지 않은 사용자의 접근을 처리하는 클래스
 * REST API에 맞게 401 응답 코드를 반환하도록 설정합니다.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // 인증 실패 시 401 Unauthorized 응답 전송
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요한 리소스입니다.");
    }
}