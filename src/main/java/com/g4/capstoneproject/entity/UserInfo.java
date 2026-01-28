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
 * Entity UserInfo - Thông tin cá nhân của người dùng
 * Tách riêng khỏi User để phân biệt thông tin bảo mật và thông tin cá nhân.
 * Tất cả các trường trong bảng này đều có thể nullable.
 */
@Entity
@Table(name = "user_info",
    indexes = {
        @Index(name = "idx_user_info_full_name", columnList = "full_name")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    /**
     * Họ và tên đầy đủ
     */
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    /**
     * Ngày sinh
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    /**
     * Giới tính
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;
    
    /**
     * Địa chỉ
     */
    @Column(length = 500)
    private String address;
    
    /**
     * URL ảnh đại diện
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
