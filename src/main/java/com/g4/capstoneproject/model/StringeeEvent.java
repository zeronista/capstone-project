package com.g4.capstoneproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO cho Stringee Event Webhook
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StringeeEvent {
    
    /**
     * Loại event
     * Ví dụ: "call.started", "call.answered", "call.ended", "recording.available"
     */
    private String event;
    
    /**
     * ID của cuộc gọi
     */
    private String callId;
    
    /**
     * User ID (nếu có)
     */
    private String userId;
    
    /**
     * Số gọi đi
     */
    private String from;
    
    /**
     * Số nhận
     */
    private String to;
    
    /**
     * Thời lượng cuộc gọi (giây)
     */
    private Integer duration;
    
    /**
     * Trạng thái cuộc gọi
     */
    private String status;
    
    /**
     * URL file ghi âm (nếu có)
     */
    private String recordingUrl;
    
    /**
     * Thời gian bắt đầu
     */
    private Long startTime;
    
    /**
     * Thời gian kết thúc
     */
    private Long endTime;
    
    /**
     * Dữ liệu custom bổ sung
     */
    private Map<String, Object> customData;
}
