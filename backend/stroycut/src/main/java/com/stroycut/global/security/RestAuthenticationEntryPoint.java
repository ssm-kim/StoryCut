package com.stroycut.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stroycut.global.model.dto.BaseResponse;
import com.stroycut.global.model.dto.BaseResponseStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * REST API에 적합한 인증 실패 처리를 위한 컴포넌트
 * 인증이 필요한 리소스에 인증 없이 접근할 경우 401 Unauthorized 응답을 반환
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) 
            throws IOException, ServletException {
        
        BaseResponse<?> errorResponse = new BaseResponse<>(BaseResponseStatus.UNAUTHORIZED);
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}