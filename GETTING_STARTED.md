# 🚀 Getting Started - ISSVSG Medical System

> Hướng dẫn chi tiết từ A-Z để setup và sử dụng Healthcare Management System

---

## 📋 Yêu cầu hệ thống

### 1. Bắt buộc
- **Java 21** (JDK 21 trở lên)
- **Maven 3.8+** (hoặc sử dụng `./mvnw` wrapper có sẵn)
- **MySQL 8.0+**
- **Docker** (cho AI Voice services)

### 2. Tùy chọn (cho production)
- **AWS Account** (S3 bucket cho lưu trữ file)
- **Stringee Account** (VoIP calling)
- **NVIDIA GPU** (cho ASR service nhanh hơn)

### 3. IDE khuyến nghị
- **IntelliJ IDEA** (Ultimate hoặc Community)
- **VS Code** với Java Extension Pack

---

## ⚙️ Bước 1: Clone & Setup Database

### 1.1. Clone repository
```bash
git clone <repository-url>
cd capstone-project
```

### 1.2. Cài đặt MySQL
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# macOS
brew install mysql

# Start MySQL service
sudo systemctl start mysql  # Linux
brew services start mysql   # macOS
```

### 1.3. Tạo database
```bash
mysql -u root -p
```

```sql
CREATE DATABASE medical_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'medicaluser'@'localhost' IDENTIFIED BY 'YourStrongPassword123!';
GRANT ALL PRIVILEGES ON medical_system.* TO 'medicaluser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 1.4. Import schema
```bash
mysql -u medicaluser -p medical_system < docs/database_full_schema_postgresql.sql
```

---

## 🔧 Bước 2: Configuration

### 2.1. Tạo file config local
```bash
cd src/main/resources
cp application-local.properties.example application-local.properties
```

### 2.2. Cấu hình application-local.properties
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/medical_system?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=medicaluser
spring.datasource.password=YourStrongPassword123!

# AWS S3 (optional - comment out nếu chưa có)
aws.s3.bucket-name=your-bucket-name
aws.access.key.id=YOUR_AWS_ACCESS_KEY
aws.secret.access.key=YOUR_AWS_SECRET_KEY
aws.region=ap-southeast-1

# Stringee (optional - comment out nếu chưa có)
stringee.key.sid=YOUR_STRINGEE_SID
stringee.key.secret=YOUR_STRINGEE_SECRET

# JWT Secret (QUAN TRỌNG - đổi trong production)
jwt.secret.key=MySecretKeyForJWTTokenGeneration2024MustBe256BitsLong
jwt.expiration.ms=86400000

# ASR Service (sẽ setup ở bước 4)
asr.service.url=http://localhost:8001
asr.service.enabled=true
asr.service.default-language=vi
```

### 2.3. Set active profile
File `application.properties` đã có sẵn:
```properties
spring.profiles.active=local
```

---

## 🏗️ Bước 3: Build Spring Boot Application

### 3.1. Build không bao gồm Docker (nhanh)
```bash
# Sử dụng Maven wrapper (khuyến nghị)
./mvnw clean package -Ddocker.build.skip=true

# Hoặc dùng Maven đã cài
mvn clean package -Ddocker.build.skip=true
```

### 3.2. Build bao gồm Docker AI services (đầy đủ)
```bash
# GPU build
./mvnw clean package -Ddocker.build.type=gpu

# CPU build (nếu không có GPU)
./mvnw clean package -Ddocker.build.type=cpu
```

Sau khi build thành công, JAR file sẽ nằm ở:
```
target/capstone-project-0.0.1-SNAPSHOT.jar
```

---

## 🚀 Bước 4: Chạy ứng dụng

### 4.1. Chạy Spring Boot
```bash
# Option 1: Chạy từ JAR
java -jar target/capstone-project-0.0.1-SNAPSHOT.jar

# Option 2: Chạy với Maven (development)
./mvnw spring-boot:run

# Option 3: Chạy từ IDE (IntelliJ/VS Code)
# Run class: com.g4.capstoneproject.CapstoneProjectApplication
```

Ứng dụng sẽ chạy tại: **http://localhost:8080**

### 4.2. Chạy AI Voice Services (optional)

#### ASR Service (Speech-to-Text)
```bash
cd src/main/docker_ai_voice/asr-service

# GPU (nhanh hơn 4x, cần NVIDIA GPU)
docker compose up -d asr-gpu

