package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.HealthForecast;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.VitalSigns;
import com.g4.capstoneproject.repository.HealthForecastRepository;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.repository.VitalSignsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

/**
 * Service for Health Forecast Management
 * Handles business logic for health risk assessment and forecasting
 */
@Service
@RequiredArgsConstructor
public class HealthForecastService {

    private final HealthForecastRepository healthForecastRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final UserRepository userRepository;

    // ========== CRUD Operations ==========

    /**
     * Get all forecasts for a patient
     */
    public List<HealthForecast> getForecastsByPatientId(Long patientId) {
        return healthForecastRepository.findByPatientIdOrderByForecastDateDesc(patientId);
    }

    /**
     * Get forecast by ID
     */
    public HealthForecast getForecastById(Long id) {
        return healthForecastRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forecast not found with ID: " + id));
    }

    /**
     * Get latest active forecast for a patient
     */
    public Optional<HealthForecast> getLatestActiveForecast(Long patientId) {
        return healthForecastRepository.findLatestActiveByPatientId(patientId);
    }

    /**
     * Get recent forecasts (last N records)
     */
    public List<HealthForecast> getRecentForecasts(Long patientId, int limit) {
        return healthForecastRepository.findRecentByPatientId(patientId, limit);
    }

    /**
     * Get forecasts within date range
     */
    public List<HealthForecast> getForecastsByDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        return healthForecastRepository.findByPatientIdAndDateRange(patientId, startDate, endDate);
    }

    // ========== Risk Calculation ==========

    /**
     * Calculate Framingham Risk Score for cardiovascular disease (10-year risk)
     * Based on: age, gender, cholesterol, HDL, blood pressure, smoking, diabetes
     */
    public double calculateFraminghamScore(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Get latest vital signs
        Optional<VitalSigns> latestVitals = vitalSignsRepository.findLatestByPatientId(patientId);
        if (latestVitals.isEmpty()) {
            throw new RuntimeException("No vital signs found for patient");
        }

        VitalSigns vitals = latestVitals.get();

        // Get patient age
        int age = calculateAge(patient.getDateOfBirth());
        if (age < 30 || age > 74) {
            throw new RuntimeException("Framingham score is only valid for ages 30-74");
        }

        // Extract vital signs (using mock values for cholesterol/HDL if not available)
        int systolic = vitals.getSystolicPressure() != null ? vitals.getSystolicPressure() : 120;
        double totalCholesterol = 200.0; // Mock: should come from lab results
        double hdl = 50.0; // Mock: should come from lab results
        boolean smoking = false; // Mock: should come from patient history
        boolean diabetes = false; // Mock: should come from medical conditions

        // Calculate score based on gender
        double score = 0.0;
        if ("M".equals(patient.getGender()) || "MALE".equals(patient.getGender())) {
            score = calculateFraminghamMale(age, totalCholesterol, hdl, systolic, smoking, diabetes);
        } else {
            score = calculateFraminghamFemale(age, totalCholesterol, hdl, systolic, smoking, diabetes);
        }

        return Math.min(score, 99.9); // Cap at 99.9%
    }

    /**
     * Framingham score for males
     */
    private double calculateFraminghamMale(int age, double totalChol, double hdl, int systolic,
            boolean smoking, boolean diabetes) {
        double points = 0.0;

        // Age points
        if (age >= 30 && age <= 34)
            points += -1;
        else if (age >= 35 && age <= 39)
            points += 0;
        else if (age >= 40 && age <= 44)
            points += 1;
        else if (age >= 45 && age <= 49)
            points += 2;
        else if (age >= 50 && age <= 54)
            points += 3;
        else if (age >= 55 && age <= 59)
            points += 4;
        else if (age >= 60 && age <= 64)
            points += 5;
        else if (age >= 65 && age <= 69)
            points += 6;
        else if (age >= 70 && age <= 74)
            points += 7;

        // Total cholesterol points
        if (totalChol < 160)
            points += 0;
        else if (totalChol < 200)
            points += 1;
        else if (totalChol < 240)
            points += 2;
        else if (totalChol < 280)
            points += 3;
        else
            points += 4;

        // HDL cholesterol points
        if (hdl >= 60)
            points += -2;
        else if (hdl >= 50)
            points += -1;
        else if (hdl >= 45)
            points += 0;
        else if (hdl >= 35)
            points += 1;
        else
            points += 2;

        // Systolic BP points (untreated)
        if (systolic < 120)
            points += 0;
        else if (systolic < 130)
            points += 1;
        else if (systolic < 140)
            points += 2;
        else if (systolic < 160)
            points += 3;
        else
            points += 4;

        // Smoking
        if (smoking)
            points += 4;

        // Diabetes
        if (diabetes)
            points += 3;

        // Convert points to risk percentage (simplified approximation)
        return convertPointsToRisk(points);
    }

    /**
     * Framingham score for females
     */
    private double calculateFraminghamFemale(int age, double totalChol, double hdl, int systolic,
            boolean smoking, boolean diabetes) {
        double points = 0.0;

        // Age points
        if (age >= 30 && age <= 34)
            points += -9;
        else if (age >= 35 && age <= 39)
            points += -4;
        else if (age >= 40 && age <= 44)
            points += 0;
        else if (age >= 45 && age <= 49)
            points += 3;
        else if (age >= 50 && age <= 54)
            points += 6;
        else if (age >= 55 && age <= 59)
            points += 7;
        else if (age >= 60 && age <= 64)
            points += 8;
        else if (age >= 65 && age <= 69)
            points += 8;
        else if (age >= 70 && age <= 74)
            points += 8;

        // Total cholesterol points
        if (totalChol < 160)
            points += 0;
        else if (totalChol < 200)
            points += 1;
        else if (totalChol < 240)
            points += 3;
        else if (totalChol < 280)
            points += 4;
        else
            points += 5;

        // HDL cholesterol points
        if (hdl >= 60)
            points += -2;
        else if (hdl >= 50)
            points += -1;
        else if (hdl >= 45)
            points += 0;
        else if (hdl >= 35)
            points += 1;
        else
            points += 2;

        // Systolic BP points
        if (systolic < 120)
            points += -3;
        else if (systolic < 130)
            points += 0;
        else if (systolic < 140)
            points += 2;
        else if (systolic < 160)
            points += 5;
        else
            points += 7;

        // Smoking
        if (smoking)
            points += 3;

        // Diabetes
        if (diabetes)
            points += 4;

        return convertPointsToRisk(points);
    }

    /**
     * Convert Framingham points to risk percentage
     */
    private double convertPointsToRisk(double points) {
        // Simplified conversion (actual Framingham uses lookup tables)
        if (points < 0)
            return 1.0;
        else if (points < 5)
            return 2.0;
        else if (points < 10)
            return 5.0;
        else if (points < 15)
            return 10.0;
        else if (points < 20)
            return 20.0;
        else if (points < 25)
            return 30.0;
        else
            return 40.0;
    }

    // ========== Trend Analysis ==========

    /**
     * Analyze vital signs trends over time
     */
    public Map<String, Object> analyzeTrends(Long patientId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<VitalSigns> recentVitals = vitalSignsRepository.findByPatientIdAndDateRange(
                patientId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));

        if (recentVitals.isEmpty()) {
            return Map.of("error", "No vital signs data available");
        }

        Map<String, Object> trends = new HashMap<>();

        // Blood Pressure Trend
        trends.put("bloodPressureTrend", analyzeBloodPressureTrend(recentVitals));

        // Weight/BMI Trend
        trends.put("weightTrend", analyzeWeightTrend(recentVitals));
        trends.put("bmiTrend", analyzeBMITrend(recentVitals));

        // Blood Sugar Trend
        trends.put("bloodSugarTrend", analyzeBloodSugarTrend(recentVitals));

        // Heart Rate Trend
        trends.put("heartRateTrend", analyzeHeartRateTrend(recentVitals));

        return trends;
    }

    private String analyzeBloodPressureTrend(List<VitalSigns> vitals) {
        List<Integer> systolicValues = vitals.stream()
                .filter(v -> v.getSystolicPressure() != null)
                .map(VitalSigns::getSystolicPressure)
                .toList();

        if (systolicValues.size() < 2)
            return "INSUFFICIENT_DATA";

        double firstHalf = systolicValues.subList(0, systolicValues.size() / 2).stream()
                .mapToInt(Integer::intValue).average().orElse(0);
        double secondHalf = systolicValues.subList(systolicValues.size() / 2, systolicValues.size()).stream()
                .mapToInt(Integer::intValue).average().orElse(0);

        double change = ((secondHalf - firstHalf) / firstHalf) * 100;

        if (change > 5)
            return "WORSENING";
        else if (change < -5)
            return "IMPROVING";
        else
            return "STABLE";
    }

    private String analyzeWeightTrend(List<VitalSigns> vitals) {
        // Similar logic to blood pressure
        return analyzeTrendGeneric(vitals, VitalSigns::getWeight);
    }

    private String analyzeBMITrend(List<VitalSigns> vitals) {
        return analyzeTrendGeneric(vitals, VitalSigns::getBmi);
    }

    private String analyzeBloodSugarTrend(List<VitalSigns> vitals) {
        return analyzeTrendGeneric(vitals, VitalSigns::getBloodSugar);
    }

    private String analyzeHeartRateTrend(List<VitalSigns> vitals) {
        List<Integer> values = vitals.stream()
                .filter(v -> v.getHeartRate() != null)
                .map(VitalSigns::getHeartRate)
                .toList();

        if (values.size() < 2)
            return "INSUFFICIENT_DATA";

        double firstHalf = values.subList(0, values.size() / 2).stream()
                .mapToInt(Integer::intValue).average().orElse(0);
        double secondHalf = values.subList(values.size() / 2, values.size()).stream()
                .mapToInt(Integer::intValue).average().orElse(0);

        double change = ((secondHalf - firstHalf) / firstHalf) * 100;

        if (change > 10)
            return "WORSENING";
        else if (change < -10)
            return "IMPROVING";
        else
            return "STABLE";
    }

    private <T extends Number> String analyzeTrendGeneric(List<VitalSigns> vitals,
            java.util.function.Function<VitalSigns, T> extractor) {
        List<Double> values = vitals.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .map(v -> v.doubleValue())
                .toList();

        if (values.size() < 2)
            return "INSUFFICIENT_DATA";

        double firstHalf = values.subList(0, values.size() / 2).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
        double secondHalf = values.subList(values.size() / 2, values.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);

        double change = ((secondHalf - firstHalf) / firstHalf) * 100;

        if (change > 5)
            return "WORSENING";
        else if (change < -5)
            return "IMPROVING";
        else
            return "STABLE";
    }

    // ========== Generate Forecast ==========

    /**
     * Generate comprehensive health forecast for a patient
     */
    @Transactional
    public HealthForecast generateForecast(Long patientId, User doctor) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Get latest vital signs
        Optional<VitalSigns> latestVitalsOpt = vitalSignsRepository.findLatestByPatientId(patientId);
        if (latestVitalsOpt.isEmpty()) {
            throw new RuntimeException("No vital signs data available for forecast");
        }
        VitalSigns latestVitals = latestVitalsOpt.get();

        // Calculate risk scores
        Map<String, Object> riskScores = new HashMap<>();
        try {
            double cvRisk = calculateFraminghamScore(patientId);
            riskScores.put("cardiovascularRisk", cvRisk);

            // Determine overall risk level
            String overallRisk;
            if (cvRisk < 5)
                overallRisk = "LOW";
            else if (cvRisk < 10)
                overallRisk = "MODERATE";
            else if (cvRisk < 20)
                overallRisk = "HIGH";
            else
                overallRisk = "VERY_HIGH";
            riskScores.put("overallRisk", overallRisk);

        } catch (Exception e) {
            riskScores.put("cardiovascularRisk", 0.0);
            riskScores.put("overallRisk", "UNKNOWN");
            riskScores.put("error", e.getMessage());
        }

        // Analyze trends
        Map<String, Object> predictions = analyzeTrends(patientId, 90); // Last 90 days

        // Build risk factors
        Map<String, Object> riskFactors = new HashMap<>();
        riskFactors.put("age", calculateAge(patient.getDateOfBirth()));
        riskFactors.put("gender", patient.getGender());
        // Add more risk factors as needed

        // Vital signs snapshot
        Map<String, Object> vitalSnapshot = new HashMap<>();
        vitalSnapshot.put("systolic", latestVitals.getSystolicPressure());
        vitalSnapshot.put("diastolic", latestVitals.getDiastolicPressure());
        vitalSnapshot.put("heartRate", latestVitals.getHeartRate());
        vitalSnapshot.put("weight", latestVitals.getWeight());
        vitalSnapshot.put("bmi", latestVitals.getBmi());
        vitalSnapshot.put("bloodSugar", latestVitals.getBloodSugar());

        // Generate recommendations
        String recommendations = generateRecommendations(riskScores, predictions, latestVitals);

        // Create forecast
        HealthForecast forecast = HealthForecast.builder()
                .patient(patient)
                .forecastDate(LocalDate.now())
                .riskScores(riskScores)
                .predictions(predictions)
                .riskFactors(riskFactors)
                .vitalSignsSnapshot(vitalSnapshot)
                .recommendations(recommendations)
                .createdBy(doctor)
                .status(HealthForecast.ForecastStatus.ACTIVE)
                .build();

        // Mark previous active forecasts as outdated
        getLatestActiveForecast(patientId).ifPresent(old -> {
            if (!old.getId().equals(forecast.getId())) {
                old.markAsOutdated();
                healthForecastRepository.save(old);
            }
        });

        return healthForecastRepository.save(forecast);
    }

    /**
     * Generate personalized recommendations based on risk analysis
     */
    private String generateRecommendations(Map<String, Object> riskScores,
            Map<String, Object> predictions,
            VitalSigns vitals) {
        StringBuilder recommendations = new StringBuilder();

        Object overallRisk = riskScores.get("overallRisk");

        if ("HIGH".equals(overallRisk) || "VERY_HIGH".equals(overallRisk)) {
            recommendations.append("⚠️ RỦI RO CAO - CẦN THEO DÕI CHẶT CHẼ\n\n");
            recommendations.append("1. Tái khám ngay trong tuần này\n");
            recommendations.append("2. Xét nghiệm toàn diện (lipid profile, HbA1c, ECG)\n");
            recommendations.append("3. Cân nhắc điều chỉnh thuốc\n\n");
        }

        // Blood pressure recommendations
        if (vitals.getSystolicPressure() != null && vitals.getSystolicPressure() > 140) {
            recommendations.append("🩺 Huyết áp:\n");
            recommendations.append("- Giảm muối trong chế độ ăn (<5g/ngày)\n");
            recommendations.append("- Tăng cường vận động 30 phút/ngày\n");
            recommendations.append("- Kiểm tra huyết áp hàng ngày\n\n");
        }

        // BMI recommendations
        if (vitals.getBmi() != null && vitals.getBmi().doubleValue() > 25) {
            recommendations.append("⚖️ Cân nặng:\n");
            recommendations.append("- Giảm 500 calories/ngày\n");
            recommendations.append("- Tăng vận động aerobic\n");
            recommendations.append("- Tham khảo chuyên gia dinh dưỡng\n\n");
        }

        // Blood sugar recommendations
        if (vitals.getBloodSugar() != null && vitals.getBloodSugar().doubleValue() > 126) {
            recommendations.append("🩸 Đường huyết:\n");
            recommendations.append("- Kiểm soát carbohydrate\n");
            recommendations.append("- Đo đường huyết 2 lần/ngày\n");
            recommendations.append("- Tái khám kiểm tra HbA1c\n\n");
        }

        if (recommendations.length() == 0) {
            recommendations.append("✅ Các chỉ số trong giới hạn bình thường.\n\n");
            recommendations.append("Duy trì:\n");
            recommendations.append("- Chế độ ăn lành mạnh\n");
            recommendations.append("- Vận động đều đặn\n");
            recommendations.append("- Tái khám định kỳ 6 tháng/lần\n");
        }

        return recommendations.toString();
    }

    // ========== Risk Alerts ==========

    /**
     * Get risk alerts for high-risk patients
     */
    public List<HealthForecast> getHighRiskForecasts() {
        return healthForecastRepository.findHighRiskForecasts();
    }

    /**
     * Get forecasts with cardiovascular risk above threshold
     */
    public List<HealthForecast> getForecastsByCardiovascularRisk(double threshold) {
        return healthForecastRepository.findByCardiovascularRiskAbove(threshold);
    }

    // ========== Helper Methods ==========

    private int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null)
            return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Update forecast status
     */
    @Transactional
    public HealthForecast updateForecastStatus(Long forecastId, HealthForecast.ForecastStatus status) {
        HealthForecast forecast = getForecastById(forecastId);
        forecast.setStatus(status);
        forecast.setUpdatedAt(java.time.LocalDateTime.now());
        return healthForecastRepository.save(forecast);
    }

    /**
     * Delete forecast
     */
    @Transactional
    public void deleteForecast(Long forecastId) {
        healthForecastRepository.deleteById(forecastId);
    }
}
