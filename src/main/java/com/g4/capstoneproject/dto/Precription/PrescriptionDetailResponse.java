package com.g4.capstoneproject.dto.Precription;

import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.PrescriptionDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Prescription Detail Response
 * Full response including medication details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDetailResponse {

        private Long id;
        private LocalDate prescriptionDate;
        private String diagnosis;
        private String notes;
        private Prescription.PrescriptionStatus status;

        // Patient info
        private Long patientId;
        private String patientName;
        private String patientEmail;
        private String patientPhone;
        private String patientAvatar;

        // Doctor info
        private Long doctorId;
        private String doctorName;

        // Timestamps
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Medication details
        private List<MedicationDetailDTO> medications;

        /**
         * Inner DTO for Medication Detail
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MedicationDetailDTO {
                private Long id;
                private String medicineName;
                private String dosage;
                private String frequency;
                private String duration;
                private String instructions;
                private Integer quantity;

                public static MedicationDetailDTO fromEntity(PrescriptionDetail detail) {
                        return MedicationDetailDTO.builder()
                                        .id(detail.getId())
                                        .medicineName(detail.getMedicineName())
                                        .dosage(detail.getDosage())
                                        .frequency(detail.getFrequency())
                                        .duration(detail.getDuration())
                                        .instructions(detail.getInstructions())
                                        .quantity(detail.getQuantity())
                                        .build();
                }
        }

        /**
         * Convert Prescription entity to PrescriptionDetailResponse DTO
         */
        public static PrescriptionDetailResponse fromEntity(Prescription prescription) {
                return PrescriptionDetailResponse.builder()
                                .id(prescription.getId())
                                .prescriptionDate(prescription.getPrescriptionDate())
                                .diagnosis(prescription.getDiagnosis())
                                .notes(prescription.getNotes())
                                .status(prescription.getStatus())
                                .patientId(prescription.getPatient() != null ? prescription.getPatient().getId() : null)
                                .patientName(prescription.getPatient() != null
                                                && prescription.getPatient().getUserInfo() != null
                                                                ? prescription.getPatient().getUserInfo().getFullName()
                                                                : null)
                                .patientEmail(prescription.getPatient() != null ? prescription.getPatient().getEmail()
                                                : null)
                                .patientPhone(prescription.getPatient() != null
                                                ? prescription.getPatient().getPhoneNumber()
                                                : null)
                                .patientAvatar(prescription.getPatient() != null
                                                && prescription.getPatient().getUserInfo() != null
                                                                ? prescription.getPatient().getUserInfo().getAvatarUrl()
                                                                : null)
                                .doctorId(prescription.getDoctor() != null ? prescription.getDoctor().getId() : null)
                                .doctorName(prescription.getDoctor() != null
                                                && prescription.getDoctor().getUserInfo() != null
                                                                ? prescription.getDoctor().getUserInfo().getFullName()
                                                                : null)
                                .createdAt(prescription.getCreatedAt())
                                .updatedAt(prescription.getUpdatedAt())
                                .medications(prescription.getDetails() != null ? prescription.getDetails().stream()
                                                .map(MedicationDetailDTO::fromEntity)
                                                .collect(Collectors.toList()) : null)
                                .build();
        }
}
