package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.AuthResponse;
import com.g4.capstoneproject.dto.LoginRequest;
import com.g4.capstoneproject.dto.RegisterRequest;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.UserInfo;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý authentication và authorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsyncEmailService asyncEmailService;

    @Value("${email.verification.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Đăng ký người dùng mới
     * Clears user-related caches after registration
     */
    @CacheEvict(value = { "users", "patients" }, allEntries = true)
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
                            ? request.getEmail()
                            : null)
                    .phoneNumber(request.getPhone() != null && !request.getPhone().trim().isEmpty()
                            ? request.getPhone()
                            : null)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(User.UserRole.PATIENT) // Mặc định là bệnh nhân
                    .isActive(true)
                    .emailVerified(false)
                    .build();

            // Tạo email verification token nếu có email
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String verificationToken = UUID.randomUUID().toString();
                user.setEmailVerificationToken(verificationToken);
                user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
            }

            // Tạo UserInfo (thông tin cá nhân)
            UserInfo userInfo = UserInfo.builder()
                    .user(user)
                    .fullName(request.getFullName())
                    .build();

            user.setUserInfo(userInfo);
            user = userRepository.save(user);

            // Gửi email xác thực nếu có email
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                sendVerificationEmail(user);
            }

            log.info("User registered successfully: {}",
                    user.getEmail() != null ? user.getEmail() : user.getPhoneNumber());

            String message = user.getEmail() != null
                    ? "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản."
                    : "Đăng ký thành công! Vui lòng đăng nhập.";

            return AuthResponse.builder()
                    .success(true)
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .role(user.getRole())
                    .message(message)
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

            log.info("User logged in successfully: {}",
                    user.getEmail() != null ? user.getEmail() : user.getPhoneNumber());

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
     * Clears user-related caches after OAuth registration/update
     */
    @CacheEvict(value = { "users", "patients" }, allEntries = true)
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

    /**
     * Gửi email xác thực cho user
     */
    private void sendVerificationEmail(User user) {
        try {
            String verificationUrl = String.format("%s/auth/verify-email?token=%s",
                    baseUrl, user.getEmailVerificationToken());
            log.info("Sending verification email asynchronously to: {}", user.getEmail());
            asyncEmailService.sendVerificationEmailAsync(
                    user.getEmail(),
                    user.getFullName() != null ? user.getFullName() : "User",
                    user.getEmailVerificationToken(),
                    verificationUrl);
        } catch (Exception ex) {
            log.error("Failed to send verification email to: {}", user.getEmail(), ex);
        }
    }

    /**
     * Xác thực email với token
     * Clears user cache after email verification
     */
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public boolean verifyEmail(String token) {
        try {
            Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);

            if (userOpt.isEmpty()) {
                log.warn("Email verification failed: Invalid token");
                return false;
            }

            User user = userOpt.get();

            // Kiểm tra token đã hết hạn chưa
            if (user.getEmailVerificationTokenExpiry() != null &&
                    user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Email verification failed: Token expired for user {}", user.getEmail());
                return false;
            }

            // Xác thực email và xóa token
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiry(null);

            userRepository.save(user);

            // Gửi welcome email
            log.info("Sending welcome email asynchronously to: {}", user.getEmail());
            asyncEmailService.sendWelcomeEmailAsync(
                    user.getEmail(),
                    user.getFullName() != null ? user.getFullName() : "User",
                    user.getFullName());

            log.info("Email verified successfully for user: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            log.error("Error during email verification", e);
            return false;
        }
    }

    /**
     * Gửi lại email xác thực
     */
    @Transactional
    public boolean resendVerificationEmail(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                log.warn("Resend verification failed: User not found with email {}", email);
                return false;
            }

            User user = userOpt.get();

            if (user.getEmailVerified()) {
                log.warn("Resend verification failed: Email already verified for {}", email);
                return false;
            }

            // Tạo token mới
            String verificationToken = UUID.randomUUID().toString();
            user.setEmailVerificationToken(verificationToken);
            user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

            userRepository.save(user);

            // Gửi email
            sendVerificationEmail(user);

            log.info("Verification email resent to: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Error resending verification email", e);
            return false;
        }
    }

    /**
     * Xử lý quên mật khẩu - tạo reset token và gửi email
     */
    @Transactional
    public AuthResponse forgotPassword(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                // Không thông báo user không tồn tại để tránh lộ thông tin
                log.warn("Forgot password request for non-existent email: {}", email);
                return AuthResponse.builder()
                        .success(true)
                        .message("Nếu email tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu.")
                        .build();
            }

            User user = userOpt.get();

            // Tạo reset token
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token có hiệu lực 1 giờ

            userRepository.save(user);

            // Gửi email reset password
            String resetUrl = String.format("%s/auth/reset-password?token=%s", baseUrl, resetToken);
            log.info("Sending password reset email to: {}", email);
            asyncEmailService.sendPasswordResetEmailAsync(user.getEmail(), 
                    user.getFullName() != null ? user.getFullName() : "User", 
                    resetToken,
                    resetUrl);

            log.info("Password reset token generated for user: {}", email);
            return AuthResponse.builder()
                    .success(true)
                    .message("Nếu email tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu.")
                    .build();
        } catch (Exception e) {
            log.error("Error during forgot password", e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Đã xảy ra lỗi khi xử lý yêu cầu. Vui lòng thử lại sau.")
                    .build();
        }
    }

    /**
     * Đặt lại mật khẩu với token
     * Clears user cache after password reset
     */
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public AuthResponse resetPassword(String token, String newPassword) {
        try {
            Optional<User> userOpt = userRepository.findByPasswordResetToken(token);

            if (userOpt.isEmpty()) {
                log.warn("Password reset failed: Invalid token");
                return AuthResponse.builder()
                        .success(false)
                        .message("Link đặt lại mật khẩu không hợp lệ.")
                        .build();
            }

            User user = userOpt.get();

            // Kiểm tra token đã hết hạn chưa
            if (user.getPasswordResetTokenExpiry() != null &&
                    user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Password reset failed: Token expired for user {}", user.getEmail());
                return AuthResponse.builder()
                        .success(false)
                        .message("Link đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu lại.")
                        .build();
            }

            // Đặt lại mật khẩu và xóa token
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);

            userRepository.save(user);

            log.info("Password reset successfully for user: {}", user.getEmail());
            return AuthResponse.builder()
                    .success(true)
                    .message("Mật khẩu đã được đặt lại thành công. Bạn có thể đăng nhập với mật khẩu mới.")
                    .build();
        } catch (Exception e) {
            log.error("Error during password reset", e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Đã xảy ra lỗi khi đặt lại mật khẩu. Vui lòng thử lại sau.")
                    .build();
        }
    }
}
