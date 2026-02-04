# Hướng dẫn sử dụng chức năng phát lại ghi âm

## Tổng quan
Tính năng này cho phép người dùng:
- Xem danh sách tất cả các file ghi âm đã lưu trong S3
- Nghe lại file ghi âm trực tiếp trên trình duyệt
- Tải xuống file ghi âm về máy

## Các API Endpoints

### 1. Lấy danh sách recordings
```
GET /api/stringee/recordings
```

**Response:**
```json
{
  "success": true,
  "recordings": [
    {
      "key": "voice/20260130_143022_user1_web_1738226822123.webm",
      "filename": "20260130_143022_user1_web_1738226822123.webm",
      "size": 245632,
      "lastModified": "2026-01-30T07:30:22Z",
      "url": "https://s3.amazonaws.com/bucket/voice/file.webm?presigned-params"
    }
  ],
  "count": 1
}
```

## Luồng hoạt động

### 1. Khi đăng nhập thành công
- Danh sách recordings tự động được load
- Section "📼 Danh sách file ghi âm đã lưu" xuất hiện

### 2. Sau khi upload file mới
- Danh sách tự động refresh sau 1 giây
- File mới nhất xuất hiện ở đầu danh sách

### 3. Nghe file ghi âm
- Bấm nút "▶️ Nghe"
- Modal player hiện lên với audio player HTML5
- File tự động phát
- Bấm "Đóng" hoặc click backdrop để đóng

### 4. Tải file xuống
- Bấm nút "⬇️ Tải"
- File tự động download về máy

## Tính năng UI

### Hiển thị thông tin
Mỗi recording hiển thị:
- 📁 Tên file
- 📅 Ngày giờ tạo (định dạng địa phương)
- 💾 Kích thước file (KB)

### Nút điều khiển
- **Nghe**: Mở audio player modal
- **Tải**: Download file về máy
- **🔄 Làm mới**: Reload danh sách từ server

### Audio Player Modal
- Autoplay khi mở
- Controls đầy đủ (play/pause, seek, volume)
- Responsive design
- Click backdrop để đóng

## Backend Implementation

### S3Service.listRecordings()
```java
public List<Map<String, Object>> listRecordings() {
    // List objects trong folder "voice/"
    // Tạo presigned URL cho mỗi file (7 ngày)
    // Sắp xếp theo thời gian mới nhất
    return recordings;
}
```

### StringeeController.getRecordings()
```java
@GetMapping("/recordings")
public ResponseEntity<?> getRecordings() {
    List<Map<String, Object>> recordings = s3Service.listRecordings();
    return ResponseEntity.ok(Map.of(
        "success", true,
        "recordings", recordings,
        "count", recordings.size()
    ));
}
```

## Logging

### Frontend
- ✅ Tải được X file ghi âm
- ▶️ Đang phát: filename
- ⬇️ Đang tải: filename
- ❌ Lỗi nếu có

### Backend
- Đang lấy danh sách recordings từ S3
- ✅ Lấy được X recordings

## Testing

### Test 1: Load danh sách
1. Đăng nhập vào web-call
2. Kiểm tra section recordings xuất hiện
3. Verify danh sách hiển thị đúng

### Test 2: Play recording
1. Bấm nút "▶️ Nghe"
2. Verify modal hiện lên
3. Verify audio tự động phát
4. Test controls (play/pause, seek)

### Test 3: Download
1. Bấm nút "⬇️ Tải"
2. Verify file download về máy
3. Kiểm tra file có thể phát được

### Test 4: Auto refresh
1. Thực hiện cuộc gọi có ghi âm
2. Kết thúc cuộc gọi
3. Verify file mới xuất hiện sau 1 giây

## Notes

- Presigned URL có hiệu lực 7 ngày
- Files được sắp xếp theo thời gian mới nhất
- Chỉ hiển thị files trong folder "voice/"
- Audio format: WebM với Opus codec
