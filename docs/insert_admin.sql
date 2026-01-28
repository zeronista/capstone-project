-- ============================================
-- THÊM TÀI KHOẢN ADMIN VÀO HỆ THỐNG
-- ============================================
-- Script để tạo tài khoản quản trị viên mặc định
-- Cập nhật: 28/01/2026 - Phù hợp với cấu trúc users + user_info

USE capstone_project;

-- Bước 1: Thêm tài khoản vào bảng users (thông tin bảo mật)
-- Mật khẩu: admin123 (đã mã hóa BCrypt)
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES (
    'admin@capstone.com',
    '0123456789',
    '$2a$12$.fQPqwPI6wfeHkqgY18w/.DQFEWYOXQEVD2krTJlJsyTzfppbPY16', -- BCrypt hash for "admin123"
    'ADMIN',
    TRUE,
    TRUE,
    NOW(),
    NOW()
);

-- Bước 2: Lấy ID vừa tạo và thêm thông tin cá nhân vào bảng user_info
INSERT INTO user_info (user_id, full_name, created_at, updated_at)
VALUES (
    LAST_INSERT_ID(),  -- ID của user vừa tạo ở trên
    'System Administrator',
    NOW(),
    NOW()
);

-- ============================================
-- KIỂM TRA KẾT QUẢ
-- ============================================

-- Xem thông tin tài khoản vừa tạo
SELECT 
    u.id,
    u.email,
    u.phone,
    u.role,
    u.is_active,
    u.email_verified,
    ui.full_name,
    u.created_at
FROM users u
LEFT JOIN user_info ui ON u.id = ui.user_id
WHERE u.email = 'admin@capstone.com';

-- ============================================
-- LƯU Ý
-- ============================================
-- 1. Mật khẩu mặc định: admin123
-- 2. Email: admin@capstone.com  
-- 3. SĐT: 0123456789
-- 4. Vai trò: ADMIN (toàn quyền)
-- 5. Tài khoản đã được kích hoạt và xác thực

-- QUAN TRỌNG: Sau khi đăng nhập lần đầu, hãy đổi mật khẩu ngay!
