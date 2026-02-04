-- ============================================
-- INSERT TEST ACCOUNTS - Tài khoản test gọi điện
-- ============================================
-- Chạy script này để tạo tài khoản test cho việc gọi điện
-- Bao gồm: 1 Lễ tân + 2 Bệnh nhân
-- Password: 123456

USE capstone_project;

-- Password hash cho "123456" bằng BCrypt
SET @password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjEEPP7Jv7YxOZx7bKKQwbT0P4vS.C';

-- ============================================
-- 1. TÀI KHOẢN LỄ TÂN (RECEPTIONIST)
-- ============================================
-- Kiểm tra xem đã tồn tại chưa
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
SELECT 'receptionist@test.com', '0900000001', @password_hash, 'RECEPTIONIST', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'receptionist@test.com');

SET @receptionist_id = (SELECT id FROM users WHERE email = 'receptionist@test.com');

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
SELECT @receptionist_id, 'Lễ Tân Test', '1995-01-15', 'FEMALE', 'ABClinic, Quận 1, TP.HCM', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM user_info WHERE user_id = @receptionist_id);

-- ============================================
-- 2. TÀI KHOẢN BỆNH NHÂN 1 (PATIENT)
-- ============================================
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
SELECT 'patient1@test.com', '0900000002', @password_hash, 'PATIENT', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'patient1@test.com');

SET @patient1_id = (SELECT id FROM users WHERE email = 'patient1@test.com');

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
SELECT @patient1_id, 'Bệnh Nhân Test 1', '1990-05-20', 'MALE', '123 Nguyễn Huệ, Quận 1, TP.HCM', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM user_info WHERE user_id = @patient1_id);

-- ============================================
-- 3. TÀI KHOẢN BỆNH NHÂN 2 (PATIENT)
-- ============================================
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
SELECT 'patient2@test.com', '0900000003', @password_hash, 'PATIENT', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'patient2@test.com');

SET @patient2_id = (SELECT id FROM users WHERE email = 'patient2@test.com');

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
SELECT @patient2_id, 'Bệnh Nhân Test 2', '1988-12-10', 'FEMALE', '456 Lê Lợi, Quận 3, TP.HCM', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM user_info WHERE user_id = @patient2_id);

-- ============================================
-- HIỂN THỊ KẾT QUẢ
-- ============================================
SELECT 
    '=== TÀI KHOẢN TEST ĐÃ TẠO ===' AS info;

SELECT 
    u.id,
    u.email,
    u.phone,
    u.role,
    ui.full_name,
    CONCAT('user_', u.id) AS stringee_user_id,
    '123456' AS password
FROM users u
LEFT JOIN user_info ui ON u.id = ui.user_id
WHERE u.email IN ('receptionist@test.com', 'patient1@test.com', 'patient2@test.com')
ORDER BY u.role, u.id;

-- ============================================
-- HƯỚNG DẪN SỬ DỤNG
-- ============================================
/*
CÁCH TEST GỌI ĐIỆN (2 TAB):

1. Mở 2 tab trình duyệt (hoặc 2 trình duyệt khác nhau - Chrome & Firefox)

2. TAB 1 - Đăng nhập LỄ TÂN:
   - URL: http://localhost:8080/login
   - Email: receptionist@test.com
   - Password: 123456
   - Sau khi đăng nhập, vào: http://localhost:8080/receptionist/callbot
   - Chọn bệnh nhân muốn gọi, bấm "Gọi ngay"

3. TAB 2 - Đăng nhập BỆNH NHÂN:
   - URL: http://localhost:8080/login
   - Email: patient1@test.com hoặc patient2@test.com
   - Password: 123456
   - Sau khi đăng nhập, vào: http://localhost:8080/patient/call
   - ĐỢI nhận cuộc gọi từ lễ tân

4. QUY TRÌNH TEST:
   a) Cả 2 tab phải đăng nhập và vào trang call TRƯỚC
   b) Bệnh nhân tab phải hiện "Đã kết nối" (xanh lá)
   c) Lễ tân chọn đúng bệnh nhân đã đăng nhập để gọi
   d) Khi lễ tân gọi, bệnh nhân sẽ thấy popup cuộc gọi đến
   e) Bệnh nhân bấm "Trả lời" (nút xanh) để nghe

LƯU Ý QUAN TRỌNG:
- Stringee User ID của mỗi người dùng là: user_{id}
- Bệnh nhân PHẢI vào trang /patient/call và đợi ở đó
- Kiểm tra trạng thái "Đã kết nối" trước khi gọi
- Nếu không kết nối được, refresh lại trang

DANH SÁCH TÀI KHOẢN TEST:
+----+----------------------+-------------+-------------+
| ID | Email                | Password    | Role        |
+----+----------------------+-------------+-------------+
| ?  | receptionist@test.com| 123456      | RECEPTIONIST|
| ?  | patient1@test.com    | 123456      | PATIENT     |
| ?  | patient2@test.com    | 123456      | PATIENT     |
+----+----------------------+-------------+-------------+
(ID sẽ được tự động tạo khi insert)
*/
