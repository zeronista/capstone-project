package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.ProfileResponse;
import com.g4.capstoneproject.entity.PatientDocument;
import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TreatmentPlan;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.PrescriptionRepository;
import com.g4.capstoneproject.repository.TicketRepository;
import com.g4.capstoneproject.repository.TreatmentPlanRepository;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.PatientDocumentService;
import com.g4.capstoneproject.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho chức năng dành riêng cho bệnh nhân
 */
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@Slf4j
public class PatientController {
    
    private final PrescriptionRepository prescriptionRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PatientDocumentService patientDocumentService;
    private final ProfileService profileService;
    
    /**
     * GET /api/patient/prescriptions - Lấy danh sách đơn thuốc của bệnh nhân
     */
    @GetMapping("/prescriptions")
    public ResponseEntity<Map<String, Object>> getMyPrescriptions(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            User patient = userRepository.findById(userId).orElse(null);
            if (patient == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            List<Prescription> prescriptions = prescriptionRepository.findByPatientOrderByPrescriptionDateDesc(patient);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prescriptions", prescriptions);
            response.put("total", prescriptions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting patient prescriptions", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
    
    /**
     * GET /api/patient/prescriptions/{id} - Lấy chi tiết đơn thuốc
     */
    @GetMapping("/prescriptions/{id}")
    public ResponseEntity<Map<String, Object>> getPrescriptionDetail(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            Prescription prescription = prescriptionRepository.findById(id).orElse(null);
            if (prescription == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy đơn thuốc"));
            }
            
            // Kiểm tra xem đơn thuốc có thuộc về bệnh nhân này không
            if (!prescription.getPatient().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Không có quyền truy cập"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prescription", prescription);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting prescription detail", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
    
    /**
     * GET /api/patient/treatments - Lấy danh sách kế hoạch điều trị
     */
    @GetMapping("/treatments")
    public ResponseEntity<Map<String, Object>> getMyTreatments(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            User patient = userRepository.findById(userId).orElse(null);
            if (patient == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            List<TreatmentPlan> treatments = treatmentPlanRepository.findByPatient(patient);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("treatments", treatments);
            response.put("total", treatments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting patient treatments", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
    
    /**
     * GET /api/patient/tickets - Lấy danh sách tickets/yêu cầu hỗ trợ
     */
    @GetMapping("/tickets")
    public ResponseEntity<Map<String, Object>> getMyTickets(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            User patient = userRepository.findById(userId).orElse(null);
            if (patient == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            List<Ticket> tickets = ticketRepository.findByPatient(patient);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tickets", tickets);
            response.put("total", tickets.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting patient tickets", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
    
    /**
     * GET /api/patient/profile - Lấy thông tin cá nhân (API endpoint)
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getMyProfile(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            ProfileResponse profile = profileService.getProfile(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", profile.getId());
            response.put("email", profile.getEmail());
            response.put("phoneNumber", profile.getPhone()); // Map 'phone' to 'phoneNumber' for backward compatibility
            response.put("fullName", profile.getFullName());
            response.put("dateOfBirth", profile.getDateOfBirth());
            response.put("gender", profile.getGender() != null ? profile.getGender().toString() : null);
            response.put("address", profile.getAddress());
            response.put("avatarUrl", profile.getAvatar());
            response.put("emailVerified", profile.getIsVerified()); // Map 'isVerified' to 'emailVerified' for backward compatibility
            response.put("createdAt", profile.getCreatedAt());
            response.put("role", profile.getRole().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting patient profile", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
    
    /**
     * GET /api/patient/stats - Lấy thống kê tổng quan
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMyStats(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            User patient = userRepository.findById(userId).orElse(null);
            if (patient == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            long prescriptionCount = prescriptionRepository.countByPatient(patient);
            long treatmentCount = treatmentPlanRepository.countByPatient(patient);
            long ticketCount = ticketRepository.countByPatient(patient);
            long openTicketCount = ticketRepository.countByPatientAndStatus(patient, Ticket.Status.OPEN);
            long documentCount = patientDocumentService.countPatientDocuments(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("prescriptions", prescriptionCount);
            stats.put("treatments", treatmentCount);
            stats.put("tickets", ticketCount);
            stats.put("openTickets", openTicketCount);
            stats.put("documents", documentCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting patient stats", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
    
    /**
     * GET /api/patient/treatment-plans - Alias cho /api/patient/treatments (để tương thích với frontend)
     */
    @GetMapping("/treatment-plans")
    public ResponseEntity<Map<String, Object>> getMyTreatmentPlans(HttpSession session) {
        // Delegate to getMyTreatments
        return getMyTreatments(session);
    }
    
    // ========== DOCUMENT MANAGEMENT ==========
    
    /**
     * GET /api/patient/documents - Lấy danh sách tài liệu của bệnh nhân
     */
    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> getMyDocuments(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            List<Map<String, Object>> documents = patientDocumentService.getPatientDocuments(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documents", documents);
            response.put("total", documents.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting patient documents", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/patient/documents/{id} - Lấy chi tiết một tài liệu
     */
    @GetMapping("/documents/{id}")
    public ResponseEntity<Map<String, Object>> getDocumentDetail(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            var documentOpt = patientDocumentService.getDocumentById(id);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy tài liệu"));
            }
            
            Map<String, Object> document = documentOpt.get();
            
            // Kiểm tra quyền sở hữu
            if (!userId.equals(document.get("patientId"))) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Không có quyền truy cập"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("document", document);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting document detail", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
    
    /**
     * POST /api/patient/documents/upload - Upload tài liệu mới
     */
    /**
     * Danh sach cac content type duoc phep upload
     */
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "text/plain"
    );
    
    /**
     * Danh sach cac extension file duoc phep
     */
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", 
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".txt"
    );
    
    @PostMapping("/documents/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "description", required = false) String description,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chua dang nhap"));
            }
            
            // Validate file khong rong
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui long chon file"));
            }
            
            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File khong duoc vuot qua 10MB"));
            }
            
            // Validate content type
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, 
                    "message", "Loai file khong duoc ho tro. Chi chap nhan: PDF, Word, Excel, anh (JPG, PNG, GIF, WebP)"));
            }
            
            // Validate file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    return ResponseEntity.badRequest().body(Map.of("success", false,
                        "message", "Dinh dang file khong hop le. Chi chap nhan: PDF, Word, Excel, anh"));
                }
            }
            
