-- ============================================
-- HOAN TAT INSERT DU LIEU MAU
-- ============================================
-- TONG KET TAI KHOAN:
-- ============================================
-- | ID  | Email              | Role         | Ho Ten           |
-- |-----|-------------------|--------------|------------------|
-- | 1   | admin@gmail.com   | ADMIN        | Quản Trị Viên    |
-- | 2   | doctor1@gmail.com | DOCTOR       | BS. Nguyễn Văn An|
-- | 3   | doctor2@gmail.com | DOCTOR       | BS. Trần Thị Bình|
-- | 4   | doctor3@gmail.com | DOCTOR       | BS. Lê Minh Cường|
-- | 5   | letan1@gmail.com  | RECEPTIONIST | Hoàng Thị Mai    |
-- | 6   | letan2@gmail.com  | RECEPTIONIST | Vũ Văn Nam       |
-- | 7   | patient1@gmail.com| PATIENT      | Đỗ Văn Phúc      |
-- | 8   | patient2@gmail.com| PATIENT      | Ngô Thị Quỳnh    |
-- | 9   | patient3@gmail.com| PATIENT      | Bùi Văn Sơn      |
-- | 10  | patient4@gmail.com| PATIENT      | Trịnh Thị Tâm    |
-- | 11  | patient5@gmail.com| PATIENT      | Đinh Văn Uy      |
-- | 12  | patient6@gmail.com| PATIENT      | Phạm Thị Vân     |
-- | 13  | patient7@gmail.com| PATIENT      | Lý Văn Wũ        |
-- | 14  | patient8@gmail.com| PATIENT      | Cao Thị Xuân     |
-- | 15  | patient9@gmail.com| PATIENT      | Hồ Văn Yên       |
-- | 16  | patient10@gmail.com| PATIENT     | Mai Thị Zara     |
-- ============================================
-- MAT KHAU CHUNG: 123456a@A
-- ============================================
-- Tong so ban ghi da them:
-- users: 16
-- user_info: 16
-- patient_documents: 7
-- family_medical_history: 7
-- medical_reports: 7
-- health_forecasts: 3
-- survey_templates: 3
-- call_logs: 6
-- web_call_logs: 6
-- tickets: 6
-- ticket_messages: 8
-- prescriptions: 6
-- prescription_details: 11
-- treatment_plans: 5
-- treatment_plan_items: 21
-- knowledge_categories: 10
-- knowledge_articles: 6
-- notifications: 9
-- feedbacks: 7
-- ============================================


-- ============================================
-- CAPSTONE PROJECT - AI CALLBOT PHONG KHAM
-- FILE 2: DATA (INSERT DU LIEU MAU)
-- PostgreSQL 14+
-- ============================================
-- Ngay tao: 03/02/2026
-- Phien ban: 4.0
-- Nhom: G4
-- ============================================
-- Chay file nay SAU KHI chay 01_schema.sql
-- ============================================

-- ============================================
-- THONG TIN DANG NHAP TAT CA TAI KHOAN
-- ============================================
-- Mat khau chung: 123456a@A
-- Mat khau da ma hoa (Bcrypt): $2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca
-- ============================================

-- ============================================
-- 1. USERS - Nguoi dung
-- ============================================
-- ID 1: Admin
-- ID 2-4: Doctors (3 bac si)
-- ID 5-6: Receptionists (2 le tan)
-- ID 7-16: Patients (10 benh nhan)
-- ============================================

INSERT INTO users (email, phone, password_hash, role, is_active, email_verified) VALUES
-- ========== ADMIN (1) ==========
('admin@gmail.com', '0900000001', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'ADMIN', TRUE, TRUE),

-- ========== DOCTORS (3) ==========
('doctor1@gmail.com', '0901111001', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'DOCTOR', TRUE, TRUE),
('doctor2@gmail.com', '0901111002', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'DOCTOR', TRUE, TRUE),
('doctor3@gmail.com', '0901111003', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'DOCTOR', TRUE, TRUE),

-- ========== RECEPTIONISTS (2) ==========
('letan1@gmail.com', '0902222001', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'RECEPTIONIST', TRUE, TRUE),
('letan2@gmail.com', '0902222002', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'RECEPTIONIST', TRUE, TRUE),

-- ========== PATIENTS (10) ==========
('patient1@gmail.com', '0903333001', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE),
('patient2@gmail.com', '0903333002', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE),
('patient3@gmail.com', '0903333003', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE),
('patient4@gmail.com', '0903333004', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE),
('patient5@gmail.com', '0903333005', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE),
('patient6@gmail.com', '0903333006', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE),
('patient7@gmail.com', '0903333007', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE),
('patient8@gmail.com', '0903333008', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE),
('patient9@gmail.com', '0903333009', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, FALSE),
('patient10@gmail.com', '0903333010', '$2a$12$4VT.nAlBTqwA4CJLiheLKuft87eyMvCJQuArJqXz31Z0Ux.i8z7Ca', 'PATIENT', TRUE, TRUE);

-- ============================================
-- 2. USER_INFO - Thong tin ca nhan
-- ============================================
INSERT INTO user_info (user_id, full_name, date_of_birth, gender, address) VALUES
-- Admin
(1, 'Quản Trị Viên', '1985-01-15', 'MALE', 'Hà Nội'),

