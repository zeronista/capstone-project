package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity Survey - Quản lý các form khảo sát (Google Form, etc.)
 */
@Entity
@Table(name = "surveys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "form_url", nullable = false, length = 500)
    private String formUrl;

    @Column(name = "icon_type", length = 50)
    @Builder.Default
    private String iconType = "survey"; // survey, health, feedback, register

    @Column(name = "icon_color", length = 50)
    @Builder.Default
    private String iconColor = "#3B82F6"; // Default blue

    @Column(length = 50)
    @Builder.Default
    private String tag = "Khảo sát";

    @Column(name = "tag_color", length = 50)
    @Builder.Default
    private String tagColor = "#3B82F6";

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "response_count")
    @Builder.Default
    private Integer responseCount = 0;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "show_on_landing")
    @Builder.Default
    private Boolean showOnLanding = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
