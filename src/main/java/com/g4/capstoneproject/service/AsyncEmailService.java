package com.g4.capstoneproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending emails asynchronously to avoid blocking the main thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailService {

    private final EmailService emailService;

    /**
     * Send email verification email asynchronously
     * This method is used during user registration to send verification emails
     * without blocking the user interface
     */
    @Async("emailExecutor")
    public CompletableFuture<Void> sendVerificationEmailAsync(String to, String username, String verificationToken,
            String verificationUrl) {
        try {
            emailService.sendVerificationEmail(to, username, verificationToken, verificationUrl);
            log.info("Verification email sent asynchronously to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email asynchronously to: {}", to, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send welcome email asynchronously
     */
    @Async("emailExecutor")
    public CompletableFuture<Void> sendWelcomeEmailAsync(String to, String username, String fullName) {
        try {
            emailService.sendWelcomeEmail(to, username, fullName);
            log.info("Welcome email sent asynchronously to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email asynchronously to: {}", to, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send password reset email asynchronously
     */
    @Async("emailExecutor")
    public CompletableFuture<Void> sendPasswordResetEmailAsync(String to, String username, String resetToken,
            String resetUrl) {
        try {
            emailService.sendPasswordResetEmail(to, username, resetToken, resetUrl);
            log.info("Password reset email sent asynchronously to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email asynchronously to: {}", to, e);
        }
        return CompletableFuture.completedFuture(null);
    }
}