-- Doctors
(2, 'BS. Nguyễn Văn An', '1975-05-20', 'MALE', '123 Đường Láng, Đống Đa, Hà Nội'),
(3, 'BS. Trần Thị Bình', '1980-08-10', 'FEMALE', '456 Kim Mã, Ba Đình, Hà Nội'),
(4, 'BS. Lê Minh Cường', '1978-03-25', 'MALE', '789 Nguyễn Trãi, Thanh Xuân, Hà Nội'),

-- Receptionists
(5, 'Hoàng Thị Mai', '1995-04-12', 'FEMALE', '15 Phố Huế, Hai Bà Trưng, Hà Nội'),
(6, 'Vũ Văn Nam', '1993-09-08', 'MALE', '28 Trần Nhân Tông, Hai Bà Trưng, Hà Nội'),

-- Patients
(7, 'Đỗ Văn Phúc', '1990-06-15', 'MALE', '45 Đội Cấn, Ba Đình, Hà Nội'),
(8, 'Ngô Thị Quỳnh', '1988-12-20', 'FEMALE', '67 Láng Hạ, Đống Đa, Hà Nội'),
(9, 'Bùi Văn Sơn', '1965-02-28', 'MALE', '89 Trần Duy Hưng, Cầu Giấy, Hà Nội'),
(10, 'Trịnh Thị Tâm', '1970-07-05', 'FEMALE', '12 Nguyễn Chí Thanh, Đống Đa, Hà Nội'),
(11, 'Đinh Văn Uy', '1995-10-18', 'MALE', '34 Xuân Thủy, Cầu Giấy, Hà Nội'),
(12, 'Phạm Thị Vân', '1992-03-22', 'FEMALE', '56 Giải Phóng, Hoàng Mai, Hà Nội'),
(13, 'Lý Văn Wũ', '1958-11-30', 'MALE', '78 Hoàng Hoa Thám, Ba Đình, Hà Nội'),
(14, 'Cao Thị Xuân', '1985-08-14', 'FEMALE', '90 Ngọc Khánh, Ba Đình, Hà Nội'),
(15, 'Hồ Văn Yên', '1972-04-09', 'MALE', '23 Thái Hà, Đống Đa, Hà Nội'),
(16, 'Mai Thị Zara', '1998-12-01', 'FEMALE', '45 Cầu Giấy, Cầu Giấy, Hà Nội');

-- ============================================
-- 3. PATIENT_DOCUMENTS - Tai lieu benh nhan
-- ============================================
INSERT INTO patient_documents (patient_id, document_type, file_name, file_url, file_size, description) VALUES
(7, 'MEDICAL_HISTORY', 'tien_su_benh_do_van_phuc.pdf', '/uploads/documents/7/tien_su_benh.pdf', 256000, 'Tiền sử bệnh tổng quát'),
(7, 'TEST_RESULT', 'xet_nghiem_mau_012026.pdf', '/uploads/documents/7/xet_nghiem_012026.pdf', 512000, 'Kết quả xét nghiệm máu tháng 01/2026'),
(8, 'MEDICAL_HISTORY', 'tien_su_benh_ngo_thi_quynh.pdf', '/uploads/documents/8/tien_su_benh.pdf', 198000, 'Tiền sử bệnh tổng quát'),
(9, 'TEST_RESULT', 'duong_huyet_bui_van_son.pdf', '/uploads/documents/9/duong_huyet.pdf', 384000, 'Kết quả đường huyết định kỳ'),
(9, 'PRESCRIPTION', 'don_thuoc_tieu_duong.pdf', '/uploads/documents/9/don_thuoc.pdf', 128000, 'Đơn thuốc tiểu đường'),
(10, 'TEST_RESULT', 'xet_nghiem_tam_ly.pdf', '/uploads/documents/10/xet_nghiem_tam_ly.pdf', 220000, 'Kết quả đánh giá tâm lý'),
(13, 'MEDICAL_HISTORY', 'tien_su_tim_mach.pdf', '/uploads/documents/13/tim_mach.pdf', 340000, 'Tiền sử bệnh tim mạch');

-- ============================================
-- 4. FAMILY_MEDICAL_HISTORY - Tien su gia dinh
-- ============================================
INSERT INTO family_medical_history (patient_id, created_by, relationship, condition, age_at_diagnosis, member_status, notes) VALUES
(7, 2, 'FATHER', 'Cao huyết áp', 55, 'ALIVE', 'Đang điều trị ổn định'),
(7, 2, 'MOTHER', 'Tiểu đường type 2', 60, 'ALIVE', 'Phát hiện 5 năm trước'),
(9, 2, 'FATHER', 'Tiểu đường type 2', 50, 'DECEASED', 'Qua đời do biến chứng tim mạch'),
(9, 2, 'GRANDFATHER_P', 'Tiểu đường type 2', 45, 'DECEASED', NULL),
(10, 3, 'MOTHER', 'Trầm cảm', 48, 'ALIVE', 'Đang điều trị tâm lý'),
(13, 2, 'FATHER', 'Bệnh tim mạch', 60, 'DECEASED', 'Nhồi máu cơ tim'),
(13, 2, 'MOTHER', 'Cao huyết áp', 58, 'ALIVE', 'Đang điều trị');

