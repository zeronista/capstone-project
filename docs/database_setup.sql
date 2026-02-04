-- ============================================
-- CAPSTONE PROJECT - MySQL Database Setup
-- ============================================
-- Tạo database và cấu hình cho ứng dụng
-- Cập nhật: 26/01/2026 - Tách bảng users thành users + user_info

-- Tạo database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS capstone_project
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Sử dụng database
USE capstone_project;

-- Tạo user cho ứng dụng (tùy chọn - để tăng bảo mật)
-- CHỈNH SỬA password theo yêu cầu của bạn
-- CREATE USER IF NOT EXISTS 'capstone_user'@'localhost' IDENTIFIED BY 'secure_password_here';
-- GRANT ALL PRIVILEGES ON capstone_project.* TO 'capstone_user'@'localhost';
-- FLUSH PRIVILEGES;

-- ============================================
-- LƯU Ý: Không cần tạo bảng thủ công
-- ============================================
-- Spring Boot JPA sẽ tự động tạo các bảng dựa trên Entity classes
-- với cấu hình: spring.jpa.hibernate.ddl-auto=update
--
-- Các bảng sẽ được tạo tự động:
-- - users (thông tin bảo mật & đăng nhập)
-- - user_info (thông tin cá nhân)
-- - staff_info (thông tin nhân viên)
-- - patient_documents
-- - prescriptions
-- - prescription_details
-- - tickets
-- - ticket_messages
-- - treatment_plans
-- - treatment_plan_items
-- - call_campaigns
-- - call_logs
-- - survey_templates
-- - notifications
-- - feedbacks
-- - knowledge_base
-- - ai_training_data

-- ============================================
-- CẤU TRÚC BẢNG CHÍNH (Tham khảo)
-- ============================================

-- Bảng USERS - Thông tin bảo mật & đăng nhập (required fields)
-- +------------------+---------------+------+-----+---------+----------------+
-- | Field            | Type          | Null | Key | Default | Extra          |
-- +------------------+---------------+------+-----+---------+----------------+
-- | id               | bigint        | NO   | PRI | NULL    | auto_increment |
-- | email            | varchar(100)  | YES  | UNI | NULL    |                |
-- | phone            | varchar(20)   | YES  | UNI | NULL    |                |
-- | password_hash    | varchar(255)  | YES  |     | NULL    |                |
-- | google_id        | varchar(100)  | YES  | UNI | NULL    |                |
-- | role             | varchar(20)   | NO   |     | PATIENT |                |
-- | is_active        | tinyint(1)    | NO   |     | 1       |                |
-- | email_verified   | tinyint(1)    | NO   |     | 0       |                |
-- | phone_verified   | tinyint(1)    | NO   |     | 0       |                |
-- | created_at       | datetime      | NO   |     | NULL    |                |
-- | updated_at       | datetime      | NO   |     | NULL    |                |
-- | last_login       | datetime      | YES  |     | NULL    |                |
-- +------------------+---------------+------+-----+---------+----------------+

-- Bảng USER_INFO - Thông tin cá nhân (nullable fields)
-- +---------------+---------------+------+-----+---------+----------------+
-- | Field         | Type          | Null | Key | Default | Extra          |
-- +---------------+---------------+------+-----+---------+----------------+
-- | id            | bigint        | NO   | PRI | NULL    | auto_increment |
-- | user_id       | bigint        | NO   | UNI | NULL    | FK -> users.id |
-- | full_name     | varchar(100)  | YES  |     | NULL    |                |
-- | date_of_birth | date          | YES  |     | NULL    |                |
-- | gender        | varchar(10)   | YES  |     | NULL    |                |
-- | address       | varchar(500)  | YES  |     | NULL    |                |
-- | avatar_url    | varchar(500)  | YES  |     | NULL    |                |
-- | created_at    | datetime      | NO   |     | NULL    |                |
-- | updated_at    | datetime      | NO   |     | NULL    |                |
-- +---------------+---------------+------+-----+---------+----------------+

-- Kiểm tra các bảng đã được tạo
SHOW TABLES;

-- Xem cấu trúc bảng users (sau khi chạy app lần đầu)
-- DESCRIBE users;
-- DESCRIBE user_info;

-- ============================================
-- MIGRATION SCRIPT (Nếu có dữ liệu cũ)
-- ============================================
-- Chạy script này NẾU bạn có dữ liệu trong bảng users cũ
-- và muốn migrate sang cấu trúc mới

-- Bước 1: Tạo bảng user_info nếu chưa tồn tại
-- (Spring Boot sẽ tự tạo, nhưng có thể tạo thủ công)
/*
CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(10),
    address VARCHAR(500),
    avatar_url VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_info_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
*/

-- Bước 2: Migrate dữ liệu từ users cũ sang user_info
-- (Chỉ chạy nếu bảng users CŨ có các cột full_name, date_of_birth, gender, address, avatar_url)
/*
INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, avatar_url, created_at, updated_at)
SELECT id, full_name, date_of_birth, gender, address, avatar_url, created_at, updated_at
FROM users
WHERE id NOT IN (SELECT user_id FROM user_info);
*/

-- Bước 3: Xóa các cột thông tin cá nhân khỏi bảng users (SAU KHI đã migrate)
-- CẢNH BÁO: Chỉ chạy sau khi đã backup và migrate thành công!
/*
ALTER TABLE users 
    DROP COLUMN IF EXISTS full_name,
    DROP COLUMN IF EXISTS date_of_birth,
    DROP COLUMN IF EXISTS gender,
    DROP COLUMN IF EXISTS address,
    DROP COLUMN IF EXISTS avatar_url;
*/

-- ============================================
-- KIỂM TRA SAU KHI MIGRATE
-- ============================================
-- SELECT u.id, u.email, u.role, ui.full_name, ui.date_of_birth 
-- FROM users u 
-- LEFT JOIN user_info ui ON u.id = ui.user_id 
-- LIMIT 10;
