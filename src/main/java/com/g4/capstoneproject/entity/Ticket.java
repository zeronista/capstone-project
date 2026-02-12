package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity Ticket - Yêu cầu hỗ trợ (khi vượt tầm hiểu biết của AI)
 */
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_status", columnList = "status"),
        @Index(name = "idx_ticket_assigned", columnList = "assigned_to_id"),
        @Index(name = "idx_ticket_patient", columnList = "patient_id"),
        @Index(name = "idx_ticket_priority", columnList = "priority")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id")
    private CallLog callLog; // Cuộc gọi liên quan

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    private User patient;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Category category;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.OPEN;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy; // Lễ tân tạo ticket

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo; // Bác sĩ được assign

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "retry_count")
    private Integer retryCount; // Số lần thử gọi lại bệnh nhân

    /**
     * Enum mức độ ưu tiên
     */
    public enum Priority {
        LOW, // Thấp
        MEDIUM, // Trung bình
        HIGH, // Cao
        URGENT // Khẩn cấp
    }

    /**
     * Enum danh mục ticket
     */
    public enum Category {
        MEDICAL_QUERY, // Câu hỏi y tế
        APPOINTMENT, // Lịch hẹn
        PRESCRIPTION, // Đơn thuốc
        TECHNICAL, // Kỹ thuật
        OTHER // Khác
    }

    /**
     * Enum trạng thái ticket
     */
    public enum Status {
        OPEN, // Mới tạo
        ASSIGNED, // Đã gán cho bác sĩ
        IN_PROGRESS, // Đang xử lý
        RESOLVED, // Đã giải quyết
        CLOSED // Đóng
    }
}
