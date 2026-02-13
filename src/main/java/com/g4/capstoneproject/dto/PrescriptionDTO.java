package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.Prescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho Prescription
 * Chức năng: Truyền dữ liệu đơn thuốc mà không gây lazy loading exception
 * Cách hoạt động: Chỉ chứa các trường cần thiết, không có quan hệ entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDate prescriptionDate;
    private String diagnosis;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ Entity sang DTO
     * Chức năng: Tạo DTO từ Prescription entity
     * Cách hoạt động: Lấy các trường cần thiết từ entity, tránh lazy loading
     */
    public static PrescriptionDTO fromEntity(Prescription prescription) {
        return PrescriptionDTO.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatient() != null ? prescription.getPatient().getId() : null)
                .patientName(prescription.getPatient() != null && prescription.getPatient().getUserInfo() != null 
                    ? prescription.getPatient().getUserInfo().getFullName() : null)
                .doctorId(prescription.getDoctor() != null ? prescription.getDoctor().getId() : null)
                .doctorName(prescription.getDoctor() != null && prescription.getDoctor().getUserInfo() != null 
                    ? prescription.getDoctor().getUserInfo().getFullName() : null)
                .prescriptionDate(prescription.getPrescriptionDate())
                .diagnosis(prescription.getDiagnosis())
                .notes(prescription.getNotes())
                .createdAt(prescription.getCreatedAt())
                .updatedAt(prescription.getUpdatedAt())
                .build();
    }
}