-- ============================================
-- 5. MEDICAL_REPORTS - Bao cao y te
-- ============================================
INSERT INTO medical_reports (patient_id, created_by, report_type, report_date, title, content, notes) VALUES
(7, 2, 'LAB_TEST', '2026-01-15', 'Xét nghiệm máu tổng quát', 'HbA1c: 6.2%, Cholesterol: 195 mg/dL, Triglyceride: 150 mg/dL', 'Các chỉ số trong giới hạn bình thường'),
(8, 3, 'CONSULTATION', '2026-01-20', 'Khám tâm lý lần đầu', 'Bệnh nhân có biểu hiện lo âu, mất ngủ kéo dài 2 tháng. Đề xuất liệu trình tâm lý trị liệu.', 'Hẹn tái khám sau 2 tuần'),
(9, 2, 'LAB_TEST', '2026-01-10', 'Xét nghiệm đường huyết định kỳ', 'Đường huyết lúc đói: 145 mg/dL, HbA1c: 7.8%', 'Cần điều chỉnh liều thuốc'),
(9, 2, 'VITAL_SIGNS', '2026-01-10', 'Đo huyết áp định kỳ', 'Huyết áp: 140/90 mmHg, Nhịp tim: 78 bpm', 'Huyết áp hơi cao, cần theo dõi'),
(10, 4, 'CONSULTATION', '2026-01-25', 'Tư vấn tâm lý', 'Bệnh nhân đáp ứng tốt với liệu trình. Tâm trạng cải thiện.', 'Tiếp tục liệu trình hiện tại'),
(13, 2, 'LAB_TEST', '2026-01-18', 'Xét nghiệm tim mạch', 'Cholesterol: 240 mg/dL, LDL: 160 mg/dL, HDL: 35 mg/dL', 'Cần điều chỉnh chế độ ăn và thuốc'),
(11, 3, 'CONSULTATION', '2026-01-22', 'Tư vấn giảm cân', 'BMI: 28.5, Cân nặng: 85kg, Chiều cao: 172cm. Đề xuất chương trình giảm cân.', 'Theo dõi hàng tháng');

-- ============================================
-- 6. HEALTH_FORECASTS - Du bao suc khoe
-- ============================================
INSERT INTO health_forecasts (patient_id, forecast_date, risk_scores, predictions, risk_factors, recommendations, vital_signs_snapshot, created_by, status) VALUES
(9, '2026-01-15', 
    '{"cardiovascular": 0.35, "diabetes_complication": 0.45, "hypertension": 0.40}',
    '{"hba1c_trend": "increasing", "blood_pressure_trend": "stable", "weight_trend": "stable"}',
    '{"smoking": false, "obesity": true, "sedentary": true, "family_history": true}',
    'Khuyến nghị: Tăng cường vận động 30 phút/ngày, giảm tinh bột, theo dõi đường huyết hàng ngày.',
    '{"blood_sugar": 145, "systolic": 140, "diastolic": 90, "weight": 78, "bmi": 27.5}',
    2, 'ACTIVE'),
(7, '2026-01-20',
    '{"cardiovascular": 0.15, "diabetes": 0.20, "hypertension": 0.18}',
    '{"weight_trend": "stable", "blood_pressure_trend": "normal"}',
    '{"smoking": false, "obesity": false, "sedentary": false, "family_history": true}',
    'Duy trì lối sống lành mạnh. Kiểm tra sức khỏe định kỳ 6 tháng/lần.',
    '{"systolic": 120, "diastolic": 80, "weight": 65, "bmi": 22.5}',
    2, 'ACTIVE'),
(13, '2026-01-20',
    '{"cardiovascular": 0.55, "stroke": 0.40, "hypertension": 0.50}',
    '{"cholesterol_trend": "increasing", "blood_pressure_trend": "increasing"}',
    '{"smoking": true, "obesity": true, "sedentary": true, "family_history": true, "age_over_60": true}',
    'Cần theo dõi chặt chẽ. Bỏ thuốc lá ngay. Chế độ ăn ít mỡ. Tập thể dục nhẹ nhàng.',
    '{"systolic": 150, "diastolic": 95, "weight": 82, "bmi": 29.1, "cholesterol": 240}',
    2, 'ACTIVE');

-- ============================================
-- 7. SURVEY_TEMPLATES - Mau khao sat AI
-- ============================================
INSERT INTO survey_templates (template_name, description, questions_json, is_active, created_by) VALUES
('Khảo sát sức khỏe tổng quát', 'Mẫu khảo sát để thu thập thông tin sức khỏe ban đầu của bệnh nhân',
    '[
        {"id": 1, "question": "Bạn có đang dùng thuốc gì không?", "type": "text"},
        {"id": 2, "question": "Bạn có tiền sử dị ứng không?", "type": "yes_no"},
        {"id": 3, "question": "Triệu chứng hiện tại của bạn là gì?", "type": "multiple_choice", "options": ["Đau đầu", "Sốt", "Ho", "Mệt mỏi", "Khác"]}
    ]',
    TRUE, 1),
