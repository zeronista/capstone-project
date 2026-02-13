package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.Ticket.TicketCreateRequest;
import com.g4.capstoneproject.dto.Ticket.TicketDetailResponse;
import com.g4.capstoneproject.dto.Ticket.TicketMessageRequest;
import com.g4.capstoneproject.dto.Ticket.TicketResponse;
import com.g4.capstoneproject.entity.GoogleFormSyncRecord;
import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TicketMessage;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.MedicalReport;
import com.g4.capstoneproject.repository.GoogleFormSyncRecordRepository;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.repository.MedicalReportRepository;
import com.g4.capstoneproject.service.PatientHealthProfileService;
import com.g4.capstoneproject.service.GoogleFormsSyncService;
import com.g4.capstoneproject.service.TicketService;
import com.g4.capstoneproject.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller cho Receptionist Dashboard - Phase 2
 * Handles all receptionist-specific routes with role-based authorization
 */
@Controller
@PreAuthorize("hasRole('RECEPTIONIST')")
@RequiredArgsConstructor
@Slf4j
public class ReceptionistController {

    private final TicketService ticketService;
    private final UserRepository userRepository;
    private final com.g4.capstoneproject.service.WebCallService webCallService;
    private final GoogleFormsSyncService googleFormsSyncService;
    private final PatientHealthProfileService patientHealthProfileService;
    private final MedicalReportRepository medicalReportRepository;
    private final S3Service s3Service;
    private final GoogleFormSyncRecordRepository googleFormSyncRecordRepository;

    /**
     * Survey Management - Quản lý khảo sát
     */
    @GetMapping("/receptionist/surveys")
    public String surveys(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "receptionist/surveys";
    }

    /**
     * Receptionist Dashboard
     */
    @GetMapping("/receptionist/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumberWithUserInfo(username).orElse(null);

        if (receptionist != null) {
            // Statistics
            List<Ticket> createdTickets = ticketService.getTicketsByCreatedByUserId(receptionist.getId());
            model.addAttribute("totalTickets", createdTickets.size());
            model.addAttribute("openTickets", createdTickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.OPEN)
                    .count());
            model.addAttribute("inProgressTickets", createdTickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.IN_PROGRESS)
                    .count());

