package com.g4.capstoneproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình Security - Tạm thời TẮT để test
 * TODO: BẬT LẠI khi deploy production!
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Cho phép tất cả requests - KHÔNG AN TOÀN!
            )
            .csrf(csrf -> csrf.disable()) // Tắt CSRF
            .oauth2Login(oauth2 -> oauth2.disable()); // Tắt OAuth2 Login

        return http.build();
    }
}
