package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity Notification - Thông báo cho người dùng
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user", columnList = "user_id"),
    @Index(name = "idx_notification_read", columnList = "is_read"),
    @Index(name = "idx_notification_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "reference_id")
    private Long referenceId; // ID của ticket, call, reminder...
    
    @Column(name = "reference_type", length = 30)
    private String referenceType; // "TICKET", "CALL", "REMINDER"...
    
    @Builder.Default
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    /**
     * Enum loại thông báo
     */
    public enum NotificationType {
        TICKET,         // Liên quan đến ticket
        REMINDER,       // Nhắc nhở
        MESSAGE,        // Tin nhắn mới
        SYSTEM,         // Thông báo hệ thống
        CALL            // Liên quan đến cuộc gọi
    }
}
