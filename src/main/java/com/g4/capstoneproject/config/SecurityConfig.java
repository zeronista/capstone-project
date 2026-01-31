package com.g4.capstoneproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Cấu hình Security với Role-based Authorization
 * OAuth2 disabled for Phase 1 demo
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers(
                                                                "/",
                                                                "/auth/**",
                                                                "/api/auth/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/static/**",
                                                                "/stitch_frontend_n/**",
                                                                "/image/**",
                                                                "/error",
                                                                // WebSocket endpoints
                                                                "/ws/**",
                                                                "/app/**",
                                                                "/topic/**",
                                                                // Swagger UI
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                // Stringee API (public for demo)
                                                                "/api/stringee/**")
                                                .permitAll()
                                                // Role-based access control
                                                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                                                .requestMatchers("/receptionist/**").hasRole("RECEPTIONIST")
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/patient/**")
                                                .hasAnyRole("PATIENT", "DOCTOR", "RECEPTIONIST", "ADMIN")
                                                // Web Call - authenticated users only (all roles)
                                                .requestMatchers("/call/**", "/api/web-call/**").authenticated()
                                                // Authenticated access
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/api/auth/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/auth/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                                                .ignoringRequestMatchers(
                                                                // Allow WebSocket connections without CSRF
                                                                "/ws/**",
                                                                "/app/**",
                                                                // Allow Swagger UI
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                // Allow Stringee webhook callbacks
                                                                "/api/stringee/webhook/**",
                                                                // Allow patient document upload (multipart form)
                                                                "/api/patient/documents/**"));

                return http.build();
        }

        /**
         * Bean cho password encoder
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
