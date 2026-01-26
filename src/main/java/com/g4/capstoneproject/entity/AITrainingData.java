package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity AITrainingData - Dữ liệu huấn luyện AI từ cuộc gọi và ticket
 */
@Entity
@Table(name = "ai_training_data", indexes = {
    @Index(name = "idx_training_batch", columnList = "training_batch_id"),
    @Index(name = "idx_training_used", columnList = "is_used_for_training")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AITrainingData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id")
    private CallLog callLog;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
    
    @Column(name = "input_text", columnDefinition = "TEXT", nullable = false)
    private String inputText;
    
    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;
    
    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;
    
    @Column(name = "feedback_score")
    private Integer feedbackScore; // 1-5
    
    @Builder.Default
    @Column(name = "is_used_for_training")
    private Boolean isUsedForTraining = false;
    
    @Column(name = "training_batch_id", length = 50)
    private String trainingBatchId;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
