# Hướng dẫn Tích hợp Stringee Call API

## Mục lục
1. [Cấu hình ban đầu](#cấu-hình-ban-đầu)
2. [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
3. [API Endpoints](#api-endpoints)
4. [Webhook Configuration](#webhook-configuration)
5. [Testing](#testing)
6. [AI Integration](#ai-integration)

---

## Cấu hình ban đầu

### 1. Lấy Stringee Credentials

Truy cập [Stringee Dashboard](https://developer.stringee.com) và lấy:
- **Key SID**: Stringee API Key ID
- **Key Secret**: Stringee API Secret Key

### 2. Cập nhật application.properties

```properties
# Stringee Configuration
stringee.key.sid=YOUR_STRINGEE_KEY_SID_HERE
stringee.key.secret=YOUR_STRINGEE_KEY_SECRET_HERE
stringee.api.base-url=https://api.stringee.com/v1
stringee.webhook.domain=https://your-server-domain.com
```

**Lưu ý:**
- Thay `YOUR_STRINGEE_KEY_SID_HERE` và `YOUR_STRINGEE_KEY_SECRET_HERE` bằng credentials thực của bạn
- `stringee.webhook.domain` phải là domain public để Stringee có thể gọi webhook về

### 3. Expose Webhook (Development)

Để test local, sử dụng ngrok:
```bash
ngrok http 8080
```

Sau đó cập nhật `stringee.webhook.domain` với URL ngrok (ví dụ: `https://abc123.ngrok.io`)

---

## Kiến trúc hệ thống

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Backend   │────1───>│   Stringee   │────2───>│  Khách hàng │
│ (Spring Boot)│         │   Platform   │         │             │
└─────────────┘         └──────────────┘         └─────────────┘
       ↑                        │                        │
       │                        │                        │
       └────────────3───────────┘                        │
                 (Webhook)                               │
       ┌────────────────────────────────────────────────┘
       │                    4
       ↓
┌─────────────┐
│  AI Server  │
│  (STT/LLM/  │
│    TTS)     │
└─────────────┘
```

**Luồng hoạt động:**
1. Backend gọi Stringee API để initiate cuộc gọi
2. Stringee gọi ra số khách hàng
3. Khi khách hàng bắt máy, Stringee gọi webhook `/api/stringee/answer`
4. Backend trả về SCCO để điều khiển cuộc gọi (có thể kết nối sang AI Server qua SIP)

---

## API Endpoints

### 1. Thực hiện cuộc gọi ra (Outbound Call)

**Endpoint:** `POST /api/stringee/call/outbound`

**Request:**
```json
{
  "fromNumber": "842873008xxx",
  "toNumber": "84987654321",
  "brandName": "Phòng Khám ABC"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Cuộc gọi đã được khởi tạo",
  "data": {
    "r": 0,
    "callId": "call-xxxxx-xxxxx-xxxxx",
    "status": "calling"
  }
}
```

**Curl Example:**
```bash
curl -X POST http://localhost:8080/api/stringee/call/outbound \
  -H "Content-Type: application/json" \
  -d '{
    "fromNumber": "842873008xxx",
    "toNumber": "84987654321",
    "brandName": "Phòng Khám ABC"
  }'
```

### 2. Lấy thông tin cuộc gọi

**Endpoint:** `GET /api/stringee/call/info/{callId}`

**Response:**
```json
{
  "r": 0,
  "callId": "call-xxxxx-xxxxx-xxxxx",
  "from": "842873008xxx",
  "to": "84987654321",
  "status": "ended",
  "duration": 120,
  "recordingUrl": "https://..."
}
```

---

## Webhook Configuration

### 1. Answer Webhook

**Endpoint:** `GET/POST /api/stringee/answer`

Stringee gọi webhook này khi khách hàng bắt máy. Backend cần trả về **SCCO** (Stringee Call Control Object).

**SCCO Response Example - Phát câu chào:**
```json
[
  {
    "action": "talk",
    "text": "Xin chào! Tôi là trợ lý ảo từ phòng khám.",
    "voice": "southern_female_1"
  }
]
```

**SCCO Response - Ghi âm:**
```json
[
  {
    "action": "talk",
    "text": "Vui lòng để lại lời nhắn sau tiếng beep.",
    "voice": "southern_female_1"
  },
  {
    "action": "record",
    "maxTime": 60,
    "recordingStatusCallback": "https://your-domain.com/api/stringee/recording"
  }
]
```

**SCCO Response - Kết nối SIP (AI Real-time):**
```json
[
  {
    "action": "talk",
    "text": "Đang kết nối với trợ lý AI.",
    "voice": "southern_female_1"
  },
  {
    "action": "connect",
    "to": {
      "type": "sip",
      "number": "ai-bot@your-sip-server.com"
    }
  }
]
```

### 2. Event Webhook

**Endpoint:** `POST /api/stringee/event`

Nhận các events từ Stringee:
- `call.started`: Cuộc gọi bắt đầu
- `call.answered`: Khách hàng bắt máy
- `call.ended`: Cuộc gọi kết thúc
- `recording.available`: File ghi âm sẵn sàng

**Event Payload Example:**
```json
{
  "event": "call.ended",
  "callId": "call-xxxxx-xxxxx-xxxxx",
  "from": "842873008xxx",
  "to": "84987654321",
  "duration": 120,
  "status": "answered",
  "recordingUrl": "https://..."
}
```

---

## Testing

### 1. Test với Postman

Import collection sau vào Postman:

```json
{
  "info": {
    "name": "Stringee Integration",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Make Outbound Call",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"fromNumber\": \"842873008xxx\",\n  \"toNumber\": \"84987654321\",\n  \"brandName\": \"Test Call\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/stringee/call/outbound",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "stringee", "call", "outbound"]
        }
      }
    }
  ]
}
```

### 2. Test Webhook với curl

```bash
# Test Answer Webhook
curl http://localhost:8080/api/stringee/answer

# Kết quả mong đợi: JSON array chứa SCCO actions
```

---

## AI Integration

### Phương án 1: SIP Trunk (Recommended)

Để tích hợp AI xử lý real-time, bạn cần:

1. **Setup SIP Server** (ví dụ: FreeSWITCH, Asterisk)
2. **Configure SIP Trunk** với Stringee
3. **Audio Processing Pipeline:**
   - Nhận audio stream qua RTP
   - STT (Speech-to-Text): Chuyển audio thành text
   - LLM: Xử lý câu hỏi và tạo response
   - TTS (Text-to-Speech): Chuyển text thành audio
   - Gửi audio response về SIP stream

**SCCO để kết nối SIP:**
```java
@GetMapping("/answer/ai-mode")
public List<Map<String, Object>> getAiModeScco() {
    List<Map<String, Object>> scco = new ArrayList<>();
    
    Map<String, Object> connect = new HashMap<>();
    connect.put("action", "connect");
    
    Map<String, String> sipDestination = new HashMap<>();
    sipDestination.put("type", "sip");
    sipDestination.put("number", "ai-callbot@your-sip-server.com");
    
    connect.put("to", sipDestination);
    scco.add(connect);
    
    return scco;
}
```

### Phương án 2: Record & Process (Async)

Đơn giản hơn nhưng không real-time:

1. **Record audio** từ khách hàng
2. **Process offline:**
   - Download file ghi âm từ `recordingUrl`
   - STT → LLM → TTS
   - Gọi lại khách hàng với câu trả lời

**SCCO để ghi âm:**
```java
Map<String, Object> record = new HashMap<>();
record.put("action", "record");
record.put("maxTime", 60);
record.put("recordingStatusCallback", 
    webhookDomain + "/api/stringee/recording");
```

---

## Audio Format

### Stringee Audio Specifications

- **Codec (qua SIP):** G.711 (PCMU/PCMA)
- **Sample Rate:** 8000 Hz (narrowband) hoặc 16000 Hz (wideband)
- **Bit Depth:** 16-bit PCM
- **Channels:** Mono

### Lưu ý khi xử lý Audio

```java
// Ví dụ convert audio cho AI processing
// Input: G.711 từ Stringee
// Output: PCM 16-bit 16kHz mono cho STT

ffmpeg -i input.g711 -ar 16000 -ac 1 -f s16le output.pcm
```

---

## Troubleshooting

### 1. JWT Token Invalid

**Lỗi:** `{"r": -1, "message": "Token invalid"}`

**Giải pháp:**
- Kiểm tra `stringee.key.sid` và `stringee.key.secret`
- Đảm bảo thời gian server đúng (NTP sync)

### 2. Webhook không nhận được

**Lỗi:** Stringee không gọi webhook

**Giải pháp:**
- Kiểm tra `stringee.webhook.domain` phải là public URL
- Test với ngrok hoặc deploy lên server public
- Kiểm tra firewall/security group

### 3. Cuộc gọi không ra

**Lỗi:** Call failed hoặc không nghe thấy gì

**Giải pháp:**
- Kiểm tra số `fromNumber` đã đăng ký Voice Brandname chưa
- Kiểm tra số `toNumber` đúng format quốc tế (84xxxxxxxxx)
- Kiểm tra tài khoản Stringee còn credit không

---

## Voice Options

Danh sách giọng đọc hỗ trợ (TTS):

| Voice ID | Mô tả |
|----------|-------|
| `southern_female_1` | Nữ miền Nam (tự nhiên) |
| `southern_male_1` | Nam miền Nam |
| `northern_female_1` | Nữ miền Bắc |
| `northern_male_1` | Nam miền Bắc |
| `central_female_1` | Nữ miền Trung |

**Sử dụng:**
```java
talkAction.put("voice", "southern_female_1");
```

---

## Next Steps

1. ✅ **Đăng ký Voice Brandname** với Stringee
2. ✅ **Setup SIP Server** nếu cần AI real-time
3. ✅ **Tích hợp STT/LLM/TTS** engine
4. ✅ **Deploy lên production** với domain public
5. ✅ **Cấu hình webhook** trong Stringee Dashboard
6. ✅ **Monitor & logging** cuộc gọi

---

## Tài liệu tham khảo

- [Stringee API Documentation](https://developer.stringee.com/docs/call-api)
- [SCCO Reference](https://developer.stringee.com/docs/scco)
- [SIP Trunk Guide](https://developer.stringee.com/docs/sip-trunk)
- [FreeSWITCH Documentation](https://freeswitch.org/confluence/)

---

## Support

Nếu gặp vấn đề, liên hệ:
- **Stringee Support:** support@stringee.com
- **Technical Team:** your-team@email.com
