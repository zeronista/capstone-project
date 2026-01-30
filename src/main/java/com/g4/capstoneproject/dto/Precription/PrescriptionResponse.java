package com.g4.capstoneproject.dto.Precription;

import com.g4.capstoneproject.entity.Prescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Prescription List Response
 * Lightweight response for listing prescriptions in table view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionResponse {

        private Long id;
        private LocalDate prescriptionDate;
        private String diagnosis;
        private String notes;
        private Prescription.PrescriptionStatus status;

        // Patient info
        private Long patientId;
        private String patientName;
        private String patientPhone;

        // Doctor info
        private Long doctorId;
        private String doctorName;

        // Timestamps
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Summary info
        private Integer medicationCount; // Number of medications in this prescription

        /**
         * Convert Prescription entity to PrescriptionResponse DTO
         */
        public static PrescriptionResponse fromEntity(Prescription prescription) {
                return PrescriptionResponse.builder()
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
                                .patientPhone(prescription.getPatient() != null
                                                ? prescription.getPatient().getPhoneNumber()
                                                : null)
                                .doctorId(prescription.getDoctor() != null ? prescription.getDoctor().getId() : null)
                                .doctorName(prescription.getDoctor() != null
                                                && prescription.getDoctor().getUserInfo() != null
                                                                ? prescription.getDoctor().getUserInfo().getFullName()
                                                                : null)
                                .createdAt(prescription.getCreatedAt())
                                .updatedAt(prescription.getUpdatedAt())
                                .medicationCount(prescription.getDetails() != null ? prescription.getDetails().size()
                                                : 0)
                                .build();
        }
}
