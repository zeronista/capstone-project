-- ============================================
-- INSERT PATIENTS - Dữ liệu bệnh nhân mẫu
-- ============================================
-- Chạy script này để thêm bệnh nhân mẫu vào database
-- Lưu ý: Password mặc định là "123456" (đã hash bằng BCrypt)

USE capstone_project;

-- ============================================
-- BƯỚC 1: Thêm users (bệnh nhân)
-- ============================================
-- Password hash cho "123456" bằng BCrypt
SET @password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjEEPP7Jv7YxOZx7bKKQwbT0P4vS.C';

-- Bệnh nhân 1: Nguyễn Văn An
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('nguyenvanan@gmail.com', '0901234567', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient1_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient1_id, 'Nguyễn Văn An', '1990-05-15', 'MALE', '123 Nguyễn Huệ, Quận 1, TP.HCM', NOW(), NOW());

-- Bệnh nhân 2: Trần Thị Bình
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('tranthibinh@gmail.com', '0912345678', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient2_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient2_id, 'Trần Thị Bình', '1985-08-22', 'FEMALE', '456 Lê Lợi, Quận 3, TP.HCM', NOW(), NOW());

-- Bệnh nhân 3: Lê Hoàng Cường
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('lehoangcuong@gmail.com', '0923456789', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient3_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient3_id, 'Lê Hoàng Cường', '1978-12-01', 'MALE', '789 Hai Bà Trưng, Quận Bình Thạnh, TP.HCM', NOW(), NOW());

-- Bệnh nhân 4: Phạm Minh Đức
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('phamminhduc@gmail.com', '0934567890', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient4_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient4_id, 'Phạm Minh Đức', '1995-03-10', 'MALE', '321 Võ Văn Tần, Quận 10, TP.HCM', NOW(), NOW());

-- Bệnh nhân 5: Hoàng Thị Em
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('hoangthiem@gmail.com', '0945678901', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient5_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient5_id, 'Hoàng Thị Em', '2000-07-25', 'FEMALE', '654 Cách Mạng Tháng 8, Quận Tân Bình, TP.HCM', NOW(), NOW());

-- Bệnh nhân 6: Võ Văn Phúc
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('vovanphuc@gmail.com', '0956789012', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient6_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient6_id, 'Võ Văn Phúc', '1988-11-30', 'MALE', '987 Phan Xích Long, Quận Phú Nhuận, TP.HCM', NOW(), NOW());

-- Bệnh nhân 7: Ngô Thị Giang
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('ngothigiang@gmail.com', '0967890123', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient7_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient7_id, 'Ngô Thị Giang', '1992-04-18', 'FEMALE', '147 Nguyễn Thị Minh Khai, Quận 1, TP.HCM', NOW(), NOW());

-- Bệnh nhân 8: Đặng Văn Hùng
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('dangvanhung@gmail.com', '0978901234', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient8_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient8_id, 'Đặng Văn Hùng', '1975-09-05', 'MALE', '258 Điện Biên Phủ, Quận Bình Thạnh, TP.HCM', NOW(), NOW());

-- Bệnh nhân 9: Bùi Thị Lan
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('buithilan@gmail.com', '0989012345', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient9_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient9_id, 'Bùi Thị Lan', '1998-02-14', 'FEMALE', '369 Trường Chinh, Quận Tân Phú, TP.HCM', NOW(), NOW());

-- Bệnh nhân 10: Trịnh Văn Minh
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified, created_at, updated_at)
VALUES ('trinhvanminh@gmail.com', '0990123456', @password_hash, 'PATIENT', 1, 1, NOW(), NOW());
SET @patient10_id = LAST_INSERT_ID();

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address, created_at, updated_at)
VALUES (@patient10_id, 'Trịnh Văn Minh', '1982-06-20', 'MALE', '741 Lý Thường Kiệt, Quận 11, TP.HCM', NOW(), NOW());

-- ============================================
-- KIỂM TRA DỮ LIỆU ĐÃ INSERT
-- ============================================
SELECT 
    u.id,
    u.email,
    u.phone,
    u.role,
    ui.full_name,
    ui.date_of_birth,
    ui.gender,
    ui.address
FROM users u
LEFT JOIN user_info ui ON u.id = ui.user_id
WHERE u.role = 'PATIENT'
ORDER BY u.id;

-- ============================================
-- TỔNG KẾT
-- ============================================
SELECT 
    'Tổng số bệnh nhân đã thêm:' AS info,
    COUNT(*) AS count
FROM users 
WHERE role = 'PATIENT';