# CPU only
docker compose up -d asr-cpu

# Kiểm tra
curl http://localhost:8001/health
```

Nếu không chạy ASR service, hệ thống sẽ fallback sang Gemini/OpenAI API.

---

## 👤 Bước 5: Tạo tài khoản và đăng nhập

### 5.1. Tạo admin account đầu tiên
```bash
# Import test accounts
mysql -u medicaluser -p medical_system < docs/insert_admin.sql
```

Hoặc tạo manual qua SQL:
```sql
INSERT INTO users (username, password, email, role, is_active, created_at)
VALUES ('admin', '$2a$10$...', 'admin@hospital.com', 'ADMIN', 1, NOW());
```

### 5.2. Đăng nhập
1. Truy cập: **http://localhost:8080/auth/login**
2. Tài khoản test (nếu đã import):
   - **Admin**: `admin` / `admin123`
   - **Doctor**: `doctor1` / `doctor123`
   - **Receptionist**: `receptionist1` / `recep123`
   - **Patient**: `patient1` / `patient123`

### 5.3. Đăng ký tài khoản mới
1. Truy cập: **http://localhost:8080/auth/register**
2. Điền form đăng ký
3. Xác nhận email (nếu đã config SMTP)

---

## 🎯 Bước 6: Các tính năng chính

### 6.1. Admin Dashboard
**URL:** http://localhost:8080/admin

**Chức năng:**
- ✅ Quản lý người dùng (users, doctors, patients)
- ✅ Quản lý tài khoản (accounts)
- ✅ Xem báo cáo hệ thống
- ✅ Cấu hình AI Callbot

### 6.2. Doctor Dashboard
**URL:** http://localhost:8080/doctor

**Chức năng:**
- ✅ Xem danh sách bệnh nhân
- ✅ Tạo/quản lý đơn thuốc (prescriptions)
- ✅ Tạo kế hoạch điều trị (treatment plans)
- ✅ Video call với bệnh nhân (Stringee)
- ✅ Xem lịch sử khám bệnh

### 6.3. Patient Portal
**URL:** http://localhost:8080/patient

**Chức năng:**
- ✅ Xem hồ sơ bệnh án
- ✅ Xem đơn thuốc
- ✅ Đặt lịch khám
- ✅ Tạo ticket hỗ trợ
- ✅ Làm khảo sát
- ✅ Upload tài liệu

### 6.4. Receptionist Dashboard
**URL:** http://localhost:8080/receptionist

**Chức năng:**
- ✅ Quản lý cuộc gọi AI (call campaigns)
- ✅ Xem call logs
- ✅ Quản lý tickets
- ✅ Tạo lịch hẹn

### 6.5. Web-to-Web Calling
**URL:** http://localhost:8080/call

**Chức năng:**
- ✅ Gọi điện WebRTC
- ✅ Ghi âm cuộc gọi (3 streams: caller, receiver, combined)
- ✅ Lưu vào S3
- ✅ Transcribe bằng ASR service

### 6.6. Knowledge Base
**URL:** http://localhost:8080/knowledge

**Chức năng:**
- ✅ Quản lý bài viết y khoa
- ✅ Phân loại theo category
- ✅ Tìm kiếm bài viết
- ✅ AI chatbot hỗ trợ

---

## 📚 Bước 7: API Documentation

### 7.1. Swagger UI
**URL:** http://localhost:8080/swagger-ui.html

Xem tất cả REST APIs với Swagger UI interactive documentation.

### 7.2. API Groups
- **Auth API** - Đăng nhập, đăng ký, JWT
- **Patient API** - Quản lý bệnh nhân
- **Doctor API** - Quản lý bác sĩ
- **Prescription API** - Đơn thuốc
- **Call API** - WebRTC calling
- **ASR API** - Speech-to-Text
- **Survey API** - Khảo sát
- **Ticket API** - Hỗ trợ CRM

### 7.3. Test API với curl
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Get token và test protected endpoint
TOKEN="your_jwt_token_here"
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🐛 Troubleshooting

### ❌ Lỗi: Port 8080 already in use
```bash
# Tìm process đang dùng port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Hoặc đổi port trong application.properties
server.port=8081
```

### ❌ Lỗi: Access denied for user
```sql
-- Kiểm tra user MySQL
SELECT user, host FROM mysql.user;

