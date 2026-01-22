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
 * Entity User - Đại diện cho người dùng trong hệ thống
 */
@Entity
@Table(name = "users", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone")
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
    
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(unique = true, length = 20)
    private String phone;
    
    @Column(nullable = false)
    private String password;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.PATIENT;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider = AuthProvider.LOCAL;
    
    @Column(unique = true, length = 100)
    private String providerId; // Google ID, Facebook ID, etc.
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean accountNonLocked = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Enum cho vai trò người dùng
     */
    public enum UserRole {
        PATIENT,    // Bệnh nhân
        DOCTOR,     // Bác sĩ
        NURSE,      // Y tá
        STAFF,      // Nhân viên
        ADMIN       // Quản trị viên
    }
    
    /**
     * Enum cho phương thức đăng ký
     */
    public enum AuthProvider {
        LOCAL,      // Đăng ký thông thường
        GOOGLE,     // Đăng ký qua Google
        FACEBOOK    // Đăng ký qua Facebook (dự phòng)
    }
}
