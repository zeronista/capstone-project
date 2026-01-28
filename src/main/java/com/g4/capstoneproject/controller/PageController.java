package com.g4.capstoneproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller đơn giản để render các trang template
 * Không có business logic, chỉ navigation giữa các trang
 */
@Controller
public class PageController {

    /**
     * Trang chủ
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
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

    // ==================== ADMIN ====================

    @GetMapping("/admin/accounts")
    public String adminAccounts() {
        return "admin/accounts";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/users";
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
}
