package com.stroycut.global.common.config;

import com.stroycut.domain.auth.filter.JwtAuthenticationFilter;
import com.stroycut.domain.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.stroycut.domain.auth.service.CustomOAuth2UserService;
import com.stroycut.global.common.filter.LoggingFilter;
import com.stroycut.global.common.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoggingFilter loggingFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(restAuthenticationEntryPoint())
            )
            .authorizeHttpRequests((auth) -> auth
                // 공개 엔드포인트 그룹 - 인증 없이 접근 가능한 모든 경로
                .requestMatchers(
                    // API 공개 엔드포인트
                    "/api/auth/**",

                    // OAuth2 소셜 로그인 관련 경로
                    "/login", "/oauth2/authorization/**", "/login/oauth2/code/**",

                    // Swagger UI 관련 경로
                    "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",

                    // 정적 리소스
                    "/", "/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico",

                    // 헬스 체크 및 모니터링
                    "/actuator/**", "/health"
                ).permitAll()

                // 보호된 API 엔드포인트 - 인증 필요
                .requestMatchers("/api/**").authenticated()

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(loggingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}