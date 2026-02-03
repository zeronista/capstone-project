package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.TreatmentPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for TreatmentPlan list view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlanResponse {
    private Long id;
    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String diagnosis;
    private String treatmentGoal;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private TreatmentPlan.PlanStatus status;
    private Boolean aiSuggested;
    private int itemCount; // Số lượng items trong plan
    private int checkupCount; // Số lượng lịch tái khám

    /**
     * Convert entity to DTO
     */
    public static TreatmentPlanResponse fromEntity(TreatmentPlan plan) {
        return TreatmentPlanResponse.builder()
                .id(plan.getId())
                .patientName(plan.getPatient() != null && plan.getPatient().getUserInfo() != null
                        ? plan.getPatient().getUserInfo().getFullName()
                        : "N/A")
                .patientPhone(plan.getPatient() != null
                        ? plan.getPatient().getPhoneNumber()
                        : "")
                .doctorName(plan.getDoctor() != null && plan.getDoctor().getUserInfo() != null
                        ? plan.getDoctor().getUserInfo().getFullName()
                        : "N/A")
                .diagnosis(plan.getDiagnosis())
                .treatmentGoal(plan.getTreatmentGoal())
                .startDate(plan.getStartDate())
                .expectedEndDate(plan.getExpectedEndDate())
                .status(plan.getStatus())
                .aiSuggested(plan.getAiSuggested())
                .itemCount(plan.getItems() != null ? plan.getItems().size() : 0)
                .checkupCount(0) // CheckupSchedule entity removed in schema v4.0
                .build();
    }
}
