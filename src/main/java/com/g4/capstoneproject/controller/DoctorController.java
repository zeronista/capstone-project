package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TreatmentPlan;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.PrescriptionService;
import com.g4.capstoneproject.service.TicketService;
import com.g4.capstoneproject.service.TreatmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final TreatmentPlanService treatmentPlanService;
    private final UserRepository userRepository;

    /**
     * Doctor Dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User doctor = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

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
        User doctor = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

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
        User doctor = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("doctor", doctor);
        return "doctor/appointments";
    }

    /**
     * Prescriptions Management
     */
    @GetMapping("/prescriptions")
    public String prescriptions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User doctor = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

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
        User doctor = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

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
        User doctor = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

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
        User doctor = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

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
        User doctor = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
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
}
