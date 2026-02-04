# Web-to-Web Call - Hướng dẫn sử dụng

## Tổng quan

Tính năng **Web-to-Web Call** cho phép 2 user đã đăng nhập gọi điện trực tiếp với nhau thông qua trình duyệt web, bao gồm:

- ✅ Gọi điện giữa 2 user đã đăng nhập
- ✅ Danh sách user đang online
- ✅ Ghi âm cuộc gọi và upload lên S3
- ✅ Xem lại file ghi âm ngay sau khi kết thúc
- ✅ Lịch sử cuộc gọi với audio player
- ✅ Đánh giá cuộc gọi (1-5 sao)
- ✅ Thống kê cuộc gọi

---

## Cấu trúc file mới

```
src/
├── main/
│   ├── java/com/g4/capstoneproject/
│   │   ├── entity/
│   │   │   └── WebCallLog.java          # Entity lưu lịch sử cuộc gọi
│   │   ├── repository/
│   │   │   └── WebCallLogRepository.java # Repository với các query
│   │   ├── service/
│   │   │   └── WebCallService.java       # Business logic
│   │   ├── dto/
│   │   │   └── WebCallDTO.java           # Data transfer object
│   │   └── controller/
│   │       └── api/
│   │           └── WebCallApiController.java # REST API endpoints
│   └── resources/
│       └── templates/
│           └── call/
│               ├── index.html            # Trang gọi điện chính
│               └── history.html          # Trang lịch sử cuộc gọi
└── docs/
    └── migrations/
        └── V20260129__create_web_call_logs.sql # Script tạo bảng DB
```

---

## Cài đặt

### 1. Chạy migration database

```sql
-- Chạy script này để tạo bảng
source docs/migrations/V20260129__create_web_call_logs.sql;
```

Hoặc Hibernate sẽ tự tạo bảng nếu `spring.jpa.hibernate.ddl-auto=update`

### 2. Kiểm tra cấu hình Stringee

Đảm bảo đã có cấu hình trong `application-local.properties`:

```properties
stringee.key.sid=SK.xxxxxxxxxxxxx
stringee.key.secret=xxxxxxxxxxxxxxxx
```

### 3. Restart ứng dụng

```bash
mvn spring-boot:run
```

---

## API Endpoints

### Authentication & Connection

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/web-call/token` | Lấy Stringee access token |
| POST | `/api/web-call/online` | Đăng ký user online |
| POST | `/api/web-call/offline` | Đăng ký user offline |
| GET | `/api/web-call/online-users` | Danh sách user đang online |

### Call Management

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/web-call/initiate` | Khởi tạo cuộc gọi |
| POST | `/api/web-call/{callId}/status` | Cập nhật trạng thái |
| POST | `/api/web-call/{callId}/recording` | Upload ghi âm |
| POST | `/api/web-call/{callId}/rate` | Đánh giá cuộc gọi |

### History & Statistics

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/web-call/history` | Lịch sử cuộc gọi (phân trang) |
| GET | `/api/web-call/recordings` | Cuộc gọi có ghi âm |
| GET | `/api/web-call/missed` | Cuộc gọi nhỡ |
| GET | `/api/web-call/{callId}` | Chi tiết cuộc gọi |
| GET | `/api/web-call/statistics` | Thống kê cuộc gọi |

---

## Các trang web

| URL | Mô tả |
|-----|-------|
| `/call` | Trang gọi điện chính |
| `/call/history` | Lịch sử cuộc gọi |

---

## Trạng thái cuộc gọi

| Status | Mô tả |
|--------|-------|
| `INITIATED` | Đã khởi tạo, đang đổ chuông |
| `RINGING` | Đang đổ chuông phía receiver |
| `ANSWERED` | Đã bắt máy, đang nói chuyện |
| `COMPLETED` | Hoàn thành (kết thúc bình thường) |
| `MISSED` | Người nhận không bắt máy |
| `REJECTED` | Người nhận từ chối |
| `CANCELLED` | Người gọi hủy |
| `FAILED` | Lỗi kỹ thuật |

---

## Cách sử dụng

### 1. Đăng nhập
User cần đăng nhập vào hệ thống trước.

### 2. Truy cập trang gọi điện
Vào `/call` hoặc click vào menu "Gọi điện".

### 3. Chờ kết nối
Hệ thống sẽ tự động kết nối Stringee và hiển thị "Đã kết nối".

### 4. Chọn người để gọi
Danh sách user đang online sẽ hiển thị bên trái. Click vào user để gọi.

### 5. Trong cuộc gọi
- **Tắt/bật mic**: Click nút mic
- **Kết thúc**: Click nút đỏ

### 6. Sau cuộc gọi
- Nghe lại ghi âm (nếu có)
- Đánh giá chất lượng (1-5 sao)
- Xem lịch sử tại `/call/history`

---

## Gợi ý tính năng mở rộng

| # | Tính năng | Mô tả | Độ khó |
|---|-----------|-------|--------|
| 1 | **Video call** | Stringee hỗ trợ video call | Medium |
| 2 | **Chat trong cuộc gọi** | Gửi tin nhắn khi đang gọi | Medium |
| 3 | **Screen sharing** | Chia sẻ màn hình | Medium |
| 4 | **AI Transcription** | Tự động chuyển ghi âm thành text | High |
| 5 | **Real-time notification** | Thông báo push khi có cuộc gọi đến | Medium |
| 6 | **Call quality analytics** | Thống kê chất lượng cuộc gọi | Low |
| 7 | **Transfer call** | Chuyển cuộc gọi cho người khác | Medium |
| 8 | **Hold/Resume** | Giữ máy và tiếp tục | Low |
| 9 | **Conference call** | Gọi nhóm nhiều người | High |
| 10 | **Schedule call** | Đặt lịch gọi | Medium |
| 11 | **Call reminder** | Nhắc nhở cuộc gọi đã lên lịch | Low |
| 12 | **Voicemail** | Để lại tin nhắn thoại | High |

---

## Troubleshooting

### Không kết nối được Stringee
- Kiểm tra `stringee.key.sid` và `stringee.key.secret`
- Đảm bảo user đã đăng nhập

### Không thấy user online
- User khác cũng cần mở trang `/call` và đợi "Đã kết nối"
- Refresh danh sách bằng nút refresh

### Không ghi âm được
- Cho phép trình duyệt truy cập microphone
- Đảm bảo checkbox "Ghi âm cuộc gọi" được tick

### File ghi âm không phát được
- Kiểm tra AWS S3 credentials
- URL pre-signed có thể hết hạn sau 7 ngày

---

## Database Schema

```sql
CREATE TABLE web_call_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stringee_call_id VARCHAR(100),
    caller_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    call_status ENUM('INITIATED', 'RINGING', 'ANSWERED', 'COMPLETED', 
                     'MISSED', 'REJECTED', 'CANCELLED', 'FAILED'),
    start_time DATETIME,
    end_time DATETIME,
    duration INT DEFAULT 0,
    recording_s3_key VARCHAR(500),
    recording_url VARCHAR(1000),
    recording_url_expiry DATETIME,
    has_recording BOOLEAN DEFAULT FALSE,
    transcript_text TEXT,
    rating TINYINT,
    notes VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (caller_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

---

## Cập nhật lần cuối
- Ngày: 29/01/2026
- Phiên bản: 1.0
