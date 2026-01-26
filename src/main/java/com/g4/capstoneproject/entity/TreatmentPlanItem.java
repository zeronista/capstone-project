package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity TreatmentPlanItem - Chi tiết kế hoạch điều trị
 */
@Entity
@Table(name = "treatment_plan_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlanItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private TreatmentPlan treatmentPlan;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(length = 100)
    private String frequency; // Tần suất (vd: "2 lần/ngày")
    
    @Column(length = 100)
    private String duration; // Thời gian (vd: "7 ngày")
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ItemStatus status = ItemStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Enum loại mục điều trị
     */
    public enum ItemType {
        MEDICATION,     // Thuốc
        THERAPY,        // Liệu pháp
        LIFESTYLE,      // Lối sống
        CHECKUP         // Tái khám
    }
    
    /**
     * Enum trạng thái mục điều trị
     */
    public enum ItemStatus {
        PENDING,    // Chờ thực hiện
        ONGOING,    // Đang thực hiện
        COMPLETED,  // Hoàn thành
        SKIPPED     // Bỏ qua
    }
}
