package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity để lưu trữ tiền sử bệnh gia đình của bệnh nhân
 */
@Entity
@Table(name = "family_medical_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Relationship relationship;

    @Column(name = "`condition`", nullable = false, length = 100)
    private String condition;

    @Column(name = "age_at_diagnosis")
    private Integer ageAtDiagnosis;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", length = 20)
    private MemberStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Relationship {
        FATHER, // Cha
        MOTHER, // Mẹ
        GRANDFATHER_P, // Ông nội
        GRANDMOTHER_P, // Bà nội
        GRANDFATHER_M, // Ông ngoại
        GRANDMOTHER_M, // Bà ngoại
        SIBLING, // Anh/Chị/Em ruột
        UNCLE_AUNT, // Cô/Dì/Chú/Bác
        OTHER // Khác
    }

    public enum MemberStatus {
        ALIVE,
        DECEASED,
        UNKNOWN
    }
}
