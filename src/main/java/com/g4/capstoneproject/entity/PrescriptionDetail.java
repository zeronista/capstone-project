package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity PrescriptionDetail - Chi tiết đơn thuốc
 */
@Entity
@Table(name = "prescription_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;
    
    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;
    
    @Column(length = 100)
    private String dosage; // Liều lượng (vd: "500mg")
    
    @Column(length = 100)
    private String frequency; // Tần suất (vd: "3 lần/ngày")
    
    @Column(length = 100)
    private String duration; // Thời gian (vd: "7 ngày")
    
    @Column(columnDefinition = "TEXT")
    private String instructions; // Hướng dẫn sử dụng
    
    private Integer quantity; // Số lượng
}