-- Reset password
ALTER USER 'medicaluser'@'localhost' IDENTIFIED BY 'NewPassword123!';
FLUSH PRIVILEGES;
```

### ❌ Lỗi: ASR service connection refused
```bash
# Kiểm tra Docker container
docker ps | grep asr

# Xem logs
docker logs asr-service

# Restart
cd src/main/docker_ai_voice/asr-service
docker compose restart
```

### ❌ Lỗi: S3 Access Denied
- Kiểm tra AWS credentials trong `application-local.properties`
- Kiểm tra IAM permissions (cần `s3:PutObject`, `s3:GetObject`)
- Kiểm tra bucket name và region

### ❌ Lỗi: Cannot load Thymeleaf templates
```bash
# Clean và rebuild
./mvnw clean
./mvnw package -Ddocker.build.skip=true
```

---

## 🔐 Security Notes

### Production Checklist
- [ ] Đổi `jwt.secret.key` thành random 256-bit key
- [ ] Đổi database password
- [ ] Enable HTTPS (SSL certificate)
- [ ] Configure CORS properly
- [ ] Enable rate limiting
- [ ] Setup firewall rules
- [ ] Backup database định kỳ
- [ ] Enable application logs monitoring

### Generate JWT Secret
```bash
openssl rand -base64 32
```

---

## 📊 Development Tools

### 1. Database GUI
- **DBeaver** (khuyến nghị): https://dbeaver.io
- **MySQL Workbench**: https://www.mysql.com/products/workbench/

### 2. API Testing
- **Postman**: Import Swagger JSON
- **Insomnia**: Alternative to Postman
- **curl**: Command-line testing

### 3. Docker Management
- **Docker Desktop**: GUI cho Docker
- **Portainer**: Web UI cho Docker

---

## 📖 Tài liệu chi tiết

| Document | Mô tả |
|----------|-------|
| [AI_PROJECT_CONTEXT.md](AI_PROJECT_CONTEXT.md) | Tổng quan kiến trúc, entities, services |
| [BACKEND_SETUP.md](docs/BACKEND_SETUP.md) | Cấu hình backend chi tiết |
| [DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) | Schema database |
| [SECURITY_GUIDE.md](docs/SECURITY_GUIDE.md) | Hướng dẫn bảo mật |
| [SWAGGER_GUIDE.md](docs/SWAGGER_GUIDE.md) | Sử dụng Swagger API |
| [STRINGEE_INTEGRATION.md](docs/STRINGEE_INTEGRATION.md) | Tích hợp VoIP calling |
| [WEB_CALL_USER_GUIDE.md](docs/WEB_CALL_USER_GUIDE.md) | Hướng dẫn gọi điện web |
| [docker_ai_voice/README.md](src/main/docker_ai_voice/README.md) | AI microservices |

---

## 🚀 Quick Start Commands

```bash
# Full setup from scratch
git clone <repo-url> && cd capstone-project
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
# Edit application-local.properties với thông tin database
./mvnw clean package -Ddocker.build.skip=true
java -jar target/capstone-project-0.0.1-SNAPSHOT.jar

# Visit: http://localhost:8080
```

---

## 💡 Tips & Best Practices

### Development
1. Sử dụng `spring.profiles.active=local` cho development
2. Enable hot reload với Spring DevTools
3. Sử dụng `./mvnw` thay vì `mvn` để đảm bảo version consistency
4. Commit thường xuyên, tách commits theo feature

### Testing
1. Test API qua Swagger UI trước khi code frontend
2. Sử dụng test accounts trong `docs/insert_test_users.sql`
3. Test trên nhiều browsers (Chrome, Firefox, Safari)

### Docker
1. Dùng GPU nếu có cho ASR service (nhanh hơn 4x)
2. Monitor Docker resource usage với `docker stats`
3. Clean unused images: `docker system prune -a`

### Database
1. Backup trước khi migrate: `mysqldump -u user -p db > backup.sql`
2. Use migrations trong `docs/migrations/`
3. Index các columns hay query

---

## 🤝 Support

Nếu gặp vấn đề:
1. Kiểm tra [Troubleshooting](#-troubleshooting)
2. Xem logs: `tail -f logs/application.log`
3. Kiểm tra Swagger API documentation
4. Tạo issue trên GitHub với:
   - Mô tả lỗi
   - Steps to reproduce
   - Logs/screenshots
   - Environment info (Java version, OS, etc.)

---

**Happy Coding! 🎉**
