package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.HealthForecast;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for HealthForecast list view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthForecastResponse {
    private Long id;
    private String patientName;
    private String patientPhone;
    private LocalDate forecastDate;
    private String overallRisk; // LOW, MODERATE, HIGH, VERY_HIGH
    private Double cardiovascularRisk; // % rủi ro tim mạch
    private HealthForecast.ForecastStatus status;
    private boolean hasRiskAlerts;
    private String createdByName;

    /**
     * Convert entity to DTO
     */
    public static HealthForecastResponse fromEntity(HealthForecast forecast) {
        // Extract overall risk
        String overallRisk = "UNKNOWN";
        if (forecast.getRiskScores() != null && forecast.getRiskScores().containsKey("overallRisk")) {
            overallRisk = forecast.getRiskScores().get("overallRisk").toString();
        }

        return HealthForecastResponse.builder()
                .id(forecast.getId())
                .patientName(forecast.getPatient() != null && forecast.getPatient().getUserInfo() != null
                        ? forecast.getPatient().getUserInfo().getFullName()
                        : "N/A")
                .patientPhone(forecast.getPatient() != null
                        ? forecast.getPatient().getPhoneNumber()
                        : "")
                .forecastDate(forecast.getForecastDate())
                .overallRisk(overallRisk)
                .cardiovascularRisk(forecast.getCardiovascularRisk())
                .status(forecast.getStatus())
                .hasRiskAlerts(forecast.hasRiskAlerts())
                .createdByName(forecast.getCreatedBy() != null && forecast.getCreatedBy().getUserInfo() != null
                        ? forecast.getCreatedBy().getUserInfo().getFullName()
                        : "System")
                .build();
    }
}
