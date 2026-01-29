package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.UserInfo;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller cho trang Profile
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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
     * POST /api/profile/update - Cập nhật thông tin cá nhân
     */
    @PostMapping("/api/profile/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> updates,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            User user = userRepository.findByIdWithUserInfo(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            // Validate phone number format if provided
            if (updates.containsKey("phoneNumber")) {
                String phoneNumber = updates.get("phoneNumber");
                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                    // Basic phone validation (Vietnam format)
                    if (!phoneNumber.matches("^0\\d{9}$")) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false, 
                            "message", "Số điện thoại không hợp lệ. Vui lòng nhập 10 chữ số bắt đầu bằng 0"
                        ));
                    }
                    user.setPhoneNumber(phoneNumber);
                }
            }
            
            // Tạo hoặc cập nhật UserInfo
            UserInfo userInfo = user.getUserInfo();
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setUser(user);
                user.setUserInfo(userInfo);
            }
            
            // Validate and update fullName
            if (updates.containsKey("fullName")) {
                String fullName = updates.get("fullName");
                if (fullName == null || fullName.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false, 
                        "message", "Họ tên không được để trống"
                    ));
                }
                userInfo.setFullName(fullName.trim());
            }
            
            // Validate and update dateOfBirth
            if (updates.containsKey("dateOfBirth")) {
                try {
                    String dobStr = updates.get("dateOfBirth");
                    if (dobStr != null && !dobStr.trim().isEmpty()) {
                        LocalDate dob = LocalDate.parse(dobStr);
                        // Validate age (must be at least 1 year old and not in future)
                        LocalDate now = LocalDate.now();
                        if (dob.isAfter(now)) {
                            return ResponseEntity.badRequest().body(Map.of(
                                "success", false, 
                                "message", "Ngày sinh không được ở tương lai"
                            ));
                        }
                        if (dob.isAfter(now.minusYears(1))) {
                            return ResponseEntity.badRequest().body(Map.of(
                                "success", false, 
                                "message", "Tuổi phải từ 1 tuổi trở lên"
                            ));
                        }
                        userInfo.setDateOfBirth(dob);
                    }
                } catch (Exception e) {
                    log.warn("Invalid date format: {}", updates.get("dateOfBirth"));
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false, 
                        "message", "Định dạng ngày sinh không hợp lệ (YYYY-MM-DD)"
                    ));
                }
            }
            
            // Validate and update gender
            if (updates.containsKey("gender")) {
                try {
                    String genderStr = updates.get("gender");
                    if (genderStr != null && !genderStr.trim().isEmpty()) {
                        userInfo.setGender(com.g4.capstoneproject.entity.Gender.valueOf(genderStr));
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid gender value: {}", updates.get("gender"));
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false, 
                        "message", "Giới tính không hợp lệ. Chọn MALE, FEMALE hoặc OTHER"
                    ));
                }
            }
            
            // Update address
            if (updates.containsKey("address")) {
                String address = updates.get("address");
                userInfo.setAddress(address != null ? address.trim() : null);
            }
            
            // Lưu vào database (cascade will save userInfo)
            userRepository.save(user);
            log.info("Profile updated successfully for user ID: {}", userId);
            
            // Cập nhật session attributes
            session.setAttribute("userFullName", userInfo.getFullName());
            session.setAttribute("userRole", user.getRole().toString());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating profile for user", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false, 
                "message", "Lỗi hệ thống. Vui lòng thử lại sau"
            ));
        }
    }
    
    /**
     * POST /api/profile/change-password - Đổi mật khẩu
     */
    @PostMapping("/api/profile/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");
            
            // Validate input
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập mật khẩu hiện tại"));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập mật khẩu mới"));
            }
            
            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng xác nhận mật khẩu mới"));
            }
            
            // Validate password length
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu mới phải có ít nhất 6 ký tự"));
            }
            
            // Validate password confirmation
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu xác nhận không khớp"));
            }
            
            // Get user
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            // Check if user has a password (Google OAuth users might not have one)
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Tài khoản đăng nhập bằng Google không thể đổi mật khẩu"
                ));
            }
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu hiện tại không đúng"));
            }
            
            // Check if new password is same as current
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Mật khẩu mới phải khác mật khẩu hiện tại"
                ));
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            log.info("Password changed successfully for user ID: {}", userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error changing password for user", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false, 
                "message", "Lỗi hệ thống. Vui lòng thử lại sau"
            ));
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
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("email", user.getEmail());
            response.put("emailVerified", user.getEmailVerified());
            response.put("hasPassword", user.getPassword() != null && !user.getPassword().isEmpty());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting email status", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi hệ thống. Vui lòng thử lại sau"
            ));
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
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            if (user.getEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email đã được xác thực"
                ));
            }
            
            boolean sent = authService.resendVerificationEmail(user.getEmail());
            
            if (sent) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email xác thực đã được gửi. Vui lòng kiểm tra hộp thư của bạn."
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Không thể gửi email xác thực. Vui lòng thử lại sau."
                ));
            }
        } catch (Exception e) {
            log.error("Error resending verification email", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi hệ thống. Vui lòng thử lại sau"
            ));
        }
    }
}
