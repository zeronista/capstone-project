package com.g4.capstoneproject.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Prescription Creation Request
 * Contains validation annotations for server-side validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionRequest {

    @NotNull(message = "ID bệnh nhân không được để trống")
    @Positive(message = "ID bệnh nhân phải là số dương")
    private Long patientId;

    @NotNull(message = "ID bác sĩ không được để trống")
    @Positive(message = "ID bác sĩ phải là số dương")
    private Long doctorId;

    @NotBlank(message = "Chẩn đoán không được để trống")
    @Size(min = 5, max = 500, message = "Chẩn đoán phải từ 5 đến 500 ký tự")
    private String diagnosis;

    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String notes;

    @NotNull(message = "Danh sách thuốc không được để trống")
    @Size(min = 1, message = "Phải có ít nhất một loại thuốc")
    private List<@Valid MedicationItem> medications;

    private Boolean requireRevisit;

    @Future(message = "Ngày tái khám phải là ngày trong tương lai")
    private LocalDate revisitDate;

    /**
     * Inner DTO for Medication Item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicationItem {

        @NotBlank(message = "Tên thuốc không được để trống")
        @Size(min = 2, max = 200, message = "Tên thuốc phải từ 2 đến 200 ký tự")
        private String name;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        @Max(value = 1000, message = "Số lượng không được quá 1000")
        private Integer quantity;

        @NotBlank(message = "Liều lượng không được để trống")
        @Size(min = 2, max = 100, message = "Liều lượng phải từ 2 đến 100 ký tự")
        private String dosage;

        @Size(max = 500, message = "Hướng dẫn sử dụng không được quá 500 ký tự")
        private String instructions;
    }

    /**
     * Custom validation: If requireRevisit is true, revisitDate must be provided
     */
    @AssertTrue(message = "Ngày tái khám là bắt buộc khi yêu cầu tái khám")
    public boolean isRevisitDateValid() {
        if (requireRevisit != null && requireRevisit) {
            return revisitDate != null;
        }
        return true;
    }
}