('Theo dõi tiểu đường', 'Mẫu khảo sát cho bệnh nhân tiểu đường',
    '[
        {"id": 1, "question": "Đường huyết sáng nay của bạn là bao nhiêu?", "type": "number"},
        {"id": 2, "question": "Bạn có tuân thủ chế độ ăn kiêng không?", "type": "scale_1_5"},
        {"id": 3, "question": "Bạn có tập thể dục hôm nay không?", "type": "yes_no"}
    ]',
    TRUE, 2),
('Đánh giá sức khỏe tâm thần', 'Mẫu khảo sát sàng lọc sức khỏe tâm thần',
    '[
        {"id": 1, "question": "Trong 2 tuần qua, bạn có thường xuyên cảm thấy buồn không?", "type": "scale_1_5"},
        {"id": 2, "question": "Bạn có khó ngủ không?", "type": "yes_no"},
        {"id": 3, "question": "Mức năng lượng của bạn như thế nào?", "type": "scale_1_5"}
    ]',
    TRUE, 3);

-- ============================================
-- 8. CALL_LOGS - Lich su cuoc goi AI
-- ============================================
INSERT INTO call_logs (patient_id, phone_number, call_type, call_status, start_time, end_time, duration, transcript_text, ai_confidence_score, is_escalated, handled_by) VALUES
(7, '0903333001', 'AI_BOT', 'COMPLETED', '2026-01-20 09:00:00', '2026-01-20 09:05:30', 330, 
    'AI: Xin chào, đây là hệ thống tự động của phòng khám ABC. Tôi có thể hỗ trợ gì cho anh/chị?
Bệnh nhân: Tôi muốn đặt lịch khám.
AI: Vâng, anh/chị muốn khám chuyên khoa nào ạ?
Bệnh nhân: Khám tổng quát.
AI: Dạ vâng. Anh/chị có thể cho biết ngày mong muốn không ạ?',
    0.92, FALSE, NULL),
(8, '0903333002', 'AI_BOT', 'TRANSFERRED', '2026-01-21 14:30:00', '2026-01-21 14:35:00', 300,
    'AI: Xin chào, đây là hệ thống tự động của phòng khám ABC.
Bệnh nhân: Tôi cần nói chuyện với bác sĩ gấp.
AI: Xin lỗi, tôi sẽ chuyển cuộc gọi cho nhân viên hỗ trợ.',
    0.65, TRUE, 5),
(9, '0903333003', 'AI_BOT', 'COMPLETED', '2026-01-22 10:15:00', '2026-01-22 10:20:00', 300,
    'AI: Xin chào ông Sơn, đây là cuộc gọi nhắc nhở tái khám định kỳ từ phòng khám ABC.
Bệnh nhân: Vâng, tôi nhớ rồi. Tuần sau đúng không?
AI: Dạ đúng rồi ạ. Ông có muốn xác nhận lịch hẹn không ạ?
Bệnh nhân: Xác nhận.',
    0.95, FALSE, NULL),
(NULL, '0909999001', 'AI_BOT', 'NO_ANSWER', '2026-01-23 11:00:00', '2026-01-23 11:00:45', 45,
    NULL, NULL, FALSE, NULL),
(11, '0903333005', 'MANUAL', 'COMPLETED', '2026-01-24 16:00:00', '2026-01-24 16:10:00', 600,
    NULL, NULL, FALSE, 6),
(13, '0903333007', 'AI_BOT', 'COMPLETED', '2026-01-25 08:30:00', '2026-01-25 08:38:00', 480,
    'AI: Xin chào, đây là cuộc gọi nhắc uống thuốc từ phòng khám ABC.
Bệnh nhân: Vâng, tôi đã uống rồi.
AI: Tuyệt vời! Ông có gặp tác dụng phụ nào không ạ?
Bệnh nhân: Không có.',
    0.88, FALSE, NULL);

-- ============================================
-- 9. WEB_CALL_LOGS - Cuoc goi Web
-- ============================================
INSERT INTO web_call_logs (stringee_call_id, caller_id, receiver_id, call_status, start_time, end_time, duration, rating, notes, has_recording) VALUES
('STR_CALL_001', 7, 2, 'COMPLETED', '2026-01-20 10:00:00', '2026-01-20 10:15:00', 900, 5, 'Tư vấn sức khỏe tổng quát', TRUE),
('STR_CALL_002', 8, 3, 'COMPLETED', '2026-01-21 15:00:00', '2026-01-21 15:30:00', 1800, 4, 'Tư vấn tâm lý', TRUE),
('STR_CALL_003', 9, 2, 'COMPLETED', '2026-01-22 11:00:00', '2026-01-22 11:20:00', 1200, 5, 'Theo dõi tiểu đường', TRUE),
('STR_CALL_004', 10, 4, 'MISSED', '2026-01-23 09:00:00', NULL, NULL, NULL, NULL, FALSE),
('STR_CALL_005', 7, 3, 'COMPLETED', '2026-01-25 14:00:00', '2026-01-25 14:10:00', 600, 4, 'Hỏi về kết quả xét nghiệm', FALSE),
('STR_CALL_006', 13, 2, 'COMPLETED', '2026-01-26 09:30:00', '2026-01-26 10:00:00', 1800, 5, 'Tư vấn bệnh tim mạch', TRUE);

