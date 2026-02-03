# 🎙️ Hướng dẫn Ghi âm Web-to-Web Call và Upload lên S3

## 📋 Tổng quan

Tính năng ghi âm Web-to-Web Call cho phép user ghi lại cuộc trò chuyện giữa 2 người dùng trên trình duyệt và tự động lưu vào AWS S3 bucket trong folder `voice/`.

## 🎯 Tính năng đã được thêm

### 1. ✅ Ghi âm Client-side
- Sử dụng **MediaRecorder API** của trình duyệt
- Ghi âm định dạng **audio/webm** (codec: Opus)
- Hiển thị thời gian ghi âm real-time
- Animation đỏ nhấp nháy khi đang ghi âm

### 2. ✅ Upload tự động lên S3
- Tự động upload sau khi dừng ghi âm
- Lưu vào folder `voice/` trong S3 bucket
- Tạo pre-signed URL (hiệu lực 7 ngày)

### 3. ✅ API Backend
- Endpoint: `POST /api/stringee/upload-recording`
- Nhận file từ client và upload lên S3
- Trả về S3 key và pre-signed URL

## 🖥️ Giao diện người dùng

### Các nút điều khiển:

```
📞 Gọi Voice        - Bắt đầu cuộc gọi
⛔ Từ chối          - Từ chối cuộc gọi đến
✅ Trả lời          - Trả lời cuộc gọi đến
❌ Kết thúc         - Kết thúc cuộc gọi

🎙️ Bắt đầu ghi âm   - Bắt đầu ghi âm cuộc gọi
⏹️ Dừng ghi âm       - Dừng và upload file
```

### Indicator khi đang ghi âm:

```
🎙️ Đang ghi âm ● 00:25
```

## 🔄 Luồng hoạt động

```
1. User A kết nối với Stringee (user1)
   ↓
2. User B kết nối với Stringee (user2)
   ↓
3. User A gọi User B → Cuộc gọi được thiết lập
   ↓
4. User A bấm "🎙️ Bắt đầu ghi âm"
   ↓
5. Trình duyệt xin quyền truy cập Microphone
   ↓
6. MediaRecorder bắt đầu ghi âm
   ↓
7. Hiển thị indicator đỏ nhấp nháy + thời gian
   ↓
8. User A bấm "⏹️ Dừng ghi âm"
   ↓
9. File được tạo thành blob (audio/webm)
   ↓
10. Tự động upload lên /api/stringee/upload-recording
   ↓
11. Backend upload lên S3 folder voice/
   ↓
12. Trả về S3 key và pre-signed URL
   ↓
13. ✅ Hoàn tất - Hiển thị log thành công
```

## 📁 Cấu trúc file trong S3

```
s3://capstone-project-files-2026/
├── voice/
│   ├── 20260128_143025_user1_web_1738050625123.webm
│   ├── 20260128_144530_user2_web_1738051130456.webm
│   └── ...
├── image/
└── text/
```

**Format tên file:** `voice/yyyyMMdd_HHmmss_userId_callId.webm`

Ví dụ: `voice/20260128_143025_user1_web_1738050625123.webm`

## 🧪 Cách sử dụng

### Bước 1: Mở 2 tab trình duyệt

**Tab 1 (User A):**
```
http://localhost:8080/ai/calls
Nhập User ID: user1
Bấm "Kết nối"
```

**Tab 2 (User B):**
```
http://localhost:8080/ai/calls
Nhập User ID: user2
Bấm "Kết nối"
```

### Bước 2: Thực hiện cuộc gọi

**Tab 1 (user1):**
```
Nhập ID người nhận: user2
Bấm "📞 Gọi Voice"
```

**Tab 2 (user2):**
```
Nhận cuộc gọi đến từ user1
Bấm "✅ Trả lời"
```

### Bước 3: Ghi âm cuộc gọi

**Tab 1 hoặc Tab 2:**
```
1. Bấm "🎙️ Bắt đầu ghi âm"
2. Cho phép trình duyệt truy cập Microphone (nếu chưa)
3. Thấy indicator đỏ nhấp nháy: 🎙️ Đang ghi âm ● 00:00
4. Nói chuyện bình thường
5. Bấm "⏹️ Dừng ghi âm" khi muốn dừng
```

### Bước 4: Kiểm tra kết quả

**Trong Browser Console:**
```
✅ Upload thành công! File: voice/20260128_143025_user1_web_xxx.webm
S3 Key: voice/20260128_143025_user1_web_xxx.webm
Pre-signed URL: https://capstone-project-files-2026.s3...
```

**Trong AWS S3:**
```
Truy cập: AWS Console → S3 → capstone-project-files-2026 → voice/
Thấy file: 20260128_143025_user1_web_xxx.webm
```

**Trong Server Log:**
```
Nhận file ghi âm: call_user1_to_user2_2026-01-28T14-30-25.webm (size: 245678 bytes)
✅ Đã upload file lên S3: voice/20260128_143025_user1_web_xxx.webm
```

## 🔧 Chi tiết kỹ thuật

### Frontend (web-call.html)

#### MediaRecorder Configuration
```javascript
const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
mediaRecorder = new MediaRecorder(stream, {
    mimeType: 'audio/webm;codecs=opus'
});
```

#### Upload Request
```javascript
const formData = new FormData();
formData.append('file', blob, filename);
formData.append('callId', 'web_' + Date.now());
formData.append('userId', userId);

const response = await fetch('/api/stringee/upload-recording', {
    method: 'POST',
    body: formData
});
```

