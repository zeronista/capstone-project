package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Hồ sơ sức khỏe cơ bản của bệnh nhân (bổ sung cho User/UserInfo).
 */
@Entity
@Table(name = "patient_health_profiles",
        indexes = {
                @Index(name = "idx_health_profile_user", columnList = "user_id", unique = true)
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientHealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Nhóm máu (A+, O-, ...)
     */
    @Column(name = "blood_type", length = 8)
    private String bloodType;

    /**
     * Chiều cao (cm)
     */
    @Column(name = "height_cm")
    private Double heightCm;

    /**
     * Cân nặng (kg)
     */
    @Column(name = "weight_kg")
    private Double weightKg;

    /**
     * Dị ứng (text tự do)
     */
    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    /**
     * Bệnh mãn tính (text tự do)
     */
    @Column(name = "chronic_diseases", columnDefinition = "TEXT")
    private String chronicDiseases;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

