package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.model.Prescription;
import com.g4.capstoneproject.model.TreatmentPlan;
import com.g4.capstoneproject.model.Ticket;
import com.g4.capstoneproject.service.PrescriptionService;
import com.g4.capstoneproject.service.S3Service;
import com.g4.capstoneproject.service.TreatmentPlanService;
import com.g4.capstoneproject.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho các thao tác AJAX
 * Chức năng: Cung cấp API endpoints cho frontend gọi qua JavaScript
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private TreatmentPlanService treatmentPlanService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private S3Service s3Service;

    // ==================== PRESCRIPTION APIs ====================

    /**
     * GET /api/prescriptions - Lấy tất cả đơn thuốc
     */
    @GetMapping("/prescriptions")
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    /**
     * GET /api/prescriptions/{id} - Lấy đơn thuốc theo ID
     */
    @GetMapping("/prescriptions/{id}")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable String id) {
        Prescription prescription = prescriptionService.getPrescriptionById(id);
        if (prescription != null) {
            return ResponseEntity.ok(prescription);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/prescriptions - Tạo đơn thuốc mới
     */
    @PostMapping("/prescriptions")
    public ResponseEntity<Prescription> createPrescription(@RequestBody Prescription prescription) {
        Prescription created = prescriptionService.createPrescription(prescription);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/prescriptions/{id} - Cập nhật đơn thuốc
     */
    @PutMapping("/prescriptions/{id}")
    public ResponseEntity<Prescription> updatePrescription(@PathVariable String id, @RequestBody Prescription prescription) {
        Prescription updated = prescriptionService.updatePrescription(id, prescription);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /api/prescriptions/{id} - Xóa đơn thuốc
     */
    @DeleteMapping("/prescriptions/{id}")
    public ResponseEntity<Map<String, String>> deletePrescription(@PathVariable String id) {
        boolean deleted = prescriptionService.deletePrescription(id);
        Map<String, String> response = new HashMap<>();
        if (deleted) {
            response.put("message", "Đã xóa đơn thuốc thành công");
            return ResponseEntity.ok(response);
        }
        response.put("message", "Không tìm thấy đơn thuốc");
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/prescriptions/stats - Thống kê đơn thuốc
     */
    @GetMapping("/prescriptions/stats")
    public ResponseEntity<Map<String, Object>> getPrescriptionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", prescriptionService.getTotalCount());
        stats.put("pending", prescriptionService.getPendingCount());
        stats.put("warnings", prescriptionService.getWarningCount());
        return ResponseEntity.ok(stats);
    }

    // ==================== TREATMENT PLAN APIs ====================

    /**
     * GET /api/treatment-plans - Lấy tất cả lộ trình điều trị
     */
    @GetMapping("/treatment-plans")
    public ResponseEntity<List<TreatmentPlan>> getAllTreatmentPlans() {
        return ResponseEntity.ok(treatmentPlanService.getAllTreatmentPlans());
    }

    /**
     * GET /api/treatment-plans/{id} - Lấy lộ trình theo ID
     */
    @GetMapping("/treatment-plans/{id}")
    public ResponseEntity<TreatmentPlan> getTreatmentPlanById(@PathVariable String id) {
        TreatmentPlan plan = treatmentPlanService.getTreatmentPlanById(id);
        if (plan != null) {
            return ResponseEntity.ok(plan);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/treatment-plans - Tạo lộ trình mới
     */
    @PostMapping("/treatment-plans")
    public ResponseEntity<TreatmentPlan> createTreatmentPlan(@RequestBody TreatmentPlan plan) {
        TreatmentPlan created = treatmentPlanService.createTreatmentPlan(plan);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/treatment-plans/{id} - Cập nhật lộ trình
     */
    @PutMapping("/treatment-plans/{id}")
    public ResponseEntity<TreatmentPlan> updateTreatmentPlan(@PathVariable String id, @RequestBody TreatmentPlan plan) {
        TreatmentPlan updated = treatmentPlanService.updateTreatmentPlan(id, plan);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /api/treatment-plans/{id} - Xóa lộ trình
     */
    @DeleteMapping("/treatment-plans/{id}")
    public ResponseEntity<Map<String, String>> deleteTreatmentPlan(@PathVariable String id) {
        boolean deleted = treatmentPlanService.deleteTreatmentPlan(id);
        Map<String, String> response = new HashMap<>();
        if (deleted) {
            response.put("message", "Đã xóa lộ trình điều trị thành công");
            return ResponseEntity.ok(response);
        }
        response.put("message", "Không tìm thấy lộ trình");
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/treatment-plans/stats - Thống kê lộ trình
     */
    @GetMapping("/treatment-plans/stats")
    public ResponseEntity<Map<String, Object>> getTreatmentPlanStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active", treatmentPlanService.getActiveCount());
        stats.put("upcomingFollowUps", treatmentPlanService.getUpcomingFollowUpCount());
        stats.put("total", treatmentPlanService.getAllTreatmentPlans().size());
        return ResponseEntity.ok(stats);
    }

    // ==================== TICKET APIs ====================

    /**
     * GET /api/tickets - Lấy tất cả tickets
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    /**
     * GET /api/tickets/{id} - Lấy ticket theo ID
     */
    @GetMapping("/tickets/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable String id) {
        Ticket ticket = ticketService.getTicketById(id);
        if (ticket != null) {
            return ResponseEntity.ok(ticket);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/tickets - Tạo ticket mới
     */
    @PostMapping("/tickets")
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        Ticket created = ticketService.createTicket(ticket);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/tickets/{id} - Cập nhật ticket
     */
    @PutMapping("/tickets/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable String id, @RequestBody Ticket ticket) {
        Ticket updated = ticketService.updateTicket(id, ticket);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * PUT /api/tickets/{id}/status - Thay đổi trạng thái ticket
     */
    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<Ticket> updateTicketStatus(@PathVariable String id, @RequestBody Map<String, String> statusUpdate) {
        Ticket ticket = ticketService.getTicketById(id);
        if (ticket != null) {
            ticket.setStatus(statusUpdate.get("status"));
            return ResponseEntity.ok(ticket);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/tickets/stats - Thống kê tickets
     */
    @GetMapping("/tickets/stats")
    public ResponseEntity<Map<String, Object>> getTicketStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("open", ticketService.getOpenCount());
        stats.put("inProgress", ticketService.getInProgressCount());
        stats.put("highPriority", ticketService.getHighPriorityCount());
        stats.put("total", ticketService.getAllTickets().size());
        return ResponseEntity.ok(stats);
    }

    // ==================== AI APIs (Mock) ====================

    /**
     * POST /api/ai/suggest-medication - Gợi ý thuốc thay thế
     */
    @PostMapping("/ai/suggest-medication")
    public ResponseEntity<Map<String, Object>> suggestMedication(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("suggestions", List.of(
            Map.of("name", "Losartan 50mg", "reason", "Hiệu quả tương đương, ít tác dụng phụ hơn"),
            Map.of("name", "Telmisartan 40mg", "reason", "Thời gian tác dụng dài hơn, 1 lần/ngày")
        ));
        response.put("needsConfirmation", true);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/check-interactions - Kiểm tra tương tác thuốc
     */
    @PostMapping("/ai/check-interactions")
    public ResponseEntity<Map<String, Object>> checkDrugInteractions(@RequestBody List<String> medications) {
        Map<String, Object> response = new HashMap<>();
        response.put("hasInteraction", medications.size() > 5);
        response.put("severity", "Moderate");
        response.put("warning", "Warfarin và Aspirin có thể tăng nguy cơ chảy máu. Theo dõi INR chặt chẽ.");
        response.put("recommendations", "Xem xét giảm liều hoặc thay thế thuốc");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/calculate-risk - Tính toán rủi ro sức khỏe
     */
    @PostMapping("/ai/calculate-risk")
    public ResponseEntity<Map<String, Object>> calculateHealthRisk(@RequestBody Map<String, Object> patientData) {
        Map<String, Object> response = new HashMap<>();
        response.put("strokeRisk10Year", 18.4);
        response.put("cvdRisk10Year", 15.2);
        response.put("riskLevel", "HIGH");
        response.put("mainFactors", List.of(
            Map.of("factor", "Huyết áp cao", "contribution", 6.8),
            Map.of("factor", "Đái tháo đường", "contribution", 4.5),
            Map.of("factor", "Hút thuốc", "contribution", 2.9)
        ));
        response.put("needsConfirmation", true);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/ai/training-status - Trạng thái huấn luyện AI
     */
    @GetMapping("/ai/training-status")
    public ResponseEntity<Map<String, Object>> getAiTrainingStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("accuracy", 94.2);
        response.put("documentsProcessed", 1247);
        response.put("documentsPending", 42);
        response.put("lastUpdate", "2026-01-22T10:30:00");
        return ResponseEntity.ok(response);
    }

    // ==================== FILE UPLOAD API ====================

    /**
     * POST /api/upload - Upload file lên AWS S3
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = s3Service.uploadFile(file);
            return ResponseEntity.ok("Upload thành công: " + fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi upload file");
        }
    }
}
