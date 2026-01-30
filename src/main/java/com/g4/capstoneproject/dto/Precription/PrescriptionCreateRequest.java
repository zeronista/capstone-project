package com.g4.capstoneproject.dto.Precription;

import com.g4.capstoneproject.entity.Prescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating/updating Prescription
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionCreateRequest {

    @NotNull(message = "ID bệnh nhân không được để trống")
    @Positive(message = "ID bệnh nhân phải là số dương")
    private Long patientId;

    @NotNull(message = "ID bác sĩ không được để trống")
    @Positive(message = "ID bác sĩ phải là số dương")
    private Long doctorId;

    @NotNull(message = "Ngày kê đơn không được để trống")
    private LocalDate prescriptionDate;

    @NotBlank(message = "Chẩn đoán không được để trống")
    @Size(min = 5, max = 1000, message = "Chẩn đoán phải từ 5 đến 1000 ký tự")
    private String diagnosis;

    @Size(max = 2000, message = "Ghi chú không được quá 2000 ký tự")
    private String notes;

    @Builder.Default
    private Prescription.PrescriptionStatus status = Prescription.PrescriptionStatus.ACTIVE;

    @NotNull(message = "Danh sách thuốc không được để trống")
    @Size(min = 1, message = "Phải có ít nhất một loại thuốc")
    @Valid
    private List<MedicationItemDTO> medications;

    /**
     * Inner DTO for Medication Item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicationItemDTO {

        @NotBlank(message = "Tên thuốc không được để trống")
        @Size(min = 2, max = 200, message = "Tên thuốc phải từ 2 đến 200 ký tự")
        private String medicineName;

        @NotBlank(message = "Liều lượng không được để trống")
        @Size(max = 100, message = "Liều lượng không được quá 100 ký tự")
        private String dosage;

        @NotBlank(message = "Tần suất không được để trống")
        @Size(max = 100, message = "Tần suất không được quá 100 ký tự")
        private String frequency;

        @NotBlank(message = "Thời gian không được để trống")
        @Size(max = 100, message = "Thời gian không được quá 100 ký tự")
        private String duration;

        @Size(max = 500, message = "Hướng dẫn không được quá 500 ký tự")
        private String instructions;

        @Positive(message = "Số lượng phải là số dương")
        private Integer quantity;
    }
}
