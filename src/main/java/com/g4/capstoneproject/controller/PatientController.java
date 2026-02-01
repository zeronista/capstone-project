package com.g4.capstoneproject.controller;

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
            
            User user = userRepository.findByIdWithUserInfo(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            // Build response with flattened structure for frontend compatibility
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("fullName", user.getFullName());
            response.put("dateOfBirth", user.getDateOfBirth());
            response.put("gender", user.getGender() != null ? user.getGender().toString() : null);
            response.put("address", user.getAddress());
            response.put("avatarUrl", user.getAvatarUrl());
            response.put("emailVerified", user.getEmailVerified());
            response.put("createdAt", user.getCreatedAt());
            response.put("role", user.getRole().toString());
            
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
    @PostMapping("/documents/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "description", required = false) String description,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng chọn file"));
            }
            
            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File không được vượt quá 10MB"));
            }
            
            // Parse document type
            PatientDocument.DocumentType docType;
            try {
                docType = PatientDocument.DocumentType.valueOf(documentType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, 
                        "message", "Loại tài liệu không hợp lệ. Các loại hợp lệ: MEDICAL_HISTORY, PRESCRIPTION, TEST_RESULT, OTHER"));
            }
            
            // Upload document
            PatientDocument document = patientDocumentService.uploadDocument(userId, file, docType, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload tài liệu thành công");
            response.put("documentId", document.getId());
            response.put("fileName", document.getFileName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading document", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi upload: " + e.getMessage()));
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
