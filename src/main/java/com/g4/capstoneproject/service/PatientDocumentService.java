package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.PatientDocument;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.PatientDocumentRepository;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service xử lý tài liệu của bệnh nhân
 * Quản lý upload, xem, xóa tài liệu lên S3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientDocumentService {

    private final S3Service s3Service;
    private final PatientDocumentRepository patientDocumentRepository;
    private final UserRepository userRepository;

    /**
     * Upload tài liệu của bệnh nhân
     * @param patientId ID của bệnh nhân
     * @param file File cần upload
     * @param documentType Loại tài liệu
     * @param description Mô tả tài liệu
     * @return PatientDocument entity đã được lưu
     */
    @Transactional
    public PatientDocument uploadDocument(Long patientId, MultipartFile file, 
            PatientDocument.DocumentType documentType, String description) throws IOException {
        
        // Kiểm tra bệnh nhân tồn tại
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + patientId));
        
        if (patient.getRole() != User.UserRole.PATIENT) {
            throw new RuntimeException("Người dùng không phải là bệnh nhân");
        }
        
        // Upload file lên S3
        String fileKey = s3Service.uploadPatientDocument(file, patientId, documentType.name());
        
        // Tạo pre-signed URL cho file
        String presignedUrl = s3Service.generatePresignedUrl(fileKey);
        
        // Lưu thông tin vào database
        PatientDocument document = PatientDocument.builder()
                .patient(patient)
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .fileUrl(fileKey) // Lưu key, không phải URL đầy đủ
                .fileSize(file.getSize())
                .description(description)
                .build();
        
        PatientDocument savedDocument = patientDocumentRepository.save(document);
        log.info("Uploaded document for patient {}: {} ({})", patientId, file.getOriginalFilename(), fileKey);
        
        return savedDocument;
    }

    /**
     * Lấy danh sách tài liệu của bệnh nhân
     * @param patientId ID của bệnh nhân
     * @return Danh sách tài liệu với pre-signed URL
     */
    public List<Map<String, Object>> getPatientDocuments(Long patientId) {
        List<PatientDocument> documents = patientDocumentRepository.findByPatientId(patientId);
        
        return documents.stream().map(doc -> {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", doc.getId());
            docMap.put("fileName", doc.getFileName());
            docMap.put("documentType", doc.getDocumentType().name());
            docMap.put("documentTypeLabel", getDocumentTypeLabel(doc.getDocumentType()));
            docMap.put("fileSize", doc.getFileSize());
            docMap.put("fileSizeFormatted", formatFileSize(doc.getFileSize()));
            docMap.put("description", doc.getDescription());
            docMap.put("uploadDate", doc.getUploadDate());
            docMap.put("fileKey", doc.getFileUrl());
            
            // Tạo pre-signed URL để xem file
            try {
                String presignedUrl = s3Service.generatePresignedUrl(doc.getFileUrl(), 7 * 24 * 3600);
                docMap.put("viewUrl", presignedUrl);
            } catch (Exception e) {
                log.warn("Could not generate presigned URL for document {}: {}", doc.getId(), e.getMessage());
                docMap.put("viewUrl", null);
            }
            
            return docMap;
        }).collect(Collectors.toList());
    }

    /**
     * Lấy danh sách tài liệu của bệnh nhân theo loại
     * @param patientId ID của bệnh nhân
     * @param documentType Loại tài liệu
     * @return Danh sách tài liệu
     */
    public List<Map<String, Object>> getPatientDocumentsByType(Long patientId, PatientDocument.DocumentType documentType) {
        List<PatientDocument> documents = patientDocumentRepository.findByPatientIdAndDocumentType(patientId, documentType);
        
        return documents.stream().map(doc -> {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", doc.getId());
            docMap.put("fileName", doc.getFileName());
            docMap.put("documentType", doc.getDocumentType().name());
            docMap.put("documentTypeLabel", getDocumentTypeLabel(doc.getDocumentType()));
            docMap.put("fileSize", doc.getFileSize());
            docMap.put("fileSizeFormatted", formatFileSize(doc.getFileSize()));
            docMap.put("description", doc.getDescription());
            docMap.put("uploadDate", doc.getUploadDate());
            docMap.put("fileKey", doc.getFileUrl());
            
            // Tạo pre-signed URL để xem file
            try {
                String presignedUrl = s3Service.generatePresignedUrl(doc.getFileUrl(), 7 * 24 * 3600);
                docMap.put("viewUrl", presignedUrl);
            } catch (Exception e) {
                log.warn("Could not generate presigned URL for document {}: {}", doc.getId(), e.getMessage());
                docMap.put("viewUrl", null);
            }
            
            return docMap;
        }).collect(Collectors.toList());
    }

    /**
     * Lấy thông tin một tài liệu
     * @param documentId ID của tài liệu
     * @return Thông tin tài liệu
     */
    public Optional<Map<String, Object>> getDocumentById(Long documentId) {
        return patientDocumentRepository.findById(documentId).map(doc -> {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", doc.getId());
            docMap.put("patientId", doc.getPatient().getId());
            docMap.put("fileName", doc.getFileName());
            docMap.put("documentType", doc.getDocumentType().name());
            docMap.put("documentTypeLabel", getDocumentTypeLabel(doc.getDocumentType()));
            docMap.put("fileSize", doc.getFileSize());
            docMap.put("fileSizeFormatted", formatFileSize(doc.getFileSize()));
            docMap.put("description", doc.getDescription());
            docMap.put("uploadDate", doc.getUploadDate());
            docMap.put("fileKey", doc.getFileUrl());
            
            // Tạo pre-signed URL để xem file
            try {
                String presignedUrl = s3Service.generatePresignedUrl(doc.getFileUrl(), 7 * 24 * 3600);
                docMap.put("viewUrl", presignedUrl);
            } catch (Exception e) {
                log.warn("Could not generate presigned URL for document {}: {}", doc.getId(), e.getMessage());
                docMap.put("viewUrl", null);
            }
            
            return docMap;
        });
    }

    /**
     * Xóa tài liệu
     * @param documentId ID của tài liệu
     * @param patientId ID của bệnh nhân (để xác thực quyền sở hữu)
     * @return true nếu xóa thành công
     */
    @Transactional
    public boolean deleteDocument(Long documentId, Long patientId) {
        Optional<PatientDocument> documentOpt = patientDocumentRepository.findById(documentId);
        
        if (documentOpt.isEmpty()) {
            log.warn("Document not found: {}", documentId);
            return false;
        }
        
        PatientDocument document = documentOpt.get();
        
        // Kiểm tra quyền sở hữu
        if (!document.getPatient().getId().equals(patientId)) {
            log.warn("Patient {} tried to delete document {} owned by patient {}", 
                    patientId, documentId, document.getPatient().getId());
            return false;
        }
        
        // Xóa file trên S3
        try {
            s3Service.deletePatientDocument(document.getFileUrl());
        } catch (Exception e) {
            log.error("Failed to delete S3 file for document {}: {}", documentId, e.getMessage());
            // Vẫn tiếp tục xóa record trong database
        }
        
        // Xóa record trong database
        patientDocumentRepository.delete(document);
        log.info("Deleted document {} for patient {}", documentId, patientId);
        
        return true;
    }

    /**
     * Đếm số tài liệu của bệnh nhân
     * @param patientId ID của bệnh nhân
     * @return Số lượng tài liệu
     */
    public long countPatientDocuments(Long patientId) {
        return patientDocumentRepository.countByPatientId(patientId);
    }

    /**
     * Lấy nhãn tiếng Việt cho loại tài liệu
     */
    private String getDocumentTypeLabel(PatientDocument.DocumentType type) {
        return switch (type) {
            case MEDICAL_HISTORY -> "Lịch sử khám bệnh";
            case PRESCRIPTION -> "Đơn thuốc";
            case TEST_RESULT -> "Kết quả xét nghiệm";
            case OTHER -> "Khác";
        };
    }

    /**
     * Format kích thước file
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null) return "N/A";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
