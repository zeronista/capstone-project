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
import com.g4.capstoneproject.entity.HealthForecast;
import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TicketMessage;
import com.g4.capstoneproject.entity.TreatmentPlan;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.repository.MedicalReportRepository;
import com.g4.capstoneproject.repository.FamilyMedicalHistoryRepository;
import com.g4.capstoneproject.repository.KnowledgeArticleRepository;
import com.g4.capstoneproject.repository.KnowledgeCategoryRepository;
import com.g4.capstoneproject.entity.KnowledgeArticle;
import com.g4.capstoneproject.entity.KnowledgeCategory;
import com.g4.capstoneproject.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.g4.capstoneproject.service.PrescriptionService;
import com.g4.capstoneproject.service.TicketService;
import com.g4.capstoneproject.service.TreatmentPlanService;
import com.g4.capstoneproject.service.HealthForecastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@Slf4j
public class DoctorController {

    private final PrescriptionService prescriptionService;
    private final TicketService ticketService;
    private final HealthForecastService healthForecastService;
    private final TreatmentPlanService treatmentPlanService;
    private final PatientService patientService;
    private final UserRepository userRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final FamilyMedicalHistoryRepository familyMedicalHistoryRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final KnowledgeCategoryRepository knowledgeCategoryRepository;

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
     * API: Get patients for dashboard with treatment plans
     * GET /doctor/api/patients
     */
    @GetMapping("/api/patients")
    @ResponseBody
    public ResponseEntity<?> getDashboardPatients(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Doctor not found"));
            }

            List<Map<String, Object>> result = new java.util.ArrayList<>();
            java.util.Set<Long> addedPatientIds = new java.util.HashSet<>();

