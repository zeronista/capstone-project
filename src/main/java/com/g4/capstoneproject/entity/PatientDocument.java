package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity PatientDocument - Tài liệu của bệnh nhân (lịch sử khám, đơn thuốc, kết quả xét nghiệm)
 */
@Entity
@Table(name = "patient_documents", indexes = {
    @Index(name = "idx_doc_patient", columnList = "patient_id"),
    @Index(name = "idx_doc_type", columnList = "document_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(length = 500)
    private String description;
    
    @CreationTimestamp
    @Column(name = "upload_date", updatable = false)
    private LocalDateTime uploadDate;
    
    /**
     * Enum loại tài liệu
     */
    public enum DocumentType {
        MEDICAL_HISTORY,    // Lịch sử khám bệnh
        PRESCRIPTION,       // Đơn thuốc
        TEST_RESULT,        // Kết quả xét nghiệm
        OTHER               // Khác
    }
}
