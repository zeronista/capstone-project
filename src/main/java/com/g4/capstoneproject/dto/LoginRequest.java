package com.g4.capstoneproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu đăng nhập
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String username; // Có thể là email hoặc số điện thoại
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    
    @Builder.Default
    private Boolean rememberMe = false;
}
