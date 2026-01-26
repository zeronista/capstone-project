package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity CallCampaign - Chiến dịch gọi điện
 */
@Entity
@Table(name = "call_campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallCampaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "campaign_name", nullable = false, length = 100)
    private String campaignName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 30)
    private CampaignType campaignType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", length = 30)
    private TargetAudience targetAudience;
    
    @Column(name = "script_template", columnDefinition = "TEXT")
    private String scriptTemplate; // Kịch bản cho bot
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_template_id")
    private SurveyTemplate surveyTemplate;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CampaignStatus status = CampaignStatus.DRAFT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Enum loại chiến dịch
     */
    public enum CampaignType {
        FOLLOW_UP,              // Theo dõi sau khám
        SURVEY,                 // Khảo sát
        APPOINTMENT_REMINDER,   // Nhắc lịch hẹn
        HEALTH_CHECK            // Kiểm tra sức khỏe
    }
    
    /**
     * Enum đối tượng mục tiêu
     */
    public enum TargetAudience {
        EXISTING_PATIENTS,  // Bệnh nhân cũ
        NEW_PATIENTS,       // Bệnh nhân mới
        ALL                 // Tất cả
    }
    
    /**
     * Enum trạng thái chiến dịch
     */
    public enum CampaignStatus {
        DRAFT,      // Nháp
        ACTIVE,     // Đang hoạt động
        PAUSED,     // Tạm dừng
        COMPLETED   // Hoàn thành
    }
}
