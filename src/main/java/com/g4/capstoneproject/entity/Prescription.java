package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Prescription - Đơn thuốc
 */
@Entity
@Table(name = "prescriptions", indexes = {
    @Index(name = "idx_prescription_patient", columnList = "patient_id"),
    @Index(name = "idx_prescription_doctor", columnList = "doctor_id"),
    @Index(name = "idx_prescription_date", columnList = "prescription_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;
    
    @Column(name = "prescription_date", nullable = false)
    private LocalDate prescriptionDate;
    
    @Column(columnDefinition = "TEXT")
    private String diagnosis;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;
    
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrescriptionDetail> details = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Enum trạng thái đơn thuốc
     */
    public enum PrescriptionStatus {
        ACTIVE,     // Đang sử dụng
        COMPLETED,  // Hoàn thành
        CANCELLED   // Đã hủy
    }
}
