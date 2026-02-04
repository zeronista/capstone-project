# 🏥 AI Project Context: Healthcare Management System

> **Mục đích file:** Tài liệu tham chiếu dành cho AI Agents để hiểu nhanh và làm việc hiệu quả với codebase này.

---

## 📋 THÔNG TIN TỔNG QUAN

| Thuộc tính | Giá trị |
|-----------|---------|
| **Tên dự án** | ISSVSG Medical System (Capstone Project) |
| **Mục đích** | Hệ thống quản lý phòng khám với AI Callbot |
| **Tech Stack** | Spring Boot 4.0.2 + Java 21 + Thymeleaf + Tailwind CSS |
| **Database** | MySQL |
| **Package gốc** | `com.g4.capstoneproject` |
| **Build Tool** | Maven |

---

## 🎯 BUSINESS DOMAIN

### Core Features
1. **AI Callbot** - Gọi điện tự động cho bệnh nhân (khảo sát, nhắc lịch, tư vấn)
2. **Patient Portal** - Cổng thông tin bệnh nhân
3. **CRM/Ticketing** - Quản lý yêu cầu, phản hồi
4. **Medical Records** - Hồ sơ bệnh án, đơn thuốc, điều trị
5. **Knowledge Base** - Kho tri thức y khoa
6. **Surveys** - Khảo sát bệnh nhân

### User Roles
| Role | Mô tả |
|------|-------|
| `PATIENT` | Bệnh nhân - sử dụng dịch vụ |
| `RECEPTIONIST` | Lễ tân - quản lý cuộc gọi AI, tạo ticket |
| `DOCTOR` | Bác sĩ - tư vấn, kê đơn |
| `ADMIN` | Quản trị viên |

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### Package Structure
```
com.g4.capstoneproject/
├── config/           # Cấu hình (Security, Web, Thymeleaf)
├── controller/       # REST + MVC Controllers
│   └── api/          # REST API Controllers
├── dto/              # Data Transfer Objects
├── entity/           # JPA Entities
├── exception/        # Custom Exceptions
├── model/            # POJOs, Request/Response models
├── repository/       # Spring Data JPA Repositories
├── security/         # Security configs, JWT, OAuth2
└── service/          # Business Logic Services
```

### Key Controllers (14 files)
| Controller | Chức năng |
|-----------|-----------|
| `AuthController` | Đăng nhập/đăng ký/OAuth2 |
| `PatientController` | Trang bệnh nhân |
| `DoctorController` | Trang bác sĩ |
| `AdminController` | Quản trị hệ thống |
| `ReceptionistController` | Trang lễ tân |
| `StringeeController` | API tích hợp Stringee Call |
| `ProfileController` | Quản lý hồ sơ cá nhân |
| `SurveyApiController` | API khảo sát |

### Key Services (22 files)
| Service | Chức năng |
|---------|-----------|
| `AuthService` | Xác thực, JWT |
| `PatientService` | Nghiệp vụ bệnh nhân |
| `StringeeService` | Tích hợp Stringee (VoIP) |
| `GeminiASRService` | Speech-to-Text (Gemini AI) |
| `OpenAIASRService` | Speech-to-Text (OpenAI) |
| `GoogleSpeechService` | Google Speech API |
| `S3Service` | AWS S3 file storage |
| `KnowledgeArticleService` | Quản lý bài viết y khoa |
| `TicketService` | Quản lý ticket/CRM |
| `PrescriptionService` | Quản lý đơn thuốc |
| `WebCallService` | Web-to-web calling |

### Key Entities (24 files)
| Entity | Mô tả |
|--------|-------|
| `User` | Tài khoản người dùng |
| `UserInfo` | Thông tin cá nhân (1-1 với User) |
| `PatientDocument` | Tài liệu bệnh nhân |
| `Prescription` | Đơn thuốc |
| `TreatmentPlan` | Kế hoạch điều trị |
| `CallLog`, `CallCampaign` | Log cuộc gọi AI |
| `Ticket`, `TicketMessage` | Ticket hỗ trợ |
| `Survey`, `SurveyTemplate` | Khảo sát |
| `KnowledgeArticle`, `KnowledgeCategory` | Bài viết y khoa |
| `HealthForecast` | Dự báo sức khỏe |

---

## 🔌 TÍCH HỢP BÊN NGOÀI

### 1. Stringee (VoIP/Calling)
- **Mục đích:** AI Callbot gọi điện cho bệnh nhân, Web-to-Web Calling
- **Config:** `stringee.key.sid`, `stringee.key.secret`
- **Docs:** [STRINGEE_INTEGRATION.md](docs/STRINGEE_INTEGRATION.md)

