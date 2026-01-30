package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.Precription.PrescriptionCreateRequest;
import com.g4.capstoneproject.dto.Precription.PrescriptionDetailResponse;
import com.g4.capstoneproject.dto.Precription.PrescriptionResponse;
import com.g4.capstoneproject.dto.Ticket.TicketDetailResponse;
import com.g4.capstoneproject.dto.Ticket.TicketMessageRequest;
import com.g4.capstoneproject.dto.Ticket.TicketResponse;
import com.g4.capstoneproject.dto.Ticket.TicketStatusUpdateRequest;
import com.g4.capstoneproject.dto.TreatmentPlanResponse;
import com.g4.capstoneproject.dto.TreatmentPlanDetailResponse;
import com.g4.capstoneproject.dto.HealthForecastResponse;
import com.g4.capstoneproject.dto.HealthForecastDetailResponse;
import com.g4.capstoneproject.dto.response.KnowledgeArticleResponse;
import com.g4.capstoneproject.dto.response.KnowledgeArticleDetailResponse;
import com.g4.capstoneproject.dto.response.KnowledgeCategoryResponse;
import com.g4.capstoneproject.entity.KnowledgeArticle.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.g4.capstoneproject.entity.CheckupSchedule;
import com.g4.capstoneproject.entity.HealthForecast;
import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TicketMessage;
import com.g4.capstoneproject.entity.TreatmentPlan;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.PrescriptionService;
import com.g4.capstoneproject.service.TicketService;
import com.g4.capstoneproject.service.TreatmentPlanService;
import com.g4.capstoneproject.service.HealthForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller cho Doctor Dashboard - Phase 2
 * Handles all doctor-specific routes with role-based authorization
 */
