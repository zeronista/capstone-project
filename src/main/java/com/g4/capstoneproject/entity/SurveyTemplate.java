package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity SurveyTemplate - Mẫu khảo sát/kịch bản cho AI Callbot
 */
@Entity
@Table(name = "survey_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "questions_json", columnDefinition = "JSON")
    private String questionsJson; // JSON chứa danh sách câu hỏi
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
