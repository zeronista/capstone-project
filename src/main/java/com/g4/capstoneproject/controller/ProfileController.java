package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.ChangePasswordRequest;
import com.g4.capstoneproject.dto.ProfileResponse;
import com.g4.capstoneproject.dto.ProfileUpdateRequest;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.AuthService;
import com.g4.capstoneproject.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho trang Profile
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final AuthService authService;

    /**
     * GET /profile - Hiển thị trang profile (phân quyền theo role)
     */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User user = userRepository.findByIdWithUserInfo(userId).orElse(null);
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Phân quyền theo role
        if (user.getRole() == User.UserRole.PATIENT) {
            return "profile/patient";
        } else {
            // Các role khác (DOCTOR, RECEPTIONIST, ADMIN) dùng profile chung
            return "profile/index";
        }
    }

    /**
     * GET /api/profile - Lấy thông tin profile (API)
     */
    @GetMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProfile(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }

            Optional<ProfileResponse> profileOpt = profileService.getProfile(userId);
            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", profileOpt.get());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting profile", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống. Vui lòng thử lại sau"));
        }
    }

    /**
     * POST /api/profile/update - Cập nhật thông tin cá nhân
     */
    @PostMapping("/api/profile/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }

            ProfileResponse profile = profileService.updateProfile(userId, request);

            // Cập nhật session attributes
            session.setAttribute("userFullName", profile.getFullName());
            session.setAttribute("userRole", profile.getRole().toString());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công");
            response.put("data", profile);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống. Vui lòng thử lại sau"));
        }
    }

    /**
     * POST /api/profile/change-password - Đổi mật khẩu
     */
    @PostMapping("/api/profile/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest request,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }

            profileService.changePassword(userId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống. Vui lòng thử lại sau"));
        }
    }

    /**
     * POST /api/profile/avatar - Upload avatar
     */
    @PostMapping("/api/profile/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }

            String avatarUrl = profileService.uploadAvatar(userId, file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload avatar thành công");
            response.put("avatarUrl", avatarUrl);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading avatar", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống. Vui lòng thử lại sau"));
        }
    }

    /**
     * GET /api/profile/email-status - Lấy trạng thái xác thực email
     */
    @GetMapping("/api/profile/email-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEmailStatus(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("email", user.getEmail());
            response.put("emailVerified", user.getEmailVerified());
            response.put("hasPassword", user.getGoogleId() == null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting email status", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống. Vui lòng thử lại sau"));
        }
    }

    /**
     * POST /api/profile/resend-verification - Gửi lại email xác thực
     */
    @PostMapping("/api/profile/resend-verification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }

            if (user.getEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email đã được xác thực"));
            }

            boolean sent = authService.resendVerificationEmail(user.getEmail());

            if (sent) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email xác thực đã được gửi. Vui lòng kiểm tra hộp thư của bạn."));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                        "success", false,
                        "message", "Không thể gửi email xác thực. Vui lòng thử lại sau."));
            }
        } catch (Exception e) {
            log.error("Error resending verification email", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống. Vui lòng thử lại sau"));
        }
    }
}
