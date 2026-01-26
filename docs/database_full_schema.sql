-- ============================================
-- CAPSTONE PROJECT - AI CALLBOT PHÒNG KHÁM
-- FULL DATABASE SCHEMA - MySQL 8.0+
-- ============================================
-- Ngày tạo: 26/01/2026
-- Phiên bản: 2.0 (Tách users thành users + user_info)
-- Nhóm: G4
-- ============================================

-- Xóa database cũ nếu tồn tại (CHỈ DÙNG CHO MÔI TRƯỜNG DEVELOPMENT)
-- DROP DATABASE IF EXISTS capstone_project;

-- Tạo database
CREATE DATABASE IF NOT EXISTS capstone_project
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE capstone_project;

-- ============================================
-- 1. QUẢN LÝ NGƯỜI DÙNG
-- ============================================

-- Bảng USERS - Thông tin tài khoản và bảo mật
-- Lưu trữ thông tin đăng nhập, phân quyền, trạng thái tài khoản
CREATE TABLE IF NOT EXISTS users (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    email               VARCHAR(100)    UNIQUE,
    phone               VARCHAR(20)     UNIQUE,
    password_hash       VARCHAR(255),
    google_id           VARCHAR(100)    UNIQUE,
    role                ENUM('PATIENT', 'RECEPTIONIST', 'DOCTOR', 'ADMIN') NOT NULL DEFAULT 'PATIENT',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    email_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    phone_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login          DATETIME,
    
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu thông tin tài khoản và bảo mật người dùng';

-- Bảng USER_INFO - Thông tin cá nhân người dùng
-- Tách riêng khỏi users để phân biệt thông tin bảo mật và thông tin cá nhân
CREATE TABLE IF NOT EXISTS user_info (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NOT NULL UNIQUE,
    full_name           VARCHAR(100),
    date_of_birth       DATE,
    gender              ENUM('MALE', 'FEMALE', 'OTHER'),
    address             VARCHAR(500),
    avatar_url          VARCHAR(500),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_info_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_info_full_name (full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu thông tin cá nhân người dùng (nullable fields)';

-- Bảng STAFF_INFO - Thông tin chi tiết nhân viên
-- Lưu thông tin bổ sung cho lễ tân và bác sĩ
CREATE TABLE IF NOT EXISTS staff_info (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NOT NULL UNIQUE,
    employee_code       VARCHAR(20)     UNIQUE,
    department          VARCHAR(100),
    specialization      VARCHAR(100)    COMMENT 'Chuyên khoa (cho bác sĩ)',
    license_number      VARCHAR(50)     UNIQUE COMMENT 'Số giấy phép hành nghề',
    hire_date           DATE,
    status              ENUM('ACTIVE', 'INACTIVE', 'ON_LEAVE') DEFAULT 'ACTIVE',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_staff_info_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_staff_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu thông tin chi tiết nhân viên (lễ tân, bác sĩ)';

-- ============================================
-- 2. QUẢN LÝ TÀI LIỆU BỆNH NHÂN
-- ============================================

-- Bảng PATIENT_DOCUMENTS - Tài liệu của bệnh nhân
CREATE TABLE IF NOT EXISTS patient_documents (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    document_type       ENUM('MEDICAL_HISTORY', 'PRESCRIPTION', 'TEST_RESULT', 'OTHER') NOT NULL,
    file_name           VARCHAR(255)    NOT NULL,
    file_url            VARCHAR(500)    NOT NULL,
    file_size           BIGINT,
    description         VARCHAR(500),
    is_verified         BOOLEAN         DEFAULT FALSE,
    verified_by         BIGINT,
    verified_at         DATETIME,
    upload_date         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_doc_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_verified_by 
        FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_doc_patient (patient_id),
    INDEX idx_doc_type (document_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu tài liệu y tế của bệnh nhân';

-- ============================================
-- 3. AI CALLBOT & CHIẾN DỊCH GỌI ĐIỆN
-- ============================================

-- Bảng SURVEY_TEMPLATES - Mẫu khảo sát/kịch bản cho AI
CREATE TABLE IF NOT EXISTS survey_templates (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    template_name       VARCHAR(100)    NOT NULL,
    description         TEXT,
    questions_json      JSON            COMMENT 'Danh sách câu hỏi dạng JSON',
    is_active           BOOLEAN         DEFAULT TRUE,
    created_by          BIGINT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_survey_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu mẫu khảo sát/kịch bản cho AI Callbot';

-- Bảng CALL_CAMPAIGNS - Chiến dịch gọi điện
CREATE TABLE IF NOT EXISTS call_campaigns (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    campaign_name       VARCHAR(100)    NOT NULL,
    campaign_type       ENUM('FOLLOW_UP', 'SURVEY', 'APPOINTMENT_REMINDER', 'HEALTH_CHECK') NOT NULL,
    target_audience     ENUM('EXISTING_PATIENTS', 'NEW_PATIENTS', 'ALL'),
    script_template     TEXT            COMMENT 'Kịch bản cho bot',
    survey_template_id  BIGINT,
    start_date          DATE,
    end_date            DATE,
    status              ENUM('DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED') DEFAULT 'DRAFT',
    created_by          BIGINT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_campaign_survey 
        FOREIGN KEY (survey_template_id) REFERENCES survey_templates(id) ON DELETE SET NULL,
    CONSTRAINT fk_campaign_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_campaign_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu chiến dịch gọi điện tự động';

-- Bảng CALL_LOGS - Lịch sử cuộc gọi
CREATE TABLE IF NOT EXISTS call_logs (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    campaign_id         BIGINT,
    patient_id          BIGINT,
    phone_number        VARCHAR(20)     NOT NULL,
    call_type           ENUM('AI_BOT', 'HUMAN_TAKEOVER', 'MANUAL') NOT NULL,
    call_status         ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'NO_ANSWER', 'TRANSFERRED') NOT NULL,
    start_time          DATETIME,
    end_time            DATETIME,
    duration            INT             COMMENT 'Thời lượng (giây)',
    recording_url       VARCHAR(500),
    transcript_text     TEXT            COMMENT 'Nội dung cuộc gọi dạng text',
    ai_confidence_score DOUBLE,
    is_escalated        BOOLEAN         DEFAULT FALSE COMMENT 'Đã chuyển cho người thật',
    escalation_reason   VARCHAR(255),
    handled_by          BIGINT          COMMENT 'Lễ tân/Bác sĩ tiếp nhận',
    survey_responses    JSON            COMMENT 'Câu trả lời khảo sát dạng JSON',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_call_campaign 
        FOREIGN KEY (campaign_id) REFERENCES call_campaigns(id) ON DELETE SET NULL,
    CONSTRAINT fk_call_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_call_handled_by 
        FOREIGN KEY (handled_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_call_patient (patient_id),
    INDEX idx_call_start_time (start_time),
    INDEX idx_call_status (call_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu lịch sử cuộc gọi AI Callbot';

-- ============================================
-- 4. QUẢN LÝ TICKET (YÊU CẦU HỖ TRỢ)
-- ============================================

-- Bảng TICKETS - Yêu cầu hỗ trợ
CREATE TABLE IF NOT EXISTS tickets (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    call_id             BIGINT          UNIQUE COMMENT 'Cuộc gọi liên quan',
    patient_id          BIGINT          NOT NULL,
    title               VARCHAR(200)    NOT NULL,
    description         TEXT,
    priority            ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    category            ENUM('MEDICAL_QUERY', 'APPOINTMENT', 'PRESCRIPTION', 'TECHNICAL', 'OTHER'),
    status              ENUM('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    created_by_id       BIGINT          NOT NULL COMMENT 'Lễ tân tạo ticket',
    assigned_to_id      BIGINT          COMMENT 'Bác sĩ được assign',
    resolved_by_id      BIGINT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at         DATETIME,
    
    CONSTRAINT fk_ticket_call 
        FOREIGN KEY (call_id) REFERENCES call_logs(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_created_by 
        FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_assigned_to 
        FOREIGN KEY (assigned_to_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_resolved_by 
        FOREIGN KEY (resolved_by_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_ticket_status (status),
    INDEX idx_ticket_assigned (assigned_to_id),
    INDEX idx_ticket_patient (patient_id),
    INDEX idx_ticket_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu yêu cầu hỗ trợ (khi AI không giải quyết được)';

-- Bảng TICKET_MESSAGES - Tin nhắn trong ticket
CREATE TABLE IF NOT EXISTS ticket_messages (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    ticket_id           BIGINT          NOT NULL,
    sender_id           BIGINT          NOT NULL,
    message_text        TEXT            NOT NULL,
    message_type        ENUM('TEXT', 'FILE', 'SYSTEM') DEFAULT 'TEXT',
    attachment_url      VARCHAR(500),
    is_internal_note    BOOLEAN         DEFAULT FALSE COMMENT 'Ghi chú nội bộ (bác sĩ - lễ tân)',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_msg_ticket 
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender 
        FOREIGN KEY (sender_id) REFERENCES users(id),
    INDEX idx_msg_ticket (ticket_id),
    INDEX idx_msg_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu tin nhắn trong ticket (hội thoại bệnh nhân - bác sĩ)';

-- ============================================
-- 5. QUẢN LÝ ĐIỀU TRỊ
-- ============================================

-- Bảng PRESCRIPTIONS - Đơn thuốc
CREATE TABLE IF NOT EXISTS prescriptions (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    doctor_id           BIGINT          NOT NULL,
    prescription_date   DATE            NOT NULL,
    diagnosis           TEXT,
    notes               TEXT,
    status              ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'ACTIVE',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_prescription_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_doctor 
        FOREIGN KEY (doctor_id) REFERENCES users(id),
    INDEX idx_prescription_patient (patient_id),
    INDEX idx_prescription_doctor (doctor_id),
    INDEX idx_prescription_date (prescription_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu đơn thuốc';

-- Bảng PRESCRIPTION_DETAILS - Chi tiết đơn thuốc
CREATE TABLE IF NOT EXISTS prescription_details (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    prescription_id     BIGINT          NOT NULL,
    medicine_name       VARCHAR(200)    NOT NULL,
    dosage              VARCHAR(100)    COMMENT 'Liều lượng (vd: 500mg)',
    frequency           VARCHAR(100)    COMMENT 'Tần suất (vd: 3 lần/ngày)',
    duration            VARCHAR(100)    COMMENT 'Thời gian (vd: 7 ngày)',
    instructions        TEXT            COMMENT 'Hướng dẫn sử dụng',
    quantity            INT,
    
    CONSTRAINT fk_detail_prescription 
        FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu chi tiết đơn thuốc';

-- Bảng TREATMENT_PLANS - Kế hoạch điều trị (được hỗ trợ bởi AI)
CREATE TABLE IF NOT EXISTS treatment_plans (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    doctor_id           BIGINT          NOT NULL,
    diagnosis           TEXT,
    treatment_goal      TEXT,
    start_date          DATE,
    expected_end_date   DATE,
    status              ENUM('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT',
    ai_suggested        BOOLEAN         DEFAULT FALSE,
    ai_suggestion_data  JSON            COMMENT 'Lưu gợi ý từ AI',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_plan_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_doctor 
        FOREIGN KEY (doctor_id) REFERENCES users(id),
    INDEX idx_plan_patient (patient_id),
    INDEX idx_plan_doctor (doctor_id),
    INDEX idx_plan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu kế hoạch điều trị (có hỗ trợ AI)';

-- Bảng TREATMENT_PLAN_ITEMS - Chi tiết kế hoạch điều trị
CREATE TABLE IF NOT EXISTS treatment_plan_items (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    plan_id             BIGINT          NOT NULL,
    item_type           ENUM('MEDICATION', 'THERAPY', 'LIFESTYLE', 'CHECKUP') NOT NULL,
    description         TEXT            NOT NULL,
    frequency           VARCHAR(100)    COMMENT 'Tần suất (vd: 2 lần/ngày)',
    duration            VARCHAR(100)    COMMENT 'Thời gian (vd: 7 ngày)',
    notes               TEXT,
    status              ENUM('PENDING', 'ONGOING', 'COMPLETED', 'SKIPPED') DEFAULT 'PENDING',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_item_plan 
        FOREIGN KEY (plan_id) REFERENCES treatment_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu chi tiết kế hoạch điều trị';

-- ============================================
-- 6. THÔNG BÁO & PHẢN HỒI
-- ============================================

-- Bảng NOTIFICATIONS - Thông báo cho người dùng
CREATE TABLE IF NOT EXISTS notifications (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NOT NULL,
    notification_type   ENUM('TICKET', 'REMINDER', 'MESSAGE', 'SYSTEM', 'CALL') NOT NULL,
    title               VARCHAR(200)    NOT NULL,
    content             TEXT,
    reference_id        BIGINT          COMMENT 'ID của ticket, call, reminder...',
    reference_type      VARCHAR(30)     COMMENT 'TICKET, CALL, REMINDER...',
    is_read             BOOLEAN         DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at             DATETIME,
    
    CONSTRAINT fk_notification_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_read (is_read),
    INDEX idx_notification_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu thông báo cho người dùng';

-- Bảng FEEDBACKS - Phản hồi từ người dùng
CREATE TABLE IF NOT EXISTS feedbacks (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    call_id             BIGINT,
    ticket_id           BIGINT,
    user_id             BIGINT          NOT NULL,
    rating              INT             NOT NULL CHECK (rating BETWEEN 1 AND 5),
    feedback_text       TEXT,
    feedback_type       ENUM('CALL_QUALITY', 'SERVICE', 'AI_PERFORMANCE', 'GENERAL') NOT NULL,
    is_reviewed         BOOLEAN         DEFAULT FALSE,
    reviewed_by         BIGINT,
    reviewed_at         DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_feedback_call 
        FOREIGN KEY (call_id) REFERENCES call_logs(id) ON DELETE SET NULL,
    CONSTRAINT fk_feedback_ticket 
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE SET NULL,
    CONSTRAINT fk_feedback_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_reviewed_by 
        FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_feedback_user (user_id),
    INDEX idx_feedback_type (feedback_type),
    INDEX idx_feedback_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu phản hồi từ người dùng';

-- ============================================
-- 7. HUẤN LUYỆN AI
-- ============================================

-- Bảng KNOWLEDGE_BASE - Cơ sở tri thức để huấn luyện AI
CREATE TABLE IF NOT EXISTS knowledge_base (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    category            VARCHAR(100),
    question            TEXT            NOT NULL,
    answer              TEXT            NOT NULL,
    context             TEXT,
    source_type         ENUM('CALL', 'TICKET', 'MANUAL', 'IMPORTED'),
    source_id           BIGINT          COMMENT 'Reference to call_id or ticket_id',
    confidence_score    DOUBLE,
    usage_count         INT             DEFAULT 0,
    is_approved         BOOLEAN         DEFAULT FALSE,
    approved_by         BIGINT,
    approved_at         DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_used_at        DATETIME,
    
    CONSTRAINT fk_kb_approved_by 
        FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_kb_category (category),
    INDEX idx_kb_approved (is_approved),
    INDEX idx_kb_source (source_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu cơ sở tri thức để huấn luyện AI Callbot';

-- Bảng AI_TRAINING_DATA - Dữ liệu huấn luyện AI
CREATE TABLE IF NOT EXISTS ai_training_data (
    id                      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    call_id                 BIGINT,
    ticket_id               BIGINT,
    input_text              TEXT            NOT NULL,
    expected_output         TEXT,
    actual_output           TEXT,
    feedback_score          INT             CHECK (feedback_score BETWEEN 1 AND 5),
    is_used_for_training    BOOLEAN         DEFAULT FALSE,
    training_batch_id       VARCHAR(50),
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_training_call 
        FOREIGN KEY (call_id) REFERENCES call_logs(id) ON DELETE SET NULL,
    CONSTRAINT fk_training_ticket 
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE SET NULL,
    INDEX idx_training_batch (training_batch_id),
    INDEX idx_training_used (is_used_for_training)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu dữ liệu huấn luyện AI từ cuộc gọi và ticket';

-- ============================================
-- 8. DỮ LIỆU MẪU (SAMPLE DATA)
-- ============================================

-- Tạo tài khoản Admin mặc định
-- Mật khẩu: Admin@123 (đã mã hóa bằng BCrypt)
INSERT INTO users (email, password_hash, role, is_active, email_verified) VALUES
('admin@clinic.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', TRUE, TRUE);

INSERT INTO user_info (user_id, full_name) VALUES
(1, 'Quản trị viên hệ thống');

-- ============================================
-- 9. VIEWS HỮU ÍCH
-- ============================================

-- View hiển thị thông tin user đầy đủ (kết hợp users + user_info)
CREATE OR REPLACE VIEW v_user_full_info AS
SELECT 
    u.id,
    u.email,
    u.phone,
    u.role,
    u.is_active,
    u.email_verified,
    u.phone_verified,
    u.last_login,
    u.created_at,
    ui.full_name,
    ui.date_of_birth,
    ui.gender,
    ui.address,
    ui.avatar_url
FROM users u
LEFT JOIN user_info ui ON u.id = ui.user_id;

-- View thống kê ticket theo trạng thái
CREATE OR REPLACE VIEW v_ticket_stats AS
SELECT 
    status,
    priority,
    COUNT(*) as total_count,
    DATE(created_at) as date
FROM tickets
GROUP BY status, priority, DATE(created_at);

-- View thống kê cuộc gọi theo ngày
CREATE OR REPLACE VIEW v_call_stats AS
SELECT 
    DATE(start_time) as call_date,
    call_type,
    call_status,
    COUNT(*) as total_calls,
    AVG(duration) as avg_duration,
    SUM(CASE WHEN is_escalated = TRUE THEN 1 ELSE 0 END) as escalated_count
FROM call_logs
GROUP BY DATE(start_time), call_type, call_status;

-- ============================================
-- 10. STORED PROCEDURES
-- ============================================

DELIMITER //

-- Procedure lấy thống kê tổng quan hệ thống
CREATE PROCEDURE sp_get_dashboard_stats()
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM users WHERE role = 'PATIENT') as total_patients,
        (SELECT COUNT(*) FROM users WHERE role = 'DOCTOR') as total_doctors,
        (SELECT COUNT(*) FROM users WHERE role = 'RECEPTIONIST') as total_receptionists,
        (SELECT COUNT(*) FROM tickets WHERE status NOT IN ('CLOSED', 'RESOLVED')) as open_tickets,
        (SELECT COUNT(*) FROM call_logs WHERE DATE(start_time) = CURDATE()) as today_calls,
        (SELECT COUNT(*) FROM call_logs WHERE is_escalated = TRUE AND DATE(start_time) = CURDATE()) as today_escalated;
END //

-- Procedure tìm kiếm bệnh nhân
CREATE PROCEDURE sp_search_patients(IN search_term VARCHAR(100))
BEGIN
    SELECT 
        u.id,
        u.email,
        u.phone,
        ui.full_name,
        ui.date_of_birth,
        ui.gender,
        u.created_at
    FROM users u
    LEFT JOIN user_info ui ON u.id = ui.user_id
    WHERE u.role = 'PATIENT'
      AND (ui.full_name LIKE CONCAT('%', search_term, '%')
           OR u.email LIKE CONCAT('%', search_term, '%')
           OR u.phone LIKE CONCAT('%', search_term, '%'));
END //

DELIMITER ;

-- ============================================
-- THÔNG TIN SCHEMA
-- ============================================
-- Tổng số bảng: 15
-- 1. users              - Thông tin tài khoản
-- 2. user_info          - Thông tin cá nhân
-- 3. staff_info         - Thông tin nhân viên
-- 4. patient_documents  - Tài liệu bệnh nhân
-- 5. survey_templates   - Mẫu khảo sát
-- 6. call_campaigns     - Chiến dịch gọi điện
-- 7. call_logs          - Lịch sử cuộc gọi
-- 8. tickets            - Yêu cầu hỗ trợ
-- 9. ticket_messages    - Tin nhắn ticket
-- 10. prescriptions     - Đơn thuốc
-- 11. prescription_details - Chi tiết đơn thuốc
-- 12. treatment_plans   - Kế hoạch điều trị
-- 13. treatment_plan_items - Chi tiết kế hoạch
-- 14. notifications     - Thông báo
-- 15. feedbacks         - Phản hồi
-- 16. knowledge_base    - Cơ sở tri thức AI
-- 17. ai_training_data  - Dữ liệu huấn luyện AI
-- ============================================
