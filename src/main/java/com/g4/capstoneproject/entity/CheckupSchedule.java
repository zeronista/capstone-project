package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity CheckupSchedule - Lịch tái khám định kỳ trong kế hoạch điều trị
 */
@Entity
@Table(name = "checkup_schedules", indexes = {
        @Index(name = "idx_checkup_treatment_plan", columnList = "treatment_plan_id"),
        @Index(name = "idx_checkup_patient", columnList = "patient_id"),
        @Index(name = "idx_checkup_doctor", columnList = "doctor_id"),
        @Index(name = "idx_checkup_status", columnList = "status"),
        @Index(name = "idx_checkup_date", columnList = "scheduled_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckupSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_plan_id", nullable = false)
    private TreatmentPlan treatmentPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "checkup_type", length = 50)
    private String checkupType; // Loại tái khám: "routine" (định kỳ), "follow_up" (theo dõi), "emergency" (khẩn
                                // cấp)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CheckupStatus status = CheckupStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String notes; // Ghi chú trước khi tái khám

    @Column(name = "completed_date")
    private LocalDate completedDate; // Ngày hoàn thành khám thực tế

    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary; // Tóm tắt kết quả sau khi khám

    @Column(name = "next_checkup_suggestion")
    private LocalDate nextCheckupSuggestion; // Gợi ý ngày tái khám tiếp theo

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum trạng thái lịch tái khám
     */
    public enum CheckupStatus {
        SCHEDULED, // Đã lên lịch
        CONFIRMED, // Đã xác nhận
        COMPLETED, // Đã hoàn thành
        CANCELLED, // Đã hủy
        NO_SHOW // Bệnh nhân không đến
    }
}
