package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity HealthForecast - Dự báo sức khỏe của bệnh nhân
 * Lưu trữ dự báo rủi ro bệnh tật dựa trên vital signs, tiền sử, và các yếu tố
 * nguy cơ
 */
@Entity
@Table(name = "health_forecasts", indexes = {
        @Index(name = "idx_forecast_patient_id", columnList = "patient_id"),
        @Index(name = "idx_forecast_date", columnList = "forecast_date"),
        @Index(name = "idx_forecast_patient_date", columnList = "patient_id, forecast_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== Quan hệ với bệnh nhân ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    // ========== Ngày dự báo ==========
    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    // ========== Risk Scores (JSON) ==========
    /**
     * Điểm rủi ro các bệnh lý khác nhau:
     * - cardiovascularRisk: % rủi ro bệnh tim mạch trong 10 năm (Framingham Score)
     * - diabetesRisk: % rủi ro tiểu đường
     * - hypertensionRisk: % rủi ro tăng huyết áp
     * - strokeRisk: % rủi ro đột quỵ
     * - overallRisk: mức độ rủi ro tổng thể (LOW, MODERATE, HIGH, VERY_HIGH)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_scores", columnDefinition = "JSONB")
    private Map<String, Object> riskScores;

    // ========== Predictions (JSON) ==========
    /**
     * Dự đoán xu hướng các chỉ số sức khỏe:
     * - bloodPressureTrend: xu hướng huyết áp (IMPROVING, STABLE, WORSENING)
     * - weightTrend: xu hướng cân nặng
     * - bmiTrend: xu hướng BMI
     * - bloodSugarTrend: xu hướng đường huyết
     * - estimatedConditions: danh sách các bệnh có nguy cơ cao
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "predictions", columnDefinition = "JSONB")
    private Map<String, Object> predictions;

    // ========== Risk Factors (JSON) ==========
    /**
     * Các yếu tố nguy cơ đã phân tích:
     * - age: tuổi
     * - gender: giới tính
     * - smoking: hút thuốc (true/false)
     * - familyHistory: tiền sử gia đình (danh sách bệnh)
     * - currentConditions: bệnh lý hiện tại
     * - medications: thuốc đang dùng
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_factors", columnDefinition = "JSONB")
    private Map<String, Object> riskFactors;

    // ========== Recommendations ==========
    /**
     * Khuyến nghị phòng ngừa và điều trị:
     * - Thay đổi lối sống
     * - Kiểm tra sức khỏe định kỳ
     * - Thuốc cần dùng
     * - Chế độ ăn uống
     * - Vận động
     */
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    // ========== Vital Signs Snapshot ==========
    /**
     * Ảnh chụp các chỉ số sinh tồn tại thời điểm dự báo
     * Lưu dưới dạng JSON để tham chiếu nhanh
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vital_signs_snapshot", columnDefinition = "JSONB")
    private Map<String, Object> vitalSignsSnapshot;

    // ========== Bác sĩ tạo dự báo ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // ========== Thời gian ==========
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== Status ==========
    /**
     * Trạng thái dự báo:
     * - DRAFT: nháp
     * - ACTIVE: đang hiệu lực
     * - OUTDATED: đã lỗi thời (có dự báo mới hơn)
     * - ARCHIVED: đã lưu trữ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ForecastStatus status = ForecastStatus.ACTIVE;

    // ========== Notes ==========
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========== Enums ==========
    public enum ForecastStatus {
        DRAFT,
        ACTIVE,
        OUTDATED,
        ARCHIVED
    }

    // ========== Helper Methods ==========

    /**
     * Kiểm tra xem dự báo có rủi ro cao không
     */
    public boolean isHighRisk() {
        if (riskScores == null)
            return false;
        Object overallRisk = riskScores.get("overallRisk");
        return "HIGH".equals(overallRisk) || "VERY_HIGH".equals(overallRisk);
    }

    /**
     * Lấy điểm rủi ro tim mạch (Framingham Score)
     */
    public Double getCardiovascularRisk() {
        if (riskScores == null)
            return null;
        Object risk = riskScores.get("cardiovascularRisk");
        if (risk instanceof Number) {
            return ((Number) risk).doubleValue();
        }
        return null;
    }

    /**
     * Kiểm tra xem có cảnh báo rủi ro không
     */
    public boolean hasRiskAlerts() {
        if (riskScores == null)
            return false;

        // Kiểm tra rủi ro tim mạch > 20%
        Double cvRisk = getCardiovascularRisk();
        if (cvRisk != null && cvRisk > 20.0) {
            return true;
        }

        // Kiểm tra các rủi ro khác
        for (Map.Entry<String, Object> entry : riskScores.entrySet()) {
            if (entry.getKey().endsWith("Risk") && entry.getValue() instanceof Number) {
                double riskValue = ((Number) entry.getValue()).doubleValue();
                if (riskValue > 15.0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Đánh dấu dự báo là lỗi thời
     */
    public void markAsOutdated() {
        this.status = ForecastStatus.OUTDATED;
        this.updatedAt = LocalDateTime.now();
    }
}