-- ============================================
-- 10. TICKETS - Yeu cau ho tro
-- ============================================
INSERT INTO tickets (call_id, patient_id, title, description, priority, category, status, created_by_id, assigned_to_id, resolved_by_id, resolved_at) VALUES
(2, 8, 'Yêu cầu tư vấn gấp', 'Bệnh nhân cần tư vấn gấp về triệu chứng lo âu', 'HIGH', 'MEDICAL_QUERY', 'RESOLVED', 5, 3, 3, '2026-01-21 16:00:00'),
(NULL, 9, 'Hỏi về đơn thuốc', 'Bệnh nhân cần hỏi về liều lượng thuốc tiểu đường', 'MEDIUM', 'PRESCRIPTION', 'RESOLVED', 6, 2, 2, '2026-01-22 12:00:00'),
(NULL, 7, 'Đặt lịch khám', 'Bệnh nhân muốn đặt lịch khám tổng quát', 'LOW', 'APPOINTMENT', 'CLOSED', 5, NULL, 5, '2026-01-20 10:00:00'),
(NULL, 11, 'Lỗi đăng nhập ứng dụng', 'Bệnh nhân không đăng nhập được ứng dụng', 'MEDIUM', 'TECHNICAL', 'IN_PROGRESS', 6, NULL, NULL, NULL),
(4, NULL, 'Số điện thoại không liên lạc được', 'Không liên lạc được với số 0909999001', 'LOW', 'OTHER', 'OPEN', 5, NULL, NULL, NULL),
(NULL, 13, 'Hỏi về chế độ ăn', 'Bệnh nhân tim mạch cần tư vấn chế độ ăn', 'MEDIUM', 'MEDICAL_QUERY', 'ASSIGNED', 5, 2, NULL, NULL);

-- ============================================
-- 11. TICKET_MESSAGES - Tin nhan ticket
-- ============================================
INSERT INTO ticket_messages (ticket_id, sender_id, message_text, message_type, is_internal_note) VALUES
(1, 5, 'Bệnh nhân gọi điện đang rất lo lắng, cần bác sĩ tư vấn gấp.', 'TEXT', TRUE),
(1, 3, 'Đã gọi điện tư vấn cho bệnh nhân. Tình trạng ổn định.', 'TEXT', FALSE),
(1, 8, 'Cảm ơn bác sĩ đã hỗ trợ.', 'TEXT', FALSE),
(2, 6, 'Bệnh nhân hỏi về việc điều chỉnh liều Metformin.', 'TEXT', TRUE),
(2, 2, 'Đã tư vấn cho bệnh nhân. Giữ nguyên liều hiện tại, theo dõi thêm.', 'TEXT', FALSE),
(4, 6, 'Đang kiểm tra lỗi hệ thống.', 'TEXT', TRUE),
(6, 5, 'Bệnh nhân cần được tư vấn về chế độ ăn cho người bệnh tim mạch.', 'TEXT', TRUE),
(6, 2, 'Sẽ liên hệ bệnh nhân trong hôm nay.', 'TEXT', TRUE);

-- ============================================
-- 12. PRESCRIPTIONS - Don thuoc
-- ============================================
INSERT INTO prescriptions (patient_id, doctor_id, prescription_date, diagnosis, notes, status) VALUES
(9, 2, '2026-01-10', 'Tiểu đường type 2, Tăng huyết áp', 'Theo dõi đường huyết hàng ngày', 'ACTIVE'),
(8, 3, '2026-01-21', 'Rối loạn lo âu', 'Kết hợp với liệu trình tâm lý', 'ACTIVE'),
(7, 2, '2026-01-15', 'Khám sức khỏe định kỳ', 'Bổ sung vitamin', 'COMPLETED'),
(10, 4, '2026-01-25', 'Trầm cảm nhẹ', 'Tiếp tục theo dõi', 'ACTIVE'),
(13, 2, '2026-01-18', 'Rối loạn lipid máu, Cao huyết áp', 'Chế độ ăn ít mỡ, tập thể dục', 'ACTIVE'),
(11, 3, '2026-01-22', 'Thừa cân', 'Chương trình giảm cân 3 tháng', 'ACTIVE');

-- ============================================
-- 13. PRESCRIPTION_DETAILS - Chi tiet don thuoc
-- ============================================
INSERT INTO prescription_details (prescription_id, medicine_name, dosage, frequency, duration, instructions, quantity) VALUES
-- Don thuoc 1: Tieu duong + huyet ap (patient 9)
(1, 'Metformin 500mg', '500mg', '2 lần/ngày', '30 ngày', 'Uống sau ăn sáng và tối', 60),
(1, 'Gliclazide 30mg', '30mg', '1 lần/ngày', '30 ngày', 'Uống trước ăn sáng', 30),
(1, 'Amlodipine 5mg', '5mg', '1 lần/ngày', '30 ngày', 'Uống buổi sáng', 30),

-- Don thuoc 2: Lo au (patient 8)
(2, 'Alprazolam 0.25mg', '0.25mg', '2 lần/ngày', '14 ngày', 'Uống khi cần, không lái xe sau khi uống', 28),

-- Don thuoc 3: Vitamin (patient 7)
(3, 'Vitamin D3 1000IU', '1000IU', '1 lần/ngày', '30 ngày', 'Uống sau ăn', 30),
(3, 'Vitamin B Complex', '1 viên', '1 lần/ngày', '30 ngày', 'Uống sau ăn sáng', 30),

