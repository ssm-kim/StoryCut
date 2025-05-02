package com.stroycut.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 소셜 로그인 관련 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class LoginController {

    /**
     * 로그인 페이지로 접근 시 구글 로그인 경로로 리다이렉트
     */
    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/google";
    }
}