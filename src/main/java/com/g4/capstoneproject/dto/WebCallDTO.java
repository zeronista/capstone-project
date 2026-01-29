package com.g4.capstoneproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho Web Call Log
 * Dùng để trả về thông tin cuộc gọi cho frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebCallDTO {
    
    private Long id;
    
    private String stringeeCallId;
    
    /**
     * true = cuộc gọi đi, false = cuộc gọi đến
     */
    private Boolean isOutgoing;
    
    /**
     * ID của người còn lại trong cuộc gọi
     */
    private Long otherUserId;
    
    /**
     * Tên người còn lại
     */
    private String otherUserName;
    
    /**
     * Avatar của người còn lại
     */
    private String otherUserAvatar;
    
    /**
     * Role của người còn lại
     */
    private String otherUserRole;
    
    /**
     * Trạng thái cuộc gọi
     */
    private String callStatus;
    
    /**
     * Thời gian bắt đầu
     */
    private LocalDateTime startTime;
    
    /**
     * Thời gian kết thúc
     */
    private LocalDateTime endTime;
    
    /**
     * Thời lượng (giây)
     */
    private Integer duration;
    
    /**
     * Thời lượng đã format (mm:ss)
     */
    private String durationFormatted;
    
    /**
     * Có ghi âm không
     */
    private Boolean hasRecording;
    
    /**
     * URL để nghe ghi âm
     */
    private String recordingUrl;
    
    /**
     * Đánh giá (1-5)
     */
    private Integer rating;
    
    /**
     * Ghi chú
     */
    private String notes;
    
    /**
     * Thời gian tạo record
     */
    private LocalDateTime createdAt;
}
