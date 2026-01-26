package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.AuthResponse;
import com.g4.capstoneproject.dto.LoginRequest;
import com.g4.capstoneproject.dto.RegisterRequest;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.UserInfo;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service xử lý authentication và authorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Đăng ký người dùng mới
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try {
            // Validate email hoặc phone phải có ít nhất 1
            if (!request.hasEmailOrPhone()) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Vui lòng cung cấp email hoặc số điện thoại")
                        .build();
            }
            
            // Validate mật khẩu khớp
            if (!request.isPasswordMatch()) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Mật khẩu xác nhận không khớp")
                        .build();
            }
            
            // Kiểm tra email đã tồn tại
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    return AuthResponse.builder()
                            .success(false)
                            .message("Email đã được sử dụng")
                            .build();
                }
            }
            
            // Kiểm tra số điện thoại đã tồn tại
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                if (userRepository.existsByPhoneNumber(request.getPhone())) {
                    return AuthResponse.builder()
                            .success(false)
                            .message("Số điện thoại đã được sử dụng")
                            .build();
                }
            }
            
            // Tạo user mới (thông tin bảo mật)
            User user = User.builder()
                    .email(request.getEmail() != null && !request.getEmail().trim().isEmpty() 
                            ? request.getEmail() : null)
                    .phoneNumber(request.getPhone() != null && !request.getPhone().trim().isEmpty() 
                            ? request.getPhone() : null)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(User.UserRole.PATIENT) // Mặc định là bệnh nhân
                    .isActive(true)
                    .emailVerified(false)
                    .phoneVerified(false)
                    .build();
            
            // Tạo UserInfo (thông tin cá nhân)
            UserInfo userInfo = UserInfo.builder()
                    .user(user)
                    .fullName(request.getFullName())
                    .build();
            
            user.setUserInfo(userInfo);
            user = userRepository.save(user);
            
            log.info("User registered successfully: {}", user.getEmail() != null ? user.getEmail() : user.getPhoneNumber());
            
            return AuthResponse.builder()
                    .success(true)
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .role(user.getRole())
                    .message("Đăng ký thành công! Vui lòng đăng nhập.")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during registration", e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Đã xảy ra lỗi trong quá trình đăng ký")
                    .build();
        }
    }
    
    /**
     * Đăng nhập người dùng
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            String username = request.getUsername();
            
            // Tìm user theo email hoặc phone
            Optional<User> userOpt = userRepository.findByEmailOrPhoneNumber(username, username);
            
            if (userOpt.isEmpty()) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Email/Số điện thoại hoặc mật khẩu không đúng")
                        .build();
            }
            
            User user = userOpt.get();
            
            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Email/Số điện thoại hoặc mật khẩu không đúng")
                        .build();
            }
            
            // Kiểm tra tài khoản có được kích hoạt không
            if (!user.getIsActive()) {
                return AuthResponse.builder()
                        .success(false)
                        .message("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.")
                        .build();
            }
            
            // Cập nhật last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("User logged in successfully: {}", user.getEmail() != null ? user.getEmail() : user.getPhoneNumber());
            
            return AuthResponse.builder()
                    .success(true)
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .role(user.getRole())
                    .message("Đăng nhập thành công!")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during login", e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Đã xảy ra lỗi trong quá trình đăng nhập")
                    .build();
        }
    }
    
    /**
     * Xử lý đăng nhập/đăng ký qua Google OAuth
     */
    @Transactional
    public AuthResponse processOAuthPostLogin(String email, String name, String googleId) {
        try {
            // Tìm user theo email hoặc googleId
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                // Nếu chưa có user, tạo mới (thông tin bảo mật)
                User newUser = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(googleId)) // Mật khẩu tạm từ googleId
                        .googleId(googleId)
                        .role(User.UserRole.PATIENT)
                        .isActive(true)
                        .emailVerified(true) // Email từ Google được coi là đã xác minh
                        .phoneVerified(false)
                        .build();
                
                // Tạo UserInfo (thông tin cá nhân)
                UserInfo userInfo = UserInfo.builder()
                        .user(newUser)
                        .fullName(name)
                        .build();
                
                newUser.setUserInfo(userInfo);
                newUser = userRepository.save(newUser);
                
                log.info("New user registered via Google OAuth: {}", email);
                
                return AuthResponse.builder()
                        .success(true)
                        .userId(newUser.getId())
                        .fullName(newUser.getFullName())
                        .email(newUser.getEmail())
                        .phone(newUser.getPhoneNumber())
                        .role(newUser.getRole())
                        .message("Đăng ký và đăng nhập thành công qua Google!")
                        .build();
            } else {
                // User đã tồn tại, đăng nhập
                User user = userOpt.get();
                
                // Update googleId if needed
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                    user.setEmailVerified(true);
                }
                
                // Cập nhật last login
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                
                log.info("User logged in via Google OAuth: {}", email);
                
                return AuthResponse.builder()
                        .success(true)
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhoneNumber())
                        .role(user.getRole())
                        .message("Đăng nhập thành công qua Google!")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("Error during OAuth login", e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Đã xảy ra lỗi trong quá trình đăng nhập qua Google")
                    .build();
        }
    }
}
