package com.g4.capstoneproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cấu hình Swagger/OpenAPI Documentation
 * Truy cập Swagger UI tại: http://localhost:8080/swagger-ui/index.html
 * Truy cập OpenAPI docs tại: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:Capstone Project}")
    private String applicationName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " - API Documentation")
                        .description("API Documentation cho hệ thống Capstone Project - Quản lý y tế và CRM")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("G4 Team")
                                .email("support@g4team.com")
                                .url("https://github.com/zeronista/capstone-project"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.yourdomain.com")
                                .description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token authentication"))
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("OAuth2 authentication")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"));
    }
}
