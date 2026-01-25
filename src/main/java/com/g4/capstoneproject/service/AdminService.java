package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.AccountResponse;
import com.g4.capstoneproject.dto.AssignRoleRequest;
import com.g4.capstoneproject.dto.CreateAccountRequest;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service xử lý quản lý tài khoản cho Admin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Lấy danh sách tất cả accounts (không bao gồm bệnh nhân)
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllNonPatientAccounts() {
        try {
            List<User> users = userRepository.findAll().stream()
                    .filter(user -> user.getRole() != User.UserRole.PATIENT)
                    .collect(Collectors.toList());
            
            return users.stream()
                    .map(AccountResponse::fromUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching non-patient accounts", e);
            throw new RuntimeException("Không thể lấy danh sách tài khoản");
        }
    }
    
    /**
     * Lấy danh sách tất cả accounts có phân trang
     */
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        try {
            return userRepository.findAll(pageable)
                    .map(AccountResponse::fromUser);
        } catch (Exception e) {
            log.error("Error fetching accounts with pagination", e);
            throw new RuntimeException("Không thể lấy danh sách tài khoản");
        }
    }
    
    /**
     * Tìm kiếm accounts theo từ khóa
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> searchAccounts(String keyword) {
        try {
            List<User> users = userRepository.findAll().stream()
                    .filter(user -> {
                        String fullName = user.getFullName() != null ? user.getFullName().toLowerCase() : "";
                        String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                        String phone = user.getPhone() != null ? user.getPhone() : "";
                        
                        return fullName.contains(keyword.toLowerCase()) 
                                || email.contains(keyword.toLowerCase())
                                || phone.contains(keyword);
                    })
                    .collect(Collectors.toList());
            
            return users.stream()
                    .map(AccountResponse::fromUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching accounts with keyword: {}", keyword, e);
            throw new RuntimeException("Không thể tìm kiếm tài khoản");
        }
    }
    
    /**
     * Lọc accounts theo role
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> filterAccountsByRole(User.UserRole role) {
        try {
            List<User> users = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == role)
                    .collect(Collectors.toList());
            
            return users.stream()
                    .map(AccountResponse::fromUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error filtering accounts by role: {}", role, e);
            throw new RuntimeException("Không thể lọc tài khoản theo vai trò");
        }
    }
    
    /**
     * Assign role cho user (chỉ cho non-patient roles)
     */
    @Transactional
    public AccountResponse assignRole(AssignRoleRequest request) {
        try {
            // Validate role không phải PATIENT
            if (request.getRole() == User.UserRole.PATIENT) {
                throw new IllegalArgumentException("Không thể assign role PATIENT qua tính năng này");
            }
            
            // Tìm user
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + request.getUserId()));
            
            // Cập nhật role
            User.UserRole oldRole = user.getRole();
            user.setRole(request.getRole());
            user = userRepository.save(user);
            
            log.info("Role updated for user {}: {} -> {}", 
                    user.getId(), oldRole, request.getRole());
            
            return AccountResponse.fromUser(user);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid assign role request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error assigning role", e);
            throw new RuntimeException("Không thể cập nhật vai trò cho tài khoản");
        }
    }
    
    /**
     * Lấy thông tin chi tiết một account
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + id));
            
            return AccountResponse.fromUser(user);
        } catch (IllegalArgumentException e) {
            log.warn("Account not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching account by id: {}", id, e);
            throw new RuntimeException("Không thể lấy thông tin tài khoản");
        }
    }
    
    /**
     * Vô hiệu hóa/Kích hoạt account
     */
    @Transactional
    public AccountResponse toggleAccountStatus(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + id));
            
            // Toggle enabled status
            user.setEnabled(!user.getEnabled());
            user = userRepository.save(user);
            
            log.info("Account status toggled for user {}: enabled = {}", 
                    user.getId(), user.getEnabled());
            
            return AccountResponse.fromUser(user);
        } catch (IllegalArgumentException e) {
            log.warn("Account not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error toggling account status for id: {}", id, e);
            throw new RuntimeException("Không thể cập nhật trạng thái tài khoản");
        }
    }
    
    /**
     * Lấy thống kê số lượng accounts theo role
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getAccountStatistics() {
        try {
            List<User> allUsers = userRepository.findAll();
            
            Map<String, Long> stats = new HashMap<>();
            stats.put("total", (long) allUsers.size());
            stats.put("admin", allUsers.stream().filter(u -> u.getRole() == User.UserRole.ADMIN).count());
            stats.put("doctor", allUsers.stream().filter(u -> u.getRole() == User.UserRole.DOCTOR).count());
            stats.put("nurse", allUsers.stream().filter(u -> u.getRole() == User.UserRole.NURSE).count());
            stats.put("staff", allUsers.stream().filter(u -> u.getRole() == User.UserRole.STAFF).count());
            stats.put("patient", allUsers.stream().filter(u -> u.getRole() == User.UserRole.PATIENT).count());
            stats.put("active", allUsers.stream().filter(User::getEnabled).count());
            stats.put("disabled", allUsers.stream().filter(u -> !u.getEnabled()).count());
            
            return stats;
        } catch (Exception e) {
            log.error("Error fetching account statistics", e);
            throw new RuntimeException("Không thể lấy thống kê tài khoản");
        }
    }
    
    /**
     * Tạo tài khoản mới với role được chỉ định (dành cho admin)
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        try {
            // Validate email hoặc phone phải có ít nhất 1
            if (!request.hasEmailOrPhone()) {
                throw new IllegalArgumentException("Vui lòng cung cấp email hoặc số điện thoại");
            }
            
            // Validate mật khẩu khớp
            if (!request.isPasswordMatch()) {
                throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
            }
            
            // Kiểm tra email đã tồn tại
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new IllegalArgumentException("Email đã được sử dụng");
                }
            }
            
            // Kiểm tra số điện thoại đã tồn tại
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                if (userRepository.existsByPhone(request.getPhone())) {
                    throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
                }
            }
            
            // Tạo user mới
            User user = User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail() != null && !request.getEmail().trim().isEmpty() 
                            ? request.getEmail() : null)
                    .phone(request.getPhone() != null && !request.getPhone().trim().isEmpty() 
                            ? request.getPhone() : null)
                    .password(passwordEncoder.encode(request.getPassword())) // Mã hóa password
                    .role(request.getRole()) // Role được chọn bởi admin
                    .provider(User.AuthProvider.LOCAL)
                    .enabled(true)
                    .accountNonLocked(true)
                    .build();
            
            user = userRepository.save(user);
            
            log.info("Account created successfully by admin: {} with role {}", 
                    user.getEmail() != null ? user.getEmail() : user.getPhone(), 
                    user.getRole());
            
            return AccountResponse.fromUser(user);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid create account request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating account", e);
            throw new RuntimeException("Không thể tạo tài khoản mới");
        }
    }
}
