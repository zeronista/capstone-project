# 🎙️ ASR Service - Whisper Large V3

Self-hosted Speech-to-Text service sử dụng OpenAI Whisper Large V3 model.

## 🚀 Quick Start

### 1. Chạy với Docker (GPU)

```bash
# Build và chạy với GPU support
docker compose up -d asr-gpu

# Kiểm tra logs
docker logs -f asr-service-gpu
```

### 2. Chạy với Docker (CPU only)

```bash
# Build và chạy CPU version
docker compose --profile cpu up -d asr-cpu

# Lưu ý: CPU version chậm hơn nhiều (~10-20x)
```

### 3. Development mode (model nhỏ)

```bash
# Dùng model base để test nhanh
docker compose --profile dev up -d asr-dev
```

## 📡 API Endpoints

### Transcribe Audio

```bash
# Full response với timestamps
curl -X POST "http://localhost:8001/transcribe" \
     -F "file=@audio.wav" \
     -F "language=vi"

# Simple response (chỉ text)
curl -X POST "http://localhost:8001/transcribe/simple" \
     -F "file=@audio.wav"
```

### Response Format

```json
{
  "success": true,
  "text": "Xin chào, tôi là bệnh nhân Nguyễn Văn A",
  "language": "vi",
  "language_probability": 0.9876,
  "duration": 5.432,
  "segments": [
    {
      "id": 0,
      "start": 0.0,
      "end": 2.5,
      "text": "Xin chào",
      "confidence": -0.234
    },
    {
      "id": 1,
      "start": 2.5,
      "end": 5.432,
      "text": "tôi là bệnh nhân Nguyễn Văn A",
      "confidence": -0.156
    }
  ],
  "processing_time": 1.234
}
```

### Health Check

```bash
curl http://localhost:8001/health
```

## ⚙️ Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `MODEL_SIZE` | `large-v3` | Model size: tiny, base, small, medium, large-v3 |
| `DEVICE` | `auto` | Device: auto, cuda, cpu |
| `COMPUTE_TYPE` | `float16` | GPU: float16, int8_float16. CPU: int8 |
| `BEAM_SIZE` | `5` | Beam search size (1-10) |
| `MAX_FILE_SIZE_MB` | `100` | Max upload file size |

## 📊 Model Sizes & Performance

| Model | Parameters | VRAM | Speed (GPU) | Speed (CPU) | Quality |
|-------|------------|------|-------------|-------------|---------|
| tiny | 39M | ~1GB | ~32x | ~1x | ⭐⭐ |
| base | 74M | ~1GB | ~16x | ~0.7x | ⭐⭐⭐ |
| small | 244M | ~2GB | ~6x | ~0.3x | ⭐⭐⭐⭐ |
| medium | 769M | ~5GB | ~2x | ~0.1x | ⭐⭐⭐⭐ |
| large-v3 | 1550M | ~10GB | ~1x | ~0.05x | ⭐⭐⭐⭐⭐ |

*Speed relative to real-time audio length*

## 🔗 Integration với Spring Boot

### Cập nhật application.properties

```properties
# ASR Service (self-hosted Whisper)
asr.service.url=http://localhost:8001
asr.service.enabled=true
```

### Java Client Example

```java
@Service
public class WhisperASRService {
    
    @Value("${asr.service.url}")
    private String asrServiceUrl;
    
    public String transcribe(MultipartFile audioFile) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename();
            }
        });
        body.add("language", "vi");
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            asrServiceUrl + "/transcribe/simple",
            requestEntity,
            Map.class
        );
        
        return (String) response.getBody().get("text");
    }
}
```

## 🐳 Docker Commands

```bash
# Build images
docker compose build

# Start GPU service
docker compose up -d asr-gpu

# Start CPU service
docker compose --profile cpu up -d asr-cpu

# View logs
docker logs -f asr-service-gpu

# Stop all
docker compose down

# Remove volumes (xóa model cache)
docker compose down -v
```

## 📁 Folder Structure

```
asr-service/
├── Dockerfile           # Multi-stage: GPU & CPU variants
├── docker-compose.yml   # Service definitions
├── main.py              # FastAPI application
├── requirements.txt     # GPU dependencies
├── requirements-cpu.txt # CPU dependencies
├── .env.example         # Environment template
└── README.md            # This file
```

## 🔧 Troubleshooting

### Model download chậm
```bash
# Pre-download model trước khi chạy
docker run --rm -v asr-whisper-models:/models \
  python:3.11-slim \
  pip install faster-whisper && \
  python -c "from faster_whisper import WhisperModel; WhisperModel('large-v3', download_root='/models')"
```

### Out of Memory (GPU)
```bash
# Dùng compute_type tiết kiệm VRAM hơn
COMPUTE_TYPE=int8_float16

# Hoặc dùng model nhỏ hơn
MODEL_SIZE=medium
```

### CPU quá chậm
```bash
# Dùng model nhỏ hơn cho CPU
MODEL_SIZE=small
COMPUTE_TYPE=int8
NUM_WORKERS=4
```

## 📝 License

MIT License - Sử dụng cho mục đích học tập và nghiên cứu.
