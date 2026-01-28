package com.g4.capstoneproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for Validation Error Response
 * Used to return validation errors to the client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationErrorResponse {

    private String message;
    private int status;
    private long timestamp;
    private Map<String, List<String>> errors;

    /**
     * Create a validation error response with field errors
     */
    public static ValidationErrorResponse of(String message, Map<String, List<String>> errors) {
        return ValidationErrorResponse.builder()
                .message(message)
                .status(400)
                .timestamp(System.currentTimeMillis())
                .errors(errors)
                .build();
    }

    /**
     * Create a validation error response with a single message
     */
    public static ValidationErrorResponse of(String message) {
        return ValidationErrorResponse.builder()
                .message(message)
                .status(400)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
