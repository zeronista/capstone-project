package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.HealthForecast;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for HealthForecast detail view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthForecastDetailResponse {
    private Long id;
    private String patientName;
    private String patientPhone;
    private Integer patientAge;
    private String patientGender;
    private LocalDate forecastDate;
    private Map<String, Object> riskScores;
    private Map<String, Object> predictions;
    private Map<String, Object> riskFactors;
    private Map<String, Object> vitalSignsSnapshot;
    private String recommendations;
    private HealthForecast.ForecastStatus status;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;

    /**
     * Convert entity to DTO
     */
    public static HealthForecastDetailResponse fromEntity(HealthForecast forecast) {
        return HealthForecastDetailResponse.builder()
                .id(forecast.getId())
                .patientName(forecast.getPatient() != null && forecast.getPatient().getUserInfo() != null
                        ? forecast.getPatient().getUserInfo().getFullName()
                        : "N/A")
                .patientPhone(forecast.getPatient() != null
                        ? forecast.getPatient().getPhoneNumber()
                        : "")
                .patientAge(forecast.getRiskFactors() != null && forecast.getRiskFactors().containsKey("age")
                        ? (Integer) forecast.getRiskFactors().get("age")
                        : null)
                .patientGender(forecast.getRiskFactors() != null && forecast.getRiskFactors().containsKey("gender")
                        ? forecast.getRiskFactors().get("gender").toString()
                        : null)
                .forecastDate(forecast.getForecastDate())
                .riskScores(forecast.getRiskScores())
                .predictions(forecast.getPredictions())
                .riskFactors(forecast.getRiskFactors())
                .vitalSignsSnapshot(forecast.getVitalSignsSnapshot())
                .recommendations(forecast.getRecommendations())
                .status(forecast.getStatus())
                .createdByName(forecast.getCreatedBy() != null && forecast.getCreatedBy().getUserInfo() != null
                        ? forecast.getCreatedBy().getUserInfo().getFullName()
                        : "System")
                .createdAt(forecast.getCreatedAt())
                .updatedAt(forecast.getUpdatedAt())
                .notes(forecast.getNotes())
                .build();
    }
}