### Backend (StringeeController.java)

#### Upload Endpoint
```java
@PostMapping("/upload-recording")
public ResponseEntity<?> uploadRecording(
        @RequestParam("file") MultipartFile file,
        @RequestParam("callId") String callId,
        @RequestParam("userId") String userId)
```

### S3 Service

#### Upload Method
```java
public String uploadRecordingFile(
        MultipartFile file, 
        String callId, 
        String userId) throws IOException
```

## 📊 Kích thước file ước tính

| Thời lượng | Kích thước file (WebM/Opus) |
|------------|----------------------------|
| 1 phút     | ~100 KB                    |
| 5 phút     | ~500 KB                    |
| 10 phút    | ~1 MB                      |
| 30 phút    | ~3 MB                      |

## ⚠️ Lưu ý quan trọng

### 1. Quyền truy cập Microphone
- Trình duyệt sẽ yêu cầu quyền khi lần đầu ghi âm
- **HTTPS hoặc localhost** là bắt buộc để sử dụng MediaRecorder API
- Nếu bị từ chối, user cần vào Settings → Site Settings để cấp quyền

### 2. Định dạng file
- Format: **audio/webm** với codec **Opus**
- Tương thích với hầu hết trình duyệt hiện đại
- Có thể convert sang MP3 nếu cần:
  ```bash
  ffmpeg -i recording.webm recording.mp3
  ```

### 3. Browser Compatibility
| Browser | Hỗ trợ MediaRecorder |
|---------|---------------------|
| Chrome  | ✅ 47+              |
| Firefox | ✅ 25+              |
| Edge    | ✅ 79+              |
| Safari  | ✅ 14.1+            |
| Opera   | ✅ 36+              |

### 4. Giới hạn kích thước
- Spring Boot mặc định giới hạn file upload: **1 MB**
- Nếu ghi âm lâu (>10 phút), cần tăng giới hạn:

```properties
# application.properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### 5. Hiệu năng
- Ghi âm không ảnh hưởng đến chất lượng cuộc gọi
- CPU sử dụng: ~1-2% khi ghi âm
- RAM: ~5-10 MB buffer

## 🚀 Tính năng nâng cao (TODO)

### 1. Tự động ghi âm
```javascript
// Tự động bắt đầu ghi khi cuộc gọi kết nối
call.on('addremotestream', function (stream) {
    autoStartRecording(); // Implement function này
});
```

### 2. Ghi âm cả 2 chiều
Hiện tại chỉ ghi âm từ microphone của user. Để ghi cả 2 chiều:
```javascript
// Merge local + remote audio streams
const audioContext = new AudioContext();
const destination = audioContext.createMediaStreamDestination();

// Add local stream
const localSource = audioContext.createMediaStreamSource(localStream);
localSource.connect(destination);

// Add remote stream
const remoteSource = audioContext.createMediaStreamSource(remoteStream);
remoteSource.connect(destination);

// Record merged stream
mediaRecorder = new MediaRecorder(destination.stream);
```

### 3. Lưu metadata vào database
```java
// TODO: Tạo CallRecording entity và repository
@Entity
public class CallRecording {
    private Long id;
    private String callId;
    private String userId;
    private String s3Key;
    private String presignedUrl;
    private Long fileSize;
    private Integer duration; // seconds
    private LocalDateTime createdAt;
}
```

### 4. Transcription (chuyển giọng nói thành text)
```java
// TODO: Tích hợp Google Speech-to-Text hoặc AWS Transcribe
public String transcribeRecording(String s3Key) {
    // Call transcription service
    // Return transcript text
}
```

### 5. AI Analysis
```java
// TODO: Phân tích nội dung cuộc gọi
public CallAnalysis analyzeRecording(String s3Key) {
    String transcript = transcribeRecording(s3Key);
    // Analyze sentiment, keywords, intent
    return aiService.analyze(transcript);
}
```

## 🐛 Troubleshooting

### Lỗi: "Không thể truy cập microphone"
**Nguyên nhân:** Trình duyệt chặn quyền hoặc site không phải HTTPS

**Giải pháp:**
1. Đảm bảo đang truy cập qua `localhost` hoặc `HTTPS`
2. Kiểm tra Settings → Privacy → Microphone
3. Thử trình duyệt khác

### Lỗi: "Upload failed"
**Nguyên nhân:** Server không nhận được file hoặc S3 credentials sai

**Giải pháp:**
1. Kiểm tra server log
2. Verify AWS credentials trong environment variables
3. Kiểm tra S3 bucket policy

### Lỗi: "File too large"
**Nguyên nhân:** File vượt quá giới hạn upload

**Giải pháp:**
```properties
# application.properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### File không có âm thanh
**Nguyên nhân:** Microphone không hoạt động hoặc stream sai

**Giải pháp:**
1. Test microphone trước: `navigator.mediaDevices.getUserMedia({ audio: true })`
2. Kiểm tra indicator "Đang ghi âm" có xuất hiện không
3. Thử ghi âm ngắn để test

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra Browser Console (F12)
2. Kiểm tra Server Log
3. Verify S3 bucket có folder `voice/`
4. Test microphone permissions

---

**Phiên bản:** 1.0  
**Ngày cập nhật:** 28/01/2026  
**Tương thích:** Chrome 47+, Firefox 25+, Edge 79+, Safari 14.1+