            // Recent tickets
            model.addAttribute("recentTickets", createdTickets);
        }

        model.addAttribute("receptionist", receptionist);
        return "receptionist/dashboard";
    }

    /**
     * Appointments Management (placeholder)
     */
    @GetMapping("/receptionist/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "receptionist/appointments";
    }

    /**
     * Patients Management
     */
    @GetMapping("/receptionist/patients")
    public String patients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        // Get all patients (role = PATIENT)
        List<User> patients = userRepository.findByRole(User.UserRole.PATIENT);
        model.addAttribute("patients", patients);
        model.addAttribute("totalPatients", patients.size());

        model.addAttribute("receptionist", receptionist);
        return "receptionist/patients";
    }

    /**
     * Reminders / Follow-ups
     */
    @GetMapping("/receptionist/reminders")
    public String reminders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "receptionist/reminders";
    }

    /**
     * Tickets Management
     */
    @GetMapping("/receptionist/tickets")
    public String tickets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist != null) {
            List<Ticket> tickets = ticketService.getTicketsByCreatedByUserId(receptionist.getId());
            model.addAttribute("tickets", tickets);
            model.addAttribute("totalCount", tickets.size());
            model.addAttribute("openCount", tickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.OPEN)
                    .count());
        }

        model.addAttribute("receptionist", receptionist);
        return "receptionist/tickets";
    }

    /**
     * AI Callbot / Web-to-Web Demo
     */
    @GetMapping("/receptionist/callbot")
    public String callbot(Model model) {
        return "ai/web-call";
    }

    /**
     * User Management - Quản lý người dùng
     */
    @GetMapping("/receptionist/users")
    public String users(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "receptionist/users";
    }

    /**
     * Danh sach benh nhan thu thap tu Google Forms
     */
    @GetMapping("/receptionist/google-form-patients")
    public String googleFormPatients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "receptionist/google-form-patients";
    }

    /**
     * Fallback route to avoid noisy 404 logs when some client accidentally navigates to /receptionist/undefined.
     * Redirects gracefully to the receptionist dashboard.
     */
    @GetMapping("/receptionist/undefined")
    public String receptionistUndefinedRedirect() {
        return "redirect:/receptionist/dashboard";
    }

    /**
     * Patient Call Detail - Chi tiết bệnh nhân và gọi điện
     */
    @GetMapping("/receptionist/call-detail/{patientId}")
    public String callDetail(@PathVariable Long patientId, Model model) {
        model.addAttribute("patientId", patientId);
        return "ai/call-detail";
    }

    /**
     * Call History
     */
    @GetMapping("/receptionist/calls")
    public String calls(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("receptionist", receptionist);
        return "ai/calls";
    }

    // ==================== REST API ENDPOINTS ====================
    
    /**
     * API: Get web call logs of a specific patient
     * GET /api/receptionist/web-calls/{patientId}
     */
    @GetMapping("/api/receptionist/web-calls/{patientId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPatientWebCalls(@PathVariable Long patientId) {
        try {
            List<com.g4.capstoneproject.dto.WebCallDTO> calls = webCallService.getCallsByPatientId(patientId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calls", calls);
            response.put("total", calls.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * API: Get all patients (for dropdown)
     * GET /api/receptionist/patients
     */
    @GetMapping("/api/receptionist/patients")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getPatients() {
        List<User> patients = userRepository.findByRole(User.UserRole.PATIENT);

        List<Map<String, Object>> result = patients.stream()
                .map(patient -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", patient.getId());
                    map.put("fullName",
                            patient.getUserInfo() != null ? patient.getUserInfo().getFullName() : patient.getEmail());
                    map.put("email", patient.getEmail());
                    map.put("phoneNumber", patient.getPhoneNumber());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * API: Chi tiết hồ sơ bệnh nhân cho lễ tân
     * GET /api/receptionist/patients/{id}/detail
     */
    @GetMapping("/api/receptionist/patients/{id}/detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPatientDetailForReceptionist(@PathVariable Long id) {
        try {
            User patient = userRepository.findByIdWithUserInfo(id).orElse(null);
            if (patient == null || patient.getRole() != User.UserRole.PATIENT) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Không tìm thấy bệnh nhân");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("id", patient.getId());
            patientInfo.put("fullName", patient.getFullName());
            patientInfo.put("email", patient.getEmail());
            patientInfo.put("phoneNumber", patient.getPhoneNumber());
            patientInfo.put("dateOfBirth", patient.getDateOfBirth());
            patientInfo.put("gender", patient.getGender() != null ? patient.getGender().name() : null);
            patientInfo.put("address", patient.getAddress());
            // Generate presigned avatar URL if available
            String avatarUrl = null;
            String avatarKey = patient.getAvatarUrl();
            if (avatarKey != null && !avatarKey.isEmpty()) {
                try {
                    avatarUrl = s3Service.generatePresignedUrl(avatarKey);
                } catch (Exception ex) {
                    org.slf4j.LoggerFactory.getLogger(ReceptionistController.class)
                            .warn("Could not generate presigned avatar URL for patient {}: {}", patient.getId(), ex.getMessage());
                }
            }
            patientInfo.put("avatarUrl", avatarUrl);

            // Health profile (blood type, height, weight, allergies, chronic diseases)
            Map<String, Object> healthProfileMap = new HashMap<>();
            patientHealthProfileService.getByUserId(patient.getId()).ifPresent(profile -> {
                healthProfileMap.put("bloodType", profile.getBloodType());
                healthProfileMap.put("heightCm", profile.getHeightCm());
                healthProfileMap.put("weightKg", profile.getWeightKg());
                healthProfileMap.put("allergies", profile.getAllergies());
                healthProfileMap.put("chronicDiseases", profile.getChronicDiseases());
            });

            // Visit count: combine consultation medical reports and web call logs
            long consultationCount = medicalReportRepository
                    .findByPatientIdAndTypeOrderByReportDateDesc(patient.getId(), MedicalReport.ReportType.CONSULTATION)
                    .size();
            long callCount = webCallService.getCallsByPatientId(patient.getId()).size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("visitCount", consultationCount + callCount);
            stats.put("callCount", callCount);

            // Lab test results
            List<MedicalReport> labReports = medicalReportRepository
                    .findByPatientIdAndTypeOrderByReportDateDesc(patient.getId(), MedicalReport.ReportType.LAB_TEST);

            List<Map<String, Object>> labReportDtos = labReports.stream().map(report -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", report.getId());
                map.put("title", report.getTitle());
                map.put("reportDate", report.getReportDate());
                map.put("notes", report.getNotes());
                map.put("fileUrl", report.getFileUrl());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("patient", patientInfo);
            data.put("healthProfile", healthProfileMap.isEmpty() ? null : healthProfileMap);
            data.put("stats", stats);
            data.put("labReports", labReportDtos);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi tải hồ sơ bệnh nhân: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * API: Get all doctors (for dropdown)
     * GET /api/receptionist/doctors
     */
    @GetMapping("/api/receptionist/doctors")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getDoctors() {
        List<User> doctors = userRepository.findByRole(User.UserRole.DOCTOR);

        List<Map<String, Object>> result = doctors.stream()
                .map(doctor -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", doctor.getId());
                    map.put("fullName",
                            doctor.getUserInfo() != null ? doctor.getUserInfo().getFullName() : doctor.getEmail());
                    map.put("email", doctor.getEmail());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * API: Lay danh sach benh nhan dong bo tu Google Forms
     * GET /api/receptionist/google-form-patients
     */
    @GetMapping("/api/receptionist/google-form-patients")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGoogleFormPatients(
            @RequestParam(defaultValue = "100") int limit) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> patients = googleFormsSyncService.getRecentSyncedPatients(limit);
        response.put("success", true);
        response.put("patients", patients);
        response.put("total", patients.size());
        return ResponseEntity.ok(response);
    }

    /**
     * API: Lay chi tiet noi dung khao sat tu ban ghi Google Forms
     * GET /api/receptionist/google-form-patients/{syncRecordId}
     */
    @GetMapping("/api/receptionist/google-form-patients/{syncRecordId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGoogleFormPatientDetail(@PathVariable Long syncRecordId) {
        try {
            Map<String, Object> result = googleFormsSyncService.getSurveyDetail(syncRecordId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * API: Cap nhat trang thai goi cho ban ghi Google Forms (placeholder)
     * PUT /api/receptionist/google-form-patients/{syncRecordId}/call-status
     */
    @PutMapping("/api/receptionist/google-form-patients/{syncRecordId}/call-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateGoogleFormCallStatus(
            @PathVariable Long syncRecordId,
            @RequestParam GoogleFormSyncRecord.CallStatus status) {
        try {
            Map<String, Object> result = googleFormsSyncService.updateCallStatus(syncRecordId, status);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * API: Convert Google Form patient to system patient
     * POST /api/receptionist/google-form-patients/{syncRecordId}/convert
     */
    @PostMapping("/api/receptionist/google-form-patients/{syncRecordId}/convert")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> convertGoogleFormPatientToSystemPatient(@PathVariable Long syncRecordId) {
        try {
            GoogleFormSyncRecord record = googleFormSyncRecordRepository.findById(syncRecordId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi đồng bộ: " + syncRecordId));

            User patient = record.getPatient();
            if (patient == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Bản ghi này chưa có thông tin bệnh nhân"));
            }

            // Xóa GoogleFormSyncRecord để patient này xuất hiện trong patient list
            // Giữ lại MedicalReport để lưu thông tin triệu chứng từ Google Form
            googleFormSyncRecordRepository.delete(record);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã chuyển đổi bệnh nhân thành công");
            response.put("patientId", patient.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error converting Google Form patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * API: Trigger dong bo du lieu tu Google Forms
     * POST /api/receptionist/google-form-patients/sync
     */
    @PostMapping("/api/receptionist/google-form-patients/sync")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> syncGoogleFormPatients() {
        try {
            Map<String, Object> result = googleFormsSyncService.syncPatientsFromConfiguredForms("manual");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Loi dong bo Google Forms: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * API: Get all tickets created by receptionist
     * GET /api/receptionist/tickets
     */
    @GetMapping("/api/receptionist/tickets")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTickets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Ticket.Status status,
            @RequestParam(required = false) Ticket.Priority priority) {

        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Ticket> tickets = ticketService.getTicketsByCreatedByUserId(receptionist.getId());

        // Apply filters
        if (status != null) {
            tickets = tickets.stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        }
        if (priority != null) {
            tickets = tickets.stream()
                    .filter(t -> t.getPriority() == priority)
                    .collect(Collectors.toList());
        }

        List<TicketResponse> ticketResponses = tickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("tickets", ticketResponses);
        response.put("total", ticketResponses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Get ticket detail
     * GET /api/receptionist/tickets/{id}
     */
    @GetMapping("/api/receptionist/tickets/{id}")
    @ResponseBody
    public ResponseEntity<TicketDetailResponse> getTicketDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());

        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this ticket was created by current receptionist
        if (ticket.getCreatedBy() == null || !ticket.getCreatedBy().getId().equals(receptionist.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TicketMessage> messages = ticketService.getTicketMessages(id);
        TicketDetailResponse response = TicketDetailResponse.fromEntity(ticket, messages);

        return ResponseEntity.ok(response);
    }

    /**
     * API: Create new ticket
     * POST /api/receptionist/tickets
     */
    @PostMapping("/api/receptionist/tickets")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTicket(
            @Valid @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get patient
        User patient = userRepository.findById(request.getPatientId()).orElse(null);
        if (patient == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Không tìm thấy bệnh nhân");
            return ResponseEntity.badRequest().body(error);
        }

        // Get assigned doctor if specified
        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId()).orElse(null);
        }

        // Create ticket
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Ticket.Priority.MEDIUM)
                .category(request.getCategory() != null ? request.getCategory() : Ticket.Category.OTHER)
                .status(assignedTo != null ? Ticket.Status.ASSIGNED : Ticket.Status.OPEN)
                .patient(patient)
                .createdBy(receptionist)
                .assignedTo(assignedTo)
                .build();

        Ticket savedTicket = ticketService.createTicket(ticket);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tạo ticket thành công");
        response.put("ticket", TicketResponse.fromEntity(savedTicket));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Update ticket
     * PUT /api/receptionist/tickets/{id}
     */
    @PutMapping("/api/receptionist/tickets/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());

        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this ticket was created by current receptionist
        if (ticket.getCreatedBy() == null || !ticket.getCreatedBy().getId().equals(receptionist.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Bạn không có quyền chỉnh sửa ticket này");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Only allow editing if ticket is OPEN or ASSIGNED
        if (ticket.getStatus() != Ticket.Status.OPEN && ticket.getStatus() != Ticket.Status.ASSIGNED) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Không thể chỉnh sửa ticket đã được xử lý");
            return ResponseEntity.badRequest().body(error);
        }

        // Update patient if changed
        if (request.getPatientId() != null) {
            User patient = userRepository.findById(request.getPatientId()).orElse(null);
            if (patient != null) {
                ticket.setPatient(patient);
            }
        }

        // Update assigned doctor
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId()).orElse(null);
            if (assignedTo != null) {
                ticket.setAssignedTo(assignedTo);
                if (ticket.getStatus() == Ticket.Status.OPEN) {
                    ticket.setStatus(Ticket.Status.ASSIGNED);
                }
            }
        } else {
            ticket.setAssignedTo(null);
            ticket.setStatus(Ticket.Status.OPEN);
        }

        // Update other fields
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : ticket.getPriority());
        ticket.setCategory(request.getCategory() != null ? request.getCategory() : ticket.getCategory());

        Ticket updatedTicket = ticketService.updateTicket(ticket);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật ticket thành công");
        response.put("ticket", TicketResponse.fromEntity(updatedTicket));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Delete ticket
     * DELETE /api/receptionist/tickets/{id}
     */
    @DeleteMapping("/api/receptionist/tickets/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());

        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this ticket was created by current receptionist
        if (ticket.getCreatedBy() == null || !ticket.getCreatedBy().getId().equals(receptionist.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Bạn không có quyền xóa ticket này");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Only allow deleting if ticket is OPEN
        if (ticket.getStatus() != Ticket.Status.OPEN) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Chỉ có thể xóa ticket chưa được xử lý");
            return ResponseEntity.badRequest().body(error);
        }

        ticketService.deleteTicket(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa ticket thành công");

        return ResponseEntity.ok(response);
    }

    /**
     * API: Add message to ticket
     * POST /api/receptionist/tickets/{id}/messages
     */
    @PostMapping("/api/receptionist/tickets/{id}/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addTicketMessage(
            @PathVariable Long id,
            @Valid @RequestBody TicketMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User receptionist = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (receptionist == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());

        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this ticket was created by current receptionist
        if (ticket.getCreatedBy() == null || !ticket.getCreatedBy().getId().equals(receptionist.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Bạn không có quyền gửi tin nhắn trong ticket này");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Create message
        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .sender(receptionist)
                .messageText(request.getMessageText())
                .messageType(
                        request.getMessageType() != null ? request.getMessageType() : TicketMessage.MessageType.TEXT)
                .attachmentUrl(request.getAttachmentUrl())
                .isInternalNote(request.getIsInternalNote() != null ? request.getIsInternalNote() : false)
                .build();

        TicketMessage savedMessage = ticketService.addMessage(id, message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Gửi tin nhắn thành công");
        response.put("data", TicketDetailResponse.TicketMessageDTO.fromEntity(savedMessage));

        return ResponseEntity.ok(response);
    }
}
