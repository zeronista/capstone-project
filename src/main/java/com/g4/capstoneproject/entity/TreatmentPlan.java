package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity TreatmentPlan - Kế hoạch điều trị (được hỗ trợ bởi AI)
 */
@Entity
@Table(name = "treatment_plans", indexes = {
    @Index(name = "idx_plan_patient", columnList = "patient_id"),
    @Index(name = "idx_plan_doctor", columnList = "doctor_id"),
    @Index(name = "idx_plan_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;
    
    @Column(columnDefinition = "TEXT")
    private String diagnosis;
    
    @Column(name = "treatment_goal", columnDefinition = "TEXT")
    private String treatmentGoal;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PlanStatus status = PlanStatus.DRAFT;
    
    @Builder.Default
    @Column(name = "ai_suggested")
    private Boolean aiSuggested = false;
    
    @Column(name = "ai_suggestion_data", columnDefinition = "JSONB")
    private String aiSuggestionData; // Lưu gợi ý từ AI
    
    @OneToMany(mappedBy = "treatmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TreatmentPlanItem> items = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Enum trạng thái kế hoạch
     */
    public enum PlanStatus {
        DRAFT,      // Nháp
        ACTIVE,     // Đang thực hiện
        COMPLETED,  // Hoàn thành
        CANCELLED   // Đã hủy
    }
}
