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
 * Theo doi trang thai dong bo tung response tu Google Forms.
 * Dung de dam bao idempotent theo cap formId + responseId.
 */
@Entity
@Table(name = "google_form_sync_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_google_form_response", columnNames = { "form_id", "response_id" })
        },
        indexes = {
                @Index(name = "idx_google_form_sync_status", columnList = "sync_status"),
                @Index(name = "idx_google_form_sync_patient", columnList = "patient_id"),
                @Index(name = "idx_google_form_sync_synced_at", columnList = "synced_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleFormSyncRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false, length = 120)
    private String formId;

    @Column(name = "response_id", nullable = false, length = 120)
    private String responseId;

    @Column(name = "form_title", length = 255)
    private String formTitle;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_report_id")
    private MedicalReport medicalReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_status", length = 20)
    private CallStatus callStatus;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @CreationTimestamp
    @Column(name = "synced_at", nullable = false, updatable = false)
    private LocalDateTime syncedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum SyncStatus {
        SYNCED,
        FAILED
    }

    public enum CallStatus {
        NOT_CALLED,
        CALLED
    }
}
