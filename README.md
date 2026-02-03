# 🏥 ISSVSG Medical System

> Healthcare Management System với AI Callbot & Voice Services

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)](https://www.docker.com/)

---

## ✨ Tính năng chính

- 🤖 **AI Callbot** - Gọi điện tự động cho bệnh nhân (khảo sát, nhắc lịch, tư vấn)
- 📞 **WebRTC Calling** - Gọi điện trực tiếp trên web với ghi âm 3 streams (caller/receiver/combined)
- 🎤 **Self-hosted ASR** - Speech-to-Text sử dụng Whisper large-v3
- 👥 **Patient Portal** - Cổng thông tin bệnh nhân với hồ sơ, đơn thuốc, lịch hẹn
- 💊 **Prescription Management** - Quản lý đơn thuốc, kế hoạch điều trị
- 🎫 **CRM/Ticketing** - Hệ thống hỗ trợ và quản lý yêu cầu
- 📚 **Knowledge Base** - Kho tri thức y khoa với AI chatbot
- 📊 **Surveys** - Khảo sát bệnh nhân với analytics

---

## 🚀 Quick Start

```bash
# 1. Clone repository
git clone <repository-url>
cd capstone-project

# 2. Setup database (MySQL)
mysql -u root -p
CREATE DATABASE medical_system;
mysql -u root -p medical_system < docs/database_full_schema_postgresql.sql

# 3. Configure
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
# Edit application-local.properties với DB credentials

# 4. Build & Run
./mvnw clean package -Ddocker.build.skip=true
java -jar target/capstone-project-0.0.1-SNAPSHOT.jar

# 5. Access
# Web: http://localhost:8080
# API Docs: http://localhost:8080/swagger-ui.html
```

### Chạy AI Voice Services (Optional)

```bash
# ASR Service (Speech-to-Text)
cd src/main/docker_ai_voice/asr-service
docker compose up -d asr-gpu  # GPU (4x faster)
# hoặc
docker compose up -d asr-cpu  # CPU only
```

📖 **Chi tiết:** [GETTING_STARTED.md](GETTING_STARTED.md)

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot 4.0.2, Java 21 |
| **Frontend** | Thymeleaf, Tailwind CSS, JavaScript |
| **Database** | MySQL 8.0+ |
| **Storage** | AWS S3 |
| **VoIP** | Stringee SDK |
| **AI Services** | FastAPI + Whisper (ASR), Gemini AI, OpenAI |
| **Authentication** | JWT, OAuth2 (Google) |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **Containerization** | Docker, Docker Compose |

---

## 📁 Cấu trúc project

```
capstone-project/
├── src/
│   ├── main/
│   │   ├── java/com/g4/capstoneproject/
│   │   │   ├── config/          # Security, Web configs
│   │   │   ├── controller/      # MVC + REST controllers
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── service/         # Business logic
│   │   │   └── repository/      # Data access
│   │   ├── resources/
│   │   │   ├── templates/       # Thymeleaf templates
│   │   │   ├── static/          # CSS, JS, images
│   │   │   └── application*.properties
│   │   └── docker_ai_voice/     # AI microservices
│   │       ├── asr-service/     # Whisper ASR
│   │       └── build-all.sh
│   └── test/
├── docs/                        # Documentation
├── pom.xml                      # Maven config
├── GETTING_STARTED.md           # Setup guide
└── AI_PROJECT_CONTEXT.md        # Architecture docs
```

---

## 👥 User Roles

| Role | Chức năng |
|------|-----------|
| **ADMIN** | Quản trị hệ thống, users, cấu hình |
| **DOCTOR** | Khám bệnh, kê đơn, tư vấn |
| **RECEPTIONIST** | Quản lý cuộc gọi AI, tickets |
| **PATIENT** | Xem hồ sơ, đặt lịch, liên hệ bác sĩ |

---

## 🔑 Tài khoản test

Sau khi import `docs/insert_test_users.sql`:

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `doctor1` | `doctor123` | DOCTOR |
| `receptionist1` | `recep123` | RECEPTIONIST |
| `patient1` | `patient123` | PATIENT |

---

## 📊 Key Features Overview

### 1. Dashboard
- **Admin**: User management, system reports
- **Doctor**: Patient list, prescriptions, appointments
- **Patient**: Medical records, documents, tickets
- **Receptionist**: Call campaigns, ticket management

### 2. Web Calling (WebRTC)
- Gọi điện web-to-web sử dụng Stringee
- Ghi âm riêng biệt: caller, receiver, combined
- Lưu trữ S3: `voice/calls/{callId}/{type}_{timestamp}.webm`
- Auto transcribe với ASR service

### 3. AI Voice Services
- **ASR**: Whisper large-v3 (self-hosted)
- API: `POST /api/asr/transcribe`
- Hỗ trợ 100+ ngôn ngữ, auto-detect
- GPU: 4x nhanh hơn CPU

### 4. Knowledge Base
- Quản lý bài viết y khoa
- Phân loại theo category
- AI chatbot hỗ trợ tìm kiếm
- Rich text editor

---

## 📚 Documentation

| Document | Mô tả |
|----------|-------|
| [GETTING_STARTED.md](GETTING_STARTED.md) | Hướng dẫn setup chi tiết |
| [AI_PROJECT_CONTEXT.md](AI_PROJECT_CONTEXT.md) | Kiến trúc tổng quan |
| [docs/BACKEND_SETUP.md](docs/BACKEND_SETUP.md) | Backend configuration |
| [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) | Database schema |
| [docs/SECURITY_GUIDE.md](docs/SECURITY_GUIDE.md) | Security practices |
| [docs/SWAGGER_GUIDE.md](docs/SWAGGER_GUIDE.md) | API documentation |
| [docs/STRINGEE_INTEGRATION.md](docs/STRINGEE_INTEGRATION.md) | VoIP setup |
| [src/main/docker_ai_voice/README.md](src/main/docker_ai_voice/README.md) | AI microservices |

---

## 🛠️ Development

### Build commands

```bash
# Build Spring Boot only (fast)
./mvnw clean package -Ddocker.build.skip=true

# Build Spring Boot + Docker AI services (GPU)
./mvnw clean package -Ddocker.build.type=gpu

# Build Spring Boot + Docker AI services (CPU)
./mvnw clean package -Ddocker.build.type=cpu

# Run with hot reload
./mvnw spring-boot:run
```

### API Testing

```bash
# Swagger UI
open http://localhost:8080/swagger-ui.html

# Login API
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 🐳 Docker Services

### ASR Service (Whisper)

```bash
cd src/main/docker_ai_voice/asr-service

# GPU (NVIDIA)
docker compose up -d asr-gpu

# CPU only
docker compose up -d asr-cpu

# Check health
curl http://localhost:8001/health

# Stop
docker compose down
```

### Future Services

- 🔮 **TTS Service** - Text-to-Speech
- 🔮 **NLU Service** - Natural Language Understanding
- 🔮 **Dialogue Service** - Conversation Management

---

## 🔐 Security

### Production Checklist

- [ ] Đổi `jwt.secret.key` (sử dụng 256-bit random key)
- [ ] Cập nhật database passwords
- [ ] Enable HTTPS/SSL
- [ ] Configure CORS properly
- [ ] Setup rate limiting
- [ ] Enable firewall rules
- [ ] Regular database backups
- [ ] Monitor application logs

```bash
# Generate secure JWT key
openssl rand -base64 32
```

---

## 📈 Performance

### Recommended Specs

| Environment | CPU | RAM | Storage | GPU |
|-------------|-----|-----|---------|-----|
| **Development** | 4 cores | 8GB | 20GB | Optional |
| **Production** | 8+ cores | 16GB+ | 100GB+ | NVIDIA (ASR) |

### ASR Service

| Mode | Speed | VRAM | RAM |
|------|-------|------|-----|
| **GPU** | 4x faster | 6GB | 8GB |
| **CPU** | Baseline | - | 16GB |

---

## 🐛 Troubleshooting

### Common Issues

**Port 8080 in use:**
```bash
lsof -i :8080
kill -9 <PID>
```

**Database connection failed:**
```bash
mysql -u root -p
GRANT ALL ON medical_system.* TO 'medicaluser'@'localhost';
```

**ASR service not responding:**
```bash
docker ps | grep asr
docker logs asr-service
docker compose restart
```

📖 Xem thêm: [GETTING_STARTED.md - Troubleshooting](GETTING_STARTED.md#-troubleshooting)

---

## 📝 License

[Specify your license here]

---

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) first.

---

## 📧 Contact

- **Team:** G4 Capstone Project
- **Email:** [your-email@example.com]
- **Documentation:** [AI_PROJECT_CONTEXT.md](AI_PROJECT_CONTEXT.md)

---

**Made with ❤️ for better healthcare**
