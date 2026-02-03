# Hướng dẫn Lưu Recording Cuộc gọi vào S3

## 📋 Tổng quan

Hệ thống đã được cấu hình để **tự động lưu file ghi âm cuộc gọi** từ Stringee vào AWS S3 bucket trong folder `voice/`.

## 🔄 Luồng hoạt động

```
1. User gọi API /api/stringee/call/outbound
   ↓
2. Backend tạo cuộc gọi với Stringee (bật record=true)
   ↓
3. Stringee thực hiện cuộc gọi và ghi âm tự động
   ↓
4. Khi cuộc gọi kết thúc, Stringee gửi event "recording.available" 
   ↓
5. Backend nhận webhook tại /api/stringee/event
   ↓
6. handleRecordingAvailable() được gọi
   ↓
7. S3Service.uploadFileFromUrl() download file từ Stringee
   ↓
8. File được upload lên S3 vào folder: voice/
   ↓
9. Tạo pre-signed URL (hiệu lực 7 ngày) để truy cập file
```

## 📁 Cấu trúc file trong S3

```
s3://capstone-project-files-2026/
├── voice/
│   ├── 20260128_143025_call_abc123.mp3
│   ├── 20260128_143145_call_xyz789.mp3
│   └── ...
├── image/
└── text/
```

**Format tên file:** `voice/yyyyMMdd_HHmmss_callId.mp3`

Ví dụ: `voice/20260128_143025_call_abc123.mp3`

## 🔧 Cấu hình đã thực hiện

### 1. S3Service.java
Đã thêm method mới:

```java
public String uploadFileFromUrl(String fileUrl, String callId, String contentType)
```

**Chức năng:**
- Download file từ URL của Stringee
- Tự động đặt tên file với timestamp và callId
- Upload lên S3 vào folder `voice/`
- Hỗ trợ các định dạng: `.mp3`, `.wav`, `.m4a`, `.ogg`

### 2. StringeeService.java
Đã bật ghi âm tự động:

```java
requestBody.put("record", true);  // ✅ Tự động ghi âm mọi cuộc gọi
```

### 3. StringeeController.java
Đã implement `handleRecordingAvailable()`:

```java
private void handleRecordingAvailable(Map<String, Object> event) {
    String callId = (String) event.get("callId");
    String recordingUrl = (String) event.get("recordingUrl");
    
    // Tự động download và upload lên S3
    String s3Key = s3Service.uploadFileFromUrl(recordingUrl, callId, "audio/mpeg");
    
    // Tạo pre-signed URL (hiệu lực 7 ngày)
    String presignedUrl = s3Service.generatePresignedUrl(s3Key, 7 * 24 * 3600);
    
    logger.info("✅ Đã lưu file ghi âm vào S3: {}", s3Key);
}
```

## 🧪 Cách test

### 1. Thực hiện cuộc gọi
```bash
POST http://localhost:8080/api/stringee/call/outbound
Content-Type: application/json

{
  "fromNumber": "842873008xxx",
  "toNumber": "84987654321",
  "brandName": "Phòng Khám ABC"
}
```

### 2. Kiểm tra log
Sau khi cuộc gọi kết thúc, bạn sẽ thấy log:

```
Recording available for call call_xxx: https://...
✅ Đã lưu file ghi âm vào S3: voice/20260128_143025_call_xxx.mp3
Pre-signed URL: https://capstone-project-files-2026.s3...
```

### 3. Kiểm tra S3 bucket
Truy cập AWS Console → S3 → `capstone-project-files-2026` → folder `voice/`

Bạn sẽ thấy file ghi âm đã được upload.

## 📝 Webhook Configuration

### Stringee Webhook Settings
Để nhận event `recording.available`, bạn cần cấu hình webhook trong Stringee Dashboard:

**Event Webhook URL:**
```
https://your-domain.com/api/stringee/event
```

**Hoặc dùng ngrok cho development:**
```
https://your-ngrok-url.ngrok.io/api/stringee/event
```

### Các event được xử lý:
- ✅ `call.started` - Cuộc gọi bắt đầu
- ✅ `call.answered` - Khách hàng bắt máy
- ✅ `call.ended` - Cuộc gọi kết thúc
- ✅ `recording.available` - **File ghi âm sẵn sàng** (tự động lưu vào S3)

## 🔐 Pre-signed URL

File ghi âm được bảo vệ bởi pre-signed URL với thời hạn **7 ngày**.

Sau 7 ngày, URL sẽ hết hiệu lực nhưng file vẫn tồn tại trong S3.

**Để lấy URL mới:**
```java
String newUrl = s3Service.generatePresignedUrl("voice/20260128_143025_call_xxx.mp3", 7 * 24 * 3600);
```

## 🚀 Tính năng mở rộng (TODO)

### 1. Lưu metadata vào database
```java
// TODO: Lưu thông tin vào database
CallRecording recording = new CallRecording();
recording.setCallId(callId);
recording.setS3Key(s3Key);
recording.setPresignedUrl(presignedUrl);
recording.setCreatedAt(LocalDateTime.now());
callRecordingRepository.save(recording);
```

### 2. Gửi file cho AI phân tích
```java
// TODO: Gửi file cho AI phân tích nội dung cuộc gọi
aiService.transcribeAndAnalyze(s3Key);
```

### 3. Thông báo cho user
```java
// TODO: Thông báo cho user rằng file ghi âm đã sẵn sàng
notificationService.sendRecordingReady(userId, presignedUrl);
```

## ⚠️ Lưu ý

1. **Ngrok cho development:** Nếu đang test local, nhớ cập nhật `STRINGEE_WEBHOOK_DOMAIN` với ngrok URL của bạn.

2. **S3 Bucket Policy:** Đảm bảo bucket có đúng quyền:
   - ✅ PutObject cho backend
   - ✅ GetObject cho pre-signed URL

3. **Dung lượng:** File ghi âm có thể lớn (1-10 MB/cuộc gọi). Cân nhắc:
   - Lifecycle policy để tự động xóa file cũ sau 90 ngày
   - Chuyển sang S3 Glacier cho lưu trữ lâu dài

4. **Error Handling:** Nếu download từ Stringee thất bại, kiểm tra:
   - Stringee recording URL có đúng không
   - Network connection từ backend tới Stringee
   - AWS credentials có đủ quyền upload không

## 📞 Support

Nếu gặp vấn đề, kiểm tra log tại:
- `StringeeController` - Webhook events
- `S3Service` - Upload status

Hoặc liên hệ team để được hỗ trợ.
