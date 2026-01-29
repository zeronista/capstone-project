package com.g4.capstoneproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Service for sending emails using Mailtrap API and Thymeleaf templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final MailtrapClient mailtrapClient;
    private final TemplateEngine templateEngine;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.from-name}")
    private String fromName;

    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send HTML email using Thymeleaf template
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping email to: {}", to);
            return;
        }

        if (!mailtrapClient.isConfigured()) {
            log.warn("Mailtrap is not configured. Cannot send email to: {}", to);
            return;
        }

        try {
            // Process Thymeleaf template
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            String htmlBody = templateEngine.process(templateName, context);

            // Send email via Mailtrap
            mailtrapClient.sendEmail(
                    fromEmail,
                    fromName,
                    to,
                    null,
                    subject,
                    htmlBody,
                    null,
                    null
            );

            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send email verification email
     */
    public void sendVerificationEmail(String to, String username, String verificationToken, String verificationUrl) {
        Map<String, Object> variables = Map.of(
                "username", username,
                "verificationUrl", verificationUrl,
                "verificationToken", verificationToken);

        sendHtmlEmail(to, "Xác thực tài khoản - ISSVSG Medical System", "email/verification", variables);
    }

    /**
     * Send welcome email after successful verification
     */
    public void sendWelcomeEmail(String to, String username, String fullName) {
        Map<String, Object> variables = Map.of(
                "username", username,
                "fullName", fullName != null ? fullName : username);

        sendHtmlEmail(to, "Chào mừng đến với ISSVSG Medical System", "email/welcome", variables);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String username, String resetToken, String resetUrl) {
        Map<String, Object> variables = Map.of(
                "username", username,
                "resetUrl", resetUrl,
                "resetToken", resetToken);

        sendHtmlEmail(to, "Đặt lại mật khẩu - ISSVSG Medical System", "email/password-reset", variables);
    }
}
