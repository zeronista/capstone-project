package com.g4.capstoneproject.controller.api;

import com.g4.capstoneproject.dto.user.*;
import com.g4.capstoneproject.entity.User.UserRole;
import com.g4.capstoneproject.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho quản lý người dùng
 * Dành cho Receptionist và Admin
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "API quản lý người dùng (CRUD + Import Excel)")
public class UserManagementApiController {

    private final UserManagementService userManagementService;

    // ==================== CRUD Endpoints ====================

    /**
     * Lấy danh sách tất cả người dùng
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Lấy danh sách người dùng", description = "Lấy tất cả người dùng, có thể filter theo role")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @Parameter(description = "Filter theo vai trò") @RequestParam(required = false) UserRole role) {

        List<UserResponse> users;
        if (role != null) {
            users = userManagementService.getUsersByRole(role);
        } else {
            users = userManagementService.getAllUsers();
        }
        return ResponseEntity.ok(users);
    }

    /**
     * Tìm kiếm người dùng
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Tìm kiếm người dùng", description = "Tìm kiếm theo tên, email, hoặc số điện thoại")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword) {

        List<UserResponse> users = userManagementService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    /**
     * Lấy thông tin người dùng theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Lấy thông tin người dùng", description = "Lấy chi tiết một người dùng theo ID")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userManagementService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tạo người dùng mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Tạo người dùng mới", description = "Tạo một người dùng mới với thông tin đầy đủ")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            UserResponse created = userManagementService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cập nhật người dùng
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Cập nhật người dùng", description = "Cập nhật thông tin người dùng theo ID")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        try {
            UserResponse updated = userManagementService.updateUser(id, request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa người dùng (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Xóa người dùng", description = "Vô hiệu hóa người dùng (soft delete)")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userManagementService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa người dùng thành công"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Khôi phục người dùng đã xóa
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Khôi phục người dùng", description = "Kích hoạt lại người dùng đã bị vô hiệu hóa")
    public ResponseEntity<?> restoreUser(@PathVariable Long id) {
        try {
            UserResponse restored = userManagementService.restoreUser(id);
            return ResponseEntity.ok(restored);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Excel Import/Export Endpoints ====================

    /**
     * Import người dùng từ file Excel
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Import từ Excel", description = "Import danh sách người dùng từ file Excel")
    public ResponseEntity<?> importUsers(
            @Parameter(description = "File Excel (.xlsx)") @RequestParam("file") MultipartFile file) {

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Vui lòng chọn file để upload"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chỉ chấp nhận file Excel (.xlsx hoặc .xls)"));
        }

        try {
            UserImportResult result = userManagementService.importUsersFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error importing Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi đọc file Excel: " + e.getMessage()));
        }
    }

    /**
     * Download file Excel mẫu
     */
    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Download mẫu Excel", description = "Tải file Excel mẫu để import người dùng")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = userManagementService.generateExcelTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "user_import_template.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(template);
        } catch (IOException e) {
            log.error("Error generating Excel template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