            // Parse document type
            PatientDocument.DocumentType docType;
            try {
                docType = PatientDocument.DocumentType.valueOf(documentType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, 
                        "message", "Loai tai lieu khong hop le. Cac loai hop le: MEDICAL_HISTORY, PRESCRIPTION, TEST_RESULT, OTHER"));
            }
            
            // Lam sach description neu co
            String cleanDescription = description;
            if (cleanDescription != null) {
                cleanDescription = cleanDescription.trim();
                if (cleanDescription.length() > 500) {
                    cleanDescription = cleanDescription.substring(0, 500);
                }
            }
            
            // Upload document
            PatientDocument document = patientDocumentService.uploadDocument(userId, file, docType, cleanDescription);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload tai lieu thanh cong");
            response.put("documentId", document.getId());
            response.put("fileName", document.getFileName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading document", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Loi upload: " + e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/patient/documents/{id} - Xóa tài liệu
     */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            boolean deleted = patientDocumentService.deleteDocument(id, userId);
            
            if (!deleted) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy tài liệu hoặc không có quyền xóa"));
            }
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa tài liệu thành công"));
        } catch (Exception e) {
            log.error("Error deleting document", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi xóa tài liệu: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/patient/documents/types - Lấy danh sách loại tài liệu
     */
    @GetMapping("/documents/types")
    public ResponseEntity<Map<String, Object>> getDocumentTypes() {
        Map<String, String> types = new HashMap<>();
        types.put("MEDICAL_HISTORY", "Lịch sử khám bệnh");
        types.put("PRESCRIPTION", "Đơn thuốc");
        types.put("TEST_RESULT", "Kết quả xét nghiệm");
        types.put("OTHER", "Khác");
        
        return ResponseEntity.ok(Map.of("success", true, "types", types));
    }
}
