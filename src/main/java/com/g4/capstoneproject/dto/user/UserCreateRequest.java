package com.g4.capstoneproject.dto.user;

import com.g4.capstoneproject.entity.Gender;
import com.g4.capstoneproject.entity.User.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho request tạo người dùng mới
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2-100 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$|^$", message = "Số điện thoại không hợp lệ (VD: 0901234567)")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @NotNull(message = "Vai trò không được để trống")
    private UserRole role;

    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String address;

    /**
     * Mật khẩu (nếu không cung cấp, hệ thống sẽ tự tạo)
     */
    @Size(min = 8, max = 100, message = "Mật khẩu phải từ 8-100 ký tự")
    private String password;

    /**
     * Kiểm tra xem có ít nhất email hoặc phone không
     */
    public boolean hasEmailOrPhone() {
        return (email != null && !email.trim().isEmpty()) ||
                (phone != null && !phone.trim().isEmpty());
    }
}
