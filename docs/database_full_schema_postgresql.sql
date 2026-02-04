-- ============================================
-- CAPSTONE PROJECT - AI CALLBOT PHONG KHAM
-- FULL DATABASE SCHEMA - PostgreSQL 14+
-- ============================================
-- Ngay tao: 01/02/2026
-- Phien ban: 3.0 (Cap nhat theo Entity classes)
-- Nhom: G4
-- Chuyen doi tu MySQL sang PostgreSQL
-- ============================================

-- Ket noi vao database capstone_project truoc khi chay script nay
-- psql -U capstone_user -d capstone_project -h localhost -W

-- ============================================
-- TAO CAC ENUM TYPES
-- ============================================

-- User roles
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('PATIENT', 'RECEPTIONIST', 'DOCTOR', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Gender
DO $$ BEGIN
    CREATE TYPE gender_type AS ENUM ('MALE', 'FEMALE', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Document type
DO $$ BEGIN
    CREATE TYPE document_type AS ENUM ('MEDICAL_HISTORY', 'PRESCRIPTION', 'TEST_RESULT', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Family relationship
DO $$ BEGIN
    CREATE TYPE family_relationship AS ENUM ('FATHER', 'MOTHER', 'GRANDFATHER_P', 'GRANDMOTHER_P', 
                                             'GRANDFATHER_M', 'GRANDMOTHER_M', 'SIBLING', 'UNCLE_AUNT', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Member status
DO $$ BEGIN
    CREATE TYPE member_status AS ENUM ('ALIVE', 'DECEASED', 'UNKNOWN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Report type
DO $$ BEGIN
    CREATE TYPE report_type AS ENUM ('LAB_TEST', 'IMAGING', 'PATHOLOGY', 'VITAL_SIGNS', 'CONSULTATION', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Forecast status
DO $$ BEGIN
    CREATE TYPE forecast_status AS ENUM ('DRAFT', 'ACTIVE', 'OUTDATED', 'ARCHIVED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Campaign type
DO $$ BEGIN
    CREATE TYPE campaign_type AS ENUM ('FOLLOW_UP', 'SURVEY', 'APPOINTMENT_REMINDER', 'HEALTH_CHECK');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Target audience
DO $$ BEGIN
    CREATE TYPE target_audience AS ENUM ('EXISTING_PATIENTS', 'NEW_PATIENTS', 'ALL');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Campaign status
DO $$ BEGIN
    CREATE TYPE campaign_status AS ENUM ('DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Call type
DO $$ BEGIN
    CREATE TYPE call_type AS ENUM ('AI_BOT', 'HUMAN_TAKEOVER', 'MANUAL');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Call status (for call_logs)
DO $$ BEGIN
    CREATE TYPE call_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'NO_ANSWER', 'TRANSFERRED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Web call status
DO $$ BEGIN
    CREATE TYPE web_call_status AS ENUM ('INITIATED', 'RINGING', 'ANSWERED', 'COMPLETED', 'MISSED', 
                                         'REJECTED', 'CANCELLED', 'FAILED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Priority
DO $$ BEGIN
    CREATE TYPE priority_type AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Ticket category
DO $$ BEGIN
    CREATE TYPE ticket_category AS ENUM ('MEDICAL_QUERY', 'APPOINTMENT', 'PRESCRIPTION', 'TECHNICAL', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Ticket status
DO $$ BEGIN
    CREATE TYPE ticket_status AS ENUM ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Message type
DO $$ BEGIN
    CREATE TYPE message_type AS ENUM ('TEXT', 'FILE', 'SYSTEM');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Prescription status
DO $$ BEGIN
    CREATE TYPE prescription_status AS ENUM ('ACTIVE', 'COMPLETED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Treatment plan status
DO $$ BEGIN
    CREATE TYPE plan_status AS ENUM ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Treatment item type
DO $$ BEGIN
    CREATE TYPE item_type AS ENUM ('MEDICATION', 'THERAPY', 'LIFESTYLE', 'CHECKUP');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Treatment item status
DO $$ BEGIN
    CREATE TYPE item_status AS ENUM ('PENDING', 'ONGOING', 'COMPLETED', 'SKIPPED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Checkup status
DO $$ BEGIN
    CREATE TYPE checkup_status AS ENUM ('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Article status
DO $$ BEGIN
    CREATE TYPE article_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Notification type
DO $$ BEGIN
    CREATE TYPE notification_type AS ENUM ('TICKET', 'REMINDER', 'MESSAGE', 'SYSTEM', 'CALL');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Feedback type
DO $$ BEGIN
    CREATE TYPE feedback_type AS ENUM ('CALL_QUALITY', 'SERVICE', 'AI_PERFORMANCE', 'GENERAL');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- ============================================
-- 1. QUAN LY NGUOI DUNG
-- ============================================

-- Bang USERS - Thong tin tai khoan va bao mat
CREATE TABLE IF NOT EXISTS users (
    id                              BIGSERIAL       PRIMARY KEY,
    email                           VARCHAR(100)    UNIQUE,
    phone                           VARCHAR(20)     UNIQUE,
    password_hash                   VARCHAR(255),
    google_id                       VARCHAR(100)    UNIQUE,
    role                            user_role       NOT NULL DEFAULT 'PATIENT',
    is_active                       BOOLEAN         NOT NULL DEFAULT TRUE,
    email_verified                  BOOLEAN         NOT NULL DEFAULT FALSE,
    email_verification_token        VARCHAR(255),
    email_verification_token_expiry TIMESTAMP,
    password_reset_token            VARCHAR(255),
    password_reset_token_expiry     TIMESTAMP,
    created_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login                      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);

COMMENT ON TABLE users IS 'Bang luu thong tin tai khoan va bao mat nguoi dung';
COMMENT ON COLUMN users.email_verification_token IS 'Token xac thuc email';
COMMENT ON COLUMN users.email_verification_token_expiry IS 'Thoi gian het han token xac thuc email';
COMMENT ON COLUMN users.password_reset_token IS 'Token reset mat khau';
COMMENT ON COLUMN users.password_reset_token_expiry IS 'Thoi gian het han token reset mat khau';

-- Bang USER_INFO - Thong tin ca nhan nguoi dung
CREATE TABLE IF NOT EXISTS user_info (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL UNIQUE,
    full_name           VARCHAR(100),
    date_of_birth       DATE,
    gender              gender_type,
    address             VARCHAR(500),
    avatar_url          VARCHAR(500),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_info_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_info_full_name ON user_info(full_name);

COMMENT ON TABLE user_info IS 'Bang luu thong tin ca nhan nguoi dung (nullable fields)';

-- ============================================
-- 2. QUAN LY TAI LIEU BENH NHAN
-- ============================================

-- Bang PATIENT_DOCUMENTS - Tai lieu cua benh nhan
CREATE TABLE IF NOT EXISTS patient_documents (
    id                  BIGSERIAL       PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    document_type       document_type   NOT NULL,
    file_name           VARCHAR(255)    NOT NULL,
    file_url            VARCHAR(500)    NOT NULL,
    file_size           BIGINT,
    description         VARCHAR(500),
    upload_date         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_doc_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_doc_patient ON patient_documents(patient_id);
CREATE INDEX IF NOT EXISTS idx_doc_type ON patient_documents(document_type);

COMMENT ON TABLE patient_documents IS 'Bang luu tai lieu y te cua benh nhan';

-- ============================================
-- 3. QUAN LY CHI SO SUC KHOE
-- ============================================

-- Bang VITAL_SIGNS - Chi so sinh ton cua benh nhan
CREATE TABLE IF NOT EXISTS vital_signs (
    id                  BIGSERIAL       PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    systolic_pressure   INT,
    diastolic_pressure  INT,
    heart_rate          INT,
    weight              DECIMAL(5,2),
    height              DECIMAL(5,2),
    bmi                 DECIMAL(4,2),
    temperature         DECIMAL(4,2),
    respiratory_rate    INT,
    oxygen_saturation   INT,
    blood_sugar         DECIMAL(5,2),
    notes               TEXT,
    recorded_by         BIGINT,
    record_date         TIMESTAMP       NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_vital_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_vital_recorded_by 
        FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_vital_patient_id ON vital_signs(patient_id);
CREATE INDEX IF NOT EXISTS idx_vital_record_date ON vital_signs(record_date);
CREATE INDEX IF NOT EXISTS idx_vital_patient_date ON vital_signs(patient_id, record_date);

COMMENT ON TABLE vital_signs IS 'Bang luu chi so sinh ton cua benh nhan';
COMMENT ON COLUMN vital_signs.systolic_pressure IS 'Huyet ap tam thu (mmHg)';
COMMENT ON COLUMN vital_signs.diastolic_pressure IS 'Huyet ap tam truong (mmHg)';
COMMENT ON COLUMN vital_signs.heart_rate IS 'Nhip tim (bpm)';
COMMENT ON COLUMN vital_signs.weight IS 'Can nang (kg)';
COMMENT ON COLUMN vital_signs.height IS 'Chieu cao (cm)';
COMMENT ON COLUMN vital_signs.bmi IS 'Chi so BMI';
COMMENT ON COLUMN vital_signs.temperature IS 'Nhiet do (C)';
COMMENT ON COLUMN vital_signs.respiratory_rate IS 'Nhip tho (breaths/min)';
COMMENT ON COLUMN vital_signs.oxygen_saturation IS 'Do bao hoa oxy (%)';
COMMENT ON COLUMN vital_signs.blood_sugar IS 'Duong huyet (mg/dL hoac mmol/L)';
COMMENT ON COLUMN vital_signs.notes IS 'Ghi chu';
COMMENT ON COLUMN vital_signs.recorded_by IS 'Bac si hoac y ta ghi nhan';
COMMENT ON COLUMN vital_signs.record_date IS 'Thoi gian ghi nhan';

-- Bang FAMILY_MEDICAL_HISTORY - Tien su benh gia dinh
CREATE TABLE IF NOT EXISTS family_medical_history (
    id                  BIGSERIAL           PRIMARY KEY,
    patient_id          BIGINT              NOT NULL,
    created_by          BIGINT,
    relationship        family_relationship NOT NULL,
    condition           VARCHAR(100)        NOT NULL,
    age_at_diagnosis    INT,
    member_status       member_status,
    notes               TEXT,
    created_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_family_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_family_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_family_patient ON family_medical_history(patient_id);
CREATE INDEX IF NOT EXISTS idx_family_relationship ON family_medical_history(relationship);

COMMENT ON TABLE family_medical_history IS 'Bang luu tien su benh gia dinh cua benh nhan';
COMMENT ON COLUMN family_medical_history.created_by IS 'Nguoi tao ban ghi';
COMMENT ON COLUMN family_medical_history.relationship IS 'Quan he voi benh nhan';
COMMENT ON COLUMN family_medical_history.condition IS 'Ten benh/tinh trang';
COMMENT ON COLUMN family_medical_history.age_at_diagnosis IS 'Tuoi khi phat hien benh';
COMMENT ON COLUMN family_medical_history.member_status IS 'Trang thai thanh vien';
COMMENT ON COLUMN family_medical_history.notes IS 'Ghi chu them';

-- Bang MEDICAL_REPORTS - Bao cao y te
CREATE TABLE IF NOT EXISTS medical_reports (
    id                  BIGSERIAL       PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    created_by          BIGINT,
    report_type         report_type     NOT NULL,
    report_date         DATE            NOT NULL,
    title               VARCHAR(255)    NOT NULL,
    content             TEXT,
    notes               TEXT,
    file_url            VARCHAR(500),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_report_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_report_patient ON medical_reports(patient_id);
CREATE INDEX IF NOT EXISTS idx_report_date ON medical_reports(report_date);
CREATE INDEX IF NOT EXISTS idx_report_type ON medical_reports(report_type);

COMMENT ON TABLE medical_reports IS 'Bang luu bao cao y te cua benh nhan';
COMMENT ON COLUMN medical_reports.created_by IS 'Nguoi tao bao cao';
COMMENT ON COLUMN medical_reports.report_type IS 'Loai bao cao';
COMMENT ON COLUMN medical_reports.report_date IS 'Ngay bao cao';
COMMENT ON COLUMN medical_reports.title IS 'Tieu de bao cao';
COMMENT ON COLUMN medical_reports.content IS 'Noi dung bao cao';
COMMENT ON COLUMN medical_reports.notes IS 'Ghi chu';
COMMENT ON COLUMN medical_reports.file_url IS 'URL file dinh kem';

-- Bang HEALTH_FORECASTS - Du bao suc khoe
CREATE TABLE IF NOT EXISTS health_forecasts (
    id                      BIGSERIAL       PRIMARY KEY,
    patient_id              BIGINT          NOT NULL,
    forecast_date           DATE            NOT NULL,
    risk_scores             JSONB,
    predictions             JSONB,
    risk_factors            JSONB,
    recommendations         TEXT,
    vital_signs_snapshot    JSONB,
    created_by              BIGINT,
    status                  forecast_status NOT NULL DEFAULT 'ACTIVE',
    notes                   TEXT,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP,
    
    CONSTRAINT fk_forecast_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_forecast_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_forecast_patient_id ON health_forecasts(patient_id);
CREATE INDEX IF NOT EXISTS idx_forecast_date ON health_forecasts(forecast_date);
CREATE INDEX IF NOT EXISTS idx_forecast_patient_date ON health_forecasts(patient_id, forecast_date);

COMMENT ON TABLE health_forecasts IS 'Bang luu du bao suc khoe cua benh nhan';
COMMENT ON COLUMN health_forecasts.forecast_date IS 'Ngay du bao';
COMMENT ON COLUMN health_forecasts.risk_scores IS 'Diem rui ro cac benh ly (cardiovascular, diabetes, hypertension, stroke)';
COMMENT ON COLUMN health_forecasts.predictions IS 'Du doan xu huong cac chi so suc khoe';
COMMENT ON COLUMN health_forecasts.risk_factors IS 'Cac yeu to nguy co da phan tich';
COMMENT ON COLUMN health_forecasts.recommendations IS 'Khuyen nghi phong ngua va dieu tri';
COMMENT ON COLUMN health_forecasts.vital_signs_snapshot IS 'Anh chup cac chi so sinh ton tai thoi diem du bao';
COMMENT ON COLUMN health_forecasts.created_by IS 'Bac si tao du bao';
COMMENT ON COLUMN health_forecasts.status IS 'Trang thai du bao';
COMMENT ON COLUMN health_forecasts.notes IS 'Ghi chu';

-- ============================================
-- 4. AI CALLBOT & CHIEN DICH GOI DIEN
-- ============================================

-- Bang SURVEY_TEMPLATES - Mau khao sat/kich ban cho AI
CREATE TABLE IF NOT EXISTS survey_templates (
    id                  BIGSERIAL       PRIMARY KEY,
    template_name       VARCHAR(100)    NOT NULL,
    description         TEXT,
    questions_json      JSONB,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_survey_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

COMMENT ON TABLE survey_templates IS 'Bang luu mau khao sat/kich ban cho AI Callbot';
COMMENT ON COLUMN survey_templates.questions_json IS 'Danh sach cau hoi dang JSON';

-- Bang CALL_CAMPAIGNS - Chien dich goi dien
CREATE TABLE IF NOT EXISTS call_campaigns (
    id                  BIGSERIAL       PRIMARY KEY,
    campaign_name       VARCHAR(100)    NOT NULL,
    campaign_type       campaign_type   NOT NULL,
    target_audience     target_audience,
    script_template     TEXT,
    survey_template_id  BIGINT,
    start_date          DATE,
    end_date            DATE,
    status              campaign_status DEFAULT 'DRAFT',
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_campaign_survey 
        FOREIGN KEY (survey_template_id) REFERENCES survey_templates(id) ON DELETE SET NULL,
    CONSTRAINT fk_campaign_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_campaign_status ON call_campaigns(status);

COMMENT ON TABLE call_campaigns IS 'Bang luu chien dich goi dien tu dong';
COMMENT ON COLUMN call_campaigns.script_template IS 'Kich ban cho bot';

-- Bang CALL_LOGS - Lich su cuoc goi (AI Bot goi dien thoai)
CREATE TABLE IF NOT EXISTS call_logs (
    id                  BIGSERIAL       PRIMARY KEY,
    campaign_id         BIGINT,
    patient_id          BIGINT,
    phone_number        VARCHAR(20)     NOT NULL,
    call_type           call_type       NOT NULL,
    call_status         call_status     NOT NULL,
    start_time          TIMESTAMP,
    end_time            TIMESTAMP,
    duration            INT,
    recording_url       VARCHAR(500),
    transcript_text     TEXT,
    ai_confidence_score DOUBLE PRECISION,
    is_escalated        BOOLEAN         DEFAULT FALSE,
    escalation_reason   VARCHAR(255),
    handled_by          BIGINT,
    survey_responses    JSONB,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_call_campaign 
        FOREIGN KEY (campaign_id) REFERENCES call_campaigns(id) ON DELETE SET NULL,
    CONSTRAINT fk_call_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_call_handled_by 
        FOREIGN KEY (handled_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_call_patient ON call_logs(patient_id);
CREATE INDEX IF NOT EXISTS idx_call_start_time ON call_logs(start_time);
CREATE INDEX IF NOT EXISTS idx_call_status ON call_logs(call_status);

COMMENT ON TABLE call_logs IS 'Bang luu lich su cuoc goi AI Callbot qua dien thoai';
COMMENT ON COLUMN call_logs.duration IS 'Thoi luong (giay)';
COMMENT ON COLUMN call_logs.transcript_text IS 'Noi dung cuoc goi dang text';
COMMENT ON COLUMN call_logs.is_escalated IS 'Da chuyen cho nguoi that';
COMMENT ON COLUMN call_logs.handled_by IS 'Le tan/Bac si tiep nhan';
COMMENT ON COLUMN call_logs.survey_responses IS 'Cau tra loi khao sat dang JSON';

-- Bang WEB_CALL_LOGS - Lich su cuoc goi Web-to-Web
CREATE TABLE IF NOT EXISTS web_call_logs (
    id                      BIGSERIAL       PRIMARY KEY,
    stringee_call_id        VARCHAR(100),
    caller_id               BIGINT          NOT NULL,
    receiver_id             BIGINT          NOT NULL,
    call_status             web_call_status NOT NULL DEFAULT 'INITIATED',
    start_time              TIMESTAMP,
    end_time                TIMESTAMP,
    duration                INT,
    recording_s3_key        VARCHAR(500),
    recording_url           VARCHAR(1000),
    recording_url_expiry    TIMESTAMP,
    transcript_text         TEXT,
    rating                  INT,
    notes                   VARCHAR(500),
    has_recording           BOOLEAN         DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_webcall_caller 
        FOREIGN KEY (caller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_webcall_receiver 
        FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_webcall_caller ON web_call_logs(caller_id);
CREATE INDEX IF NOT EXISTS idx_webcall_receiver ON web_call_logs(receiver_id);
CREATE INDEX IF NOT EXISTS idx_webcall_start_time ON web_call_logs(start_time);
CREATE INDEX IF NOT EXISTS idx_webcall_status ON web_call_logs(call_status);

COMMENT ON TABLE web_call_logs IS 'Bang luu lich su cuoc goi Web-to-Web giua 2 user qua trinh duyet';
COMMENT ON COLUMN web_call_logs.stringee_call_id IS 'Stringee Call ID de tracking';
COMMENT ON COLUMN web_call_logs.caller_id IS 'Nguoi goi';
COMMENT ON COLUMN web_call_logs.receiver_id IS 'Nguoi nhan cuoc goi';
COMMENT ON COLUMN web_call_logs.call_status IS 'Trang thai cuoc goi';
COMMENT ON COLUMN web_call_logs.start_time IS 'Thoi gian bat dau goi';
COMMENT ON COLUMN web_call_logs.end_time IS 'Thoi gian ket thuc';
COMMENT ON COLUMN web_call_logs.duration IS 'Thoi luong cuoc goi (giay)';
COMMENT ON COLUMN web_call_logs.recording_s3_key IS 'S3 Key cua file ghi am';
COMMENT ON COLUMN web_call_logs.recording_url IS 'URL pre-signed de nghe lai (tam thoi, het han 7 ngay)';
COMMENT ON COLUMN web_call_logs.recording_url_expiry IS 'Thoi gian het han cua recording URL';
COMMENT ON COLUMN web_call_logs.transcript_text IS 'Transcript text (neu co AI phan tich)';
COMMENT ON COLUMN web_call_logs.rating IS 'Danh gia chat luong cuoc goi (1-5 sao)';
COMMENT ON COLUMN web_call_logs.notes IS 'Ghi chu cua nguoi dung';
COMMENT ON COLUMN web_call_logs.has_recording IS 'Cuoc goi co ghi am khong';

-- ============================================
-- 5. QUAN LY TICKET (YEU CAU HO TRO)
-- ============================================

-- Bang TICKETS - Yeu cau ho tro
CREATE TABLE IF NOT EXISTS tickets (
    id                  BIGSERIAL       PRIMARY KEY,
    call_id             BIGINT          UNIQUE,
    patient_id          BIGINT          NOT NULL,
    title               VARCHAR(200)    NOT NULL,
    description         TEXT,
    priority            priority_type   DEFAULT 'MEDIUM',
    category            ticket_category,
    status              ticket_status   NOT NULL DEFAULT 'OPEN',
    created_by_id       BIGINT          NOT NULL,
    assigned_to_id      BIGINT,
    resolved_by_id      BIGINT,
    retry_count         INT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at         TIMESTAMP,
    
    CONSTRAINT fk_ticket_call 
        FOREIGN KEY (call_id) REFERENCES call_logs(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_created_by 
        FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_assigned_to 
        FOREIGN KEY (assigned_to_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_resolved_by 
        FOREIGN KEY (resolved_by_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_ticket_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_ticket_assigned ON tickets(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_ticket_patient ON tickets(patient_id);
CREATE INDEX IF NOT EXISTS idx_ticket_priority ON tickets(priority);

COMMENT ON TABLE tickets IS 'Bang luu yeu cau ho tro (khi AI khong giai quyet duoc)';
COMMENT ON COLUMN tickets.call_id IS 'Cuoc goi lien quan';
COMMENT ON COLUMN tickets.created_by_id IS 'Le tan tao ticket';
COMMENT ON COLUMN tickets.assigned_to_id IS 'Bac si duoc assign';
COMMENT ON COLUMN tickets.retry_count IS 'So lan thu goi lai benh nhan';

-- Bang TICKET_MESSAGES - Tin nhan trong ticket
CREATE TABLE IF NOT EXISTS ticket_messages (
    id                  BIGSERIAL       PRIMARY KEY,
    ticket_id           BIGINT          NOT NULL,
    sender_id           BIGINT          NOT NULL,
    message_text        TEXT            NOT NULL,
    message_type        message_type    DEFAULT 'TEXT',
    attachment_url      VARCHAR(500),
    is_internal_note    BOOLEAN         DEFAULT FALSE,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_msg_ticket 
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender 
        FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_msg_ticket ON ticket_messages(ticket_id);
CREATE INDEX IF NOT EXISTS idx_msg_created ON ticket_messages(created_at);

COMMENT ON TABLE ticket_messages IS 'Bang luu tin nhan trong ticket (hoi thoai benh nhan - bac si)';
COMMENT ON COLUMN ticket_messages.is_internal_note IS 'Ghi chu noi bo (bac si - le tan)';

-- ============================================
-- 6. QUAN LY DIEU TRI
-- ============================================

-- Bang PRESCRIPTIONS - Don thuoc
CREATE TABLE IF NOT EXISTS prescriptions (
    id                  BIGSERIAL           PRIMARY KEY,
    patient_id          BIGINT              NOT NULL,
    doctor_id           BIGINT              NOT NULL,
    prescription_date   DATE                NOT NULL,
    diagnosis           TEXT,
    notes               TEXT,
    status              prescription_status DEFAULT 'ACTIVE',
    created_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_prescription_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_doctor 
        FOREIGN KEY (doctor_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_prescription_patient ON prescriptions(patient_id);
CREATE INDEX IF NOT EXISTS idx_prescription_doctor ON prescriptions(doctor_id);
CREATE INDEX IF NOT EXISTS idx_prescription_date ON prescriptions(prescription_date);

COMMENT ON TABLE prescriptions IS 'Bang luu don thuoc';

-- Bang PRESCRIPTION_DETAILS - Chi tiet don thuoc
CREATE TABLE IF NOT EXISTS prescription_details (
    id                  BIGSERIAL       PRIMARY KEY,
    prescription_id     BIGINT          NOT NULL,
    medicine_name       VARCHAR(200)    NOT NULL,
    dosage              VARCHAR(100),
    frequency           VARCHAR(100),
    duration            VARCHAR(100),
    instructions        TEXT,
    quantity            INT,
    
    CONSTRAINT fk_detail_prescription 
        FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
);

COMMENT ON TABLE prescription_details IS 'Bang luu chi tiet don thuoc';
COMMENT ON COLUMN prescription_details.dosage IS 'Lieu luong (vd: 500mg)';
COMMENT ON COLUMN prescription_details.frequency IS 'Tan suat (vd: 3 lan/ngay)';
COMMENT ON COLUMN prescription_details.duration IS 'Thoi gian (vd: 7 ngay)';
COMMENT ON COLUMN prescription_details.instructions IS 'Huong dan su dung';

-- Bang TREATMENT_PLANS - Ke hoach dieu tri (duoc ho tro boi AI)
CREATE TABLE IF NOT EXISTS treatment_plans (
    id                  BIGSERIAL       PRIMARY KEY,
    patient_id          BIGINT          NOT NULL,
    doctor_id           BIGINT          NOT NULL,
    diagnosis           TEXT,
    treatment_goal      TEXT,
    start_date          DATE,
    expected_end_date   DATE,
    status              plan_status     DEFAULT 'DRAFT',
    ai_suggested        BOOLEAN         DEFAULT FALSE,
    ai_suggestion_data  JSONB,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_plan_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_doctor 
        FOREIGN KEY (doctor_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_plan_patient ON treatment_plans(patient_id);
CREATE INDEX IF NOT EXISTS idx_plan_doctor ON treatment_plans(doctor_id);
CREATE INDEX IF NOT EXISTS idx_plan_status ON treatment_plans(status);

COMMENT ON TABLE treatment_plans IS 'Bang luu ke hoach dieu tri (co ho tro AI)';
COMMENT ON COLUMN treatment_plans.ai_suggestion_data IS 'Luu goi y tu AI';

-- Bang TREATMENT_PLAN_ITEMS - Chi tiet ke hoach dieu tri
CREATE TABLE IF NOT EXISTS treatment_plan_items (
    id                  BIGSERIAL       PRIMARY KEY,
    plan_id             BIGINT          NOT NULL,
    item_type           item_type       NOT NULL,
    description         TEXT            NOT NULL,
    frequency           VARCHAR(100),
    duration            VARCHAR(100),
    notes               TEXT,
    status              item_status     DEFAULT 'PENDING',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_item_plan 
        FOREIGN KEY (plan_id) REFERENCES treatment_plans(id) ON DELETE CASCADE
);

COMMENT ON TABLE treatment_plan_items IS 'Bang luu chi tiet ke hoach dieu tri';
COMMENT ON COLUMN treatment_plan_items.frequency IS 'Tan suat (vd: 2 lan/ngay)';
COMMENT ON COLUMN treatment_plan_items.duration IS 'Thoi gian (vd: 7 ngay)';

-- Bang CHECKUP_SCHEDULES - Lich tai kham dinh ky
CREATE TABLE IF NOT EXISTS checkup_schedules (
    id                      BIGSERIAL       PRIMARY KEY,
    treatment_plan_id       BIGINT          NOT NULL,
    patient_id              BIGINT          NOT NULL,
    doctor_id               BIGINT          NOT NULL,
    scheduled_date          DATE            NOT NULL,
    checkup_type            VARCHAR(50),
    status                  checkup_status  NOT NULL DEFAULT 'SCHEDULED',
    notes                   TEXT,
    completed_date          DATE,
    result_summary          TEXT,
    next_checkup_suggestion DATE,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_checkup_treatment_plan 
        FOREIGN KEY (treatment_plan_id) REFERENCES treatment_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkup_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkup_doctor 
        FOREIGN KEY (doctor_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_checkup_treatment_plan ON checkup_schedules(treatment_plan_id);
CREATE INDEX IF NOT EXISTS idx_checkup_patient ON checkup_schedules(patient_id);
CREATE INDEX IF NOT EXISTS idx_checkup_doctor ON checkup_schedules(doctor_id);
CREATE INDEX IF NOT EXISTS idx_checkup_status ON checkup_schedules(status);
CREATE INDEX IF NOT EXISTS idx_checkup_date ON checkup_schedules(scheduled_date);

COMMENT ON TABLE checkup_schedules IS 'Bang luu lich tai kham dinh ky trong ke hoach dieu tri';
COMMENT ON COLUMN checkup_schedules.treatment_plan_id IS 'Ke hoach dieu tri lien quan';
COMMENT ON COLUMN checkup_schedules.scheduled_date IS 'Ngay tai kham du kien';
COMMENT ON COLUMN checkup_schedules.checkup_type IS 'Loai tai kham: routine, follow_up, emergency';
COMMENT ON COLUMN checkup_schedules.status IS 'Trang thai lich tai kham';
COMMENT ON COLUMN checkup_schedules.notes IS 'Ghi chu truoc khi tai kham';
COMMENT ON COLUMN checkup_schedules.completed_date IS 'Ngay hoan thanh kham thuc te';
COMMENT ON COLUMN checkup_schedules.result_summary IS 'Tom tat ket qua sau khi kham';
COMMENT ON COLUMN checkup_schedules.next_checkup_suggestion IS 'Goi y ngay tai kham tiep theo';

-- ============================================
-- 7. HE THONG KIEN THUC (KNOWLEDGE BASE)
-- ============================================

-- Bang KNOWLEDGE_CATEGORIES - Danh muc kien thuc
CREATE TABLE IF NOT EXISTS knowledge_categories (
    id                  BIGSERIAL       PRIMARY KEY,
    name                VARCHAR(200)    NOT NULL,
    description         TEXT,
    parent_id           BIGINT,
    display_order       INT,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_category_parent 
        FOREIGN KEY (parent_id) REFERENCES knowledge_categories(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_category_parent ON knowledge_categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_category_active ON knowledge_categories(active);

COMMENT ON TABLE knowledge_categories IS 'Bang luu danh muc kien thuc (ho tro phan cap)';
COMMENT ON COLUMN knowledge_categories.name IS 'Ten danh muc';
COMMENT ON COLUMN knowledge_categories.description IS 'Mo ta danh muc';
COMMENT ON COLUMN knowledge_categories.parent_id IS 'Danh muc cha (null neu la root)';
COMMENT ON COLUMN knowledge_categories.display_order IS 'Thu tu hien thi';
COMMENT ON COLUMN knowledge_categories.active IS 'Trang thai kich hoat';

-- Bang KNOWLEDGE_ARTICLES - Bai viet kien thuc
CREATE TABLE IF NOT EXISTS knowledge_articles (
    id                  BIGSERIAL       PRIMARY KEY,
    title               VARCHAR(500)    NOT NULL,
    summary             TEXT,
    content             TEXT            NOT NULL,
    category_id         BIGINT,
    tags                VARCHAR(1000),
    created_by          BIGINT          NOT NULL,
    updated_by          BIGINT,
    status              article_status  NOT NULL DEFAULT 'DRAFT',
    views               INT             NOT NULL DEFAULT 0,
    featured            BOOLEAN         DEFAULT FALSE,
    published_at        TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_article_category 
        FOREIGN KEY (category_id) REFERENCES knowledge_categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_article_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_article_updated_by 
        FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_article_category ON knowledge_articles(category_id);
CREATE INDEX IF NOT EXISTS idx_article_status ON knowledge_articles(status);
CREATE INDEX IF NOT EXISTS idx_article_created_by ON knowledge_articles(created_by);
CREATE INDEX IF NOT EXISTS idx_article_created_at ON knowledge_articles(created_at);

COMMENT ON TABLE knowledge_articles IS 'Bang luu bai viet kien thuc y te';
COMMENT ON COLUMN knowledge_articles.title IS 'Tieu de bai viet';
COMMENT ON COLUMN knowledge_articles.summary IS 'Tom tat noi dung';
COMMENT ON COLUMN knowledge_articles.content IS 'Noi dung bai viet';
COMMENT ON COLUMN knowledge_articles.tags IS 'Cac tag (phan cach boi dau phay)';
COMMENT ON COLUMN knowledge_articles.created_by IS 'Nguoi tao bai viet';
COMMENT ON COLUMN knowledge_articles.updated_by IS 'Nguoi cap nhat cuoi';
COMMENT ON COLUMN knowledge_articles.status IS 'Trang thai bai viet';
COMMENT ON COLUMN knowledge_articles.views IS 'So luot xem';
COMMENT ON COLUMN knowledge_articles.featured IS 'Bai viet noi bat';
COMMENT ON COLUMN knowledge_articles.published_at IS 'Thoi gian xuat ban';

-- ============================================
-- 8. QUAN LY KHAO SAT (SURVEY MANAGEMENT)
-- ============================================

-- Bang SURVEYS - Quan ly cac form khao sat (Google Form, Typeform, etc.)
CREATE TABLE IF NOT EXISTS surveys (
    id                  BIGSERIAL       PRIMARY KEY,
    title               VARCHAR(200)    NOT NULL,
    description         TEXT,
    form_url            VARCHAR(500)    NOT NULL,
    icon_type           VARCHAR(50)     DEFAULT 'survey',
    icon_color          VARCHAR(50)     DEFAULT '#3B82F6',
    tag                 VARCHAR(50)     DEFAULT 'Khao sat',
    tag_color           VARCHAR(50)     DEFAULT '#3B82F6',
    display_order       INT             DEFAULT 0,
    response_count      INT             DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    show_on_landing     BOOLEAN         DEFAULT TRUE,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_survey_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_surveys_active ON surveys(is_active);
CREATE INDEX IF NOT EXISTS idx_surveys_landing ON surveys(show_on_landing, is_active);
CREATE INDEX IF NOT EXISTS idx_surveys_order ON surveys(display_order);

COMMENT ON TABLE surveys IS 'Bang luu cac form khao sat (Google Form, Typeform) de hien thi tren landing page';
COMMENT ON COLUMN surveys.title IS 'Tieu de khao sat';
COMMENT ON COLUMN surveys.description IS 'Mo ta khao sat';
COMMENT ON COLUMN surveys.form_url IS 'Duong dan form (Google Form, Typeform...)';
COMMENT ON COLUMN surveys.icon_type IS 'Loai icon: survey, health, feedback, register';
COMMENT ON COLUMN surveys.icon_color IS 'Ma mau icon';
COMMENT ON COLUMN surveys.tag IS 'Nhan hien thi';
COMMENT ON COLUMN surveys.tag_color IS 'Ma mau nhan';
COMMENT ON COLUMN surveys.display_order IS 'Thu tu hien thi';
COMMENT ON COLUMN surveys.response_count IS 'So luong phan hoi';
COMMENT ON COLUMN surveys.is_active IS 'Trang thai hoat dong';
COMMENT ON COLUMN surveys.show_on_landing IS 'Hien thi tren trang chu';
COMMENT ON COLUMN surveys.created_by IS 'Nguoi tao khao sat';

-- ============================================
-- 9. THONG BAO & PHAN HOI
-- ============================================

-- Bang NOTIFICATIONS - Thong bao cho nguoi dung
CREATE TABLE IF NOT EXISTS notifications (
    id                  BIGSERIAL           PRIMARY KEY,
    user_id             BIGINT              NOT NULL,
    notification_type   notification_type   NOT NULL,
    title               VARCHAR(200)        NOT NULL,
    content             TEXT,
    reference_id        BIGINT,
    reference_type      VARCHAR(30),
    is_read             BOOLEAN             DEFAULT FALSE,
    created_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at             TIMESTAMP,
    
    CONSTRAINT fk_notification_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notification_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notification_created ON notifications(created_at);

COMMENT ON TABLE notifications IS 'Bang luu thong bao cho nguoi dung';
COMMENT ON COLUMN notifications.reference_id IS 'ID cua ticket, call, reminder...';
COMMENT ON COLUMN notifications.reference_type IS 'TICKET, CALL, REMINDER...';

-- Bang FEEDBACKS - Phan hoi tu nguoi dung
CREATE TABLE IF NOT EXISTS feedbacks (
    id                  BIGSERIAL       PRIMARY KEY,
    call_id             BIGINT,
    ticket_id           BIGINT,
    user_id             BIGINT          NOT NULL,
    rating              INT             NOT NULL CHECK (rating BETWEEN 1 AND 5),
    feedback_text       TEXT,
    feedback_type       feedback_type   NOT NULL,
    is_reviewed         BOOLEAN         DEFAULT FALSE,
    reviewed_by         BIGINT,
    reviewed_at         TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_feedback_call 
        FOREIGN KEY (call_id) REFERENCES call_logs(id) ON DELETE SET NULL,
    CONSTRAINT fk_feedback_ticket 
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE SET NULL,
    CONSTRAINT fk_feedback_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_reviewed_by 
        FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_feedback_user ON feedbacks(user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_type ON feedbacks(feedback_type);
CREATE INDEX IF NOT EXISTS idx_feedback_rating ON feedbacks(rating);

COMMENT ON TABLE feedbacks IS 'Bang luu phan hoi tu nguoi dung';

-- ============================================
-- 10. TRIGGER CHO updated_at
-- ============================================

-- Function de tu dong cap nhat updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Tao triggers cho cac bang co updated_at
CREATE OR REPLACE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_user_info_updated_at BEFORE UPDATE ON user_info
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_survey_templates_updated_at BEFORE UPDATE ON survey_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_call_campaigns_updated_at BEFORE UPDATE ON call_campaigns
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_tickets_updated_at BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_prescriptions_updated_at BEFORE UPDATE ON prescriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_treatment_plans_updated_at BEFORE UPDATE ON treatment_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_checkup_schedules_updated_at BEFORE UPDATE ON checkup_schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_knowledge_categories_updated_at BEFORE UPDATE ON knowledge_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_knowledge_articles_updated_at BEFORE UPDATE ON knowledge_articles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_surveys_updated_at BEFORE UPDATE ON surveys
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 11. DU LIEU MAU (SAMPLE DATA)
-- ============================================

-- Tao tai khoan Admin mac dinh
-- Mat khau: Admin@123 (da ma hoa bang BCrypt)
INSERT INTO users (email, password_hash, role, is_active, email_verified) 
VALUES ('admin@clinic.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', TRUE, TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_info (user_id, full_name) 
SELECT id, 'Quan tri vien he thong' 
FROM users 
WHERE email = 'admin@clinic.com'
ON CONFLICT (user_id) DO NOTHING;

-- ============================================
-- 12. VIEWS HUU ICH
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
-- 13. STORED FUNCTIONS (thay the PROCEDURES)
-- ============================================

-- Function lay thong ke tong quan he thong
CREATE OR REPLACE FUNCTION fn_get_dashboard_stats()
RETURNS TABLE (
    total_patients BIGINT,
    total_doctors BIGINT,
    total_receptionists BIGINT,
    open_tickets BIGINT,
    today_calls BIGINT,
    today_escalated BIGINT,
    today_web_calls BIGINT,
    today_vital_records BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM users WHERE role = 'PATIENT')::BIGINT as total_patients,
        (SELECT COUNT(*) FROM users WHERE role = 'DOCTOR')::BIGINT as total_doctors,
        (SELECT COUNT(*) FROM users WHERE role = 'RECEPTIONIST')::BIGINT as total_receptionists,
        (SELECT COUNT(*) FROM tickets WHERE status NOT IN ('CLOSED', 'RESOLVED'))::BIGINT as open_tickets,
        (SELECT COUNT(*) FROM call_logs WHERE DATE(start_time) = CURRENT_DATE)::BIGINT as today_calls,
        (SELECT COUNT(*) FROM call_logs WHERE is_escalated = TRUE AND DATE(start_time) = CURRENT_DATE)::BIGINT as today_escalated,
        (SELECT COUNT(*) FROM web_call_logs WHERE DATE(start_time) = CURRENT_DATE)::BIGINT as today_web_calls,
        (SELECT COUNT(*) FROM vital_signs WHERE DATE(record_date) = CURRENT_DATE)::BIGINT as today_vital_records;
END;
$$ LANGUAGE plpgsql;

-- Function tim kiem benh nhan
CREATE OR REPLACE FUNCTION fn_search_patients(search_term VARCHAR(100))
RETURNS TABLE (
    id BIGINT,
    email VARCHAR(100),
    phone VARCHAR(20),
    full_name VARCHAR(100),
    date_of_birth DATE,
    gender gender_type,
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
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
      AND (ui.full_name ILIKE '%' || search_term || '%'
           OR u.email ILIKE '%' || search_term || '%'
           OR u.phone ILIKE '%' || search_term || '%');
END;
$$ LANGUAGE plpgsql;

-- Function lay vital signs theo benh nhan va khoang thoi gian
CREATE OR REPLACE FUNCTION fn_get_vital_signs_history(
    p_patient_id BIGINT,
    p_start_date TIMESTAMP,
    p_end_date TIMESTAMP
)
RETURNS TABLE (
    id BIGINT,
    patient_id BIGINT,
    systolic_pressure INT,
    diastolic_pressure INT,
    heart_rate INT,
    weight DECIMAL(5,2),
    height DECIMAL(5,2),
    bmi DECIMAL(4,2),
    temperature DECIMAL(4,2),
    respiratory_rate INT,
    oxygen_saturation INT,
    blood_sugar DECIMAL(5,2),
    notes TEXT,
    recorded_by BIGINT,
    record_date TIMESTAMP,
    created_at TIMESTAMP,
    recorded_by_email VARCHAR(100),
    recorded_by_name VARCHAR(100)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        vs.id,
        vs.patient_id,
        vs.systolic_pressure,
        vs.diastolic_pressure,
        vs.heart_rate,
        vs.weight,
        vs.height,
        vs.bmi,
        vs.temperature,
        vs.respiratory_rate,
        vs.oxygen_saturation,
        vs.blood_sugar,
        vs.notes,
        vs.recorded_by,
        vs.record_date,
        vs.created_at,
        recorder.email as recorded_by_email,
        recorder_info.full_name as recorded_by_name
    FROM vital_signs vs
    LEFT JOIN users recorder ON vs.recorded_by = recorder.id
    LEFT JOIN user_info recorder_info ON recorder.id = recorder_info.user_id
    WHERE vs.patient_id = p_patient_id
      AND vs.record_date BETWEEN p_start_date AND p_end_date
    ORDER BY vs.record_date DESC;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- THONG TIN SCHEMA
-- ============================================
-- Tong so bang: 23 (theo Entity classes hien tai)
-- 1. users                  - Thong tin tai khoan (User.java)
-- 2. user_info              - Thong tin ca nhan (UserInfo.java)
-- 3. patient_documents      - Tai lieu benh nhan (PatientDocument.java)
-- 4. vital_signs            - Chi so sinh ton (VitalSigns.java)
-- 5. family_medical_history - Tien su benh gia dinh (FamilyMedicalHistory.java)
-- 6. medical_reports        - Bao cao y te (MedicalReport.java)
-- 7. health_forecasts       - Du bao suc khoe (HealthForecast.java)
-- 8. survey_templates       - Mau khao sat AI Callbot (SurveyTemplate.java)
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
-- 21. surveys               - Khao sat form (Survey.java) - Google Form, Typeform
-- 22. notifications         - Thong bao (Notification.java)
-- 23. feedbacks             - Phan hoi (Feedback.java)
-- ============================================