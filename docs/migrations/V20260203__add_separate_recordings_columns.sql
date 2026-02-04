-- Migration: Thêm các cột để lưu ghi âm riêng biệt cho caller, receiver và combined
-- Date: 2026-02-03
-- Description: Hỗ trợ ghi âm riêng từng người trong cuộc gọi web-to-web

-- Thêm cột recording_folder để lưu đường dẫn folder chứa recordings
ALTER TABLE web_call_logs 
ADD COLUMN IF NOT EXISTS recording_folder VARCHAR(500) NULL COMMENT 'S3 Folder chứa tất cả recordings của cuộc gọi này';

-- Thêm cột để lưu S3 key của file ghi âm caller
ALTER TABLE web_call_logs 
ADD COLUMN IF NOT EXISTS recording_caller_s3_key VARCHAR(500) NULL COMMENT 'S3 Key của file ghi âm người gọi (caller)';

-- Thêm cột để lưu S3 key của file ghi âm receiver
ALTER TABLE web_call_logs 
ADD COLUMN IF NOT EXISTS recording_receiver_s3_key VARCHAR(500) NULL COMMENT 'S3 Key của file ghi âm người nhận (receiver)';

-- Thêm cột để lưu pre-signed URL cho caller recording
ALTER TABLE web_call_logs 
ADD COLUMN IF NOT EXISTS recording_caller_url VARCHAR(1000) NULL COMMENT 'URL pre-signed để nghe lại file caller';

-- Thêm cột để lưu pre-signed URL cho receiver recording
ALTER TABLE web_call_logs 
ADD COLUMN IF NOT EXISTS recording_receiver_url VARCHAR(1000) NULL COMMENT 'URL pre-signed để nghe lại file receiver';

-- Thêm index cho recording_folder để tìm kiếm nhanh
CREATE INDEX IF NOT EXISTS idx_webcall_recording_folder ON web_call_logs(recording_folder);

-- Comment giải thích cấu trúc folder mới trên S3:
-- voice/calls/{callId}/
--   ├── caller_{timestamp}.webm      - Ghi âm của người gọi
--   ├── receiver_{timestamp}.webm    - Ghi âm của người nhận  
--   └── combined_{timestamp}.webm    - Ghi âm chung cả 2 người
