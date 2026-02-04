package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.TreatmentPlan;
import com.g4.capstoneproject.entity.TreatmentPlanItem;
import com.g4.capstoneproject.entity.CheckupSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for TreatmentPlan detail view with items and checkup schedules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlanDetailResponse {
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
    private String aiSuggestionData;
    private List<TreatmentItemDTO> items;
    private List<CheckupScheduleDTO> checkups;

    /**
     * Nested DTO for treatment plan items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreatmentItemDTO {
        private Long id;
        private String treatmentType;
        private String description;
        private String medication;
        private String dosage;
        private String frequency;
        private String duration;
        private String instructions;
        private TreatmentPlanItem.ItemStatus status;

        public static TreatmentItemDTO fromEntity(TreatmentPlanItem item) {
            return TreatmentItemDTO.builder()
                    .id(item.getId())
                    .treatmentType(item.getItemType() != null ? item.getItemType().toString() : "")
                    .description(item.getDescription())
                    .medication("") // TreatmentPlanItem doesn't have medication field
                    .dosage("") // TreatmentPlanItem doesn't have dosage field
                    .frequency(item.getFrequency())
                    .duration(item.getDuration())
                    .instructions(item.getNotes())
                    .status(item.getStatus())
                    .build();
        }
    }

    /**
     * Nested DTO for checkup schedules
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckupScheduleDTO {
        private Long id;
        private LocalDate scheduledDate;
        private String checkupType;
        private CheckupSchedule.CheckupStatus status;
        private String notes;
        private LocalDate completedDate;
        private String resultSummary;

        public static CheckupScheduleDTO fromEntity(CheckupSchedule checkup) {
            return CheckupScheduleDTO.builder()
                    .id(checkup.getId())
                    .scheduledDate(checkup.getScheduledDate())
                    .checkupType(checkup.getCheckupType())
                    .status(checkup.getStatus())
                    .notes(checkup.getNotes())
                    .completedDate(checkup.getCompletedDate())
                    .resultSummary(checkup.getResultSummary())
                    .build();
        }
    }

    /**
     * Convert entity to DTO
     */
    public static TreatmentPlanDetailResponse fromEntity(TreatmentPlan plan, List<CheckupSchedule> checkups) {
        return TreatmentPlanDetailResponse.builder()
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
                .aiSuggestionData(plan.getAiSuggestionData())
                .items(plan.getItems() != null
                        ? plan.getItems().stream().map(TreatmentItemDTO::fromEntity).collect(Collectors.toList())
                        : List.of())
                .checkups(checkups != null
                        ? checkups.stream().map(CheckupScheduleDTO::fromEntity).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
