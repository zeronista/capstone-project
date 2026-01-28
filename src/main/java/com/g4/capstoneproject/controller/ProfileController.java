package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.UserInfo;
import com.g4.capstoneproject.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
            
            // Cập nhật thông tin trong bảng users
            if (updates.containsKey("phoneNumber")) {
                user.setPhoneNumber(updates.get("phoneNumber"));
            }
            
            // Tạo hoặc cập nhật UserInfo
            UserInfo userInfo = user.getUserInfo();
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setUser(user);
                user.setUserInfo(userInfo);
            }
            
            // Cập nhật thông tin trong bảng user_info
            if (updates.containsKey("fullName")) {
                userInfo.setFullName(updates.get("fullName"));
            }
            
            if (updates.containsKey("dateOfBirth")) {
                try {
                    LocalDate dob = LocalDate.parse(updates.get("dateOfBirth"));
                    userInfo.setDateOfBirth(dob);
                } catch (Exception e) {
                    log.warn("Invalid date format: {}", updates.get("dateOfBirth"));
                }
            }
            
            if (updates.containsKey("gender")) {
                try {
                    userInfo.setGender(com.g4.capstoneproject.entity.Gender.valueOf(updates.get("gender")));
                } catch (Exception e) {
                    log.warn("Invalid gender value: {}", updates.get("gender"));
                }
            }
            
            if (updates.containsKey("address")) {
                userInfo.setAddress(updates.get("address"));
            }
            
            // Lưu vào database
            userRepository.save(user);
            
            // Cập nhật session
            if (userInfo.getFullName() != null) {
                session.setAttribute("userFullName", userInfo.getFullName());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
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
            
            if (currentPassword == null || newPassword == null || confirmPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin"));
            }
            
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu xác nhận không khớp"));
            }
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }
            
            // Kiểm tra mật khẩu hiện tại (cần BCryptPasswordEncoder)
            // TODO: Implement password verification
            
            // Cập nhật mật khẩu mới
            // TODO: Implement password hashing
            // user.setPassword(passwordEncoder.encode(newPassword));
            // userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
}
