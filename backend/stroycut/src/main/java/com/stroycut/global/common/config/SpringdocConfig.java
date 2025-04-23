package com.stroycut.global.common.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocConfig {

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        Server httpsServer = new Server();
        httpsServer.setUrl(baseUrl);
        httpsServer.setDescription("HTTPS Server");

        return new OpenAPI()
            .servers(List.of(httpsServer))
            .components(new Components())
            .info(new Info()
                .title("Storycut API")
                .version("1.0")
                .description("Storycut 서비스 API 명세서"))
            .components(new Components()
                .addSecuritySchemes("JWT", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .in(SecurityScheme.In.HEADER)
                    .name("Authorization")
                    .description("JWT Access 토큰을 입력하세요. 예: eyJhbGciOiJ...")
                )
            );
    }

//    @Bean
//    public OpenApiCustomizer customOpenApi() {
//        return openApi -> openApi.getComponents().getSchemas().keySet()
//            .removeIf(name -> name.startsWith("BaseResponse"));
//    }

    // OpenApiCustomiser를 이용해, "/api/member/public" 경로를 제외한 엔드포인트에 자동으로 보안 요구사항 추가
    @Bean
    public OpenApiCustomizer securityOpenApiCustomiser() {
        return openApi -> {
            openApi.getPaths().forEach((path, pathItem) -> {
                // public 경로가 포함된 경우는 건너뜁니다.
                if (!path.matches("^/api/[^/]+/public.*")) {
                    pathItem.readOperations().forEach(operation -> {
                        operation.addSecurityItem(new SecurityRequirement().addList("JWT"));
                    });
                }
            });
        };
    }
}
