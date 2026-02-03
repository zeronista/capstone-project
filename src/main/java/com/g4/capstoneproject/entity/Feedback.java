package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity Feedback - Phản hồi từ người dùng (đánh giá cuộc gọi, dịch vụ)
 */
@Entity
@Table(name = "feedbacks", indexes = {
    @Index(name = "idx_feedback_user", columnList = "user_id"),
    @Index(name = "idx_feedback_type", columnList = "feedback_type"),
    @Index(name = "idx_feedback_rating", columnList = "rating")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "form_url", nullable = false, length = 500)
    private String formUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id")
    private CallLog callLog;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Integer rating; // 1-5
    
    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 30)
    private FeedbackType feedbackType;
    
    @Builder.Default
    @Column(name = "is_reviewed")
    private Boolean isReviewed = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Enum loại phản hồi
     */
    public enum FeedbackType {
        CALL_QUALITY,       // Chất lượng cuộc gọi
        SERVICE,            // Dịch vụ
        AI_PERFORMANCE,     // Hiệu suất AI
        GENERAL             // Chung
    }
}
