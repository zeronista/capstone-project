-- ============================================
-- TEST USERS FOR ALL ROLES - ABClinic
-- ============================================
-- Mật khẩu cho tất cả: Test@123
-- BCrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- ============================================

USE capstone_project;

-- ============================================
-- 1. DOCTOR ACCOUNTS (2 users)
-- ============================================
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified) VALUES
('doctor1@abclinic.com', '0901111111', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'DOCTOR', TRUE, TRUE),
('doctor2@abclinic.com', '0901111112', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'DOCTOR', TRUE, TRUE);

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address) VALUES
((SELECT id FROM users WHERE email = 'doctor1@abclinic.com'), 'BS. Nguyễn Trần Kiên', '1980-05-15', 'MALE', 'Quận 1, TP.HCM'),
((SELECT id FROM users WHERE email = 'doctor2@abclinic.com'), 'BS. Trần Thị Mai', '1985-08-20', 'FEMALE', 'Quận 3, TP.HCM');

-- ============================================
-- 2. RECEPTIONIST ACCOUNTS (2 users)
-- ============================================
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified) VALUES
('receptionist1@abclinic.com', '0902222221', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'RECEPTIONIST', TRUE, TRUE),
('receptionist2@abclinic.com', '0902222222', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'RECEPTIONIST', TRUE, TRUE);

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address) VALUES
((SELECT id FROM users WHERE email = 'receptionist1@abclinic.com'), 'Lễ tân Mai', '1995-03-10', 'FEMALE', 'Quận 7, TP.HCM'),
((SELECT id FROM users WHERE email = 'receptionist2@abclinic.com'), 'Lễ tân Hùng', '1993-07-25', 'MALE', 'Quận Bình Thạnh, TP.HCM');

-- ============================================
-- 3. PATIENT ACCOUNTS (2 users)
-- ============================================
INSERT INTO users (email, phone, password_hash, role, is_active, email_verified) VALUES
('patient1@gmail.com', '0903333331', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'PATIENT', TRUE, TRUE),
('patient2@gmail.com', '0903333332', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'PATIENT', TRUE, TRUE);

INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address) VALUES
((SELECT id FROM users WHERE email = 'patient1@gmail.com'), 'Nguyễn Văn An', '1990-12-01', 'MALE', 'Quận 10, TP.HCM'),
((SELECT id FROM users WHERE email = 'patient2@gmail.com'), 'Trần Thị Bình', '1988-04-18', 'FEMALE', 'Quận Tân Bình, TP.HCM');

-- ============================================
-- SUMMARY - TEST ACCOUNTS
-- ============================================
-- | Role         | Email                       | Password  | Phone      |
-- |--------------|-----------------------------|-----------| -----------|
-- | ADMIN        | admin@clinic.com            | Admin@123 | N/A        |
-- | DOCTOR       | doctor1@abclinic.com        | Test@123  | 0901111111 |
-- | DOCTOR       | doctor2@abclinic.com        | Test@123  | 0901111112 |
-- | RECEPTIONIST | receptionist1@abclinic.com  | Test@123  | 0902222221 |
-- | RECEPTIONIST | receptionist2@abclinic.com  | Test@123  | 0902222222 |
-- | PATIENT      | patient1@gmail.com          | Test@123  | 0903333331 |
-- | PATIENT      | patient2@gmail.com          | Test@123  | 0903333332 |
-- ============================================

-- Verify inserted data
SELECT 
    u.id, 
    u.email, 
    u.phone,
    u.role, 
    ui.full_name,
    u.is_active
FROM users u 
LEFT JOIN user_info ui ON u.id = ui.user_id 
WHERE u.email LIKE '%abclinic.com' OR u.email LIKE 'patient%@gmail.com'
ORDER BY u.role, u.id;
