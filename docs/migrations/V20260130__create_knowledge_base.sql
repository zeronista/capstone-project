-- Migration for Knowledge Base Management (Phase 5)
-- Create tables for Knowledge Categories and Articles

-- Knowledge Categories Table
CREATE TABLE IF NOT EXISTS knowledge_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    display_order INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES knowledge_categories(id) ON DELETE CASCADE,
    INDEX idx_parent (parent_id),
    INDEX idx_active (active),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Knowledge Articles Table
CREATE TABLE IF NOT EXISTS knowledge_articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    summary TEXT,
    content LONGTEXT NOT NULL,
    category_id BIGINT,
    tags VARCHAR(1000),
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    views INT DEFAULT 0 NOT NULL,
    featured BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES knowledge_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at),
    INDEX idx_published_at (published_at),
    INDEX idx_featured (featured),
    
    -- Full-text search index for title and content
    FULLTEXT INDEX idx_fulltext_search (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default categories
INSERT INTO knowledge_categories (name, description, parent_id, display_order, active) VALUES
('Chẩn đoán bệnh', 'Hướng dẫn chẩn đoán các bệnh lý', NULL, 1, TRUE),
('Điều trị', 'Phương pháp điều trị và thuốc', NULL, 2, TRUE),
('Phòng ngừa', 'Biện pháp phòng ngừa và chăm sóc sức khỏe', NULL, 3, TRUE),
('Dinh dưỡng', 'Hướng dẫn dinh dưỡng cho bệnh nhân', NULL, 4, TRUE),
('Y học cơ bản', 'Kiến thức y học cơ bản và giải phẫu', NULL, 5, TRUE);

-- Insert subcategories
INSERT INTO knowledge_categories (name, description, parent_id, display_order, active) VALUES
('Bệnh tim mạch', 'Chẩn đoán bệnh tim mạch', 1, 1, TRUE),
('Bệnh tiểu đường', 'Chẩn đoán bệnh tiểu đường', 1, 2, TRUE),
('Bệnh hô hấp', 'Chẩn đoán bệnh hô hấp', 1, 3, TRUE),
('Kháng sinh', 'Sử dụng kháng sinh', 2, 1, TRUE),
('Thuốc tim mạch', 'Thuốc điều trị tim mạch', 2, 2, TRUE);

-- Insert sample articles
INSERT INTO knowledge_articles (title, summary, content, category_id, tags, created_by, status, featured, published_at) 
SELECT 
    'Hướng dẫn chẩn đoán tăng huyết áp',
    'Quy trình chẩn đoán và phân loại tăng huyết áp theo tiêu chuẩn mới nhất',
    '<h2>Định nghĩa</h2><p>Tăng huyết áp là tình trạng huyết áp tâm thu ≥140 mmHg hoặc huyết áp tâm trương ≥90 mmHg.</p><h2>Phân loại</h2><ul><li>Tăng huyết áp độ 1: 140-159/90-99 mmHg</li><li>Tăng huyết áp độ 2: 160-179/100-109 mmHg</li><li>Tăng huyết áp độ 3: ≥180/≥110 mmHg</li></ul>',
    6,
    'tăng huyết áp,tim mạch,chẩn đoán',
    (SELECT id FROM users WHERE role = 'DOCTOR' LIMIT 1),
    'PUBLISHED',
    TRUE,
    NOW()
WHERE EXISTS (SELECT 1 FROM users WHERE role = 'DOCTOR');

INSERT INTO knowledge_articles (title, summary, content, category_id, tags, created_by, status, published_at) 
SELECT 
    'Nguyên tắc sử dụng kháng sinh',
    'Hướng dẫn sử dụng kháng sinh an toàn và hiệu quả',
    '<h2>Nguyên tắc chung</h2><ol><li>Chỉ dùng khi có chỉ định rõ ràng</li><li>Chọn kháng sinh phù hợp với vi khuẩn gây bệnh</li><li>Liều lượng và thời gian điều trị đầy đủ</li><li>Theo dõi tác dụng phụ và kháng thuốc</li></ol>',
    9,
    'kháng sinh,điều trị,thuốc',
    (SELECT id FROM users WHERE role = 'DOCTOR' LIMIT 1),
    'PUBLISHED',
    NOW()
WHERE EXISTS (SELECT 1 FROM users WHERE role = 'DOCTOR');