-- Don thuoc 4: Tram cam (patient 10)
(4, 'Sertraline 50mg', '50mg', '1 lần/ngày', '30 ngày', 'Uống buổi sáng sau ăn', 30),

-- Don thuoc 5: Tim mach (patient 13)
(5, 'Atorvastatin 20mg', '20mg', '1 lần/ngày', '30 ngày', 'Uống buổi tối', 30),
(5, 'Aspirin 81mg', '81mg', '1 lần/ngày', '30 ngày', 'Uống sau ăn sáng', 30),
(5, 'Lisinopril 10mg', '10mg', '1 lần/ngày', '30 ngày', 'Uống buổi sáng', 30),

-- Don thuoc 6: Giam can (patient 11)
(6, 'Orlistat 120mg', '120mg', '3 lần/ngày', '30 ngày', 'Uống trong bữa ăn có chất béo', 90);

-- ============================================
-- 14. TREATMENT_PLANS - Ke hoach dieu tri
-- ============================================
INSERT INTO treatment_plans (patient_id, doctor_id, diagnosis, treatment_goal, start_date, expected_end_date, status, ai_suggested, ai_suggestion_data) VALUES
(9, 2, 'Tiểu đường type 2, Tăng huyết áp', 
    'Kiểm soát đường huyết HbA1c < 7%, huyết áp < 140/90', 
    '2026-01-10', '2026-07-10', 'ACTIVE', TRUE,
    '{"suggested_medications": ["Metformin", "Gliclazide"], "lifestyle_changes": ["Giảm cân 5%", "Tập thể dục 150 phút/tuần"], "monitoring": ["Đường huyết hàng ngày", "HbA1c mỗi 3 tháng"]}'),
(8, 3, 'Rối loạn lo âu',
    'Giảm triệu chứng lo âu, cải thiện chất lượng giấc ngủ',
    '2026-01-21', '2026-04-21', 'ACTIVE', FALSE, NULL),
(10, 4, 'Trầm cảm nhẹ',
    'Cải thiện tâm trạng, duy trì hoạt động xã hội',
    '2026-01-25', '2026-07-25', 'ACTIVE', FALSE, NULL),
(13, 2, 'Rối loạn lipid máu, Bệnh tim mạch',
    'Giảm cholesterol LDL < 100 mg/dL, kiểm soát huyết áp',
    '2026-01-18', '2026-07-18', 'ACTIVE', TRUE,
    '{"suggested_medications": ["Statin", "Aspirin", "ACE inhibitor"], "lifestyle_changes": ["Bỏ thuốc lá", "Chế độ ăn DASH", "Tập thể dục nhẹ"], "monitoring": ["Lipid profile mỗi 3 tháng", "ECG mỗi 6 tháng"]}'),
(11, 3, 'Thừa cân, BMI > 28',
    'Giảm 10% cân nặng trong 3 tháng, BMI < 25',
    '2026-01-22', '2026-04-22', 'ACTIVE', FALSE, NULL);

-- ============================================
-- 15. TREATMENT_PLAN_ITEMS - Chi tiet ke hoach
-- ============================================
INSERT INTO treatment_plan_items (plan_id, item_type, description, frequency, duration, notes, status) VALUES
-- Ke hoach 1: Tieu duong (patient 9)
(1, 'MEDICATION', 'Uống Metformin 500mg', '2 lần/ngày', '6 tháng', 'Sau ăn sáng và tối', 'ONGOING'),
(1, 'MEDICATION', 'Uống Gliclazide 30mg', '1 lần/ngày', '6 tháng', 'Trước ăn sáng', 'ONGOING'),
(1, 'LIFESTYLE', 'Đi bộ nhanh 30 phút', 'Hàng ngày', '6 tháng', 'Tránh tập khi đường huyết < 100', 'ONGOING'),
(1, 'LIFESTYLE', 'Chế độ ăn ít tinh bột', 'Hàng ngày', '6 tháng', 'Tham khảo menu từ chuyên gia dinh dưỡng', 'ONGOING'),
(1, 'CHECKUP', 'Xét nghiệm HbA1c', 'Mỗi 3 tháng', NULL, 'Tại phòng khám', 'PENDING'),

-- Ke hoach 2: Lo au (patient 8)
(2, 'THERAPY', 'Liệu pháp nhận thức hành vi (CBT)', '1 lần/tuần', '3 tháng', 'Với BS. Trần Thị Bình', 'ONGOING'),
(2, 'MEDICATION', 'Alprazolam khi cần', 'Khi cần', '2 tuần', 'Không quá 2 lần/ngày', 'ONGOING'),
(2, 'LIFESTYLE', 'Thiền định 15 phút', 'Hàng ngày', '3 tháng', 'Sử dụng app hướng dẫn', 'PENDING'),

-- Ke hoach 3: Tram cam (patient 10)
(3, 'MEDICATION', 'Sertraline 50mg', '1 lần/ngày', '6 tháng', 'Buổi sáng sau ăn', 'ONGOING'),
(3, 'THERAPY', 'Tư vấn tâm lý', '2 tuần/lần', '6 tháng', 'Với BS. Lê Minh Cường', 'ONGOING'),
(3, 'LIFESTYLE', 'Hoạt động ngoài trời', '3 lần/tuần', '6 tháng', 'Đi bộ công viên, gặp gỡ bạn bè', 'PENDING'),

