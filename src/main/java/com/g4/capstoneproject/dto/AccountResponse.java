package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.Gender;
import com.g4.capstoneproject.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho response account information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private User.UserRole role;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    
    /**
     * Chuyển đổi từ User entity sang AccountResponse
     * Thông tin cá nhân được lấy từ UserInfo thông qua helper methods trong User
     */
    public static AccountResponse fromUser(User user) {
        return AccountResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())  // Sử dụng helper method
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .dateOfBirth(user.getDateOfBirth())  // Sử dụng helper method
                .gender(user.getGender())  // Sử dụng helper method
                .address(user.getAddress())  // Sử dụng helper method
                .avatarUrl(user.getAvatarUrl())  // Sử dụng helper method
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
