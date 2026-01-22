# 🌐 Hướng Dẫn Test Stringee Web-to-Web Call

## 📋 Tổng Quan

Hệ thống đã được cấu hình hoàn chỉnh để test cuộc gọi Web-to-Web sử dụng Stringee với tự động lấy Access Token từ Backend.

## ✅ Các Thành Phần Đã Cấu Hình

### 1. Backend - StringeeService.java
- ✅ Hàm `getClientAccessToken(String userId)` - Tạo JWT token cho client
- ✅ Token hợp lệ trong 1 giờ (3600 giây)
- ✅ Token chứa `userId` để định danh người dùng
- ✅ **Không** có claim `rest_api` (dành riêng cho client, không phải server admin)

### 2. Backend - StringeeController.java
- ✅ Endpoint `GET /api/stringee/access-token?userId={userId}`
- ✅ Trả về JSON: `{"userId": "...", "token": "..."}`
- ✅ Xử lý lỗi và logging đầy đủ

### 3. Frontend - test-auto-connect.html
- ✅ Tự động random User ID khi load trang
- ✅ Tự động gọi API `/api/stringee/access-token` để lấy token
- ✅ Tự động kết nối tới Stringee Server
- ✅ UI đẹp với trạng thái màu sắc rõ ràng
- ✅ Hỗ trợ gọi điện và nhận cuộc gọi
- ✅ Hiển thị trạng thái cuộc gọi real-time

## 🚀 Cách Test Web-to-Web Call

### Bước 1: Khởi động Backend
```bash
# Chạy Spring Boot Application
# Đảm bảo các biến môi trường Stringee đã được set:
# - STRINGEE_KEY_SID
# - STRINGEE_KEY_SECRET
```

### Bước 2: Mở Tab User 1
1. Truy cập: `http://localhost:8080/test-auto-connect.html`
2. Hệ thống tự động tạo User ID (ví dụ: `user_1234`)
3. Nhấn nút **"Kết nối ngay"**
4. Đợi trạng thái chuyển sang ✅ **"Đã kết nối thành công!"**
5. Lưu lại User ID hiển thị (cần dùng cho bước sau)

### Bước 3: Mở Tab User 2 (Incognito/Private)
1. Mở tab ẩn danh: `http://localhost:8080/test-auto-connect.html`
2. Hệ thống tự động tạo User ID khác (ví dụ: `user_5678`)
3. Nhấn nút **"Kết nối ngay"**
4. Đợi trạng thái chuyển sang ✅ **"Đã kết nối thành công!"**

### Bước 4: Thực Hiện Cuộc Gọi
**Từ Tab User 1:**
1. Trong ô **"Gọi tới User ID"**, nhập ID của User 2 (ví dụ: `user_5678`)
2. Nhấn nút **"Gọi điện"**
3. Cho phép trình duyệt truy cập microphone khi được hỏi

**Từ Tab User 2:**
1. Popup sẽ hiện: "Cuộc gọi từ user_1234. Chấp nhận?"
2. Nhấn **OK** để chấp nhận cuộc gọi
3. Cho phép trình duyệt truy cập microphone

### Bước 5: Kiểm Tra Kết Quả
- ✅ Cả 2 tab đều hiển thị thông tin cuộc gọi
- ✅ Trạng thái cuộc gọi cập nhật real-time
- ✅ Audio hoạt động 2 chiều (nếu có microphone)
- ✅ Có thể dập máy từ bất kỳ bên nào

## 🎯 Flow Hoạt Động

```
┌─────────────┐          ┌──────────────┐          ┌─────────────┐
│   Browser   │          │    Backend   │          │   Stringee  │
│   (User 1)  │          │ Spring Boot  │          │   Server    │
└──────┬──────┘          └──────┬───────┘          └──────┬──────┘
       │                        │                         │
       │ 1. GET /access-token   │                         │
       │   ?userId=user_1234    │                         │
       ├───────────────────────>│                         │
       │                        │                         │
       │ 2. Create JWT Token    │                         │
       │    {userId: user_1234} │                         │
       │                        │                         │
       │ 3. Return Token        │                         │
       │<───────────────────────┤                         │
       │                        │                         │
       │ 4. connect(token)      │                         │
       ├────────────────────────┼────────────────────────>│
       │                        │                         │
       │ 5. authen success      │                         │
       │<────────────────────────┼─────────────────────────┤
       │                        │                         │
       │ 6. makeCall(user_5678) │                         │
       ├────────────────────────┼────────────────────────>│
       │                        │                         │
```

## 🔍 Debug & Troubleshooting

