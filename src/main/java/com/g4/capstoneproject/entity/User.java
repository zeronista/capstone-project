package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity User - Thông tin tài khoản và bảo mật của người dùng
 * Chỉ chứa các thông tin liên quan đến đăng nhập và phân quyền.
 * Thông tin cá nhân được lưu trong bảng user_info.
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
    
    // ========== Thông tin đăng nhập ==========
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(name = "phone", unique = true, length = 20)
    private String phoneNumber;
    
    @Column(name = "password_hash")
    private String password;
    
    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;
    
    // ========== Phân quyền ==========
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.PATIENT;
    
    // ========== Trạng thái tài khoản ==========
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Builder.Default
    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;
    
    // ========== Quan hệ với UserInfo ==========
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserInfo userInfo;
    
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
    
    // ========== Helper methods để truy cập thông tin cá nhân ==========
    
    /**
     * Lấy họ tên từ UserInfo (để tương thích ngược)
     */
    public String getFullName() {
        return userInfo != null ? userInfo.getFullName() : null;
    }
    
    /**
     * Lấy ngày sinh từ UserInfo (để tương thích ngược)
     */
    public java.time.LocalDate getDateOfBirth() {
        return userInfo != null ? userInfo.getDateOfBirth() : null;
    }
    
    /**
     * Lấy giới tính từ UserInfo (để tương thích ngược)
     */
    public UserInfo.Gender getGender() {
        return userInfo != null ? userInfo.getGender() : null;
    }
    
    /**
     * Lấy địa chỉ từ UserInfo (để tương thích ngược)
     */
    public String getAddress() {
        return userInfo != null ? userInfo.getAddress() : null;
    }
    
    /**
     * Lấy URL avatar từ UserInfo (để tương thích ngược)
     */
    public String getAvatarUrl() {
        return userInfo != null ? userInfo.getAvatarUrl() : null;
    }
}
