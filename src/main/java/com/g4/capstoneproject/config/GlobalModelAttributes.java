package com.g4.capstoneproject.config;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global Model Attributes - Inject common data to all views
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    /**
     * Inject current user as 'admin', 'doctor', 'receptionist', or 'patient'
     * based on their role for sidebar display
     */
    @ModelAttribute("admin")
    public User adminUser() {
        return getCurrentUserWithRole(User.UserRole.ADMIN);
    }

    @ModelAttribute("doctor")
    public User doctorUser() {
        return getCurrentUserWithRole(User.UserRole.DOCTOR);
    }

    @ModelAttribute("receptionist")
    public User receptionistUser() {
        return getCurrentUserWithRole(User.UserRole.RECEPTIONIST);
    }

    @ModelAttribute("patient")
    public User patientUser() {
        return getCurrentUserWithRole(User.UserRole.PATIENT);
    }

    /**
     * Get current authenticated user if they have the specified role
     */
    private User getCurrentUserWithRole(User.UserRole role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        try {
            Object principal = auth.getPrincipal();
            String email = null;
            
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                email = (String) principal;
            }

            if (email != null) {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null && user.getRole() == role) {
                    return user;
                }
            }
        } catch (Exception e) {
            // Silently fail - user will be null
        }
        return null;
    }

    /**
     * Inject current user regardless of role
     */
    @ModelAttribute("currentUser")
    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        try {
            Object principal = auth.getPrincipal();
            String email = null;
            
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                email = (String) principal;
            }

            if (email != null) {
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            // Silently fail
        }
        return null;
    }
}
