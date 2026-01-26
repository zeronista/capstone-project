package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity KnowledgeBase - Cơ sở tri thức để huấn luyện AI Callbot
 */
@Entity
@Table(name = "knowledge_base", indexes = {
    @Index(name = "idx_kb_category", columnList = "category"),
    @Index(name = "idx_kb_approved", columnList = "is_approved"),
    @Index(name = "idx_kb_source", columnList = "source_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 100)
    private String category;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;
    
    @Column(columnDefinition = "TEXT")
    private String context;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30)
    private SourceType sourceType;
    
    @Column(name = "source_id")
    private Long sourceId; // Reference to call_id or ticket_id
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Builder.Default
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Builder.Default
    @Column(name = "is_approved")
    private Boolean isApproved = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    /**
     * Enum nguồn tri thức
     */
    public enum SourceType {
        MANUAL,             // Nhập tay
        CALL_TRANSCRIPT,    // Từ cuộc gọi
        DOCTOR_INPUT        // Bác sĩ cung cấp
    }
}
