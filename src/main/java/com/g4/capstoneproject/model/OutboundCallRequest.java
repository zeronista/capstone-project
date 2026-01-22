package com.g4.capstoneproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request gọi ra (Outbound Call)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundCallRequest {
    
    /**
     * Số Voice Brandname đã đăng ký với Stringee
     * Ví dụ: "842873008xxx"
     */
    private String fromNumber;
    
    /**
     * Số điện thoại khách hàng
     * Định dạng quốc tế: 84xxxxxxxxx
     */
    private String toNumber;
    
    /**
     * Tên hiển thị trên điện thoại khách hàng (optional)
     * Mặc định: "Phòng Khám"
     */
    private String brandName;
    
    /**
     * Loại cuộc gọi (optional)
     * Ví dụ: "appointment_reminder", "survey", "emergency"
     */
    private String callType;
    
    /**
     * Metadata bổ sung (optional)
     * Ví dụ: patientId, appointmentId, etc.
     */
    private String metadata;
}
