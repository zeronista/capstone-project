package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.user.*;
import com.g4.capstoneproject.entity.Gender;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.User.UserRole;
import com.g4.capstoneproject.entity.UserInfo;
import com.g4.capstoneproject.repository.GoogleFormSyncRecordRepository;
import com.g4.capstoneproject.repository.UserInfoRepository;
import com.g4.capstoneproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Service cho quản lý người dùng (CRUD + Import Excel)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final GoogleFormSyncRecordRepository googleFormSyncRecordRepository;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0[3|5|7|8|9])+([0-9]{8})$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // ==================== CRUD Operations ====================

    /**
     * Convert User entity to UserResponse with pre-signed avatar URL
     */
    private UserResponse toUserResponse(User user) {
        UserResponse response = UserResponse.from(user);
        
        // Generate pre-signed URL for avatar if it exists
        if (response.getAvatarUrl() != null && !response.getAvatarUrl().isEmpty()) {
            try {
                String presignedUrl = s3Service.generatePresignedUrl(response.getAvatarUrl(), 7 * 24 * 3600); // 7 days
                response.setAvatarUrl(presignedUrl);
            } catch (Exception e) {
                log.warn("Could not generate presigned URL for avatar: {}", response.getAvatarUrl(), e);
                response.setAvatarUrl(null);
            }
        }
        
        return response;
    }

    /**
     * Lấy danh sách tất cả người dùng
     */
    @Cacheable(value = "users", key = "'all'")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllWithUserInfo().stream()
                .map(this::toUserResponse)
                .toList();
    }

    /**
     * Lấy danh sách người dùng theo role
     * Loại bỏ users được tạo từ Google Form sync (chỉ hiển thị trong Google Form patients list)
     */
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .filter(user -> {
                    // Loại bỏ users có GoogleFormSyncRecord (guests từ Google Form)
                    // Chỉ hiển thị users đã đăng ký trong hệ thống
                    return !googleFormSyncRecordRepository.existsByPatientId(user.getId());
                })
                .map(this::toUserResponse)
                .toList();
    }

    /**
     * Lấy danh sách người dùng với phân trang
     */
    public Page<UserResponse> getUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    /**
     * Tìm kiếm người dùng
     */
    public List<UserResponse> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(keyword.trim()).stream()
                .map(this::toUserResponse)
                .toList();
    }

    /**
     * Lấy thông tin người dùng theo ID
     */
    @Cacheable(value = "users", key = "#userId")
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdWithUserInfo(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        return toUserResponse(user);
    }

    /**
     * Tạo người dùng mới
     */
    @Transactional
    @CacheEvict(value = { "users", "patients" }, allEntries = true)
    public UserResponse createUser(UserCreateRequest request) {
        // Validate email/phone unique
        validateUniqueConstraints(request.getEmail(), request.getPhone(), null);

        // Validate có ít nhất email hoặc phone
        if (!request.hasEmailOrPhone()) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất email hoặc số điện thoại");
        }

        // Tạo password nếu không cung cấp
        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = generateRandomPassword();
        }

        // Tạo User
        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhone())
                .password(passwordEncoder.encode(rawPassword))
                .role(request.getRole())
                .isActive(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // Tạo UserInfo
        UserInfo userInfo = UserInfo.builder()
                .user(user)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .build();

        userInfoRepository.save(userInfo);
        user.setUserInfo(userInfo);

        log.info("Created new user: {} ({})", user.getId(), request.getFullName());

        return toUserResponse(user);
    }

    /**
     * Cập nhật người dùng
     */
    @Transactional
    @CacheEvict(value = { "users", "patients" }, allEntries = true)
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findByIdWithUserInfo(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Validate email/phone unique (exclude current user)
        validateUniqueConstraints(request.getEmail(), request.getPhone(), userId);

        // Update User fields
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhoneNumber(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // Update UserInfo
        UserInfo userInfo = user.getUserInfo();
        if (userInfo == null) {
            userInfo = UserInfo.builder().user(user).build();
        }

        if (request.getFullName() != null) {
            userInfo.setFullName(request.getFullName());
        }
        if (request.getDateOfBirth() != null) {
            userInfo.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            userInfo.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            userInfo.setAddress(request.getAddress());
        }

        userInfoRepository.save(userInfo);
        user = userRepository.save(user);

        log.info("Updated user: {}", userId);

        return toUserResponse(user);
    }

    /**
     * Xóa người dùng (soft delete - set isActive = false)
     */
    @Transactional
    @CacheEvict(value = { "users", "patients" }, allEntries = true)
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("Soft deleted user: {}", userId);
    }

    /**
     * Khôi phục người dùng đã xóa
     */
    @Transactional
    @CacheEvict(value = { "users", "patients" }, allEntries = true)
    public UserResponse restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        user.setIsActive(true);
        user = userRepository.save(user);

        log.info("Restored user: {}", userId);

        return toUserResponse(user);
    }

    // ==================== Year/Month Grouping ====================

    /**
     * Lấy danh sách các năm có bệnh nhân đăng ký
     * Trả về danh sách năm, sắp xếp giảm dần (năm mới nhất trước)
     */
    public List<Integer> getAvailableYears() {
        List<Integer> years = userRepository.findDistinctYearsByPatientRole();
        log.debug("Found {} years with patient registrations", years.size());
        return years;
    }

    /**
     * Lấy bệnh nhân theo năm, nhóm theo tháng
     * Loại bỏ users được tạo từ Google Form sync
     */
    public YearMonthPatientsResponse getPatientsByYearMonth(Integer year) {
        // Lấy tất cả bệnh nhân của năm
        List<User> patients = userRepository.findPatientsByYear(year).stream()
                .filter(user -> !googleFormSyncRecordRepository.existsByPatientId(user.getId()))
                .toList();

        // Group theo tháng
        Map<Integer, List<User>> patientsByMonth = new LinkedHashMap<>();
        for (int month = 12; month >= 1; month--) {
            patientsByMonth.put(month, new ArrayList<>());
        }

        // Phân loại bệnh nhân vào các tháng
        for (User patient : patients) {
            LocalDateTime createdAt = patient.getCreatedAt();
            if (createdAt != null) {
                int month = createdAt.getMonthValue();
                patientsByMonth.get(month).add(patient);
            }
        }

        // Tạo danh sách MonthPatientsGroup
        List<MonthPatientsGroup> months = new ArrayList<>();
        for (int month = 12; month >= 1; month--) {
            List<User> monthPatients = patientsByMonth.get(month);
            
            // Chỉ thêm tháng có bệnh nhân
            if (!monthPatients.isEmpty()) {
                List<UserResponse> patientResponses = monthPatients.stream()
                        .map(this::toUserResponse)
                        .toList();

                MonthPatientsGroup group = MonthPatientsGroup.builder()
                        .month(month)
                        .monthName("Tháng " + month)
                        .count((long) monthPatients.size())
                        .patients(patientResponses)
                        .build();

                months.add(group);
            }
        }

        // Tạo response
        return YearMonthPatientsResponse.builder()
                .year(year)
                .totalCount((long) patients.size())
                .months(months)
                .build();
    }

    // ==================== Excel Import ====================

    /**
     * Import người dùng từ file Excel
     */
    @Transactional
    @CacheEvict(value = { "users", "patients" }, allEntries = true)
    public UserImportResult importUsersFromExcel(MultipartFile file) throws IOException {
        UserImportResult result = UserImportResult.builder().build();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            int startRow = 1;
            result.setTotalRows(sheet.getLastRowNum());

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    UserCreateRequest request = parseExcelRow(row, i + 1); // Row number for error reporting
                    UserResponse created = createUser(request);
                    result.addSuccess(created);
                } catch (Exception e) {
                    result.addError(i + 1, "general", e.getMessage());
                    log.warn("Error importing row {}: {}", i + 1, e.getMessage());
                }
            }
        }

        log.info("Excel import completed: {} success, {} errors", result.getSuccessCount(), result.getErrorCount());

        return result;
    }

    /**
     * Tạo file Excel mẫu để download
     */
    public byte[] generateExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Import Users");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Headers
            String[] headers = { "Họ và tên *", "Email", "Số điện thoại", "Vai trò *", "Ngày sinh (dd/MM/yyyy)",
                    "Giới tính", "Địa chỉ" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // Example row
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("Nguyễn Văn A");
            exampleRow.createCell(1).setCellValue("nguyenvana@email.com");
            exampleRow.createCell(2).setCellValue("0901234567");
            exampleRow.createCell(3).setCellValue("PATIENT");
            exampleRow.createCell(4).setCellValue("15/03/1990");
            exampleRow.createCell(5).setCellValue("MALE");
            exampleRow.createCell(6).setCellValue("123 Đường ABC, Quận 1, TP.HCM");

            // Instructions row
            Row instructionRow = sheet.createRow(3);
            instructionRow.createCell(0).setCellValue("Ghi chú:");
            instructionRow.createCell(1).setCellValue("Vai trò: PATIENT, DOCTOR, RECEPTIONIST, ADMIN");
            instructionRow.createCell(5).setCellValue("Giới tính: MALE, FEMALE, OTHER");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ==================== Helper Methods ====================

    private void validateUniqueConstraints(String email, String phone, Long excludeUserId) {
        if (email != null && !email.isEmpty()) {
            Optional<User> existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(excludeUserId)) {
                throw new IllegalArgumentException("Email đã được sử dụng: " + email);
            }
        }

        if (phone != null && !phone.isEmpty()) {
            Optional<User> existingByPhone = userRepository.findByPhoneNumber(phone);
            if (existingByPhone.isPresent() && !existingByPhone.get().getId().equals(excludeUserId)) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng: " + phone);
            }
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private UserCreateRequest parseExcelRow(Row row, int rowNumber) {
        String fullName = getCellStringValue(row.getCell(0));
        String email = getCellStringValue(row.getCell(1));
        String phone = getCellStringValue(row.getCell(2));
        String roleStr = getCellStringValue(row.getCell(3));
        String dateStr = getCellStringValue(row.getCell(4));
        String genderStr = getCellStringValue(row.getCell(5));
        String address = getCellStringValue(row.getCell(6));

        // Validate required fields
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Dòng " + rowNumber + ": Họ và tên không được để trống");
        }

        // Parse role
        UserRole role = UserRole.PATIENT; // Default
        if (roleStr != null && !roleStr.trim().isEmpty()) {
            try {
                role = UserRole.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Dòng " + rowNumber + ": Vai trò không hợp lệ: " + roleStr);
            }
        }

        // Validate email format
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Dòng " + rowNumber + ": Email không hợp lệ: " + email);
        }

        // Validate phone format
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Dòng " + rowNumber + ": Số điện thoại không hợp lệ: " + phone);
        }

        // Parse date
        LocalDate dateOfBirth = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                dateOfBirth = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Dòng " + rowNumber + ": Ngày sinh không hợp lệ (định dạng dd/MM/yyyy): " + dateStr);
            }
        }

        // Parse gender
        Gender gender = null;
        if (genderStr != null && !genderStr.trim().isEmpty()) {
            try {
                gender = Gender.valueOf(genderStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Dòng " + rowNumber + ": Giới tính không hợp lệ: " + genderStr);
            }
        }

        return UserCreateRequest.builder()
                .fullName(fullName.trim())
                .email(email != null ? email.trim() : null)
                .phone(phone != null ? phone.trim() : null)
                .role(role)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .address(address != null ? address.trim() : null)
                .build();
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
}
