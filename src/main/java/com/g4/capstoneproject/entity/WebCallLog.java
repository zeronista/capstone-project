package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity WebCallLog - Lịch sử cuộc gọi Web-to-Web
 * Dành riêng cho cuộc gọi giữa 2 user đã đăng nhập qua trình duyệt
 */
@Entity
@Table(name = "web_call_logs", indexes = {
    @Index(name = "idx_webcall_caller", columnList = "caller_id"),
    @Index(name = "idx_webcall_receiver", columnList = "receiver_id"),
    @Index(name = "idx_webcall_start_time", columnList = "start_time"),
    @Index(name = "idx_webcall_status", columnList = "call_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebCallLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Stringee Call ID để tracking
     */
    @Column(name = "stringee_call_id", length = 100)
    private String stringeeCallId;
    
    /**
     * Người gọi
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;
    
    /**
     * Người nhận cuộc gọi
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
    
    /**
     * Trạng thái cuộc gọi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "call_status", nullable = false, length = 20)
    @Builder.Default
    private WebCallStatus callStatus = WebCallStatus.INITIATED;
    
    /**
     * Thời gian bắt đầu gọi
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * Thời gian kết thúc
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * Thời lượng cuộc gọi (giây)
     */
    @Column(name = "duration")
    private Integer duration;
    
    /**
     * S3 Folder chứa tất cả recordings của cuộc gọi này
     * Format: voice/calls/{callId}/
     */
    @Column(name = "recording_folder", length = 500)
    private String recordingFolder;
    
    /**
     * S3 Key của file ghi âm chung (combined)
     */
    @Column(name = "recording_s3_key", length = 500)
    private String recordingS3Key;
    
    /**
     * S3 Key của file ghi âm người gọi (caller)
     */
    @Column(name = "recording_caller_s3_key", length = 500)
    private String recordingCallerS3Key;
    
    /**
     * S3 Key của file ghi âm người nhận (receiver)
     */
    @Column(name = "recording_receiver_s3_key", length = 500)
    private String recordingReceiverS3Key;
    
    /**
     * URL pre-signed để nghe lại file combined (tạm thời, hết hạn 7 ngày)
     */
    @Column(name = "recording_url", length = 1000)
    private String recordingUrl;
    
    /**
     * URL pre-signed để nghe lại file caller
     */
    @Column(name = "recording_caller_url", length = 1000)
    private String recordingCallerUrl;
    
    /**
     * URL pre-signed để nghe lại file receiver
     */
    @Column(name = "recording_receiver_url", length = 1000)
    private String recordingReceiverUrl;
    
    /**
     * Thời gian hết hạn của recording URL
     */
    @Column(name = "recording_url_expiry")
    private LocalDateTime recordingUrlExpiry;
    
    /**
     * Transcript text (nếu có AI phân tích)
     */
    @Column(name = "transcript_text", columnDefinition = "TEXT")
    private String transcriptText;
    
    /**
     * Đánh giá chất lượng cuộc gọi (1-5 sao)
     */
    @Column(name = "rating")
    private Integer rating;
    
    /**
     * Ghi chú của người dùng
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    /**
     * Cuộc gọi có ghi âm không
     */
    @Builder.Default
    @Column(name = "has_recording")
    private Boolean hasRecording = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Enum trạng thái cuộc gọi web
     */
    public enum WebCallStatus {
        INITIATED,      // Đã khởi tạo, đang đổ chuông
        RINGING,        // Đang đổ chuông phía receiver
        ANSWERED,       // Đã bắt máy, đang nói chuyện
        COMPLETED,      // Hoàn thành (kết thúc bình thường)
        MISSED,         // Người nhận không bắt máy
        REJECTED,       // Người nhận từ chối
        CANCELLED,      // Người gọi hủy
        FAILED          // Lỗi kỹ thuật
    }
}
