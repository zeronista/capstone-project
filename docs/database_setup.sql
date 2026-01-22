-- ============================================
-- CAPSTONE PROJECT - MySQL Database Setup
-- ============================================
-- Tạo database và cấu hình cho ứng dụng

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
-- - users (User entity)
-- - prescriptions
-- - tickets
-- - treatment_plans
-- - etc.

-- Kiểm tra các bảng đã được tạo
SHOW TABLES;

-- Xem cấu trúc bảng users (sau khi chạy app lần đầu)
-- DESCRIBE users;
