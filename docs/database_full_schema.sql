-- ============================================
-- CAPSTONE PROJECT - AI CALLBOT PHONG KHAM
-- FULL DATABASE SCHEMA - MySQL 8.0+
-- ============================================
-- Ngay tao: 01/02/2026
-- Phien ban: 3.0 (Cap nhat theo Entity classes)
-- Nhom: G4
-- ============================================

-- Xoa database cu neu ton tai (CHI DUNG CHO MOI TRUONG DEVELOPMENT)
-- DROP DATABASE IF EXISTS capstone_project;

-- Tao database
CREATE DATABASE IF NOT EXISTS capstone_project
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE capstone_project;

-- ============================================
-- 1. QUAN LY NGUOI DUNG
-- ============================================

-- Bang USERS - Thong tin tai khoan va bao mat
-- Luu tru thong tin dang nhap, phan quyen, trang thai tai khoan
CREATE TABLE IF NOT EXISTS users (
    id                              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    email                           VARCHAR(100)    UNIQUE,
    phone                           VARCHAR(20)     UNIQUE,
    password_hash                   VARCHAR(255),
    google_id                       VARCHAR(100)    UNIQUE,
    role                            ENUM('PATIENT', 'RECEPTIONIST', 'DOCTOR', 'ADMIN') NOT NULL DEFAULT 'PATIENT',
    is_active                       BOOLEAN         NOT NULL DEFAULT TRUE,
    email_verified                  BOOLEAN         NOT NULL DEFAULT FALSE,
    email_verification_token        VARCHAR(255)    COMMENT 'Token xac thuc email',
    email_verification_token_expiry DATETIME        COMMENT 'Thoi gian het han token xac thuc email',
    password_reset_token            VARCHAR(255)    COMMENT 'Token reset mat khau',
    password_reset_token_expiry     DATETIME        COMMENT 'Thoi gian het han token reset mat khau',
    created_at                      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login                      DATETIME,
    
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu thong tin tai khoan va bao mat nguoi dung';

-- Bang USER_INFO - Thong tin ca nhan nguoi dung
-- Tach rieng khoi users de phan biet thong tin bao mat va thong tin ca nhan
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
COMMENT='Bang luu thong tin ca nhan nguoi dung (nullable fields)';

-- ============================================
-- 2. QUAN LY TAI LIEU BENH NHAN
-- ============================================

-- Bang PATIENT_DOCUMENTS - Tai lieu cua benh nhan
CREATE TABLE IF NOT EXISTS patient_documents (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    document_type       ENUM('MEDICAL_HISTORY', 'PRESCRIPTION', 'TEST_RESULT', 'OTHER') NOT NULL,
    file_name           VARCHAR(255)    NOT NULL,
    file_url            VARCHAR(500)    NOT NULL,
    file_size           BIGINT,
    description         VARCHAR(500),
    upload_date         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_doc_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_doc_patient (patient_id),
    INDEX idx_doc_type (document_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu tai lieu y te cua benh nhan';

-- ============================================
-- 3. QUAN LY CHI SO SUC KHOE
-- ============================================

-- Bang VITAL_SIGNS - Chi so sinh ton cua benh nhan
-- Luu tru cac chi so y te quan trong nhu huyet ap, can nang, nhip tim, nhiet do
CREATE TABLE IF NOT EXISTS vital_signs (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    systolic_pressure   INT             COMMENT 'Huyet ap tam thu (mmHg)',
    diastolic_pressure  INT             COMMENT 'Huyet ap tam truong (mmHg)',
    heart_rate          INT             COMMENT 'Nhip tim (bpm)',
    weight              DECIMAL(5,2)    COMMENT 'Can nang (kg)',
    height              DECIMAL(5,2)    COMMENT 'Chieu cao (cm)',
    bmi                 DECIMAL(4,2)    COMMENT 'Chi so BMI',
    temperature         DECIMAL(4,2)    COMMENT 'Nhiet do (°C)',
    respiratory_rate    INT             COMMENT 'Nhip tho (breaths/min)',
    oxygen_saturation   INT             COMMENT 'Do bao hoa oxy (%)',
    blood_sugar         DECIMAL(5,2)    COMMENT 'Duong huyet (mg/dL hoac mmol/L)',
    notes               TEXT            COMMENT 'Ghi chu',
    recorded_by         BIGINT          COMMENT 'Bac si hoac y ta ghi nhan',
    record_date         DATETIME        NOT NULL COMMENT 'Thoi gian ghi nhan',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_vital_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_vital_recorded_by 
        FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_patient_id (patient_id),
    INDEX idx_record_date (record_date),
    INDEX idx_patient_date (patient_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu chi so sinh ton cua benh nhan';

-- Bang FAMILY_MEDICAL_HISTORY - Tien su benh gia dinh
-- Luu tru tien su benh cua thanh vien gia dinh benh nhan
CREATE TABLE IF NOT EXISTS family_medical_history (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    created_by          BIGINT          COMMENT 'Nguoi tao ban ghi',
    relationship        ENUM('FATHER', 'MOTHER', 'GRANDFATHER_P', 'GRANDMOTHER_P', 
                             'GRANDFATHER_M', 'GRANDMOTHER_M', 'SIBLING', 'UNCLE_AUNT', 'OTHER') 
                        NOT NULL COMMENT 'Quan he voi benh nhan',
    `condition`         VARCHAR(100)    NOT NULL COMMENT 'Ten benh/tinh trang',
    age_at_diagnosis    INT             COMMENT 'Tuoi khi phat hien benh',
    member_status       ENUM('ALIVE', 'DECEASED', 'UNKNOWN') COMMENT 'Trang thai thanh vien',
    notes               TEXT            COMMENT 'Ghi chu them',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_family_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_family_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_family_patient (patient_id),
    INDEX idx_family_relationship (relationship)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu tien su benh gia dinh cua benh nhan';

-- Bang MEDICAL_REPORTS - Bao cao y te
-- Luu tru cac bao cao xet nghiem, chan doan hinh anh, kham benh
CREATE TABLE IF NOT EXISTS medical_reports (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    created_by          BIGINT          COMMENT 'Nguoi tao bao cao',
    report_type         ENUM('LAB_TEST', 'IMAGING', 'PATHOLOGY', 'VITAL_SIGNS', 'CONSULTATION', 'OTHER') 
                        NOT NULL COMMENT 'Loai bao cao',
    report_date         DATE            NOT NULL COMMENT 'Ngay bao cao',
    title               VARCHAR(255)    NOT NULL COMMENT 'Tieu de bao cao',
    content             TEXT            COMMENT 'Noi dung bao cao',
    notes               TEXT            COMMENT 'Ghi chu',
    file_url            VARCHAR(500)    COMMENT 'URL file dinh kem',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_report_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_report_patient (patient_id),
    INDEX idx_report_date (report_date),
    INDEX idx_report_type (report_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu bao cao y te cua benh nhan';

-- Bang HEALTH_FORECASTS - Du bao suc khoe
-- Luu tru du bao rui ro benh tat dua tren vital signs, tien su, va cac yeu to nguy co
CREATE TABLE IF NOT EXISTS health_forecasts (
    id                      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    patient_id              BIGINT          NOT NULL,
    forecast_date           DATE            NOT NULL COMMENT 'Ngay du bao',
    risk_scores             JSON            COMMENT 'Diem rui ro cac benh ly (cardiovascular, diabetes, hypertension, stroke)',
    predictions             JSON            COMMENT 'Du doan xu huong cac chi so suc khoe',
    risk_factors            JSON            COMMENT 'Cac yeu to nguy co da phan tich',
    recommendations         TEXT            COMMENT 'Khuyen nghi phong ngua va dieu tri',
    vital_signs_snapshot    JSON            COMMENT 'Anh chup cac chi so sinh ton tai thoi diem du bao',
    created_by              BIGINT          COMMENT 'Bac si tao du bao',
    status                  ENUM('DRAFT', 'ACTIVE', 'OUTDATED', 'ARCHIVED') NOT NULL DEFAULT 'ACTIVE'
                            COMMENT 'Trang thai du bao',
    notes                   TEXT            COMMENT 'Ghi chu',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME,
    
    CONSTRAINT fk_forecast_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_forecast_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_forecast_patient_id (patient_id),
    INDEX idx_forecast_date (forecast_date),
    INDEX idx_forecast_patient_date (patient_id, forecast_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu du bao suc khoe cua benh nhan';

-- ============================================
-- 4. AI CALLBOT & CHIEN DICH GOI DIEN
-- ============================================

-- Bang SURVEY_TEMPLATES - Mau khao sat/kich ban cho AI
CREATE TABLE IF NOT EXISTS survey_templates (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    template_name       VARCHAR(100)    NOT NULL,
    description         TEXT,
    questions_json      JSON            COMMENT 'Danh sach cau hoi dang JSON',
    is_active           BOOLEAN         DEFAULT TRUE,
    created_by          BIGINT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_survey_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu mau khao sat/kich ban cho AI Callbot';

-- Bang CALL_CAMPAIGNS - Chien dich goi dien
CREATE TABLE IF NOT EXISTS call_campaigns (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    campaign_name       VARCHAR(100)    NOT NULL,
    campaign_type       ENUM('FOLLOW_UP', 'SURVEY', 'APPOINTMENT_REMINDER', 'HEALTH_CHECK') NOT NULL,
    target_audience     ENUM('EXISTING_PATIENTS', 'NEW_PATIENTS', 'ALL'),
    script_template     TEXT            COMMENT 'Kich ban cho bot',
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
COMMENT='Bang luu chien dich goi dien tu dong';

-- Bang CALL_LOGS - Lich su cuoc goi (AI Bot goi dien thoai)
CREATE TABLE IF NOT EXISTS call_logs (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    campaign_id         BIGINT,
    patient_id          BIGINT,
    phone_number        VARCHAR(20)     NOT NULL,
    call_type           ENUM('AI_BOT', 'HUMAN_TAKEOVER', 'MANUAL') NOT NULL,
    call_status         ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'NO_ANSWER', 'TRANSFERRED') NOT NULL,
    start_time          DATETIME,
    end_time            DATETIME,
    duration            INT             COMMENT 'Thoi luong (giay)',
    recording_url       VARCHAR(500),
    transcript_text     TEXT            COMMENT 'Noi dung cuoc goi dang text',
    ai_confidence_score DOUBLE,
    is_escalated        BOOLEAN         DEFAULT FALSE COMMENT 'Da chuyen cho nguoi that',
    escalation_reason   VARCHAR(255),
    handled_by          BIGINT          COMMENT 'Le tan/Bac si tiep nhan',
    survey_responses    JSON            COMMENT 'Cau tra loi khao sat dang JSON',
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
COMMENT='Bang luu lich su cuoc goi AI Callbot qua dien thoai';

-- Bang WEB_CALL_LOGS - Lich su cuoc goi Web-to-Web
-- Danh rieng cho cuoc goi giua 2 user da dang nhap qua trinh duyet
CREATE TABLE IF NOT EXISTS web_call_logs (
    id                      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    stringee_call_id        VARCHAR(100)    COMMENT 'Stringee Call ID de tracking',
    caller_id               BIGINT          NOT NULL COMMENT 'Nguoi goi',
    receiver_id             BIGINT          NOT NULL COMMENT 'Nguoi nhan cuoc goi',
    call_status             ENUM('INITIATED', 'RINGING', 'ANSWERED', 'COMPLETED', 'MISSED', 
                                 'REJECTED', 'CANCELLED', 'FAILED') NOT NULL DEFAULT 'INITIATED'
                            COMMENT 'Trang thai cuoc goi',
    start_time              DATETIME        COMMENT 'Thoi gian bat dau goi',
    end_time                DATETIME        COMMENT 'Thoi gian ket thuc',
    duration                INT             COMMENT 'Thoi luong cuoc goi (giay)',
    recording_s3_key        VARCHAR(500)    COMMENT 'S3 Key cua file ghi am',
    recording_url           VARCHAR(1000)   COMMENT 'URL pre-signed de nghe lai (tam thoi, het han 7 ngay)',
    recording_url_expiry    DATETIME        COMMENT 'Thoi gian het han cua recording URL',
    transcript_text         TEXT            COMMENT 'Transcript text (neu co AI phan tich)',
    rating                  INT             COMMENT 'Danh gia chat luong cuoc goi (1-5 sao)',
    notes                   VARCHAR(500)    COMMENT 'Ghi chu cua nguoi dung',
    has_recording           BOOLEAN         DEFAULT FALSE COMMENT 'Cuoc goi co ghi am khong',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_webcall_caller 
        FOREIGN KEY (caller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_webcall_receiver 
        FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_webcall_caller (caller_id),
    INDEX idx_webcall_receiver (receiver_id),
    INDEX idx_webcall_start_time (start_time),
    INDEX idx_webcall_status (call_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu lich su cuoc goi Web-to-Web giua 2 user qua trinh duyet';

-- ============================================
-- 5. QUAN LY TICKET (YEU CAU HO TRO)
-- ============================================

-- Bang TICKETS - Yeu cau ho tro
CREATE TABLE IF NOT EXISTS tickets (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    call_id             BIGINT          UNIQUE COMMENT 'Cuoc goi lien quan',
    patient_id          BIGINT          NOT NULL,
    title               VARCHAR(200)    NOT NULL,
    description         TEXT,
    priority            ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    category            ENUM('MEDICAL_QUERY', 'APPOINTMENT', 'PRESCRIPTION', 'TECHNICAL', 'OTHER'),
    status              ENUM('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    created_by_id       BIGINT          NOT NULL COMMENT 'Le tan tao ticket',
    assigned_to_id      BIGINT          COMMENT 'Bac si duoc assign',
    resolved_by_id      BIGINT,
    retry_count         INT             COMMENT 'So lan thu goi lai benh nhan',
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
COMMENT='Bang luu yeu cau ho tro (khi AI khong giai quyet duoc)';

-- Bang TICKET_MESSAGES - Tin nhan trong ticket
CREATE TABLE IF NOT EXISTS ticket_messages (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    ticket_id           BIGINT          NOT NULL,
    sender_id           BIGINT          NOT NULL,
    message_text        TEXT            NOT NULL,
    message_type        ENUM('TEXT', 'FILE', 'SYSTEM') DEFAULT 'TEXT',
    attachment_url      VARCHAR(500),
    is_internal_note    BOOLEAN         DEFAULT FALSE COMMENT 'Ghi chu noi bo (bac si - le tan)',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_msg_ticket 
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender 
        FOREIGN KEY (sender_id) REFERENCES users(id),
    INDEX idx_msg_ticket (ticket_id),
    INDEX idx_msg_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu tin nhan trong ticket (hoi thoai benh nhan - bac si)';

-- ============================================
-- 6. QUAN LY DIEU TRI
-- ============================================

-- Bang PRESCRIPTIONS - Don thuoc
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
COMMENT='Bang luu don thuoc';

-- Bang PRESCRIPTION_DETAILS - Chi tiet don thuoc
CREATE TABLE IF NOT EXISTS prescription_details (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    prescription_id     BIGINT          NOT NULL,
    medicine_name       VARCHAR(200)    NOT NULL,
    dosage              VARCHAR(100)    COMMENT 'Lieu luong (vd: 500mg)',
    frequency           VARCHAR(100)    COMMENT 'Tan suat (vd: 3 lan/ngay)',
    duration            VARCHAR(100)    COMMENT 'Thoi gian (vd: 7 ngay)',
    instructions        TEXT            COMMENT 'Huong dan su dung',
    quantity            INT,
    
    CONSTRAINT fk_detail_prescription 
        FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu chi tiet don thuoc';

-- Bang TREATMENT_PLANS - Ke hoach dieu tri (duoc ho tro boi AI)
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
    ai_suggestion_data  JSON            COMMENT 'Luu goi y tu AI',
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
COMMENT='Bang luu ke hoach dieu tri (co ho tro AI)';

-- Bang TREATMENT_PLAN_ITEMS - Chi tiet ke hoach dieu tri
CREATE TABLE IF NOT EXISTS treatment_plan_items (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    plan_id             BIGINT          NOT NULL,
    item_type           ENUM('MEDICATION', 'THERAPY', 'LIFESTYLE', 'CHECKUP') NOT NULL,
    description         TEXT            NOT NULL,
    frequency           VARCHAR(100)    COMMENT 'Tan suat (vd: 2 lan/ngay)',
    duration            VARCHAR(100)    COMMENT 'Thoi gian (vd: 7 ngay)',
    notes               TEXT,
    status              ENUM('PENDING', 'ONGOING', 'COMPLETED', 'SKIPPED') DEFAULT 'PENDING',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_item_plan 
        FOREIGN KEY (plan_id) REFERENCES treatment_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu chi tiet ke hoach dieu tri';

-- Bang CHECKUP_SCHEDULES - Lich tai kham dinh ky
-- Lien ket voi ke hoach dieu tri de theo doi lich tai kham
CREATE TABLE IF NOT EXISTS checkup_schedules (
    id                      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    treatment_plan_id       BIGINT          NOT NULL COMMENT 'Ke hoach dieu tri lien quan',
    patient_id              BIGINT          NOT NULL,
    doctor_id               BIGINT          NOT NULL,
    scheduled_date          DATE            NOT NULL COMMENT 'Ngay tai kham du kien',
    checkup_type            VARCHAR(50)     COMMENT 'Loai tai kham: routine, follow_up, emergency',
    status                  ENUM('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') 
                            NOT NULL DEFAULT 'SCHEDULED' COMMENT 'Trang thai lich tai kham',
    notes                   TEXT            COMMENT 'Ghi chu truoc khi tai kham',
    completed_date          DATE            COMMENT 'Ngay hoan thanh kham thuc te',
    result_summary          TEXT            COMMENT 'Tom tat ket qua sau khi kham',
    next_checkup_suggestion DATE            COMMENT 'Goi y ngay tai kham tiep theo',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_checkup_treatment_plan 
        FOREIGN KEY (treatment_plan_id) REFERENCES treatment_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkup_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkup_doctor 
        FOREIGN KEY (doctor_id) REFERENCES users(id),
    INDEX idx_checkup_treatment_plan (treatment_plan_id),
    INDEX idx_checkup_patient (patient_id),
    INDEX idx_checkup_doctor (doctor_id),
    INDEX idx_checkup_status (status),
    INDEX idx_checkup_date (scheduled_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu lich tai kham dinh ky trong ke hoach dieu tri';

-- ============================================
-- 7. HE THONG KIEN THUC (KNOWLEDGE BASE)
-- ============================================

-- Bang KNOWLEDGE_CATEGORIES - Danh muc kien thuc
-- Ho tro phan cap (category cha - con)
CREATE TABLE IF NOT EXISTS knowledge_categories (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200)    NOT NULL COMMENT 'Ten danh muc',
    description         TEXT            COMMENT 'Mo ta danh muc',
    parent_id           BIGINT          COMMENT 'Danh muc cha (null neu la root)',
    display_order       INT             COMMENT 'Thu tu hien thi',
    active              BOOLEAN         NOT NULL DEFAULT TRUE COMMENT 'Trang thai kich hoat',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_category_parent 
        FOREIGN KEY (parent_id) REFERENCES knowledge_categories(id) ON DELETE SET NULL,
    INDEX idx_category_parent (parent_id),
    INDEX idx_category_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu danh muc kien thuc (ho tro phan cap)';

-- Bang KNOWLEDGE_ARTICLES - Bai viet kien thuc
-- Luu tru cac bai viet y te, huong dan, thong tin suc khoe
CREATE TABLE IF NOT EXISTS knowledge_articles (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(500)    NOT NULL COMMENT 'Tieu de bai viet',
    summary             TEXT            COMMENT 'Tom tat noi dung',
    content             LONGTEXT        NOT NULL COMMENT 'Noi dung bai viet',
    category_id         BIGINT          COMMENT 'Danh muc bai viet',
    tags                VARCHAR(1000)   COMMENT 'Cac tag (phan cach boi dau phay)',
    created_by          BIGINT          NOT NULL COMMENT 'Nguoi tao bai viet',
    updated_by          BIGINT          COMMENT 'Nguoi cap nhat cuoi',
    status              ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT'
                        COMMENT 'Trang thai bai viet',
    views               INT             NOT NULL DEFAULT 0 COMMENT 'So luot xem',
    featured            BOOLEAN         DEFAULT FALSE COMMENT 'Bai viet noi bat',
    published_at        DATETIME        COMMENT 'Thoi gian xuat ban',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_article_category 
        FOREIGN KEY (category_id) REFERENCES knowledge_categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_article_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_article_updated_by 
        FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang luu bai viet kien thuc y te';

-- ============================================
-- 8. THONG BAO & PHAN HOI
-- ============================================

-- Bang NOTIFICATIONS - Thong bao cho nguoi dung
CREATE TABLE IF NOT EXISTS notifications (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NOT NULL,
    notification_type   ENUM('TICKET', 'REMINDER', 'MESSAGE', 'SYSTEM', 'CALL') NOT NULL,
    title               VARCHAR(200)    NOT NULL,
    content             TEXT,
    reference_id        BIGINT          COMMENT 'ID cua ticket, call, reminder...',
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
COMMENT='Bang luu thong bao cho nguoi dung';

-- Bang FEEDBACKS - Phan hoi tu nguoi dung
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
COMMENT='Bang luu phan hoi tu nguoi dung';

-- ============================================
-- 9. DU LIEU MAU (SAMPLE DATA)
-- ============================================

-- Tao tai khoan Admin mac dinh
-- Mat khau: Admin@123 (da ma hoa bang BCrypt)
INSERT INTO users (email, password_hash, role, is_active, email_verified) VALUES
('admin@clinic.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', TRUE, TRUE);

INSERT INTO user_info (user_id, full_name) VALUES
(1, 'Quan tri vien he thong');

-- ============================================
-- 10. VIEWS HUU ICH
-- ============================================

-- View hien thi thong tin user day du (ket hop users + user_info)
CREATE OR REPLACE VIEW v_user_full_info AS
SELECT 
    u.id,
    u.email,
    u.phone,
    u.role,
    u.is_active,
    u.email_verified,
    u.last_login,
    u.created_at,
    ui.full_name,
    ui.date_of_birth,
    ui.gender,
    ui.address,
    ui.avatar_url
FROM users u
LEFT JOIN user_info ui ON u.id = ui.user_id;

-- View thong ke ticket theo trang thai
CREATE OR REPLACE VIEW v_ticket_stats AS
SELECT 
    status,
    priority,
    COUNT(*) as total_count,
    DATE(created_at) as date
FROM tickets
GROUP BY status, priority, DATE(created_at);

-- View thong ke cuoc goi theo ngay (call_logs - AI Bot)
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

-- View thong ke cuoc goi web theo ngay (web_call_logs)
CREATE OR REPLACE VIEW v_web_call_stats AS
SELECT 
    DATE(start_time) as call_date,
    call_status,
    COUNT(*) as total_calls,
    AVG(duration) as avg_duration,
    SUM(CASE WHEN has_recording = TRUE THEN 1 ELSE 0 END) as recorded_count
FROM web_call_logs
GROUP BY DATE(start_time), call_status;

-- View thong ke vital signs gan nhat cua benh nhan
CREATE OR REPLACE VIEW v_latest_vital_signs AS
SELECT 
    vs.*,
    ui.full_name as patient_name,
    u.email as patient_email
FROM vital_signs vs
INNER JOIN (
    SELECT patient_id, MAX(record_date) as latest_date
    FROM vital_signs
    GROUP BY patient_id
) latest ON vs.patient_id = latest.patient_id AND vs.record_date = latest.latest_date
LEFT JOIN users u ON vs.patient_id = u.id
LEFT JOIN user_info ui ON u.id = ui.user_id;

-- ============================================
-- 11. STORED PROCEDURES
-- ============================================

DELIMITER //

-- Procedure lay thong ke tong quan he thong
CREATE PROCEDURE sp_get_dashboard_stats()
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM users WHERE role = 'PATIENT') as total_patients,
        (SELECT COUNT(*) FROM users WHERE role = 'DOCTOR') as total_doctors,
        (SELECT COUNT(*) FROM users WHERE role = 'RECEPTIONIST') as total_receptionists,
        (SELECT COUNT(*) FROM tickets WHERE status NOT IN ('CLOSED', 'RESOLVED')) as open_tickets,
        (SELECT COUNT(*) FROM call_logs WHERE DATE(start_time) = CURDATE()) as today_calls,
        (SELECT COUNT(*) FROM call_logs WHERE is_escalated = TRUE AND DATE(start_time) = CURDATE()) as today_escalated,
        (SELECT COUNT(*) FROM web_call_logs WHERE DATE(start_time) = CURDATE()) as today_web_calls,
        (SELECT COUNT(*) FROM vital_signs WHERE DATE(record_date) = CURDATE()) as today_vital_records;
END //

-- Procedure tim kiem benh nhan
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

-- Procedure lay vital signs theo benh nhan va khoang thoi gian
CREATE PROCEDURE sp_get_vital_signs_history(
    IN p_patient_id BIGINT,
    IN p_start_date DATETIME,
    IN p_end_date DATETIME
)
BEGIN
    SELECT 
        vs.*,
        recorder.email as recorded_by_email,
        recorder_info.full_name as recorded_by_name
    FROM vital_signs vs
    LEFT JOIN users recorder ON vs.recorded_by = recorder.id
    LEFT JOIN user_info recorder_info ON recorder.id = recorder_info.user_id
    WHERE vs.patient_id = p_patient_id
      AND vs.record_date BETWEEN p_start_date AND p_end_date
    ORDER BY vs.record_date DESC;
END //

DELIMITER ;

-- ============================================
-- THONG TIN SCHEMA
-- ============================================
-- Tong so bang: 22 (theo Entity classes hien tai)
-- 1. users                  - Thong tin tai khoan (User.java)
-- 2. user_info              - Thong tin ca nhan (UserInfo.java)
-- 3. patient_documents      - Tai lieu benh nhan (PatientDocument.java)
-- 4. vital_signs            - Chi so sinh ton (VitalSigns.java)
-- 5. family_medical_history - Tien su benh gia dinh (FamilyMedicalHistory.java)
-- 6. medical_reports        - Bao cao y te (MedicalReport.java)
-- 7. health_forecasts       - Du bao suc khoe (HealthForecast.java)
-- 8. survey_templates       - Mau khao sat (SurveyTemplate.java)
-- 9. call_campaigns         - Chien dich goi dien (CallCampaign.java)
-- 10. call_logs             - Lich su cuoc goi AI Bot (CallLog.java)
-- 11. web_call_logs         - Lich su cuoc goi Web (WebCallLog.java)
-- 12. tickets               - Yeu cau ho tro (Ticket.java)
-- 13. ticket_messages       - Tin nhan ticket (TicketMessage.java)
-- 14. prescriptions         - Don thuoc (Prescription.java)
-- 15. prescription_details  - Chi tiet don thuoc (PrescriptionDetail.java)
-- 16. treatment_plans       - Ke hoach dieu tri (TreatmentPlan.java)
-- 17. treatment_plan_items  - Chi tiet ke hoach (TreatmentPlanItem.java)
-- 18. checkup_schedules     - Lich tai kham (CheckupSchedule.java)
-- 19. knowledge_categories  - Danh muc kien thuc (KnowledgeCategory.java)
-- 20. knowledge_articles    - Bai viet kien thuc (KnowledgeArticle.java)
-- 21. notifications         - Thong bao (Notification.java)
-- 22. feedbacks             - Phan hoi (Feedback.java)
-- ============================================
