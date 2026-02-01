package com.g4.capstoneproject.dto.user;

import com.g4.capstoneproject.entity.Gender;
import com.g4.capstoneproject.entity.User.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho import danh sách người dùng từ Excel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserImportRequest {

    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;

    // Row number in Excel for error reporting
    private int rowNumber;

    // Validation errors for this row
    private List<String> errors;

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
