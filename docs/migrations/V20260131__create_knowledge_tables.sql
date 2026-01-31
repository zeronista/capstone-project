-- Create medical_reports table
CREATE TABLE IF NOT EXISTS medical_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    created_by BIGINT,
    report_type VARCHAR(50) NOT NULL,
    report_date DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    notes TEXT,
    file_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create family_medical_history table
CREATE TABLE IF NOT EXISTS family_medical_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    created_by BIGINT,
    relationship VARCHAR(50) NOT NULL,
    `condition` VARCHAR(100) NOT NULL,
    age_at_diagnosis INT,
    member_status VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Add indexes for better query performance
CREATE INDEX idx_medical_reports_patient ON medical_reports(patient_id);
CREATE INDEX idx_medical_reports_date ON medical_reports(report_date);
CREATE INDEX idx_family_history_patient ON family_medical_history(patient_id);
