# 🤖 AI Voice Microservices

> Thư mục chứa các microservices AI cho hệ thống Healthcare Management

---

## 📁 Cấu trúc thư mục

```
docker_ai_voice/
├── build-all.sh          # Script build tất cả services
├── README.md             # Documentation này
│
├── asr-service/          # Automatic Speech Recognition (Whisper)
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── main.py
│   ├── requirements.txt
│   └── README.md
│
├── tts-service/          # (Future) Text-to-Speech
├── nlu-service/          # (Future) Natural Language Understanding
└── dialogue-service/     # (Future) Dialogue Management
```

---

## 🚀 Quick Start

### Build tất cả services với Maven
```bash
# Build project + Docker images (GPU)
mvn clean package

# Build project + Docker images (CPU only)
mvn clean package -Ddocker.build.type=cpu

# Build project only, skip Docker
mvn clean package -Ddocker.build.skip=true
```

### Build trực tiếp bằng script
```bash
cd src/main/docker_ai_voice

# GPU build (mặc định)
./build-all.sh gpu

# CPU build
./build-all.sh cpu

# Build cả hai
./build-all.sh all
```

---

## 🎤 Services hiện có

### 1. ASR Service (Speech-to-Text)
- **Model:** `openai/whisper-large-v3`
- **Framework:** FastAPI + faster-whisper
- **Port:** 8001 (GPU), 8002 (CPU)
- **Documentation:** [asr-service/README.md](asr-service/README.md)

```bash
# Chạy ASR service
cd asr-service
docker compose up -d asr-gpu    # GPU
docker compose up -d asr-cpu    # CPU

# Test
curl http://localhost:8001/health
```

---

## 📋 Services tương lai

| Service | Mục đích | Model dự kiến |
|---------|----------|---------------|
| `tts-service` | Text-to-Speech | Coqui TTS / VITS |
| `nlu-service` | Intent Classification, NER | PhoBERT / ViT5 |
| `dialogue-service` | Dialogue Management | Rasa / Custom |
| `embedding-service` | Text Embeddings | vietnamese-sbert |

---

## 🔧 Configuration

### Maven Properties (pom.xml)
```xml
<properties>
    <!-- Đường dẫn Docker microservices -->
    <docker.ai.voice.dir>${project.basedir}/src/main/docker_ai_voice</docker.ai.voice.dir>
    
    <!-- Skip Docker build -->
    <docker.build.skip>false</docker.build.skip>
    
    <!-- Build type: gpu, cpu, all -->
    <docker.build.type>gpu</docker.build.type>
</properties>
```

### Spring Boot Properties (application.properties)
```properties
# ASR Service
asr.service.url=http://localhost:8001
asr.service.enabled=true
asr.service.default-language=vi
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
│                      (Main Monolith)                        │
├─────────────────────────────────────────────────────────────┤
│  WhisperASRService  │  TTSService  │  NLUService  │  ...    │
│   (Java Client)     │   (Future)   │   (Future)   │         │
└────────┬────────────┴──────┬───────┴──────┬───────┴─────────┘
         │                   │              │
         ▼                   ▼              ▼
┌─────────────────┐  ┌─────────────┐  ┌─────────────┐
│   ASR Service   │  │ TTS Service │  │ NLU Service │
│   (Docker)      │  │  (Docker)   │  │  (Docker)   │
│   Port: 8001    │  │ Port: 8003  │  │ Port: 8004  │
└─────────────────┘  └─────────────┘  └─────────────┘
     Whisper            Coqui TTS        PhoBERT
```

---

## 📝 Thêm service mới

### 1. Tạo thư mục service
```bash
mkdir -p src/main/docker_ai_voice/my-new-service
cd src/main/docker_ai_voice/my-new-service
```

### 2. Tạo Dockerfile
```dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 3. Tạo docker-compose.yml
```yaml
services:
  my-new-service:
    build: .
    ports:
      - "8005:8000"
```

### 4. Thêm vào build-all.sh
```bash
SERVICES=(
    "asr-service"
    "my-new-service"  # Thêm dòng này
)
```

### 5. Tạo Java client service
```java
@Service
public class MyNewService {
    @Value("${my.service.url:http://localhost:8005}")
    private String serviceUrl;
    
    // ... implementation
}
```

---

## 🐳 Docker Commands Reference

```bash
# Xem tất cả containers
docker ps -a | grep -E "asr|tts|nlu"

# Xem logs
docker logs -f asr-service

# Restart service
docker compose -f asr-service/docker-compose.yml restart

# Stop tất cả
docker compose -f asr-service/docker-compose.yml down

# Xóa images
docker rmi asr-service:gpu asr-service:cpu
```

---

## 📊 Resource Requirements

| Service | GPU Memory | RAM | CPU |
|---------|------------|-----|-----|
| ASR (GPU) | 6GB VRAM | 8GB | 4 cores |
| ASR (CPU) | - | 16GB | 8 cores |
| TTS | 2GB VRAM | 4GB | 2 cores |
| NLU | 2GB VRAM | 4GB | 2 cores |

---

## 🔗 Related Documentation

- [AI_PROJECT_CONTEXT.md](../../../AI_PROJECT_CONTEXT.md) - Tổng quan project
- [VOICE_RECORDING_S3.md](../../../docs/VOICE_RECORDING_S3.md) - Voice recording
- [STRINGEE_INTEGRATION.md](../../../docs/STRINGEE_INTEGRATION.md) - Stringee VoIP
