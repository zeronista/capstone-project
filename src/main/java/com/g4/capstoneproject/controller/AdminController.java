package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.AccountResponse;
import com.g4.capstoneproject.dto.AssignRoleRequest;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho Admin quản lý accounts
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final AdminService adminService;
    
    /**
     * GET /api/admin/accounts - Lấy danh sách tất cả accounts
     */
    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<AccountResponse> accountsPage = adminService.getAllAccounts(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accounts", accountsPage.getContent());
            response.put("currentPage", accountsPage.getNumber());
            response.put("totalItems", accountsPage.getTotalElements());
            response.put("totalPages", accountsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching accounts", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể lấy danh sách tài khoản");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * GET /api/admin/accounts/non-patient - Lấy danh sách accounts không phải bệnh nhân
     */
    @GetMapping("/accounts/non-patient")
    public ResponseEntity<Map<String, Object>> getNonPatientAccounts() {
        try {
            List<AccountResponse> accounts = adminService.getAllNonPatientAccounts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accounts", accounts);
            response.put("total", accounts.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching non-patient accounts", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể lấy danh sách tài khoản");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * GET /api/admin/accounts/search - Tìm kiếm accounts
     */
    @GetMapping("/accounts/search")
    public ResponseEntity<Map<String, Object>> searchAccounts(
            @RequestParam String keyword) {
        
        try {
            List<AccountResponse> accounts = adminService.searchAccounts(keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accounts", accounts);
            response.put("total", accounts.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching accounts with keyword: {}", keyword, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể tìm kiếm tài khoản");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * GET /api/admin/accounts/filter - Lọc accounts theo role
     */
    @GetMapping("/accounts/filter")
    public ResponseEntity<Map<String, Object>> filterAccountsByRole(
            @RequestParam User.UserRole role) {
        
        try {
            List<AccountResponse> accounts = adminService.filterAccountsByRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accounts", accounts);
            response.put("total", accounts.size());
            response.put("role", role);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error filtering accounts by role: {}", role, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể lọc tài khoản");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * GET /api/admin/accounts/{id} - Lấy thông tin chi tiết một account
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<Map<String, Object>> getAccountById(@PathVariable Long id) {
        try {
            AccountResponse account = adminService.getAccountById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("account", account);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error fetching account by id: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể lấy thông tin tài khoản");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * PUT /api/admin/accounts/assign-role - Assign role cho user
     */
    @PutMapping("/accounts/assign-role")
    public ResponseEntity<Map<String, Object>> assignRole(
            @Valid @RequestBody AssignRoleRequest request) {
        
        try {
            AccountResponse account = adminService.assignRole(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật vai trò thành công");
            response.put("account", account);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error assigning role", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể cập nhật vai trò");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * PUT /api/admin/accounts/{id}/toggle-status - Vô hiệu hóa/Kích hoạt account
     */
    @PutMapping("/accounts/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleAccountStatus(@PathVariable Long id) {
        try {
            AccountResponse account = adminService.toggleAccountStatus(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", account.getEnabled() 
                    ? "Kích hoạt tài khoản thành công" 
                    : "Vô hiệu hóa tài khoản thành công");
            response.put("account", account);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error toggling account status for id: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể cập nhật trạng thái tài khoản");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * GET /api/admin/accounts/statistics - Lấy thống kê accounts
     */
    @GetMapping("/accounts/statistics")
    public ResponseEntity<Map<String, Object>> getAccountStatistics() {
        try {
            Map<String, Long> stats = adminService.getAccountStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching account statistics", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể lấy thống kê tài khoản");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
