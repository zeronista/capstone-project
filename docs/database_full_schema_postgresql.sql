-- ============================================
-- CAPSTONE PROJECT - AI CALLBOT PHONG KHAM
-- FILE 1: SCHEMA (TAO BANG)
-- PostgreSQL 14+
-- ============================================
-- Ngay tao: 03/02/2026
-- Phien ban: 4.0 (Da loai bo: vital_signs, call_campaigns, checkup_schedules, surveys)
-- Nhom: G4
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

COMMENT ON TABLE user_info IS 'Bang luu thong tin ca nhan nguoi dung';

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
    file_size           BIGINT          CHECK (file_size >= 0),
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

-- Bang FAMILY_MEDICAL_HISTORY - Tien su benh gia dinh
CREATE TABLE IF NOT EXISTS family_medical_history (
    id                  BIGSERIAL           PRIMARY KEY,
    patient_id          BIGINT              NOT NULL,
    created_by          BIGINT,
    relationship        family_relationship NOT NULL,
    condition           VARCHAR(100)        NOT NULL,
    age_at_diagnosis    INT                 CHECK (age_at_diagnosis >= 0 AND age_at_diagnosis <= 150),
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
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_forecast_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_forecast_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_forecast_patient_id ON health_forecasts(patient_id);
CREATE INDEX IF NOT EXISTS idx_forecast_date ON health_forecasts(forecast_date);
CREATE INDEX IF NOT EXISTS idx_forecast_status ON health_forecasts(status);

COMMENT ON TABLE health_forecasts IS 'Bang luu du bao suc khoe cua benh nhan';

-- ============================================
-- 4. AI CALLBOT - LICH SU CUOC GOI
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

-- Bang CALL_LOGS - Lich su cuoc goi (AI Bot goi dien thoai)
CREATE TABLE IF NOT EXISTS call_logs (
    id                  BIGSERIAL       PRIMARY KEY,
    patient_id          BIGINT,
    phone_number        VARCHAR(20)     NOT NULL,
    call_type           call_type       NOT NULL,
    call_status         call_status     NOT NULL,
    start_time          TIMESTAMP,
    end_time            TIMESTAMP,
    duration            INT             CHECK (duration >= 0),
    recording_url       VARCHAR(500),
    transcript_text     TEXT,
    ai_confidence_score DOUBLE PRECISION CHECK (ai_confidence_score >= 0 AND ai_confidence_score <= 1),
    is_escalated        BOOLEAN         DEFAULT FALSE,
    escalation_reason   VARCHAR(255),
    handled_by          BIGINT,
    survey_responses    JSONB,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_call_patient 
        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_call_handled_by 
        FOREIGN KEY (handled_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_call_patient ON call_logs(patient_id);
CREATE INDEX IF NOT EXISTS idx_call_start_time ON call_logs(start_time);
CREATE INDEX IF NOT EXISTS idx_call_status ON call_logs(call_status);

COMMENT ON TABLE call_logs IS 'Bang luu lich su cuoc goi AI Callbot qua dien thoai';

-- Bang WEB_CALL_LOGS - Lich su cuoc goi Web-to-Web
CREATE TABLE IF NOT EXISTS web_call_logs (
    id                      BIGSERIAL       PRIMARY KEY,
    stringee_call_id        VARCHAR(100),
    caller_id               BIGINT          NOT NULL,
    receiver_id             BIGINT          NOT NULL,
    call_status             web_call_status NOT NULL DEFAULT 'INITIATED',
    start_time              TIMESTAMP,
    end_time                TIMESTAMP,
    duration                INT             CHECK (duration >= 0),
    recording_s3_key        VARCHAR(500),
    recording_url           VARCHAR(1000),
    recording_url_expiry    TIMESTAMP,
    transcript_text         TEXT,
    rating                  INT             CHECK (rating BETWEEN 1 AND 5),
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

COMMENT ON TABLE web_call_logs IS 'Bang luu lich su cuoc goi Web-to-Web giua 2 user qua trinh duyet';

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
    retry_count         INT             CHECK (retry_count >= 0),
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

-- Bang TICKET_MESSAGES - Tin nhan trong ticket
CREATE TABLE IF NOT EXISTS ticket_messages (
    id                  BIGSERIAL       PRIMARY KEY,
    ticket_id           BIGINT          NOT NULL,
    sender_id           BIGINT,
    message_text        TEXT            NOT NULL,
    message_type        message_type    DEFAULT 'TEXT',
    attachment_url      VARCHAR(500),
    is_internal_note    BOOLEAN         DEFAULT FALSE,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_msg_ticket 
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender 
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_msg_ticket ON ticket_messages(ticket_id);
CREATE INDEX IF NOT EXISTS idx_msg_created ON ticket_messages(created_at);

COMMENT ON TABLE ticket_messages IS 'Bang luu tin nhan trong ticket';

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
    quantity            INT             CHECK (quantity > 0),
    
    CONSTRAINT fk_detail_prescription 
        FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
);

-- Index cho prescription_details de tang hieu suat JOIN
CREATE INDEX IF NOT EXISTS idx_detail_prescription ON prescription_details(prescription_id);

COMMENT ON TABLE prescription_details IS 'Bang luu chi tiet don thuoc';

-- Bang TREATMENT_PLANS - Ke hoach dieu tri
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

-- Index cho treatment_plan_items de tang hieu suat JOIN
CREATE INDEX IF NOT EXISTS idx_item_plan ON treatment_plan_items(plan_id);

COMMENT ON TABLE treatment_plan_items IS 'Bang luu chi tiet ke hoach dieu tri';

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

COMMENT ON TABLE knowledge_categories IS 'Bang luu danh muc kien thuc';

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
    views               INT             NOT NULL DEFAULT 0 CHECK (views >= 0),
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

COMMENT ON TABLE knowledge_articles IS 'Bang luu bai viet kien thuc y te';

-- ============================================
-- 8. THONG BAO & PHAN HOI
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

COMMENT ON TABLE notifications IS 'Bang luu thong bao cho nguoi dung';

-- Bang FEEDBACKS - Phan hoi tu nguoi dung (ho tro Google Form)
CREATE TABLE IF NOT EXISTS feedbacks (
    id                  BIGSERIAL       PRIMARY KEY,
    title               VARCHAR(200)    NOT NULL,
    description         TEXT,
    form_url            VARCHAR(500)    NOT NULL,
    call_id             BIGINT,
    ticket_id           BIGINT,
    user_id             BIGINT          NOT NULL,
    rating              INT             CHECK (rating BETWEEN 1 AND 5),
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

COMMENT ON TABLE feedbacks IS 'Bang luu phan hoi tu nguoi dung (ho tro Google Form)';
COMMENT ON COLUMN feedbacks.title IS 'Tieu de form khao sat';
COMMENT ON COLUMN feedbacks.description IS 'Mo ta form khao sat';
COMMENT ON COLUMN feedbacks.form_url IS 'Duong dan Google Form';

-- ============================================
-- 9. TRIGGER CHO updated_at
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

CREATE OR REPLACE TRIGGER update_tickets_updated_at BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_prescriptions_updated_at BEFORE UPDATE ON prescriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_treatment_plans_updated_at BEFORE UPDATE ON treatment_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_knowledge_categories_updated_at BEFORE UPDATE ON knowledge_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_knowledge_articles_updated_at BEFORE UPDATE ON knowledge_articles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- DANH SACH BANG DA TAO (19 BANG)
-- ============================================
-- 1. users                  - Thong tin tai khoan
-- 2. user_info              - Thong tin ca nhan
-- 3. patient_documents      - Tai lieu benh nhan
-- 4. family_medical_history - Tien su benh gia dinh
-- 5. medical_reports        - Bao cao y te
-- 6. health_forecasts       - Du bao suc khoe
-- 7. survey_templates       - Mau khao sat AI Callbot
-- 8. call_logs              - Lich su cuoc goi AI Bot
-- 9. web_call_logs          - Lich su cuoc goi Web
-- 10. tickets               - Yeu cau ho tro
-- 11. ticket_messages       - Tin nhan ticket
-- 12. prescriptions         - Don thuoc
-- 13. prescription_details  - Chi tiet don thuoc
-- 14. treatment_plans       - Ke hoach dieu tri
-- 15. treatment_plan_items  - Chi tiet ke hoach
-- 16. knowledge_categories  - Danh muc kien thuc
-- 17. knowledge_articles    - Bai viet kien thuc
-- 18. notifications         - Thong bao
-- 19. feedbacks             - Phan hoi
-- ============================================
