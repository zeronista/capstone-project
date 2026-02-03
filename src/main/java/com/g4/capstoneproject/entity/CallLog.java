package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity CallLog - Lịch sử cuộc gọi
 */
@Entity
@Table(name = "call_logs", indexes = {
    @Index(name = "idx_call_patient", columnList = "patient_id"),
    @Index(name = "idx_call_start_time", columnList = "start_time"),
    @Index(name = "idx_call_status", columnList = "call_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private User patient;
    
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false, length = 20)
    private CallType callType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "call_status", nullable = false, length = 20)
    private CallStatus callStatus;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration")
    private Integer duration; // Thời lượng (giây)
    
    @Column(name = "recording_url", length = 500)
    private String recordingUrl;
    
    @Column(name = "transcript_text", columnDefinition = "TEXT")
    private String transcriptText; // Nội dung cuộc gọi dạng text
    
    @Column(name = "ai_confidence_score")
    private Double aiConfidenceScore;
    
    @Builder.Default
    @Column(name = "is_escalated")
    private Boolean isEscalated = false; // Đã chuyển cho người thật
    
    @Column(name = "escalation_reason", length = 255)
    private String escalationReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy; // Lễ tân/Bác sĩ tiếp nhận
    
    @Column(name = "survey_responses", columnDefinition = "JSONB")
    private String surveyResponses; // Lưu câu trả lời khảo sát dạng JSON
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Enum loại cuộc gọi
     */
    public enum CallType {
        AI_BOT,         // Bot gọi tự động
        HUMAN_TAKEOVER, // Chuyển cho người
        MANUAL          // Gọi thủ công
    }
    
    /**
     * Enum trạng thái cuộc gọi
     */
    public enum CallStatus {
        PENDING,        // Đang chờ
        IN_PROGRESS,    // Đang diễn ra
        COMPLETED,      // Hoàn thành
        FAILED,         // Thất bại
        NO_ANSWER,      // Không trả lời
        TRANSFERRED     // Đã chuyển tiếp
    }
}
