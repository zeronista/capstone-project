package com.g4.capstoneproject.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu đăng ký
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    
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
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu phải từ 8-100 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số")
    private String password;
    
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
    
    /**
     * Kiểm tra xem có ít nhất email hoặc phone không
     */
    public boolean hasEmailOrPhone() {
        return (email != null && !email.trim().isEmpty()) || 
               (phone != null && !phone.trim().isEmpty());
    }
    
    /**
     * Kiểm tra xem mật khẩu có khớp không
     */
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
