package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity VitalSigns - Chỉ số sinh tồn của bệnh nhân
 * Lưu trữ các chỉ số y tế quan trọng như huyết áp, cân nặng, nhịp tim, nhiệt độ
 */
@Entity
@Table(name = "vital_signs", indexes = {
        @Index(name = "idx_patient_id", columnList = "patient_id"),
        @Index(name = "idx_record_date", columnList = "record_date"),
        @Index(name = "idx_patient_date", columnList = "patient_id, record_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSigns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== Quan hệ với bệnh nhân ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    // ========== Huyết áp (Blood Pressure) ==========
    @Column(name = "systolic_pressure")
    private Integer systolicPressure; // Huyết áp tâm thu (mmHg)

    @Column(name = "diastolic_pressure")
    private Integer diastolicPressure; // Huyết áp tâm trương (mmHg)

    // ========== Nhịp tim (Heart Rate) ==========
    @Column(name = "heart_rate")
    private Integer heartRate; // bpm (beats per minute)

    // ========== Cân nặng (Weight) ==========
    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight; // kg

    // ========== Chiều cao (Height) ==========
    @Column(name = "height", precision = 5, scale = 2)
    private BigDecimal height; // cm

    // ========== BMI (Body Mass Index) ==========
    @Column(name = "bmi", precision = 4, scale = 2)
    private BigDecimal bmi; // Calculated: weight(kg) / (height(m))^2

    // ========== Nhiệt độ (Temperature) ==========
    @Column(name = "temperature", precision = 4, scale = 2)
    private BigDecimal temperature; // °C

    // ========== Nhịp thở (Respiratory Rate) ==========
    @Column(name = "respiratory_rate")
    private Integer respiratoryRate; // breaths per minute

    // ========== SpO2 (Oxygen Saturation) ==========
    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation; // %

    // ========== Đường huyết (Blood Sugar) ==========
    @Column(name = "blood_sugar", precision = 5, scale = 2)
    private BigDecimal bloodSugar; // mg/dL hoặc mmol/L

    // ========== Ghi chú ==========
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========== Người ghi nhận ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy; // Bác sĩ hoặc y tá ghi nhận

    // ========== Thời gian ==========
    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========== Helper method ==========

    /**
     * Tính BMI dựa trên cân nặng và chiều cao
     */
    public void calculateBMI() {
        if (weight != null && height != null && height.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightInMeters = height.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            this.bmi = weight.divide(heightInMeters.multiply(heightInMeters), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * Format huyết áp thành chuỗi "systolic/diastolic"
     */
    public String getBloodPressureFormatted() {
        if (systolicPressure != null && diastolicPressure != null) {
            return systolicPressure + "/" + diastolicPressure;
        }
        return null;
    }

    /**
     * Đánh giá mức độ huyết áp
     */
    public String getBloodPressureCategory() {
        if (systolicPressure == null || diastolicPressure == null) {
            return "UNKNOWN";
        }

        if (systolicPressure < 120 && diastolicPressure < 80) {
            return "NORMAL";
        } else if (systolicPressure < 130 && diastolicPressure < 80) {
            return "ELEVATED";
        } else if (systolicPressure < 140 || diastolicPressure < 90) {
            return "HIGH_STAGE_1";
        } else if (systolicPressure < 180 || diastolicPressure < 120) {
            return "HIGH_STAGE_2";
        } else {
            return "HYPERTENSIVE_CRISIS";
        }
    }

    /**
     * Đánh giá BMI
     */
    public String getBMICategory() {
        if (bmi == null) {
            return "UNKNOWN";
        }

        double bmiValue = bmi.doubleValue();
        if (bmiValue < 18.5) {
            return "UNDERWEIGHT";
        } else if (bmiValue < 25) {
            return "NORMAL";
        } else if (bmiValue < 30) {
            return "OVERWEIGHT";
        } else {
            return "OBESE";
        }
    }
}
