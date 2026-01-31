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
 * DTO cho response profile (flattened User + UserInfo)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    // User fields
    private Long id;
    private String email;
    private String phone;
    private User.UserRole role;
    private Boolean isActive;
    private Boolean isVerified;
    private String googleId;
    private LocalDateTime createdAt;

    // UserInfo fields
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String avatar;

    // Calculated fields
    private Integer profileCompletion; // 0-100%
    private Boolean canChangePassword; // false if Google OAuth user
}