            // 1. Get patients from treatment plans (with treatment info)
            List<TreatmentPlan> plans = treatmentPlanService.getTreatmentPlansByDoctorId(doctor.getId());
            for (TreatmentPlan plan : plans) {
                try {
                    User patient = plan.getPatient();
                    if (patient != null && !addedPatientIds.contains(patient.getId())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", patient.getId());
                        data.put("fullName", patient.getFullName() != null ? patient.getFullName() : "N/A");
                        data.put("email", patient.getEmail() != null ? patient.getEmail() : "N/A");
                        data.put("phone", patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A");
                        data.put("treatmentPlanId", plan.getId());
                        data.put("diagnosis", plan.getDiagnosis() != null ? plan.getDiagnosis() : "N/A");
                        data.put("status", plan.getStatus() != null ? plan.getStatus().name() : "DRAFT");
                        data.put("startDate", plan.getStartDate());
                        data.put("endDate", plan.getExpectedEndDate());
                        data.put("lastUpdated", plan.getUpdatedAt());
                        data.put("createdAt", plan.getCreatedAt());
                        result.add(data);
                        addedPatientIds.add(patient.getId());
                    }
                } catch (Exception e) {
                    // Log and skip this patient if there's an error
                    System.err.println("Error processing treatment plan " + plan.getId() + ": " + e.getMessage());
                }
            }

            // 2. If no treatment plans found, get all patients assigned to doctor
            if (result.isEmpty()) {
                List<User> patients = patientService.getPatientsByDoctorId(doctor.getId());
                for (User patient : patients) {
                    try {
                        if (!addedPatientIds.contains(patient.getId())) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", patient.getId());
                            data.put("fullName", patient.getFullName() != null ? patient.getFullName() : "N/A");
                            data.put("email", patient.getEmail() != null ? patient.getEmail() : "N/A");
                            data.put("phone", patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A");
                            data.put("treatmentPlanId", null);
                            data.put("diagnosis", null);
                            data.put("status", "DRAFT"); // Default status for patients without treatment plan
                            data.put("startDate", null);
                            data.put("endDate", null);
                            data.put("lastUpdated", patient.getUpdatedAt());
                            data.put("createdAt", patient.getCreatedAt());
                            result.add(data);
                            addedPatientIds.add(patient.getId());
                        }
                    } catch (Exception e) {
                        // Log and skip this patient if there's an error
                        System.err.println("Error processing patient " + patient.getId() + ": " + e.getMessage());
                    }
                }
            }

            // 3. If still empty, get all active patients in the system (for testing/demo)
            if (result.isEmpty()) {
                List<User> allPatients = patientService.getAllActivePatients();
                for (User patient : allPatients) {
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", patient.getId());
                        data.put("fullName", patient.getFullName() != null ? patient.getFullName() : "N/A");
                        data.put("email", patient.getEmail() != null ? patient.getEmail() : "N/A");
                        data.put("phone", patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A");
                        data.put("treatmentPlanId", null);
                        data.put("diagnosis", null);
                        data.put("status", "DRAFT");
                        data.put("startDate", null);
                        data.put("endDate", null);
                        data.put("lastUpdated", patient.getUpdatedAt());
                        data.put("createdAt", patient.getCreatedAt());
                        result.add(data);
                    } catch (Exception e) {
                        // Log and skip this patient if there's an error
                        System.err.println("Error processing patient " + patient.getId() + ": " + e.getMessage());
                    }
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error in getDashboardPatients: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load patients: " + e.getMessage()));
        }
    }

    /**
     * API: Get dashboard statistics
     * GET /doctor/api/stats
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> stats = new HashMap<>();

        List<TreatmentPlan> plans = treatmentPlanService.getTreatmentPlansByDoctorId(doctor.getId());
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctor.getId());
        List<Ticket> tickets = ticketService.getTicketsByAssignedUserId(doctor.getId());

        // Total patients count - from multiple sources
        long totalPatients = 0;
        if (!plans.isEmpty()) {
            totalPatients = plans.stream()
                    .map(p -> p.getPatient().getId())
                    .distinct()
                    .count();
        } else {
            // Fallback to patients assigned to doctor or all active patients
            List<User> patients = patientService.getPatientsByDoctorId(doctor.getId());
            if (patients.isEmpty()) {
                patients = patientService.getAllActivePatients();
            }
            totalPatients = patients.size();
        }

        // Count by status
        long activeCount = plans.stream()
                .filter(p -> p.getStatus() == TreatmentPlan.PlanStatus.ACTIVE)
                .count();

        long completedCount = plans.stream()
                .filter(p -> p.getStatus() == TreatmentPlan.PlanStatus.COMPLETED)
                .count();

        long pendingCount = plans.stream()
                .filter(p -> p.getStatus() == TreatmentPlan.PlanStatus.DRAFT)
                .count();

        // Urgent alerts (from tickets)
        long urgentCount = tickets.stream()
                .filter(t -> t.getPriority() == Ticket.Priority.URGENT && t.getStatus() == Ticket.Status.OPEN)
                .count();

        // Today's appointments (simplified - could be enhanced with actual appointment
        // entity)
        long todayAppointments = plans.stream()
                .filter(p -> {
                    LocalDate today = LocalDate.now();
                    return p.getStartDate() != null && p.getStartDate().equals(today);
                })
                .count();

        stats.put("totalPatients", totalPatients);
        stats.put("activePatients", activeCount);
        stats.put("completedPatients", completedCount);
        stats.put("pendingPatients", pendingCount);
        stats.put("urgentAlerts", urgentCount);
        stats.put("todayAppointments", todayAppointments);
        stats.put("totalPrescriptions", prescriptions.size());
        stats.put("pendingTickets", tickets.stream().filter(t -> t.getStatus() == Ticket.Status.OPEN).count());

        return ResponseEntity.ok(stats);
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
     * Patient Detail Page
     * GET /doctor/patients/{id}
     */
    @GetMapping("/patients/{id}")
    public String patientDetail(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);
        model.addAttribute("doctor", doctor);
        model.addAttribute("patientId", id);
        return "doctor/patient-detail";
    }