### 2. AWS S3
- **Mục đích:** Lưu trữ file (ghi âm, tài liệu)
- **Cấu trúc folder recordings:** `voice/calls/{callId}/{type}_{timestamp}.webm`
  - `caller_*.webm` - Ghi âm của người gọi
  - `receiver_*.webm` - Ghi âm của người nhận
  - `combined_*.webm` - Ghi âm kết hợp cả 2
- **Dependencies:** `software.amazon.awssdk:s3`
- **Docs:** [VOICE_RECORDING_S3.md](docs/VOICE_RECORDING_S3.md)

### 3. AI Services (ASR/TTS)

#### Self-hosted Whisper ASR (Recommended)
- **Mục đích:** Speech-to-Text sử dụng model `openai/whisper-large-v3`
- **Service:** `WhisperASRService.java`
- **Config:** 
  - `asr.service.url=http://localhost:8001`
  - `asr.service.enabled=true`
  - `asr.service.default-language=vi`
- **Docker:** Xem thư mục `src/main/docker_ai_voice/asr-service/`
- **API Endpoint:** `POST /api/asr/transcribe`

#### Cloud ASR (Legacy/Fallback)
- **Gemini AI:** Speech recognition
- **OpenAI:** Speech recognition backup
- **Google Speech:** Alternative ASR

### 4. OAuth2
- **Google OAuth:** Đăng nhập bằng Google

---

## 🤖 AI VOICE MICROSERVICES

### Kiến trúc tổng quan
```
src/main/docker_ai_voice/
├── build-all.sh              # Script build tất cả services
├── README.md                 # Documentation
├── asr-service/              # Speech-to-Text (Whisper)
├── tts-service/              # (Future) Text-to-Speech
├── nlu-service/              # (Future) NLU
└── dialogue-service/         # (Future) Dialogue Management
```

### Build với Maven
```bash
# Build project + Docker (GPU)
mvn clean package

# Build project + Docker (CPU)
mvn clean package -Ddocker.build.type=cpu

# Skip Docker build
mvn clean package -Ddocker.build.skip=true
```

### Services hiện có

#### 1. ASR Service (Speech-to-Text)
```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Spring Boot   │────▶│   FastAPI ASR   │────▶│  faster-whisper │
│   Application   │     │   (Docker)      │     │  large-v3       │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                      │
        ▼                      ▼
  /api/asr/*             localhost:8001
```

| File | Đường dẫn |
|------|-----------|
| Dockerfile | `src/main/docker_ai_voice/asr-service/Dockerfile` |
| FastAPI app | `src/main/docker_ai_voice/asr-service/main.py` |
| Docker Compose | `src/main/docker_ai_voice/asr-service/docker-compose.yml` |
| Java Client | `com.g4.capstoneproject.service.WhisperASRService` |
| REST Controller | `com.g4.capstoneproject.controller.api.ASRApiController` |

### Chạy ASR Service
```bash
cd src/main/docker_ai_voice/asr-service

# GPU (recommended, 4x faster)
docker compose up -d asr-gpu

# CPU only
docker compose up -d asr-cpu
```

