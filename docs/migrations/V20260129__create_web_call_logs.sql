-- =====================================================
-- Migration Script: Web Call Logs
-- Description: Tạo bảng lưu trữ lịch sử cuộc gọi Web-to-Web
-- Date: 2026-01-29
-- =====================================================

-- Tạo bảng web_call_logs
CREATE TABLE IF NOT EXISTS web_call_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Stringee tracking
    stringee_call_id VARCHAR(100),
    
    -- Người tham gia cuộc gọi
    caller_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    
    -- Trạng thái cuộc gọi
    call_status ENUM('INITIATED', 'RINGING', 'ANSWERED', 'COMPLETED', 'MISSED', 'REJECTED', 'CANCELLED', 'FAILED') NOT NULL DEFAULT 'INITIATED',
    
    -- Thời gian
    start_time DATETIME,
    end_time DATETIME,
    duration INT DEFAULT 0 COMMENT 'Thời lượng tính bằng giây',
    
    -- Ghi âm
    recording_s3_key VARCHAR(500) COMMENT 'S3 key của file ghi âm',
    recording_url VARCHAR(1000) COMMENT 'Pre-signed URL để nghe',
    recording_url_expiry DATETIME COMMENT 'Thời gian hết hạn URL',
    has_recording BOOLEAN DEFAULT FALSE,
    
    -- AI Analysis
    transcript_text TEXT COMMENT 'Nội dung cuộc gọi (AI transcription)',
    
    -- Đánh giá
    rating TINYINT COMMENT 'Đánh giá 1-5 sao',
    notes VARCHAR(500) COMMENT 'Ghi chú của người dùng',
    
    -- Timestamps
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_webcall_caller FOREIGN KEY (caller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_webcall_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_webcall_caller (caller_id),
    INDEX idx_webcall_receiver (receiver_id),
    INDEX idx_webcall_start_time (start_time),
    INDEX idx_webcall_status (call_status),
    INDEX idx_webcall_stringee_id (stringee_call_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Sample Data (Optional - cho testing)
-- =====================================================

-- Uncomment các dòng dưới để insert sample data
/*
INSERT INTO web_call_logs (stringee_call_id, caller_id, receiver_id, call_status, start_time, end_time, duration, has_recording, created_at)
VALUES 
    ('call_sample_001', 1, 2, 'COMPLETED', '2026-01-28 10:00:00', '2026-01-28 10:05:30', 330, FALSE, '2026-01-28 10:00:00'),
    ('call_sample_002', 2, 1, 'COMPLETED', '2026-01-28 14:00:00', '2026-01-28 14:02:15', 135, TRUE, '2026-01-28 14:00:00'),
    ('call_sample_003', 1, 3, 'MISSED', '2026-01-29 09:00:00', NULL, 0, FALSE, '2026-01-29 09:00:00'),
    ('call_sample_004', 3, 1, 'REJECTED', '2026-01-29 11:00:00', NULL, 0, FALSE, '2026-01-29 11:00:00');
*/

-- =====================================================
-- Useful Queries
-- =====================================================

-- Lấy tất cả cuộc gọi của user (cả gọi đi và gọi đến)
-- SELECT * FROM web_call_logs WHERE caller_id = ? OR receiver_id = ? ORDER BY created_at DESC;

-- Đếm cuộc gọi nhỡ
-- SELECT COUNT(*) FROM web_call_logs WHERE receiver_id = ? AND call_status = 'MISSED';

-- Tổng thời gian gọi (giây)
-- SELECT SUM(duration) FROM web_call_logs WHERE (caller_id = ? OR receiver_id = ?) AND call_status = 'COMPLETED';

-- Thống kê theo ngày
-- SELECT DATE(start_time) as date, COUNT(*) as count 
-- FROM web_call_logs 
-- WHERE (caller_id = ? OR receiver_id = ?) AND start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
-- GROUP BY DATE(start_time);