-- Ke hoach 4: Tim mach (patient 13)
(4, 'MEDICATION', 'Atorvastatin 20mg', '1 lần/ngày', '6 tháng', 'Uống buổi tối', 'ONGOING'),
(4, 'MEDICATION', 'Aspirin 81mg', '1 lần/ngày', '6 tháng', 'Sau ăn sáng', 'ONGOING'),
(4, 'LIFESTYLE', 'Bỏ thuốc lá', 'Ngay lập tức', 'Vĩnh viễn', 'Tham gia nhóm hỗ trợ bỏ thuốc', 'ONGOING'),
(4, 'LIFESTYLE', 'Chế độ ăn DASH', 'Hàng ngày', '6 tháng', 'Giảm muối, tăng rau xanh, trái cây', 'ONGOING'),
(4, 'CHECKUP', 'Xét nghiệm lipid', 'Mỗi 3 tháng', NULL, 'Tại phòng khám', 'PENDING'),

-- Ke hoach 5: Giam can (patient 11)
(5, 'MEDICATION', 'Orlistat 120mg', '3 lần/ngày', '3 tháng', 'Trong bữa ăn có chất béo', 'ONGOING'),
(5, 'LIFESTYLE', 'Chế độ ăn giảm calo', 'Hàng ngày', '3 tháng', '1500-1800 kcal/ngày', 'ONGOING'),
(5, 'LIFESTYLE', 'Tập gym', '4 lần/tuần', '3 tháng', 'Cardio + strength training', 'PENDING'),
(5, 'CHECKUP', 'Cân và đo vòng eo', 'Hàng tuần', '3 tháng', 'Tự theo dõi tại nhà', 'ONGOING');

-- ============================================
-- 16. KNOWLEDGE_CATEGORIES - Danh muc kien thuc
-- ============================================
INSERT INTO knowledge_categories (name, description, parent_id, display_order, active) VALUES
('Bệnh Tiểu Đường', 'Kiến thức về bệnh tiểu đường và cách quản lý', NULL, 1, TRUE),
('Sức Khỏe Tâm Thần', 'Kiến thức về sức khỏe tâm thần', NULL, 2, TRUE),
('Dinh Dưỡng', 'Kiến thức về dinh dưỡng và chế độ ăn', NULL, 3, TRUE),
('Giảm Cân', 'Kiến thức về giảm cân lành mạnh', NULL, 4, TRUE),
('Tim Mạch', 'Kiến thức về bệnh tim mạch', NULL, 5, TRUE),
('Tiểu đường Type 1', 'Thông tin về tiểu đường Type 1', 1, 1, TRUE),
('Tiểu đường Type 2', 'Thông tin về tiểu đường Type 2', 1, 2, TRUE),
('Lo Âu', 'Rối loạn lo âu và cách điều trị', 2, 1, TRUE),
('Trầm Cảm', 'Bệnh trầm cảm và cách điều trị', 2, 2, TRUE),
('Cao Huyết Áp', 'Kiến thức về bệnh cao huyết áp', 5, 1, TRUE);

-- ============================================
-- 17. KNOWLEDGE_ARTICLES - Bai viet kien thuc
-- ============================================
INSERT INTO knowledge_articles (title, summary, content, category_id, tags, created_by, status, views, featured, published_at) VALUES
('Hướng dẫn tự theo dõi đường huyết tại nhà', 
    'Bài viết hướng dẫn cách đo và theo dõi đường huyết đúng cách tại nhà.',
    'Theo dõi đường huyết là một phần quan trọng trong quản lý bệnh tiểu đường. Bài viết này hướng dẫn bạn cách đo đường huyết đúng cách, thời điểm đo phù hợp, và cách ghi chép kết quả...',
    7, 'tiểu đường,đường huyết,tự theo dõi', 2, 'PUBLISHED', 150, TRUE, '2026-01-01'),

('Chế độ ăn cho người tiểu đường Type 2',
    'Hướng dẫn xây dựng chế độ ăn phù hợp cho người tiểu đường Type 2.',
    'Chế độ ăn đóng vai trò quan trọng trong kiểm soát đường huyết. Bài viết này hướng dẫn cách chọn thực phẩm, tính toán carbohydrate, và lên kế hoạch bữa ăn...',
    7, 'tiểu đường,dinh dưỡng,chế độ ăn', 2, 'PUBLISHED', 200, TRUE, '2026-01-05'),

('Nhận biết và xử lý cơn lo âu',
    'Hướng dẫn nhận biết triệu chứng lo âu và cách xử lý.',
    'Rối loạn lo âu có thể ảnh hưởng nghiêm trọng đến chất lượng cuộc sống. Bài viết này giúp bạn nhận biết các triệu chứng và học cách kiểm soát cơn lo âu...',
    8, 'lo âu,sức khỏe tâm thần,kỹ năng', 3, 'PUBLISHED', 180, FALSE, '2026-01-10'),

('Kỹ thuật thở để giảm stress',
    'Các kỹ thuật thở đơn giản giúp giảm căng thẳng hiệu quả.',
    'Thở đúng cách có thể giúp giảm căng thẳng và cải thiện sức khỏe tinh thần. Bài viết này hướng dẫn các kỹ thuật thở cơ bản như thở bụng, thở 4-7-8...',
    2, 'stress,thở,thiền định', 4, 'PUBLISHED', 120, FALSE, '2026-01-15'),

