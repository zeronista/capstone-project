package com.g4.capstoneproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request/response hồ sơ sức khỏe bệnh nhân cơ bản.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientHealthProfileRequest {

    private String bloodType;
    private Double heightCm;
    private Double weightKg;
    private String allergies;
    private String chronicDiseases;
}

