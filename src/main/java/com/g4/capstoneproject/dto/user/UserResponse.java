package com.g4.capstoneproject.dto.user;

import com.g4.capstoneproject.entity.Gender;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.User.UserRole;
import com.g4.capstoneproject.entity.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho response thông tin người dùng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String phone;
    private UserRole role;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // UserInfo fields
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String avatarUrl;

    /**
     * Static factory method để tạo UserResponse từ User entity
     */
    public static UserResponse from(User user) {
        UserInfo userInfo = user.getUserInfo();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                // UserInfo fields
                .fullName(userInfo != null ? userInfo.getFullName() : null)
                .dateOfBirth(userInfo != null ? userInfo.getDateOfBirth() : null)
                .gender(userInfo != null ? userInfo.getGender() : null)
                .address(userInfo != null ? userInfo.getAddress() : null)
                .avatarUrl(userInfo != null ? userInfo.getAvatarUrl() : null)
                .build();
    }
}
