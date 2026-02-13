package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.TreatmentPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho TreatmentPlan
 * Chức năng: Truyền dữ liệu kế hoạch điều trị mà không gây lazy loading exception
 * Cách hoạt động: Chỉ chứa các trường cần thiết, không có quan hệ entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlanDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String diagnosis;
    private String treatmentGoals;
    private String treatmentSteps;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ Entity sang DTO
     * Chức năng: Tạo DTO từ TreatmentPlan entity
     * Cách hoạt động: Lấy các trường cần thiết từ entity, tránh lazy loading
     */
    public static TreatmentPlanDTO fromEntity(TreatmentPlan plan) {
        return TreatmentPlanDTO.builder()
                .id(plan.getId())
                .patientId(plan.getPatient() != null ? plan.getPatient().getId() : null)
                .patientName(plan.getPatient() != null && plan.getPatient().getUserInfo() != null 
                    ? plan.getPatient().getUserInfo().getFullName() : null)
                .doctorId(plan.getDoctor() != null ? plan.getDoctor().getId() : null)
                .doctorName(plan.getDoctor() != null && plan.getDoctor().getUserInfo() != null 
                    ? plan.getDoctor().getUserInfo().getFullName() : null)
                .diagnosis(plan.getDiagnosis())
                .treatmentGoals(plan.getTreatmentGoal())
                .treatmentSteps(null) // TreatmentPlan không có field này, có thể lấy từ items
                .startDate(plan.getStartDate())
                .endDate(plan.getExpectedEndDate())
                .status(plan.getStatus() != null ? plan.getStatus().name() : null)
                .notes(null) // TreatmentPlan không có field notes
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