('10 bước giảm cân lành mạnh',
    'Hướng dẫn giảm cân an toàn và bền vững.',
    'Giảm cân không chỉ là ăn ít đi mà cần một kế hoạch toàn diện. Bài viết này hướng dẫn 10 bước để giảm cân lành mạnh và duy trì kết quả lâu dài...',
    4, 'giảm cân,dinh dưỡng,lối sống', 3, 'DRAFT', 0, FALSE, NULL),

('Phòng ngừa bệnh tim mạch',
    'Các biện pháp phòng ngừa bệnh tim mạch hiệu quả.',
    'Bệnh tim mạch là nguyên nhân gây tử vong hàng đầu. Bài viết này hướng dẫn cách phòng ngừa thông qua lối sống lành mạnh, chế độ ăn và vận động...',
    5, 'tim mạch,phòng ngừa,lối sống', 2, 'PUBLISHED', 95, TRUE, '2026-01-20');

-- ============================================
-- 18. NOTIFICATIONS - Thong bao
-- ============================================
INSERT INTO notifications (user_id, notification_type, title, content, reference_id, reference_type, is_read) VALUES
(7, 'REMINDER', 'Nhắc nhở khám định kỳ', 'Bạn có lịch khám định kỳ vào ngày 15/02/2026', NULL, NULL, FALSE),
(8, 'MESSAGE', 'Tin nhắn mới từ bác sĩ', 'BS. Trần Thị Bình đã gửi tin nhắn cho bạn', 1, 'TICKET', TRUE),
(9, 'REMINDER', 'Nhắc nhở tái khám', 'Lịch tái khám tiểu đường vào ngày 10/02/2026', NULL, NULL, FALSE),
(9, 'SYSTEM', 'Cập nhật kế hoạch điều trị', 'Kế hoạch điều trị của bạn đã được cập nhật', 1, 'TREATMENT_PLAN', FALSE),
(10, 'REMINDER', 'Lịch tư vấn tâm lý', 'Lịch hẹn tư vấn tâm lý vào ngày 08/02/2026', NULL, NULL, FALSE),
(2, 'TICKET', 'Ticket mới được gán', 'Bạn được gán ticket #2', 2, 'TICKET', TRUE),
(3, 'TICKET', 'Ticket mới được gán', 'Bạn được gán ticket #1', 1, 'TICKET', TRUE),
(13, 'REMINDER', 'Nhắc uống thuốc', 'Đừng quên uống thuốc tim mạch buổi tối', NULL, NULL, FALSE),
(11, 'REMINDER', 'Nhắc tập thể dục', 'Hôm nay là ngày tập gym theo lịch', NULL, NULL, FALSE);

-- ============================================
-- 19. FEEDBACKS - Phan hoi
-- ============================================
INSERT INTO feedbacks (title, description, form_url, call_id, ticket_id, user_id, rating, feedback_text, feedback_type, is_reviewed, reviewed_by, reviewed_at) VALUES
('Đánh giá cuộc gọi AI', 'Khảo sát chất lượng cuộc gọi tự động', 'https://forms.google.com/ai-call-feedback', 1, NULL, 7, 5, 'AI trả lời nhanh và chính xác. Rất hài lòng!', 'AI_PERFORMANCE', TRUE, 1, '2026-01-21'),
('Đánh giá cuộc gọi AI', 'Khảo sát chất lượng cuộc gọi tự động', 'https://forms.google.com/ai-call-feedback', 3, NULL, 9, 4, 'Cuộc gọi nhắc nhở rất hữu ích.', 'CALL_QUALITY', TRUE, 1, '2026-01-23'),
('Đánh giá dịch vụ', 'Khảo sát chất lượng dịch vụ', 'https://forms.google.com/service-feedback', NULL, 1, 8, 5, 'Bác sĩ tư vấn rất tận tình.', 'SERVICE', TRUE, 1, '2026-01-22'),
('Đánh giá dịch vụ', 'Khảo sát chất lượng dịch vụ', 'https://forms.google.com/service-feedback', NULL, 2, 9, 4, 'Được hỗ trợ nhanh chóng.', 'SERVICE', FALSE, NULL, NULL),
('Đánh giá tổng thể', 'Khảo sát ý kiến chung', 'https://forms.google.com/general-feedback', NULL, NULL, 7, 5, 'Ứng dụng dễ sử dụng, giao diện đẹp.', 'GENERAL', FALSE, NULL, NULL),
('Đánh giá cuộc gọi AI', 'Khảo sát chất lượng cuộc gọi tự động', 'https://forms.google.com/ai-call-feedback', 2, 1, 8, 3, 'AI chưa hiểu được yêu cầu của tôi, phải chuyển cho nhân viên.', 'AI_PERFORMANCE', TRUE, 1, '2026-01-22'),
('Đánh giá dịch vụ', 'Khảo sát chất lượng dịch vụ', 'https://forms.google.com/service-feedback', NULL, NULL, 13, 5, 'Bác sĩ giải thích rõ ràng về bệnh tim mạch.', 'SERVICE', FALSE, NULL, NULL);
