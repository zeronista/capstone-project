package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity StaffInfo - Thông tin nhân viên (Lễ tân, Bác sĩ)
 */
@Entity
@Table(name = "staff_info", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "employee_code"),
        @UniqueConstraint(columnNames = "license_number")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "employee_code", unique = true, length = 20)
    private String employeeCode;
    
    @Column(length = 100)
    private String department;
    
    @Column(length = 100)
    private String specialization; // Chuyên khoa (cho bác sĩ)
    
    @Column(name = "license_number", unique = true, length = 50)
    private String licenseNumber; // Số giấy phép hành nghề (cho bác sĩ)
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StaffStatus status = StaffStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Enum trạng thái nhân viên
     */
    public enum StaffStatus {
        ACTIVE,     // Đang làm việc
        INACTIVE,   // Nghỉ việc
        ON_LEAVE    // Đang nghỉ phép
    }
}