    /**
     * API: Get patient detail with all related data
     * GET /doctor/api/patients/{id}
     */
    @GetMapping("/api/patients/{id}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPatientDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Check authentication
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "User not authenticated"));
        }

        try {
            String username = userDetails.getUsername();
            log.info("Loading patient detail - PatientID: {}, RequestedBy: {}", id, username);

            User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

            if (doctor == null) {
                log.error("Doctor not found for username: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized", "message", "Doctor not found"));
            }

            // Get patient
            User patient = userRepository.findById(id).orElse(null);
            if (patient == null) {
                log.warn("Patient not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Not Found", "message", "Patient not found"));
            }

            Map<String, Object> result = new HashMap<>();

            // Basic patient info
            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("id", patient.getId());
            patientInfo.put("fullName", patient.getFullName() != null ? patient.getFullName() : "N/A");
            patientInfo.put("email", patient.getEmail() != null ? patient.getEmail() : "N/A");
            patientInfo.put("phone", patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A");
            patientInfo.put("dateOfBirth", patient.getDateOfBirth());
            patientInfo.put("gender", patient.getGender() != null ? patient.getGender().name() : null);
            patientInfo.put("address", patient.getAddress());
            patientInfo.put("avatarUrl", patient.getAvatarUrl());
            patientInfo.put("createdAt", patient.getCreatedAt());
            result.put("patient", patientInfo);

            // Treatment plans for this patient
            List<TreatmentPlan> plans = null;
            try {
                plans = treatmentPlanService.getTreatmentPlansByPatientId(id);
                log.debug("Found {} treatment plans for patient {}", plans != null ? plans.size() : 0, id);
            } catch (Exception e) {
                log.error("Error loading treatment plans for patient {}: {}", id, e.getMessage(), e);
                plans = List.of(); // Use empty list on error
            }

            List<Map<String, Object>> treatmentPlans = plans.stream().map(plan -> {
                Map<String, Object> planData = new HashMap<>();
                planData.put("id", plan.getId());
                planData.put("diagnosis", plan.getDiagnosis());
                planData.put("treatmentGoal", plan.getTreatmentGoal());
                planData.put("status", plan.getStatus() != null ? plan.getStatus().name() : "DRAFT");
                planData.put("startDate", plan.getStartDate());
                planData.put("expectedEndDate", plan.getExpectedEndDate());
                planData.put("createdAt", plan.getCreatedAt());
                // Safe access to lazy-loaded doctor
                try {
                    planData.put("doctorName", plan.getDoctor() != null ? plan.getDoctor().getFullName() : null);
                } catch (Exception e) {
                    log.warn("Could not load doctor name for treatment plan {}", plan.getId());
                    planData.put("doctorName", null);
                }
                return planData;
            }).collect(Collectors.toList());
            result.put("treatmentPlans", treatmentPlans);

            // Prescriptions for this patient
            List<Prescription> prescriptions = null;
            try {
                prescriptions = prescriptionService.getPrescriptionHistory(id);
                log.debug("Found {} prescriptions for patient {}", prescriptions != null ? prescriptions.size() : 0,
                        id);
            } catch (Exception e) {
                log.error("Error loading prescriptions for patient {}: {}", id, e.getMessage(), e);
                prescriptions = List.of(); // Use empty list on error
            }

            List<Map<String, Object>> prescriptionList = prescriptions.stream().map(rx -> {
                Map<String, Object> rxData = new HashMap<>();
                rxData.put("id", rx.getId());
                rxData.put("prescriptionCode", "RX-" + rx.getId());
                rxData.put("status", rx.getStatus() != null ? rx.getStatus().name() : "DRAFT");
                rxData.put("prescribedAt", rx.getPrescriptionDate());
                rxData.put("diagnosis", rx.getDiagnosis());
                rxData.put("notes", rx.getNotes());
                // Safe access to lazy-loaded doctor
                try {
                    rxData.put("doctorName", rx.getDoctor() != null ? rx.getDoctor().getFullName() : null);
                } catch (Exception e) {
                    log.warn("Could not load doctor name for prescription {}", rx.getId());
                    rxData.put("doctorName", null);
                }
                return rxData;
            }).collect(Collectors.toList());
            result.put("prescriptions", prescriptionList);

            // NOTE: VitalSigns entity removed in schema v4.0
            // Vital signs data now stored as JSONB in health_forecasts.vital_signs_snapshot
            result.put("vitalSigns", List.of()); // Empty list for backward compatibility

            // Tickets for this patient (using createdBy = patientId)
            List<Ticket> tickets = null;
            try {
                tickets = ticketService.getTicketsByCreatedByUserId(id);
                log.debug("Found {} tickets for patient {}", tickets != null ? tickets.size() : 0, id);
            } catch (Exception e) {
                log.error("Error loading tickets for patient {}: {}", id, e.getMessage(), e);
                tickets = List.of(); // Use empty list on error
            }

            List<Map<String, Object>> ticketList = tickets.stream().map(ticket -> {
                Map<String, Object> ticketData = new HashMap<>();
                ticketData.put("id", ticket.getId());
                ticketData.put("subject", ticket.getTitle());
                ticketData.put("status", ticket.getStatus() != null ? ticket.getStatus().name() : "OPEN");
                ticketData.put("priority", ticket.getPriority() != null ? ticket.getPriority().name() : "MEDIUM");
                ticketData.put("category", ticket.getCategory() != null ? ticket.getCategory().name() : "GENERAL");
                ticketData.put("createdAt", ticket.getCreatedAt());
                return ticketData;
            }).collect(Collectors.toList());
            result.put("tickets", ticketList);

            // Summary stats
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTreatmentPlans", plans.size());
            stats.put("activeTreatmentPlans",
                    plans.stream().filter(p -> p.getStatus() == TreatmentPlan.PlanStatus.ACTIVE).count());
            stats.put("totalPrescriptions", prescriptions.size());
            stats.put("totalVitalRecords", 0); // VitalSigns entity removed in schema v4.0
            stats.put("openTickets", tickets.stream().filter(t -> t.getStatus() == Ticket.Status.OPEN).count());
            result.put("stats", stats);

            log.info("Successfully loaded patient detail for patient {}", id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting patient detail for patient {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal Server Error",
                            "message", "Failed to load patient details: " + e.getMessage(),
                            "patientId", id));
        }
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
     * Edit Prescription Form
     */
    @GetMapping("/prescriptions/edit/{id}")
    public String editPrescriptionForm(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor != null) {
            Prescription prescription = prescriptionService.getPrescriptionById(id);
            if (prescription != null && prescription.getDoctor().getId().equals(doctor.getId())) {
                model.addAttribute("prescription", prescription);
                model.addAttribute("patient", prescription.getPatient());
                model.addAttribute("prescriptionId", id);
            }
        }

        model.addAttribute("doctor", doctor);
        return "doctor/prescriptions/edit";
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
     * Create New Treatment Plan Form
     */
    @GetMapping("/treatments/create")
    public String createTreatmentPlanForm(@RequestParam(required = false) Long patientId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor != null && patientId != null) {
            User patient = userRepository.findById(patientId).orElse(null);
            model.addAttribute("patient", patient);

            // Get patient's existing treatment plans
            List<TreatmentPlan> existingPlans = treatmentPlanService.getTreatmentPlansByPatientId(patientId);
            model.addAttribute("existingPlans", existingPlans);
        }

        model.addAttribute("doctor", doctor);
        return "doctor/treatments/create";
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
     * API: Delete prescription
     * DELETE /api/doctor/prescriptions/{id}
     */
    @DeleteMapping("/api/doctor/prescriptions/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePrescription(
            @PathVariable Long id,
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

        // Delete prescription using service
        boolean deleted = prescriptionService.deletePrescription(id);

        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("success", true);
            response.put("message", "Xóa đơn thuốc thành công");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Không thể xóa đơn thuốc");
            return ResponseEntity.badRequest().body(response);
        }
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

        // Convert to DTOs
        // NOTE: CheckupSchedule entity removed in schema v4.0
        List<TreatmentPlanResponse> planResponses = plans.stream()
                .map(plan -> {
                    TreatmentPlanResponse response = TreatmentPlanResponse.fromEntity(plan);
                    response.setCheckupCount(0); // CheckupSchedule removed
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

        // NOTE: CheckupSchedule entity removed in schema v4.0
        TreatmentPlanDetailResponse response = TreatmentPlanDetailResponse.fromEntity(plan);

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
     * API: Create new treatment plan
     * POST /api/doctor/treatments
     */
    @PostMapping("/api/doctor/treatments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTreatmentPlan(
            @RequestBody Map<String, Object> treatmentData,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get patient
        Long patientId = Long.valueOf(treatmentData.get("patientId").toString());
        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không tìm thấy bệnh nhân");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Create treatment plan
        TreatmentPlan plan = TreatmentPlan.builder()
                .patient(patient)
                .doctor(doctor)
                .diagnosis((String) treatmentData.get("diagnosis"))
                .treatmentGoal((String) treatmentData.get("treatmentGoal"))
                .startDate(treatmentData.get("startDate") != null
                        ? LocalDate.parse((String) treatmentData.get("startDate"))
                        : null)
                .expectedEndDate(treatmentData.get("expectedEndDate") != null
                        ? LocalDate.parse((String) treatmentData.get("expectedEndDate"))
                        : null)
                .status(treatmentData.get("status") != null
                        ? TreatmentPlan.PlanStatus.valueOf((String) treatmentData.get("status"))
                        : TreatmentPlan.PlanStatus.DRAFT)
                .build();

        TreatmentPlan savedPlan = treatmentPlanService.createTreatmentPlan(plan);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tạo lộ trình điều trị thành công");
        response.put("treatment", TreatmentPlanResponse.fromEntity(savedPlan));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Update treatment plan
     * PUT /api/doctor/treatments/{id}
     */
    @PutMapping("/api/doctor/treatments/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTreatmentPlan(
            @PathVariable Long id,
            @RequestBody Map<String, Object> treatmentData,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TreatmentPlan existingPlan = treatmentPlanService.getTreatmentPlanById(id).orElse(null);

        if (existingPlan == null) {
            return ResponseEntity.notFound().build();
        }

        // Check authorization
        if (existingPlan.getDoctor() == null || !existingPlan.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update fields
        existingPlan.setDiagnosis((String) treatmentData.get("diagnosis"));
        existingPlan.setTreatmentGoal((String) treatmentData.get("treatmentGoal"));
        if (treatmentData.get("startDate") != null) {
            existingPlan.setStartDate(LocalDate.parse((String) treatmentData.get("startDate")));
        }
        if (treatmentData.get("expectedEndDate") != null) {
            existingPlan.setExpectedEndDate(LocalDate.parse((String) treatmentData.get("expectedEndDate")));
        }
        if (treatmentData.get("status") != null) {
            existingPlan.setStatus(TreatmentPlan.PlanStatus.valueOf((String) treatmentData.get("status")));
        }

        TreatmentPlan updatedPlan = treatmentPlanService.updateTreatmentPlan(id, existingPlan);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật lộ trình thành công");
        response.put("treatment", TreatmentPlanResponse.fromEntity(updatedPlan));

        return ResponseEntity.ok(response);
    }

    /**
     * API: Delete treatment plan
     * DELETE /api/doctor/treatments/{id}
     */
    @DeleteMapping("/api/doctor/treatments/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTreatmentPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TreatmentPlan existingPlan = treatmentPlanService.getTreatmentPlanById(id).orElse(null);

        if (existingPlan == null) {
            return ResponseEntity.notFound().build();
        }

        // Check authorization
        if (existingPlan.getDoctor() == null || !existingPlan.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = treatmentPlanService.deleteTreatmentPlan(id);

        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("success", true);
            response.put("message", "Xóa lộ trình thành công");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Không thể xóa lộ trình");
            return ResponseEntity.badRequest().body(response);
        }
    }
    // ================== HEALTH FORECAST APIs ==================

    /**
     * API: Get Medical Reports for a patient
     * GET /api/doctor/health-forecast/reports/{patientId}
     */
    @GetMapping("/api/doctor/health-forecast/reports/{patientId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMedicalReports(@PathVariable Long patientId) {
        List<com.g4.capstoneproject.entity.MedicalReport> reports = medicalReportRepository
                .findByPatientIdOrderByReportDateDesc(patientId);

        List<Map<String, Object>> reportList = reports.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("type", r.getType().name());
            map.put("reportDate", r.getReportDate());
            map.put("title", r.getTitle());
            map.put("content", r.getContent());
            map.put("notes", r.getNotes());
            map.put("createdAt", r.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("reports", reportList);
        return ResponseEntity.ok(response);
    }

    /**
     * API: Create Medical Report
     * POST /api/doctor/health-forecast/reports
     */
    @PostMapping("/api/doctor/health-forecast/reports")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createMedicalReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> data) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        Long patientId = Long.valueOf(data.get("patientId").toString());
        User patient = userRepository.findById(patientId).orElse(null);

        if (patient == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Không tìm thấy bệnh nhân");
            return ResponseEntity.badRequest().body(error);
        }

        com.g4.capstoneproject.entity.MedicalReport report = com.g4.capstoneproject.entity.MedicalReport.builder()
                .patient(patient)
                .createdBy(doctor)
                .type(com.g4.capstoneproject.entity.MedicalReport.ReportType.valueOf(data.get("type").toString()))
                .reportDate(LocalDate.parse(data.get("reportDate").toString()))
                .title(data.get("title").toString())
                .content(data.get("content").toString())
                .notes(data.get("notes") != null ? data.get("notes").toString() : null)
                .build();

        report = medicalReportRepository.save(report);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lưu báo cáo y tế thành công");
        response.put("id", report.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * API: Delete Medical Report
     * DELETE /api/doctor/health-forecast/reports/{id}
     */
    @DeleteMapping("/api/doctor/health-forecast/reports/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMedicalReport(@PathVariable Long id) {
        medicalReportRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa báo cáo thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * API: Get Family History for a patient
     * GET /api/doctor/health-forecast/family-history/{patientId}
     */
    @GetMapping("/api/doctor/health-forecast/family-history/{patientId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFamilyHistory(@PathVariable Long patientId) {
        List<com.g4.capstoneproject.entity.FamilyMedicalHistory> history = familyMedicalHistoryRepository
                .findByPatientIdOrderByCreatedAtDesc(patientId);

        List<Map<String, Object>> historyList = history.stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", h.getId());
            map.put("relationship", h.getRelationship().name());
            map.put("condition", h.getCondition());
            map.put("ageAtDiagnosis", h.getAgeAtDiagnosis());
            map.put("status", h.getStatus() != null ? h.getStatus().name() : null);
            map.put("notes", h.getNotes());
            map.put("createdAt", h.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("history", historyList);
        return ResponseEntity.ok(response);
    }

    /**
     * API: Create Family History Entry
     * POST /api/doctor/health-forecast/family-history
     */
    @PostMapping("/api/doctor/health-forecast/family-history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createFamilyHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> data) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        Long patientId = Long.valueOf(data.get("patientId").toString());
        User patient = userRepository.findById(patientId).orElse(null);

        if (patient == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Không tìm thấy bệnh nhân");
            return ResponseEntity.badRequest().body(error);
        }

        com.g4.capstoneproject.entity.FamilyMedicalHistory history = com.g4.capstoneproject.entity.FamilyMedicalHistory
                .builder()
                .patient(patient)
                .createdBy(doctor)
                .relationship(com.g4.capstoneproject.entity.FamilyMedicalHistory.Relationship
                        .valueOf(data.get("relationship").toString()))
                .condition(data.get("condition").toString())
                .ageAtDiagnosis(data.get("ageAtDiagnosis") != null && !data.get("ageAtDiagnosis").toString().isEmpty()
                        ? Integer.valueOf(data.get("ageAtDiagnosis").toString())
                        : null)
                .status(data.get("status") != null
                        ? com.g4.capstoneproject.entity.FamilyMedicalHistory.MemberStatus
                                .valueOf(data.get("status").toString())
                        : null)
                .notes(data.get("notes") != null ? data.get("notes").toString() : null)
                .build();

        history = familyMedicalHistoryRepository.save(history);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lưu tiền sử gia đình thành công");
        response.put("id", history.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * API: Delete Family History Entry
     * DELETE /api/doctor/health-forecast/family-history/{id}
     */
    @DeleteMapping("/api/doctor/health-forecast/family-history/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFamilyHistory(@PathVariable Long id) {
        familyMedicalHistoryRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa tiền sử gia đình thành công");
        return ResponseEntity.ok(response);
    }

    // ==================== KNOWLEDGE BASE APIs ====================

    /**
     * Knowledge Base Page
     * GET /doctor/knowledge
     */
    @GetMapping("/knowledge")
    public String knowledgePage() {
        return "doctor/knowledge";
    }

    /**
     * API: Get Categories
     * GET /api/doctor/knowledge/categories
     */
    @GetMapping("/api/doctor/knowledge/categories")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getKnowledgeCategories() {
        List<KnowledgeCategory> categories = knowledgeCategoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
        List<Map<String, Object>> result = categories.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("description", c.getDescription());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * API: Get Statistics
     * GET /api/doctor/knowledge/statistics
     */
    @GetMapping("/api/doctor/knowledge/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getKnowledgeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", knowledgeArticleRepository.count());
        stats.put("published", knowledgeArticleRepository.countByStatus(KnowledgeArticle.ArticleStatus.PUBLISHED));
        stats.put("draft", knowledgeArticleRepository.countByStatus(KnowledgeArticle.ArticleStatus.DRAFT));
        stats.put("categories", knowledgeCategoryRepository.countByActiveTrue());
        return ResponseEntity.ok(stats);
    }

    /**
     * API: Get All Tags
     * GET /api/doctor/knowledge/tags
     */
    @GetMapping("/api/doctor/knowledge/tags")
    @ResponseBody
    public ResponseEntity<List<String>> getKnowledgeTags() {
        List<String> tags = knowledgeArticleRepository.findAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * API: Get Articles with Search & Pagination
     * GET /api/doctor/knowledge/articles
     */
    @GetMapping("/api/doctor/knowledge/articles")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getKnowledgeArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<KnowledgeArticle> articlePage;

        KnowledgeArticle.ArticleStatus articleStatus = null;
        if (status != null && !status.isEmpty()) {
            articleStatus = KnowledgeArticle.ArticleStatus.valueOf(status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            if (categoryId != null) {
                articlePage = knowledgeArticleRepository.searchByCategoryAndKeyword(categoryId, keyword.trim(),
                        articleStatus, pageable);
            } else {
                articlePage = knowledgeArticleRepository.searchByKeyword(keyword.trim(), articleStatus, pageable);
            }
        } else if (categoryId != null) {
            if (articleStatus != null) {
                articlePage = knowledgeArticleRepository.findByCategoryIdAndStatus(categoryId, articleStatus, pageable);
            } else {
                articlePage = knowledgeArticleRepository.findByCategoryId(categoryId, pageable);
            }
        } else if (articleStatus != null) {
            articlePage = knowledgeArticleRepository.findByStatus(articleStatus, pageable);
        } else {
            articlePage = knowledgeArticleRepository.findAll(pageable);
        }

        List<Map<String, Object>> content = articlePage.getContent().stream().map(this::mapArticleToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("number", articlePage.getNumber());
        response.put("totalPages", articlePage.getTotalPages());
        response.put("totalElements", articlePage.getTotalElements());
        response.put("size", articlePage.getSize());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Get Article Detail
     * GET /api/doctor/knowledge/articles/{id}
     */
    @GetMapping("/api/doctor/knowledge/articles/{id}")
    @ResponseBody
    public ResponseEntity<?> getKnowledgeArticle(@PathVariable Long id) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(id).orElse(null);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }

        // Increment views
        knowledgeArticleRepository.incrementViews(id);

        Map<String, Object> response = mapArticleToResponse(article);
        response.put("content", article.getContent());

        return ResponseEntity.ok(response);
    }

    /**
     * API: Create Article
     * POST /api/doctor/knowledge/articles
     */
    @PostMapping("/api/doctor/knowledge/articles")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createKnowledgeArticle(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> data) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        KnowledgeCategory category = null;
        if (data.get("categoryId") != null && !data.get("categoryId").toString().isEmpty()) {
            category = knowledgeCategoryRepository.findById(Long.valueOf(data.get("categoryId").toString()))
                    .orElse(null);
        }

        KnowledgeArticle.ArticleStatus status = KnowledgeArticle.ArticleStatus.DRAFT;
        if (data.get("status") != null) {
            status = KnowledgeArticle.ArticleStatus.valueOf(data.get("status").toString());
        }

        KnowledgeArticle article = KnowledgeArticle.builder()
                .title(data.get("title").toString())
                .summary(data.get("summary") != null ? data.get("summary").toString() : null)
                .content(data.get("content").toString())
                .category(category)
                .tags(data.get("tags") != null ? data.get("tags").toString() : null)
                .status(status)
                .featured(data.get("featured") != null && Boolean.parseBoolean(data.get("featured").toString()))
                .createdBy(doctor)
                .views(0)
                .publishedAt(status == KnowledgeArticle.ArticleStatus.PUBLISHED ? LocalDateTime.now() : null)
                .build();

        article = knowledgeArticleRepository.save(article);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tạo bài viết thành công");
        response.put("id", article.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * API: Update Article
     * PUT /api/doctor/knowledge/articles/{id}
     */
    @PutMapping("/api/doctor/knowledge/articles/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateKnowledgeArticle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        KnowledgeArticle article = knowledgeArticleRepository.findById(id).orElse(null);
        if (article == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Không tìm thấy bài viết");
            return ResponseEntity.badRequest().body(error);
        }

        KnowledgeCategory category = null;
        if (data.get("categoryId") != null && !data.get("categoryId").toString().isEmpty()) {
            category = knowledgeCategoryRepository.findById(Long.valueOf(data.get("categoryId").toString()))
                    .orElse(null);
        }

        KnowledgeArticle.ArticleStatus newStatus = KnowledgeArticle.ArticleStatus
                .valueOf(data.get("status").toString());
        boolean wasNotPublished = article.getStatus() != KnowledgeArticle.ArticleStatus.PUBLISHED;
        boolean isNowPublished = newStatus == KnowledgeArticle.ArticleStatus.PUBLISHED;

        article.setTitle(data.get("title").toString());
        article.setSummary(data.get("summary") != null ? data.get("summary").toString() : null);
        article.setContent(data.get("content").toString());
        article.setCategory(category);
        article.setTags(data.get("tags") != null ? data.get("tags").toString() : null);
        article.setStatus(newStatus);
        article.setFeatured(data.get("featured") != null && Boolean.parseBoolean(data.get("featured").toString()));
        article.setUpdatedBy(doctor);

        if (wasNotPublished && isNowPublished) {
            article.setPublishedAt(LocalDateTime.now());
        }

        knowledgeArticleRepository.save(article);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật bài viết thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * API: Delete Article
     * DELETE /api/doctor/knowledge/articles/{id}
     */
    @DeleteMapping("/api/doctor/knowledge/articles/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteKnowledgeArticle(@PathVariable Long id) {
        knowledgeArticleRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa bài viết thành công");
        return ResponseEntity.ok(response);
    }

    // Helper method to map article to response
    private Map<String, Object> mapArticleToResponse(KnowledgeArticle article) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", article.getId());
        map.put("title", article.getTitle());
        map.put("summary", article.getSummary());
        map.put("categoryId", article.getCategory() != null ? article.getCategory().getId() : null);
        map.put("categoryName", article.getCategory() != null ? article.getCategory().getName() : null);
        map.put("tags", article.getTags());
        map.put("status", article.getStatus().name());
        map.put("featured", article.getFeatured());
        map.put("views", article.getViews());
        map.put("createdAt", article.getCreatedAt());
        map.put("updatedAt", article.getUpdatedAt());
        map.put("publishedAt", article.getPublishedAt());
        map.put("createdByName", article.getCreatedBy() != null ? article.getCreatedBy().getFullName() : null);
        return map;
    }
}
