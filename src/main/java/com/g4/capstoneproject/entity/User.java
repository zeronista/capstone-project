package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity User - Đại diện cho người dùng trong hệ thống
 */
@Entity
@Table(name = "users", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone"),
        @UniqueConstraint(columnNames = "google_id")
    },
    indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_phone", columnList = "phone")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(name = "phone", unique = true, length = 20)
    private String phoneNumber;
    
    @Column(name = "password_hash")
    private String password;
    
    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.PATIENT;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Builder.Default
    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;
    
    // ========== Profile Fields ==========
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;
    
    @Column(length = 500)
    private String address;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    // ========== Timestamps ==========
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    /**
     * Enum cho vai trò người dùng
     */
    public enum UserRole {
        PATIENT,        // Bệnh nhân
        RECEPTIONIST,   // Lễ tân
        DOCTOR,         // Bác sĩ
        ADMIN           // Quản trị viên
    }
    
    /**
     * Enum cho giới tính
     */
    public enum Gender {
        MALE,       // Nam
        FEMALE,     // Nữ
        OTHER       // Khác
    }
}
