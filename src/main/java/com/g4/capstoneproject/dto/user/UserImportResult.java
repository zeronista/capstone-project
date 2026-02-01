package com.g4.capstoneproject.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO cho kết quả import từ Excel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserImportResult {

    @Builder.Default
    private int totalRows = 0;

    @Builder.Default
    private int successCount = 0;

    @Builder.Default
    private int errorCount = 0;

    @Builder.Default
    private List<UserImportError> errors = new ArrayList<>();

    @Builder.Default
    private List<UserResponse> createdUsers = new ArrayList<>();

    public void addError(int rowNumber, String field, String message) {
        errors.add(new UserImportError(rowNumber, field, message));
        errorCount++;
    }

    public void addSuccess(UserResponse user) {
        createdUsers.add(user);
        successCount++;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserImportError {
        private int rowNumber;
        private String field;
        private String message;
    }
}
