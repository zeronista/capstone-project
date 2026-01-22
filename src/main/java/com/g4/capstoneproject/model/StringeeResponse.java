package com.g4.capstoneproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response của Stringee API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StringeeResponse {
    
    /**
     * HTTP status code
     */
    private Integer r;
    
    /**
     * Thông báo từ Stringee
     */
    private String message;
    
    /**
     * ID của cuộc gọi (nếu tạo thành công)
     */
    private String callId;
    
    /**
     * Trạng thái cuộc gọi
     */
    private String status;
}
