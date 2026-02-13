package com.g4.capstoneproject.config;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final S3Service s3Service;

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
     * Inject pre-signed avatar URLs for all roles
     */
    @ModelAttribute("adminAvatarUrl")
    public String adminAvatarUrl() {
        return getAvatarUrlForRole(User.UserRole.ADMIN);
    }

    @ModelAttribute("doctorAvatarUrl")
    public String doctorAvatarUrl() {
        return getAvatarUrlForRole(User.UserRole.DOCTOR);
    }

    @ModelAttribute("receptionistAvatarUrl")
    public String receptionistAvatarUrl() {
        return getAvatarUrlForRole(User.UserRole.RECEPTIONIST);
    }

    @ModelAttribute("patientAvatarUrl")
    public String patientAvatarUrl() {
        return getAvatarUrlForRole(User.UserRole.PATIENT);
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
     * Get pre-signed avatar URL for user with specified role
     */
    private String getAvatarUrlForRole(User.UserRole role) {
        User user = getCurrentUserWithRole(role);
        if (user == null || user.getUserInfo() == null) {
            return null;
        }

        String avatarKey = user.getUserInfo().getAvatarUrl();
        if (avatarKey == null || avatarKey.isEmpty()) {
            return null;
        }

        try {
            return s3Service.generatePresignedUrl(avatarKey, 7 * 24 * 3600); // 7 days
        } catch (Exception e) {
            log.warn("Could not generate presigned URL for avatar: {}", avatarKey, e);
            return null;
        }
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
