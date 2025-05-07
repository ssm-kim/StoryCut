package com.storycut.global.model.enums;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublicEndpoint {
    // 인증 관련 경로
    SIGNIN("/signin"),
    LOGIN("/login"),
    WEB_LOGIN("/web/login"),    // 웹 로그인 경로 추가
    API_AUTH("/api/auth/**"),  // 모든 인증 API 경로 추가

    // OAuth2 관련 경로
    OAUTH2("/oauth2/**"),
    OAUTH2_AUTHORIZATION("/oauth2/authorization/**"),
    LOGIN_OAUTH2_CODE("/login/oauth2/code/**"),

    // 멤버 공개 API
    MEMBER_PUBLIC("/api/member/public/**"),

    // 정적 리소스
    ROOT("/"),
    STATIC("/static/**"),
    CSS("/css/**"),
    JS("/js/**"),
    IMAGES("/images/**"),
    FAVICON("/favicon.ico"),

    // 오류 페이지
    ERROR("/error"),

    // Swagger/API 문서
    SWAGGER_UI("/swagger-ui/**"),
    API_DOCS("/v3/api-docs/**"),
    SWAGGER_RESOURCES("/swagger-resources/**"),
    WEBJARS("/webjars/**"),
    SWAGGER_HTML("/swagger-ui.html"),

    // 모니터링 및 헬스 체크
    ACTUATOR_HEALTH("/actuator/health"),
    ACTUATOR_PROMETHEUS("/actuator/prometheus"),
    GRAFANA("/grafana**"),
    ACTUATOR("/actuator/**"),
    HEALTH("/health");

    private final String url;

    public static List<String> getAll() {
        return List.of(values()).stream().map(PublicEndpoint::getUrl).toList();
    }
}
