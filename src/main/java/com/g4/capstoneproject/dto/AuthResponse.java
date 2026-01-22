package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private User.UserRole role;
    private String message;
    private Boolean success;
}
