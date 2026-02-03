package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu cập nhật tài khoản (dành cho admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountRequest {
    
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2-100 ký tự")
    private String fullName;
    
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;
    
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$|^$", 
             message = "Số điện thoại không hợp lệ (VD: 0901234567)")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;
    
    // Role không bắt buộc khi update (không cho phép thay đổi role khi edit)
    private User.UserRole role;
    
    /**
     * Kiểm tra xem có ít nhất email hoặc phone không
     */
    public boolean hasEmailOrPhone() {
        return (email != null && !email.trim().isEmpty()) || 
               (phone != null && !phone.trim().isEmpty());
    }
}
