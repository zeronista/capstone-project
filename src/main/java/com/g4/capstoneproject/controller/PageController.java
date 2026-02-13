package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller đơn giản để render các trang template
 * Updated in Phase 2 with role-based dashboard redirects
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserRepository userRepository;

    /**
     * Trang chủ
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // ==================== DASHBOARD ====================

    /**
     * Role-based dashboard redirect - Phase 2
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        String username = userDetails.getUsername();
        User user = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        // Redirect based on role
        return switch (user.getRole()) {
            case DOCTOR -> "redirect:/doctor/dashboard";
            case RECEPTIONIST -> "redirect:/receptionist/dashboard";
            case PATIENT -> "redirect:/patient";
            case ADMIN -> "redirect:/admin/dashboard";
        };
    }

    // ==================== AUTHENTICATION ====================

    @GetMapping("/auth/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/auth/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @GetMapping("/auth/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    // ==================== ADMIN ====================

    @GetMapping("/admin/accounts")
    public String adminAccounts() {
        return "admin/accounts";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/users";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    // ==================== MEDICAL ====================

    @GetMapping("/medical/prescriptions")
    public String medicalPrescriptions() {
        return "medical/prescriptions";
    }

    @GetMapping("/medical/treatments")
    public String medicalTreatments() {
        return "medical/treatments";
    }

    @GetMapping("/medical/forecast")
    public String medicalForecast() {
        return "medical/forecast";
    }

    @GetMapping("/medical/knowledge")
    public String medicalKnowledge() {
        return "medical/knowledge";
    }

    // ==================== AI ====================

    @GetMapping("/ai/calls")
    public String aiCalls() {
        return "ai/calls";
    }

    @GetMapping("/ai/voice")
    public String aiVoice() {
        return "ai/voice";
    }

    @GetMapping("/ai/config")
    public String aiConfig() {
        return "ai/config";
    }

    @GetMapping("/ai/web-call")
    public String aiWebCall() {
        return "ai/web-call";
    }

    /**
     * Web Call mới - gọi điện giữa 2 user đã đăng nhập
     */
    @GetMapping("/call")
    public String webCallPage() {
        return "call/index";
    }

    /**
     * Lịch sử cuộc gọi
     */
    @GetMapping("/call/history")
    public String callHistory() {
        return "call/history";
    }

    // ==================== CRM ====================

    @GetMapping("/crm/tickets")
    public String crmTickets() {
        return "crm/tickets";
    }

    @GetMapping("/crm/surveys")
    public String crmSurveys() {
        return "crm/surveys";
    }

    @GetMapping("/crm/social")
    public String crmSocial() {
        return "crm/social";
    }

    @GetMapping("/crm/notifications")
    public String crmNotifications() {
        return "crm/notifications";
    }

    // ==================== REPORTS ====================

    @GetMapping("/reports")
    public String reports() {
        return "reports/index";
    }

    @GetMapping("/reports/detail")
    public String reportsDetail() {
        return "reports/detail";
    }

    // ==================== PATIENT ====================

    @GetMapping("/patient")
    public String patientHome() {
        return "patient/index";
    }

    @GetMapping("/patient/call")
    public String patientCall() {
        return "patient/call";
    }

    @GetMapping("/patient/prescriptions")
    public String patientPrescriptions() {
        return "patient/prescriptions";
    }

    @GetMapping("/patient/treatments")
    public String patientTreatments() {
        return "patient/treatments";
    }

    @GetMapping("/patient/appointments")
    public String patientAppointments() {
        return "patient/appointments";
    }

    @GetMapping("/patient/tickets")
    public String patientTickets() {
        return "patient/tickets";
    }

    @GetMapping("/patient/documents")
    public String patientDocuments() {
        return "patient/documents";
    }

    @GetMapping("/patient/notifications")
    public String patientNotifications() {
        return "patient/notifications";
    }
}