@Controller
@RequestMapping("/doctor")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorController {

    private final PrescriptionService prescriptionService;
    private final TicketService ticketService;
    private final HealthForecastService healthForecastService;
    private final TreatmentPlanService treatmentPlanService;
    private final UserRepository userRepository;
    private final com.g4.capstoneproject.service.KnowledgeArticleService knowledgeArticleService;
    private final com.g4.capstoneproject.service.KnowledgeCategoryService knowledgeCategoryService;

    /**
     * Doctor Dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor != null) {
            // Statistics
            model.addAttribute("totalPrescriptions",
                    prescriptionService.getPrescriptionsByDoctorId(doctor.getId()).size());
            model.addAttribute("activeTreatmentPlans",
                    treatmentPlanService.getTreatmentPlansByDoctorId(doctor.getId()).size());
            model.addAttribute("pendingTickets", ticketService.getTicketsByAssignedUserId(doctor.getId()).size());

            // Recent data
            List<Prescription> recentPrescriptions = prescriptionService.getPrescriptionsByDoctorId(doctor.getId());
            List<TreatmentPlan> activePlans = treatmentPlanService.getTreatmentPlansByDoctorId(doctor.getId());
            List<Ticket> assignedTickets = ticketService.getTicketsByAssignedUserId(doctor.getId());

            model.addAttribute("recentPrescriptions", recentPrescriptions);
            model.addAttribute("activePlans", activePlans);
            model.addAttribute("tickets", assignedTickets);
        }

        model.addAttribute("doctor", doctor);
        return "doctor/dashboard";
    }

    /**
     * Patients List
     */
    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor != null) {
            // Get patients from treatment plans
            List<TreatmentPlan> plans = treatmentPlanService.getTreatmentPlansByDoctorId(doctor.getId());
            model.addAttribute("treatmentPlans", plans);
        }

        model.addAttribute("doctor", doctor);
        return "doctor/patients";
    }

    /**
     * Appointments (placeholder for future implementation)
     */
    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("doctor", doctor);
        return "doctor/appointments";
    }

    /**
     * Prescriptions Management
     */
    @GetMapping("/prescriptions")
    public String prescriptions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor != null) {
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctor.getId());
            model.addAttribute("prescriptions", prescriptions);
            model.addAttribute("totalCount", prescriptions.size());
            model.addAttribute("activeCount", prescriptions.stream()
                    .filter(p -> p.getStatus() == Prescription.PrescriptionStatus.ACTIVE)
                    .count());
        }

        model.addAttribute("doctor", doctor);
        return "doctor/prescriptions";
    }

    /**
     * Create New Prescription Form
     */
    @GetMapping("/prescriptions/create")
    public String createPrescriptionForm(@RequestParam(required = false) Long patientId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor != null && patientId != null) {
            User patient = userRepository.findById(patientId).orElse(null);
            model.addAttribute("patient", patient);

            // Get patient's active treatment plan
            List<TreatmentPlan> plans = treatmentPlanService.getTreatmentPlansByPatientId(patientId);
            if (!plans.isEmpty()) {
                model.addAttribute("activePlan", plans.get(0));
            }
        }

        model.addAttribute("doctor", doctor);
        return "doctor/prescriptions/create";
    }

    /**
     * Treatment Plans Management
     */
    @GetMapping("/treatments")
    public String treatments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor != null) {
            List<TreatmentPlan> plans = treatmentPlanService.getTreatmentPlansByDoctorId(doctor.getId());
            model.addAttribute("treatmentPlans", plans);
            model.addAttribute("totalCount", plans.size());
            model.addAttribute("activeCount", treatmentPlanService.getActiveTreatmentPlans().size());
        }

        model.addAttribute("doctor", doctor);
        return "doctor/treatments";
    }

    /**
     * Tickets Management
     */
    @GetMapping("/tickets")
    public String tickets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor != null) {
            List<Ticket> tickets = ticketService.getTicketsByAssignedUserId(doctor.getId());
            model.addAttribute("tickets", tickets);
            model.addAttribute("totalCount", tickets.size());
            model.addAttribute("openCount", tickets.stream()
                    .filter(t -> t.getStatus() == Ticket.Status.OPEN)
                    .count());
        }

        model.addAttribute("doctor", doctor);
        return "doctor/tickets";
    }

    /**
     * Medical Records
     */
    @GetMapping("/medical-records")
    public String medicalRecords(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("doctor", doctor);
        return "doctor/medical-records";
    }

    /**
     * Knowledge Base
     */
    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        return "doctor/knowledge";
    }

    // ==================== REST API ENDPOINTS ====================

    /**
     * API: Get all tickets assigned to the doctor
     * GET /api/doctor/tickets
     */
    @GetMapping("/api/doctor/tickets")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTickets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Ticket.Status status,
            @RequestParam(required = false) Ticket.Priority priority) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get tickets assigned to this doctor
        List<Ticket> tickets = ticketService.getTicketsByAssignedUserId(doctor.getId());

        // Apply filters if provided
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

        // Convert to DTOs
        List<TicketResponse> ticketResponses = tickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("tickets", ticketResponses);
        response.put("total", ticketResponses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Get ticket detail by ID
     * GET /api/doctor/tickets/{id}
     */
    @GetMapping("/api/doctor/tickets/{id}")
    @ResponseBody
    public ResponseEntity<TicketDetailResponse> getTicketDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());

        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this ticket is assigned to the current doctor
        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get ticket messages
        List<TicketMessage> messages = ticketService.getTicketMessages(id);

        TicketDetailResponse response = TicketDetailResponse.fromEntity(ticket, messages);

        return ResponseEntity.ok(response);
    }

    /**
     * API: Update ticket status
     * PUT /api/doctor/tickets/{id}/status
     */
    @PutMapping("/api/doctor/tickets/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());

        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this ticket is assigned to the current doctor
        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update status
        Ticket updatedTicket = ticketService.updateTicketStatus(id, request.getStatus(), doctor);

        // Add system message if note is provided
        if (request.getNote() != null && !request.getNote().isEmpty()) {
            TicketMessage systemMessage = TicketMessage.builder()
                    .ticket(updatedTicket)
                    .sender(doctor)
                    .messageText("Trạng thái thay đổi: " + request.getStatus() + ". " + request.getNote())
                    .messageType(TicketMessage.MessageType.SYSTEM)
                    .isInternalNote(false)
                    .build();
            ticketService.addMessage(id, systemMessage);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật trạng thái thành công");
        response.put("ticket", TicketResponse.fromEntity(updatedTicket));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Add message to ticket
     * POST /api/doctor/tickets/{id}/messages
     */
    @PostMapping("/api/doctor/tickets/{id}/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addTicketMessage(
            @PathVariable Long id,
            @Valid @RequestBody TicketMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());

        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this ticket is assigned to the current doctor
        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Create and save message
        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .sender(doctor)
                .messageText(request.getMessageText())
                .messageType(request.getMessageType())
                .attachmentUrl(request.getAttachmentUrl())
                .isInternalNote(request.getIsInternalNote())
                .build();

        TicketMessage savedMessage = ticketService.addMessage(id, message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Gửi tin nhắn thành công");
        response.put("data", TicketDetailResponse.TicketMessageDTO.fromEntity(savedMessage));

        return ResponseEntity.ok(response);
    }

    // ==================== PRESCRIPTION REST API ENDPOINTS ====================

    /**
     * API: Get all prescriptions created by the doctor
     * GET /api/doctor/prescriptions
     */
    @GetMapping("/api/doctor/prescriptions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPrescriptions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Prescription.PrescriptionStatus status,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get prescriptions created by this doctor
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctor.getId());

        // Apply filters if provided
        if (status != null) {
            prescriptions = prescriptions.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (patientId != null) {
            prescriptions = prescriptions.stream()
                    .filter(p -> p.getPatient() != null && p.getPatient().getId().equals(patientId))
                    .collect(Collectors.toList());
        }

        // Convert to DTOs
        List<PrescriptionResponse> prescriptionResponses = prescriptions.stream()
                .map(PrescriptionResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("prescriptions", prescriptionResponses);
        response.put("total", prescriptionResponses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Get prescription detail by ID
     * GET /api/doctor/prescriptions/{id}
     */
    @GetMapping("/api/doctor/prescriptions/{id}")
    @ResponseBody
    public ResponseEntity<PrescriptionDetailResponse> getPrescriptionDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Prescription prescription = prescriptionService.getPrescriptionById(id);

        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this prescription was created by the current doctor
        if (prescription.getDoctor() == null || !prescription.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PrescriptionDetailResponse response = PrescriptionDetailResponse.fromEntity(prescription);

        return ResponseEntity.ok(response);
    }

    /**
     * API: Create new prescription
     * POST /api/doctor/prescriptions
     */
    @PostMapping("/api/doctor/prescriptions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPrescription(
            @Valid @RequestBody PrescriptionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verify patient exists
        User patient = userRepository.findById(request.getPatientId()).orElse(null);
        if (patient == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không tìm thấy bệnh nhân");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Create prescription using service
        Prescription prescription = prescriptionService.createPrescriptionFromRequest(request, doctor, patient);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tạo đơn thuốc thành công");
        response.put("data", PrescriptionResponse.fromEntity(prescription));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Update existing prescription
     * PUT /api/doctor/prescriptions/{id}
     */
    @PutMapping("/api/doctor/prescriptions/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Prescription existingPrescription = prescriptionService.getPrescriptionById(id);

        if (existingPrescription == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this prescription was created by the current doctor
        if (existingPrescription.getDoctor() == null ||
                !existingPrescription.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update prescription using service
        Prescription updatedPrescription = prescriptionService.updatePrescriptionFromRequest(id, request, doctor);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật đơn thuốc thành công");
        response.put("data", PrescriptionResponse.fromEntity(updatedPrescription));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Get prescription history for a patient
     * GET /api/doctor/prescriptions/patient/{patientId}/history
     */
    @GetMapping("/api/doctor/prescriptions/patient/{patientId}/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPatientPrescriptionHistory(
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get prescription history
        List<Prescription> prescriptions = prescriptionService.getPrescriptionHistory(patientId);

        List<PrescriptionResponse> prescriptionResponses = prescriptions.stream()
                .map(PrescriptionResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("prescriptions", prescriptionResponses);
        response.put("total", prescriptionResponses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Check medication usage count for a patient
     * GET
     * /api/doctor/prescriptions/patient/{patientId}/medication/{medicineName}/usage
     */
    @GetMapping("/api/doctor/prescriptions/patient/{patientId}/medication/{medicineName}/usage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkMedicationUsage(
            @PathVariable Long patientId,
            @PathVariable String medicineName,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int usageCount = prescriptionService.checkMedicationUsageCount(patientId, medicineName);

        Map<String, Object> response = new HashMap<>();
        response.put("medicineName", medicineName);
        response.put("usageCount", usageCount);
        response.put("status",
                usageCount == 0 ? "Lần đầu kê đơn" : usageCount == 1 ? "Lần thứ 2" : "Đã kê " + usageCount + " lần");

        return ResponseEntity.ok(response);
    }

    // ==================== TREATMENT PLAN REST API ENDPOINTS ====================

    /**
     * API: Get all treatment plans created by the doctor
     * GET /api/doctor/treatments
     */
    @GetMapping("/api/doctor/treatments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTreatmentPlans(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) TreatmentPlan.PlanStatus status,
            @RequestParam(required = false) Long patientId) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get treatment plans created by this doctor
        List<TreatmentPlan> plans = treatmentPlanService.getTreatmentPlansByDoctorId(doctor.getId());

        // Apply filters if provided
        if (status != null) {
            plans = plans.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (patientId != null) {
            plans = plans.stream()
                    .filter(p -> p.getPatient() != null && p.getPatient().getId().equals(patientId))
                    .collect(Collectors.toList());
        }

        // Convert to DTOs and add checkup count
        List<TreatmentPlanResponse> planResponses = plans.stream()
                .map(plan -> {
                    TreatmentPlanResponse response = TreatmentPlanResponse.fromEntity(plan);
                    int checkupCount = treatmentPlanService.getCheckupSchedulesByPlanId(plan.getId()).size();
                    response.setCheckupCount(checkupCount);
                    return response;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("treatments", planResponses);
        response.put("total", planResponses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Get treatment plan detail by ID
     * GET /api/doctor/treatments/{id}
     */
    @GetMapping("/api/doctor/treatments/{id}")
    @ResponseBody
    public ResponseEntity<TreatmentPlanDetailResponse> getTreatmentPlanDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TreatmentPlan plan = treatmentPlanService.getTreatmentPlanById(id).orElse(null);

        if (plan == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if this treatment plan was created by the current doctor
        if (plan.getDoctor() == null || !plan.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get checkup schedules
        List<CheckupSchedule> checkups = treatmentPlanService.getCheckupSchedulesByPlanId(id);

        TreatmentPlanDetailResponse response = TreatmentPlanDetailResponse.fromEntity(plan, checkups);

        return ResponseEntity.ok(response);
    }

    /**
     * API: Update treatment plan status
     * PUT /api/doctor/treatments/{id}/status
     */
    @PutMapping("/api/doctor/treatments/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTreatmentPlanStatus(
            @PathVariable Long id,
            @RequestParam TreatmentPlan.PlanStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TreatmentPlan plan = treatmentPlanService.getTreatmentPlanById(id).orElse(null);

        if (plan == null) {
            return ResponseEntity.notFound().build();
        }

        // Check authorization
        if (plan.getDoctor() == null || !plan.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update status
        plan.setStatus(status);
        TreatmentPlan updatedPlan = treatmentPlanService.updateTreatmentPlan(id, plan);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật trạng thái thành công");
        response.put("treatment", TreatmentPlanResponse.fromEntity(updatedPlan));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Create checkup schedule for treatment plan
     * POST /api/doctor/treatments/{id}/checkups
     */
    @PostMapping("/api/doctor/treatments/{id}/checkups")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCheckupSchedule(
            @PathVariable Long id,
            @RequestBody Map<String, Object> checkupData,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TreatmentPlan plan = treatmentPlanService.getTreatmentPlanById(id).orElse(null);

        if (plan == null) {
            return ResponseEntity.notFound().build();
        }

        // Check authorization
        if (plan.getDoctor() == null || !plan.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Create checkup schedule
        CheckupSchedule checkup = CheckupSchedule.builder()
                .scheduledDate(LocalDate.parse((String) checkupData.get("scheduledDate")))
                .checkupType((String) checkupData.get("checkupType"))
                .notes((String) checkupData.get("notes"))
                .status(CheckupSchedule.CheckupStatus.SCHEDULED)
                .build();

        CheckupSchedule savedCheckup = treatmentPlanService.createCheckupSchedule(id, checkup, doctor);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tạo lịch tái khám thành công");
        response.put("checkup", TreatmentPlanDetailResponse.CheckupScheduleDTO.fromEntity(savedCheckup));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Update checkup schedule status
     * PUT /api/doctor/checkups/{checkupId}/status
     */
    @PutMapping("/api/doctor/checkups/{checkupId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCheckupStatus(
            @PathVariable Long checkupId,
            @RequestBody Map<String, Object> statusData,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CheckupSchedule checkup = treatmentPlanService.getCheckupScheduleById(checkupId);

        if (checkup == null) {
            return ResponseEntity.notFound().build();
        }

        // Check authorization
        if (checkup.getDoctor() == null || !checkup.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update status
        CheckupSchedule.CheckupStatus status = CheckupSchedule.CheckupStatus.valueOf((String) statusData.get("status"));
        LocalDate completedDate = statusData.get("completedDate") != null
                ? LocalDate.parse((String) statusData.get("completedDate"))
                : null;
        String resultSummary = (String) statusData.get("resultSummary");

        CheckupSchedule updatedCheckup = treatmentPlanService.updateCheckupScheduleStatus(
                checkupId, status, completedDate, resultSummary);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật trạng thái tái khám thành công");
        response.put("checkup", TreatmentPlanDetailResponse.CheckupScheduleDTO.fromEntity(updatedCheckup));

        return ResponseEntity.ok(response);
    }

    // ========================================
    // Health Forecast Management
    // ========================================

    /**
     * Page: Health Forecast Management
     * GET /doctor/health-forecast
     */
    @GetMapping("/health-forecast")
    public String healthForecastPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Add any model attributes needed for the page
        return "doctor/health-forecast";
    }

    /**
     * API: Get all health forecasts (with optional filters)
     * GET /api/doctor/forecasts
     */
    @GetMapping("/api/doctor/forecasts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getForecasts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String riskLevel,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<HealthForecast> forecasts;

        // Apply filters
        if (patientId != null) {
            forecasts = healthForecastService.getForecastsByPatientId(patientId);
        } else if ("HIGH_RISK".equals(riskLevel)) {
            forecasts = healthForecastService.getHighRiskForecasts();
        } else {
            // Get all forecasts created by this doctor
            String username = userDetails.getUsername();
            User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
            if (doctor != null) {
                forecasts = healthForecastService.getForecastsByPatientId(doctor.getId());
            } else {
                forecasts = List.of();
            }
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            HealthForecast.ForecastStatus forecastStatus = HealthForecast.ForecastStatus.valueOf(status);
            forecasts = forecasts.stream()
                    .filter(f -> f.getStatus() == forecastStatus)
                    .collect(Collectors.toList());
        }

        List<HealthForecastResponse> forecastResponses = forecasts.stream()
                .map(HealthForecastResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("forecasts", forecastResponses);
        response.put("total", forecastResponses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Get health forecast by ID
     * GET /api/doctor/forecasts/{id}
     */
    @GetMapping("/api/doctor/forecasts/{id}")
    @ResponseBody
    public ResponseEntity<HealthForecastDetailResponse> getForecastById(@PathVariable Long id) {
        HealthForecast forecast = healthForecastService.getForecastById(id);
        return ResponseEntity.ok(HealthForecastDetailResponse.fromEntity(forecast));
    }

    /**
     * API: Generate new health forecast for a patient
     * POST /api/doctor/forecasts/generate
     */
    @PostMapping("/api/doctor/forecasts/generate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateForecast(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long patientId = Long.valueOf(request.get("patientId").toString());

        try {
            HealthForecast forecast = healthForecastService.generateForecast(patientId, doctor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo dự báo sức khỏe thành công");
            response.put("forecast", HealthForecastDetailResponse.fromEntity(forecast));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tạo dự báo: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * API: Get trend analysis for a patient
     * GET /api/doctor/forecasts/trends/{patientId}
     */
    @GetMapping("/api/doctor/forecasts/trends/{patientId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTrends(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "90") int days) {

        try {
            Map<String, Object> trends = healthForecastService.analyzeTrends(patientId, days);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patientId", patientId);
            response.put("days", days);
            response.put("trends", trends);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi phân tích xu hướng: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * API: Get high-risk alerts
     * GET /api/doctor/forecasts/alerts
     */
    @GetMapping("/api/doctor/forecasts/alerts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHighRiskAlerts() {
        List<HealthForecast> highRiskForecasts = healthForecastService.getHighRiskForecasts();

        List<HealthForecastResponse> alerts = highRiskForecasts.stream()
                .map(HealthForecastResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("alerts", alerts);
        response.put("count", alerts.size());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Update forecast status
     * PUT /api/doctor/forecasts/{id}/status
     */
    @PutMapping("/api/doctor/forecasts/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateForecastStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData) {

        HealthForecast.ForecastStatus status = HealthForecast.ForecastStatus.valueOf(statusData.get("status"));
        HealthForecast forecast = healthForecastService.updateForecastStatus(id, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật trạng thái dự báo thành công");
        response.put("forecast", HealthForecastResponse.fromEntity(forecast));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Calculate Framingham score for a patient
     * GET /api/doctor/forecasts/framingham/{patientId}
     */
    @GetMapping("/api/doctor/forecasts/framingham/{patientId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculateFraminghamScore(@PathVariable Long patientId) {
        try {
            double score = healthForecastService.calculateFraminghamScore(patientId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patientId", patientId);
            response.put("framinghamScore", score);

            String riskLevel;
            if (score < 5)
                riskLevel = "LOW";
            else if (score < 10)
                riskLevel = "MODERATE";
            else if (score < 20)
                riskLevel = "HIGH";
            else
                riskLevel = "VERY_HIGH";

            response.put("riskLevel", riskLevel);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