### Kiểm tra Console Browser
Mở Developer Tools (F12) và xem tab Console:
```javascript
✅ Đã lấy được token: eyJhbGc...
📡 Connected to Stringee Server
🔐 Authen response: {r: 0, userId: "user_1234"}
📞 Making call to: user_5678
```

### Kiểm tra Backend Logs
```
DEBUG c.g.c.service.StringeeService : Stringee Client Access Token created successfully for userId: user_1234
INFO  c.g.c.controller.StringeeController : Returning token for userId: user_1234
```

### Lỗi Thường Gặp

#### 1. "Không lấy được token"
- ✅ Kiểm tra Backend có chạy không (http://localhost:8080)
- ✅ Kiểm tra biến môi trường `STRINGEE_KEY_SID` và `STRINGEE_KEY_SECRET`
- ✅ Xem logs Backend có lỗi không

#### 2. "Lỗi xác thực"
- ✅ Kiểm tra token có hợp lệ không (xem logs)
- ✅ Kiểm tra `STRINGEE_KEY_SID` và `STRINGEE_KEY_SECRET` có đúng không
- ✅ Token có thể đã hết hạn (1 giờ), thử kết nối lại

#### 3. "Không gọi được"
- ✅ Đảm bảo 2 user đã kết nối thành công (trạng thái màu xanh)
- ✅ User ID phải khớp chính xác (case-sensitive)
- ✅ Cho phép browser truy cập microphone

#### 4. "Không nghe thấy âm thanh"
- ✅ Kiểm tra microphone/speaker có hoạt động không
- ✅ Kiểm tra browser đã cho phép quyền truy cập microphone
- ✅ Thử refresh và kết nối lại

## 📱 Test Scenarios

### Scenario 1: Basic Call Flow
1. User A kết nối ✅
2. User B kết nối ✅
3. User A gọi User B
4. User B chấp nhận
5. Cuộc gọi thành công 🎉
6. User A hoặc B dập máy

### Scenario 2: Reject Call
1. User A kết nối ✅
2. User B kết nối ✅
3. User A gọi User B
4. User B từ chối (Cancel)
5. Cuộc gọi kết thúc

### Scenario 3: Multiple Users
1. User A kết nối ✅
2. User B kết nối ✅
3. User C kết nối ✅ (mở tab thứ 3)
4. A có thể gọi B
5. B có thể gọi C
6. C có thể gọi A
7. (Không hỗ trợ conference call - chỉ 1-1)

## 🎨 UI Features

### Màu Sắc Trạng Thái
- 🟠 **Vàng**: Chưa kết nối / Đang xử lý
- 🟢 **Xanh**: Đã kết nối thành công
- 🔴 **Đỏ**: Lỗi / Mất kết nối
- 🔵 **Xanh dương**: Đang xử lý

### Thông Tin Hiển Thị
- User ID hiện tại
- Trạng thái kết nối
- Token status
- Call ID và trạng thái cuộc gọi

## 🔒 Security Notes

**⚠️ QUAN TRỌNG**: Cấu hình hiện tại chỉ dành cho môi trường TEST!

### Production Checklist:
- [ ] Bật lại Spring Security
- [ ] Xác thực người dùng trước khi cấp token
- [ ] Giới hạn rate limit cho endpoint `/access-token`
- [ ] Lưu log cuộc gọi vào database
- [ ] Kiểm tra quyền truy cập (authorization)
- [ ] Sử dụng HTTPS
- [ ] Bảo vệ Stringee credentials

## 📚 API Reference

### GET /api/stringee/access-token

**Query Parameters:**
- `userId` (required): ID của người dùng cần kết nối

**Response Success (200):**
```json
{
  "userId": "user_1234",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6InN0cmluZ2VlLWFwaTt2PTEiLCJraWQiOiJTSy4wLjRVUlMxQnpt..."
}
```

**Response Error (500):**
```json
{
  "error": "Error creating Client Access Token"
}
```

## 🎓 Next Steps

Sau khi test thành công Web-to-Web Call, bạn có thể:

1. **Tích hợp vào UI chính**: Di chuyển logic từ file test vào ứng dụng chính
2. **Thêm Video Call**: Sửa `makeCall(..., true)` để bật video
3. **Lưu lịch sử cuộc gọi**: Kết nối với database để lưu call logs
4. **Tích hợp AI**: Thêm speech-to-text và AI processing
5. **Thêm features**: Screen sharing, call recording, group call

## 📞 Support

Nếu gặp vấn đề, kiểm tra:
1. Backend logs
2. Browser console (F12)
3. Network tab để xem API requests
4. Stringee Dashboard để kiểm tra usage

---
**Last Updated**: January 22, 2026
**Version**: 1.0.0
