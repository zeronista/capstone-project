-- V20260202__create_surveys_table.sql
-- Table for managing surveys (Google Form links, etc.)

CREATE TABLE IF NOT EXISTS surveys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    form_url VARCHAR(500) NOT NULL,
    icon_type VARCHAR(50) DEFAULT 'survey',
    icon_color VARCHAR(50) DEFAULT '#3B82F6',
    tag VARCHAR(50) DEFAULT 'Khảo sát',
    tag_color VARCHAR(50) DEFAULT '#3B82F6',
    display_order INT DEFAULT 0,
    response_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    show_on_landing BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_surveys_active (is_active),
    INDEX idx_surveys_landing (show_on_landing, is_active),
    INDEX idx_surveys_order (display_order)
);

-- Insert sample surveys
INSERT INTO surveys (title, description, form_url, icon_type, icon_color, tag, tag_color, display_order, response_count, is_active, show_on_landing) VALUES
('Khảo sát mức độ hài lòng dịch vụ', 'Giúp chúng tôi cải thiện chất lượng phục vụ bạn tốt hơn', 'https://forms.gle/abc123xyz', 'feedback', '#3B82F6', 'Hài lòng', '#3B82F6', 1, 156, TRUE, TRUE),
('Đăng ký tư vấn miễn phí', 'Để lại thông tin để được bác sĩ tư vấn trong 24h', 'https://forms.gle/def456uvw', 'register', '#8B5CF6', 'Đăng ký', '#8B5CF6', 2, 89, TRUE, TRUE),
('Khảo sát sức khỏe định kỳ', 'Đánh giá tình trạng sức khỏe của bạn trong 5 phút', 'https://forms.gle/ghi789rst', 'health', '#EF4444', 'Sức khỏe', '#EF4444', 3, 234, TRUE, TRUE),
('Góp ý cải thiện dịch vụ', 'Mọi ý kiến của bạn đều quý giá với chúng tôi', 'https://forms.gle/jkl012mno', 'survey', '#10B981', 'Góp ý', '#10B981', 4, 45, FALSE, TRUE);