### API Endpoints
| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/api/asr/transcribe` | POST | Transcribe audio file |
| `/api/asr/transcribe/detailed` | POST | Transcribe với timestamps |
| `/api/asr/health` | GET | Kiểm tra service status |
| `/api/asr/languages` | GET | Danh sách ngôn ngữ hỗ trợ |

---

## 📁 CẤU TRÚC FRONTEND

### Templates (Thymeleaf)
```
templates/
├── fragments/layout.html    # Layout chung (head, sidebar, navbar)
├── fragments/components.html # Reusable UI components
├── admin/                   # Trang admin
├── auth/                    # Login, Register, Forgot Password
├── patient/                 # Patient portal
├── doctor/                  # Doctor dashboard
├── receptionist/            # Receptionist view
├── ai/                      # AI features (calls, config)
├── crm/                     # Tickets, surveys
└── profile/                 # User profiles
```

### Static Assets
```
static/
├── css/app.css              # Centralized styles
├── js/
│   ├── tailwind-config.js   # Tailwind configuration
│   ├── api-client.js        # API client utility
│   └── modules/             # Feature modules
└── images/
```

### Design System
- **CSS Framework:** Tailwind CSS (CDN)
- **Primary Color:** Cyan (#0891B2)
- **Icons:** Heroicons
- **Fonts:** Open Sans, Poppins
- **Components:** Card, Button, Modal, Badge, Alert (xem `fragments/components.html`)

---

## 🗄️ DATABASE SCHEMA

### Bảng chính
| Bảng | Mô tả |
|------|-------|
| `users` | Tài khoản + bảo mật |
| `user_info` | Thông tin cá nhân |
| `staff_info` | Thông tin nhân viên |
| `patient_documents` | Tài liệu bệnh nhân |
| `prescriptions`, `prescription_details` | Đơn thuốc |
| `treatment_plans`, `treatment_plan_items` | Kế hoạch điều trị |
| `call_logs`, `call_campaigns` | Log cuộc gọi AI |
| `web_call_logs` | Log web calls |
| `tickets`, `ticket_messages` | Hệ thống ticket |
| `surveys`, `survey_templates` | Khảo sát |
| `knowledge_categories`, `knowledge_articles` | Kho tri thức |
| `health_forecasts` | Dự báo sức khỏe |
| `notifications` | Thông báo |

### SQL Files
- Schema: [database_full_schema.sql](docs/database_full_schema.sql)
- PostgreSQL: [database_full_schema_postgresql.sql](docs/database_full_schema_postgresql.sql)
- Migrations: `docs/migrations/V*.sql`

---

## ⚙️ CẤU HÌNH & CHẠY DỰ ÁN

### Profiles
| Profile | File | Mục đích |
|---------|------|----------|
| `local` | `application-local.properties` | Development |
| default | `application.properties` | Base config |

### Environment Variables cần thiết
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/capstone
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password

# Stringee
STRINGEE_KEY_SID=SKxxxx
STRINGEE_KEY_SECRET=your_secret

# AWS S3
AWS_ACCESS_KEY_ID=xxxx
AWS_SECRET_ACCESS_KEY=xxxx
AWS_S3_BUCKET=your-bucket
```

### Commands
```bash
# Run
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Build
./mvnw clean package -DskipTests

# Test
./mvnw test
```

---

## 📚 TÀI LIỆU THAM CHIẾU

| File | Nội dung |
|------|----------|
| [IMPLEMENTATION_SUMMARY.md](docs/IMPLEMENTATION_SUMMARY.md) | Tổng kết Frontend |
| [FRONTEND_ARCHITECTURE.md](docs/FRONTEND_ARCHITECTURE.md) | Kiến trúc Frontend chi tiết |
| [DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) | Schema database đầy đủ |
| [STRINGEE_INTEGRATION.md](docs/STRINGEE_INTEGRATION.md) | Tích hợp Stringee |
| [SECURITY_GUIDE.md](docs/SECURITY_GUIDE.md) | Bảo mật API keys |
| [PHASE5_KNOWLEDGE_BASE.md](docs/PHASE5_KNOWLEDGE_BASE.md) | Knowledge Base feature |
| [SWAGGER_GUIDE.md](docs/SWAGGER_GUIDE.md) | API Documentation |
| [PATIENT_PORTAL_GUIDE.md](docs/PATIENT_PORTAL_GUIDE.md) | Hướng dẫn Patient Portal |

---

## 🔍 QUICK LOOKUP

### Tìm code theo feature
| Feature | Files liên quan |
|---------|----------------|
| **Authentication** | `AuthController`, `AuthService`, `security/*` |
| **Patient** | `PatientController`, `PatientService`, `patient/*` |
| **AI Calls** | `StringeeController`, `StringeeService`, `CallLog`, `CallCampaign` |
| **Prescriptions** | `PrescriptionService`, `Prescription`, `PrescriptionDetail` |
| **Knowledge Base** | `KnowledgeArticleService`, `KnowledgeCategoryService` |
| **File Upload** | `S3Service`, `PatientDocumentService` |

### API Base URLs
- **Swagger UI:** `/swagger-ui.html`
- **API Docs:** `/v3/api-docs`
- **Actuator Health:** `/actuator/health`

---

## 🚨 LƯU Ý QUAN TRỌNG CHO AI AGENTS

1. **Không commit API keys** - Dùng `application-local.properties` (đã gitignore)
2. **Spring Boot 4.0.2** - Phiên bản mới, một số API có thể khác
3. **Java 21** - Sử dụng features mới (records, pattern matching)
4. **Thymeleaf fragments** - Ưu tiên dùng fragments từ `layout.html` và `components.html`
5. **API Client** - Frontend dùng `api-client.js` cho HTTP requests
6. **Logging** - Đã config giảm verbose logging trong `application.properties`

---

*Cập nhật: February 2026*
