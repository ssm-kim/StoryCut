package com.stroycut.global.common.model.enums;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublicEndpoint {
    SIGNIN("/signin"),
    LOGIN("/login"),
    FAVICON("/favicon.ico"),
    MEMBER_PUBLIC("/api/member/public/**"),
    OAUTH2("/oauth2/**"),
    ERROR("/error"),
    SWAGGER_UI("/swagger-ui/**"),
    API_DOCS("/v3/api-docs/**"),
    SWAGGER_RESOURCES("/swagger-resources/**"),
    WEBJARS("/webjars/**"),
    SWAGGER_HTML("/swagger-ui.html"),
    ACTUATOR_HEALTH("/actuator/health"),
    ACTUATOR_PROMETHEUS("/actuator/prometheus"),
    GRAFANA("/grafana**"),
    ACTUATOR("/actuator/**");

    private final String url;

    public static List<String> getAll() {
        return List.of(values()).stream().map(PublicEndpoint::getUrl).toList();
    }
}
