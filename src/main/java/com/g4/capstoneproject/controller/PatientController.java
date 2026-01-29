package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TreatmentPlan;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.PrescriptionRepository;
import com.g4.capstoneproject.repository.TicketRepository;
import com.g4.capstoneproject.repository.TreatmentPlanRepository;
import com.g4.capstoneproject.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("prescriptions", prescriptionCount);
            stats.put("treatments", treatmentCount);
            stats.put("tickets", ticketCount);
            stats.put("openTickets", openTicketCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting patient stats", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
}
