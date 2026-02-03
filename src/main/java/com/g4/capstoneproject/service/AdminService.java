package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.AccountResponse;
import com.g4.capstoneproject.dto.AssignRoleRequest;
import com.g4.capstoneproject.dto.CreateAccountRequest;
import com.g4.capstoneproject.dto.UpdateAccountRequest;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.UserInfo;
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
                        String phone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";
                        
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
                throw new IllegalArgumentException("Không thể gán vai trò Bệnh nhân qua tính năng này. Vui lòng sử dụng tính năng quản lý bệnh nhân");
            }
            
            // Không cho phép assign role ADMIN
            if (request.getRole() == User.UserRole.ADMIN) {
                throw new IllegalArgumentException("Không thể gán vai trò Quản trị viên. Vai trò này chỉ có thể được cấp bởi hệ thống");
            }
            
            // Tìm user
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Không tìm thấy tài khoản với ID: %d", request.getUserId())));
            
            // Cập nhật role
            User.UserRole oldRole = user.getRole();
            user.setRole(request.getRole());
            user = userRepository.save(user);
            
            log.info("Role updated for user {} (ID: {}, FullName: {}): {} -> {}", 
                    user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                    user.getId(),
                    user.getFullName(),
                    oldRole, 
                    request.getRole());
            
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
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Không tìm thấy tài khoản với ID: %d", id)));
            
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
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Không tìm thấy tài khoản với ID: %d", id)));
            
            // Toggle isActive status
            user.setIsActive(!user.getIsActive());
            user = userRepository.save(user);
            
            log.info("Account status toggled for user {} (ID: {}, FullName: {}): isActive = {} -> {}", 
                    user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                    user.getId(),
                    user.getFullName(),
                    !user.getIsActive(),
                    user.getIsActive());
            
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
            stats.put("receptionist", allUsers.stream().filter(u -> u.getRole() == User.UserRole.RECEPTIONIST).count());
            stats.put("patient", allUsers.stream().filter(u -> u.getRole() == User.UserRole.PATIENT).count());
            stats.put("active", allUsers.stream().filter(User::getIsActive).count());
            stats.put("disabled", allUsers.stream().filter(u -> !u.getIsActive()).count());
            
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
                throw new IllegalArgumentException("Vui lòng cung cấp email hoặc số điện thoại để tạo tài khoản");
            }
            
            // Validate mật khẩu khớp
            if (!request.isPasswordMatch()) {
                throw new IllegalArgumentException("Mật khẩu và mật khẩu xác nhận không khớp. Vui lòng kiểm tra lại");
            }
            
            // Validate fullName
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                throw new IllegalArgumentException("Họ và tên không được để trống");
            }
            
            if (request.getFullName().trim().length() < 2) {
                throw new IllegalArgumentException("Họ và tên phải có ít nhất 2 ký tự");
            }
            
            // Không cho phép tạo account với role ADMIN
            if (request.getRole() == User.UserRole.ADMIN) {
                throw new IllegalArgumentException("Không thể tạo tài khoản với vai trò Quản trị viên. Vai trò này chỉ có thể được cấp bởi hệ thống");
            }
            
            // Kiểm tra email đã tồn tại
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new IllegalArgumentException(String.format("Email '%s' đã được sử dụng bởi tài khoản khác", request.getEmail()));
                }
            }
            
            // Kiểm tra số điện thoại đã tồn tại
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                if (userRepository.existsByPhoneNumber(request.getPhone())) {
                    throw new IllegalArgumentException(String.format("Số điện thoại '%s' đã được sử dụng bởi tài khoản khác", request.getPhone()));
                }
            }
            
            // Tạo user mới (thông tin bảo mật)
            User user = User.builder()
                    .email(request.getEmail() != null && !request.getEmail().trim().isEmpty() 
                            ? request.getEmail() : null)
                    .phoneNumber(request.getPhone() != null && !request.getPhone().trim().isEmpty() 
                            ? request.getPhone() : null)
                    .password(passwordEncoder.encode(request.getPassword())) // Mã hóa password
                    .role(request.getRole()) // Role được chọn bởi admin
                    .isActive(true)
                    .emailVerified(false)
                    .build();
            
            // Tạo UserInfo (thông tin cá nhân)
            UserInfo userInfo = UserInfo.builder()
                    .user(user)
                    .fullName(request.getFullName())
                    .build();
            
            user.setUserInfo(userInfo);
            user = userRepository.save(user);
            
            log.info("Account created successfully by admin: {} (ID: {}) with role {} - FullName: {}", 
                    user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                    user.getId(),
                    user.getRole(),
                    user.getFullName());
            
            return AccountResponse.fromUser(user);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid create account request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating account", e);
            throw new RuntimeException("Không thể tạo tài khoản mới");
        }
    }
    
    /**
     * Cập nhật tài khoản với thông tin mới (dành cho admin)
     */
    @Transactional
    public AccountResponse updateAccount(Long id, UpdateAccountRequest request) {
        try {
            // Validate email hoặc phone phải có ít nhất 1
            if (!request.hasEmailOrPhone()) {
                throw new IllegalArgumentException("Vui lòng cung cấp email hoặc số điện thoại để cập nhật tài khoản");
            }
            
            // Tìm user cần cập nhật
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Không tìm thấy tài khoản với ID: %d", id)));
            
            // Validate fullName
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                throw new IllegalArgumentException("Họ và tên không được để trống");
            }
            
            if (request.getFullName().trim().length() < 2) {
                throw new IllegalArgumentException("Họ và tên phải có ít nhất 2 ký tự");
            }
            
            // Kiểm tra email đã tồn tại (nếu thay đổi)
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (!request.getEmail().equals(user.getEmail())) {
                    if (userRepository.existsByEmail(request.getEmail())) {
                        throw new IllegalArgumentException(String.format("Email '%s' đã được sử dụng bởi tài khoản khác", request.getEmail()));
                    }
                }
            }
            
            // Kiểm tra số điện thoại đã tồn tại (nếu thay đổi)
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                if (!request.getPhone().equals(user.getPhoneNumber())) {
                    if (userRepository.existsByPhoneNumber(request.getPhone())) {
                        throw new IllegalArgumentException(String.format("Số điện thoại '%s' đã được sử dụng bởi tài khoản khác", request.getPhone()));
                    }
                }
            }
            
            // Cập nhật thông tin user (không cập nhật role khi edit)
            user.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty() 
                    ? request.getEmail() : null);
            user.setPhoneNumber(request.getPhone() != null && !request.getPhone().trim().isEmpty() 
                    ? request.getPhone() : null);
            // Note: Role không được cập nhật khi edit (theo yêu cầu)
            
            // Cập nhật UserInfo
            UserInfo userInfo = user.getUserInfo();
            if (userInfo == null) {
                userInfo = UserInfo.builder()
                        .user(user)
                        .fullName(request.getFullName())
                        .build();
                user.setUserInfo(userInfo);
            } else {
                userInfo.setFullName(request.getFullName());
            }
            
            user = userRepository.save(user);
            
            log.info("Account updated successfully by admin: {} (ID: {}) - FullName: {} -> {}", 
                    user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                    user.getId(),
                    user.getFullName(),
                    request.getFullName());
            
            return AccountResponse.fromUser(user);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update account request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating account", e);
            throw new RuntimeException("Không thể cập nhật tài khoản");
        }
    }
}
