package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.ChangePasswordRequest;
import com.g4.capstoneproject.dto.ProfileResponse;
import com.g4.capstoneproject.dto.ProfileUpdateRequest;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.UserInfo;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service xử lý logic cho Profile Management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    /**
     * Lấy thông tin profile của user
     * 
     * @param userId ID của user
     * @return ProfileResponse hoặc empty nếu không tìm thấy
     */
    public Optional<ProfileResponse> getProfile(Long userId) {
        Optional<User> userOpt = userRepository.findByIdWithUserInfo(userId);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        UserInfo userInfo = user.getUserInfo();

        ProfileResponse response = ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isVerified(user.getEmailVerified())
                .googleId(user.getGoogleId())
                .canChangePassword(user.getGoogleId() == null) // Chỉ cho đổi MK nếu không phải Google OAuth
                .build();

        if (userInfo != null) {
            response.setFullName(userInfo.getFullName());
            response.setDateOfBirth(userInfo.getDateOfBirth());
            response.setGender(userInfo.getGender());
            response.setAddress(userInfo.getAddress());
            response.setAvatar(userInfo.getAvatarUrl());
        }

        response.setProfileCompletion(calculateProfileCompletion(user, userInfo));

        return Optional.of(response);
    }

    /**
     * Cập nhật thông tin profile
     * 
     * @param userId  ID của user
     * @param request Thông tin cần cập nhật
     * @return ProfileResponse sau khi cập nhật
     * @throws IllegalArgumentException nếu validation failed
     */
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findByIdWithUserInfo(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate phone number
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (!request.getPhoneNumber().matches("^0\\d{9}$")) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ. Phải có 10 chữ số và bắt đầu bằng 0");
            }

            // Kiểm tra phone đã tồn tại (trừ user hiện tại)
            Optional<User> existingUser = userRepository.findByPhoneNumber(request.getPhoneNumber());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài khoản khác");
            }

            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Validate date of birth
        if (request.getDateOfBirth() != null) {
            if (request.getDateOfBirth().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày sinh không được ở tương lai");
            }
            if (request.getDateOfBirth().isAfter(LocalDate.now().minusYears(1))) {
                throw new IllegalArgumentException("Người dùng phải ít nhất 1 tuổi");
            }
        }

        // Update UserInfo
        UserInfo userInfo = user.getUserInfo();
        if (userInfo == null) {
            userInfo = UserInfo.builder()
                    .user(user)
                    .build();
            user.setUserInfo(userInfo);
        }

        userInfo.setFullName(request.getFullName());
        userInfo.setDateOfBirth(request.getDateOfBirth());
        userInfo.setGender(request.getGender());
        userInfo.setAddress(request.getAddress());

        userRepository.save(user);
        log.info("Profile updated for user: {}", userId);

        return getProfile(userId).orElseThrow();
    }

    /**
     * Đổi mật khẩu
     * 
     * @param userId  ID của user
     * @param request Request chứa mật khẩu cũ và mới
     * @throws IllegalArgumentException nếu validation failed
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Kiểm tra nếu là Google OAuth user
        if (user.getGoogleId() != null) {
            throw new IllegalArgumentException("Không thể đổi mật khẩu cho tài khoản đăng nhập bằng Google");
        }

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        // Kiểm tra mật khẩu mới khác mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", userId);
    }

    /**
     * Upload avatar
     * 
     * @param userId ID của user
     * @param file   File avatar
     * @return URL của avatar
     * @throws IllegalArgumentException nếu validation failed
     * @throws IOException              nếu upload failed
     */
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findByIdWithUserInfo(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png")
                || contentType.equals("image/jpg"))) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh định dạng JPG, JPEG, PNG");
        }

        // Upload to S3
        String fileKey = s3Service.uploadFile(file);
        String avatarUrl = s3Service.generatePresignedUrl(fileKey);

        // Update UserInfo
        UserInfo userInfo = user.getUserInfo();
        if (userInfo == null) {
            userInfo = UserInfo.builder()
                    .user(user)
                    .build();
            user.setUserInfo(userInfo);
        }

        userInfo.setAvatarUrl(fileKey); // Lưu file key để có thể generate presigned URL sau
        userRepository.save(user);

        log.info("Avatar uploaded for user: {}", userId);

        return avatarUrl;
    }

    /**
     * Tính phần trăm hoàn thiện profile (0-100)
     * Các trường bắt buộc: email, phone, fullName, dateOfBirth, gender
     * Các trường optional: address, avatar
     */
    public int calculateProfileCompletion(User user, UserInfo userInfo) {
        int totalFields = 7;
        int completedFields = 0;

        // Required fields
        if (user.getEmail() != null && !user.getEmail().isEmpty())
            completedFields++;
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty())
            completedFields++;

        if (userInfo != null) {
            if (userInfo.getFullName() != null && !userInfo.getFullName().isEmpty())
                completedFields++;
            if (userInfo.getDateOfBirth() != null)
                completedFields++;
            if (userInfo.getGender() != null)
                completedFields++;

            // Optional fields
            if (userInfo.getAddress() != null && !userInfo.getAddress().isEmpty())
                completedFields++;
            if (userInfo.getAvatarUrl() != null && !userInfo.getAvatarUrl().isEmpty())
                completedFields++;
        }

        return (completedFields * 100) / totalFields;
    }
}
