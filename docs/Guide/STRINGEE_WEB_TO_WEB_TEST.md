# 🌐 Hướng Dẫn Test Stringee Web-to-Web Call

## 📋 Tổng Quan

Hệ thống cung cấp trang web call để test cuộc gọi Web-to-Web sử dụng Stringee với tự động lấy Access Token từ Backend.

## ✅ Các Thành Phần Đã Cấu Hình

### 1. Backend - StringeeService.java
- ✅ Hàm `getClientAccessToken(String userId)` - Tạo JWT token cho client
- ✅ Token hợp lệ trong 1 giờ (3600 giây)
- ✅ Token chứa:
  - `userId` - Định danh người dùng
  - `jti` - JWT ID unique
  - `iss` - Issuer (keySid)
  - `exp` - Expiration time
- ✅ **Không** có claim `rest_api` (dành riêng cho client, không phải server admin)

### 2. Backend - StringeeController.java
- ✅ Endpoint `GET /api/stringee/access-token?userId={userId}`
- ✅ Trả về JSON: `{"userId": "...", "token": "..."}`
- ✅ Xử lý lỗi và logging đầy đủ

### 3. Frontend - /ai/web-call
- ✅ Nhập User ID thủ công để kết nối
- ✅ Tự động gọi API `/api/stringee/access-token` để lấy token
- ✅ Kết nối tới Stringee Server với server addresses tối ưu
- ✅ UI đẹp với log console chi tiết
- ✅ Hỗ trợ gọi điện và nhận cuộc gọi voice
- ✅ Hiển thị trạng thái cuộc gọi real-time

## 🚀 Cách Test Web-to-Web Call

### Bước 1: Khởi động Backend
```bash
# Chạy Spring Boot Application
# Đảm bảo các biến môi trường Stringee đã được set:
# - STRINGEE_KEY_SID=SK.0.4URS1BzmRmAs6WF3jtJgnIyfSSS1NnaR
# - STRINGEE_KEY_SECRET=OWVFV294WER4MlcwQkdQVFJqQ2tHeWJ6THpGU0QyMg==
```

### Bước 2: Mở Tab User 1
1. Truy cập: `http://localhost:8080/ai/web-call`
2. Nhập User ID (ví dụ: `user1` hoặc `test`)
3. Nhấn nút **"Kết nối"**
4. Đợi trạng thái chuyển sang 🟢 **"Đã kết nối"**
5. Kiểm tra log console hiển thị: ✅ Xác thực thành công!

### Bước 3: Mở Tab User 2 (Incognito/Private)
1. Mở tab ẩn danh: `http://localhost:8080/ai/web-call`
2. Nhập User ID khác (ví dụ: `user2` hoặc `test2`)
3. Nhấn nút **"Kết nối"**
4. Đợi trạng thái chuyển sang 🟢 **"Đã kết nối"**

### Bước 4: Thực Hiện Cuộc Gọi
**Từ Tab User 1:**
1. Trong ô **"Gọi tới User ID"**, nhập ID của User 2 (ví dụ: `user2` hoặc `test2`)
2. Nhấn nút **"📞 Gọi Voice"**
3. Cho phép trình duyệt truy cập microphone khi được hỏi
4. Xem log console hiển thị: "Đang gọi tới: user2"

**Từ Tab User 2:**
1. Trang sẽ tự động hiển thị: "📞 Đang có cuộc gọi đến từ: user1"
2. Nhấn nút **"✅ Trả lời"** màu xanh
3. Cho phép trình duyệt truy cập microphone

### Bước 5: Kiểm Tra Kết Quả
- ✅ Cả 2 tab đều hiển thị trạng thái "In call"
- ✅ Trạng thái cuộc gọi cập nhật real-time trong log console
- ✅ Audio hoạt động 2 chiều (nếu có microphone)
- ✅ Có thể nhấn **"❌ Kết thúc"** từ bất kỳ bên nào

## 🎯 Flow Hoạt Động

```
┌─────────────┐          ┌──────────────┐          ┌─────────────┐
│   Browser   │          │    Backend   │          │   Stringee  │
│   (User 1)  │          │ Spring Boot  │          │   Server    │
└──────┬──────┘          └──────┬───────┘          └──────┬──────┘
       │                        │                         │
       │ 1. GET /access-token   │                         │
       │   ?userId=user1        │                         │
       ├───────────────────────>│                         │
       │                        │                         │
       │ 2. Create JWT Token    │                         │
       │    with iss, jti, exp  │                         │
       │    {userId: "user1"}   │                         │
       │                        │                         │
       │ 3. Return Token        │                         │
       │<───────────────────────┤                         │
       │                        │                         │
       │ 4. connect(token)      │                         │
       │    with server addrs   │                         │
       ├────────────────────────┼────────────────────────>│
       │                        │                         │
       │ 5. authen success      │                         │
       │    {r: 0, userId}      │                         │
       │<────────────────────────┼─────────────────────────┤
       │                        │                         │
       │ 6. makeCall("user2")   │                         │
       ├────────────────────────┼────────────────────────>│
       │                        │                         │
```

## 🔍 Debug & Troubleshooting

### Kiểm tra Console Browser
Mở Developer Tools (F12) và xem tab Console:
```javascript
[10:30:45] Đang kết nối với user: user1
[10:30:45] Đang lấy access token từ server...
[10:30:45] ✅ Đã nhận được access token từ server
[10:30:45] Đã khởi tạo Stringee Client với server addresses
[10:30:46] Đã kết nối tới Stringee Server
[10:30:46] ✅ Xác thực thành công! User ID: user1
[10:30:46] Bạn có thể thực hiện cuộc gọi ngay bây giờ
[10:31:20] Đang gọi tới: user2
[10:31:20] Đang thực hiện cuộc gọi...
[10:31:22] Đã nhận luồng âm thanh
[10:31:22] Trạng thái: answered
```

### Kiểm tra Backend Logs
```
INFO  c.g.c.controller.StringeeController : Requesting access token for userId: user1
INFO  c.g.c.service.StringeeService : Stringee Client Access Token created successfully for userId: user1 (jti: SK.0.xxx-1234567890)
INFO  c.g.c.controller.StringeeController : Successfully created token for userId: user1 (length: 250)
```

### Lỗi Thường Gặp

#### 1. "Lỗi lấy token từ server"
**Thông báo lỗi:**
```
❌ Lỗi lấy token: HTTP error! status: 500 - Internal Server Error
```

**Kiểm tra:**
- ✅ Backend có đang chạy không (`http://localhost:8080`)
- ✅ Kiểm tra biến môi trường `STRINGEE_KEY_SID` và `STRINGEE_KEY_SECRET` đã được set
- ✅ Xem logs Backend có lỗi không

**Giải pháp:**
```bash
# Set lại environment variables
${env:STRINGEE_KEY_SID}='SK.0.4URS1BzmRmAs6WF3jtJgnIyfSSS1NnaR'
${env:STRINGEE_KEY_SECRET}='OWVFV294WER4MlcwQkdQVFJqQ2tHeWJ6THpGU0QyMg=='
```

#### 2. "ACCESS_TOKEN_INCORRECT_PAYLOAD" (mã lỗi r: 7)
**Nguyên nhân:** Token không đúng định dạng cho Client

**Kiểm tra:**
- ✅ Hàm `getClientAccessToken()` phải có `iss` (issuer)
- ✅ Hàm phải có `jti` (JWT ID)
- ✅ **KHÔNG ĐƯỢC** có claim `rest_api: true`

**Giải pháp:**
Đảm bảo [StringeeService.java](../src/main/java/com/g4/capstoneproject/service/StringeeService.java#L88) có code:
```java
.withIssuer(keySid)
.withJWTId(jti)
.withClaim("userId", userId)
// KHÔNG có .withClaim("rest_api", true)
```

#### 3. "Không gọi được" / "Call failed"
**Kiểm tra:**
- ✅ Đảm bảo 2 user đã kết nối thành công (trạng thái 🟢 "Đã kết nối")
- ✅ User ID phải khớp chính xác (case-sensitive: `user1` ≠ `User1`)
- ✅ Cho phép browser truy cập microphone khi được hỏi
- ✅ Kiểm tra trong log console có lỗi gì không

#### 4. "Không nghe thấy âm thanh"
**Kiểm tra:**
- ✅ Microphone/speaker có hoạt động không
- ✅ Browser đã cho phép quyền truy cập microphone (kiểm tra icon khóa trên thanh địa chỉ)
- ✅ Volume không bị mute
- ✅ Thử dùng headphone để tránh echo

**Giải pháp:**
- F5 refresh trang và kết nối lại
- Kiểm tra Settings > Microphone trong browser

## 📱 Test Scenarios

### Scenario 1: Basic Call Flow ✅
1. User A (user1) kết nối ✅
2. User B (user2) kết nối ✅
3. User A nhập "user2" và nhấn **📞 Gọi Voice**
4. User B thấy thông báo cuộc gọi, nhấn **✅ Trả lời**
5. Cuộc gọi thành công, 2 bên có thể nói chuyện 🎉
6. Bất kỳ bên nào nhấn **❌ Kết thúc** để dập máy

### Scenario 2: Reject Call 🚫
1. User A kết nối ✅
2. User B kết nối ✅
3. User A gọi User B
4. User B **KHÔNG** nhấn Trả lời (hoặc đóng tab)
5. Sau vài giây, cuộc gọi tự động kết thúc (timeout)

### Scenario 3: Multiple Users 👥
1. User A (user1) kết nối ✅
2. User B (user2) kết nối ✅
3. User C (user3) kết nối ✅ (mở tab incognito thứ 3)
4. A có thể gọi B
5. B có thể gọi C
6. C có thể gọi A
7. **Lưu ý:** Hệ thống chỉ hỗ trợ cuộc gọi 1-1 (không có conference call)

### Scenario 4: Reconnection 🔄
1. User A kết nối, sau đó đóng tab
2. Mở lại tab mới, truy cập `/ai/web-call`
3. Nhập lại User ID (ví dụ: user1)
4. Kết nối lại thành công ✅
5. Có thể tiếp tục thực hiện cuộc gọi

## 🎨 UI Features

### 📊 Log Console
Hiển thị thời gian thực:
- 🔵 **Info (màu xanh)**: Thông tin thông thường
- 🟢 **Success (màu xanh lá)**: Thành công
- 🔴 **Error (màu đỏ)**: Lỗi

### 🎨 Màu Sắc Trạng Thái
- 🟠 **Cam**: Đang kết nối...
- 🟢 **Xanh lá**: Đã kết nối
- 🔴 **Đỏ**: Đã ngắt kết nối / Lỗi

### 🎛️ Buttons
- **Kết nối** (giai đoạn login)
- **📞 Gọi Voice** (màu xanh dương) - Bắt đầu cuộc gọi
- **✅ Trả lời** (màu xanh lá) - Chấp nhận cuộc gọi đến
- **❌ Kết thúc** (màu đỏ) - Dập máy

### 📋 Thông Tin Hiển Thị
- User ID hiện tại
- Trạng thái kết nối real-time
- Log console với timestamp
- Trạng thái cuộc gọi

## 🔧 Technical Details

### Stringee SDK
- **Version**: latest.sdk.bundle.min.js
- **Path**: `/js/latest.sdk.bundle.min.js`
- **Type**: Web SDK for browser

### Server Addresses (Best Practice)
```javascript
const STRINGEE_SERVER_ADDRS = [
    "wss://v1.stringee.com:6899/", 
    "wss://v2.stringee.com:6899/"
];
```
Sử dụng 2 server addresses giúp:
- Failover tự động nếu server 1 gặp sự cố
- Tốc độ kết nối nhanh hơn
- Độ ổn định cao hơn

### Token Structure
```json
{
  "header": {
    "cty": "stringee-api;v=1",
    "typ": "JWT",
    "alg": "HS256",
    "kid": "SK.0.xxx"
  },
  "payload": {
    "jti": "SK.0.xxx-1737531234567",
    "iss": "SK.0.xxx",
    "exp": 1737534834,
    "userId": "user1"
  }
}
```

**Claims quan trọng:**
- `jti` (JWT ID): Unique identifier
- `iss` (Issuer): Stringee Key SID
- `exp` (Expiration): Thời điểm hết hạn (Unix timestamp)
- `userId`: Định danh người dùng
- **KHÔNG có** `rest_api: true` (dành cho Server Token)

## 🔒 Security Notes

**⚠️ QUAN TRỌNG**: Cấu hình hiện tại chỉ dành cho môi trường **TEST/DEVELOPMENT**!

### Development Setup:
- ✅ Token được tạo động từ backend
- ✅ Không hardcode token trong frontend
- ✅ Token hết hạn sau 1 giờ

### Production Checklist:
- [ ] Bật lại Spring Security authentication
- [ ] Xác thực người dùng trước khi cấp token (JWT/Session)
- [ ] Giới hạn rate limit cho endpoint `/access-token`
- [ ] Lưu log cuộc gọi vào database
- [ ] Kiểm tra quyền truy cập (authorization)
- [ ] Sử dụng HTTPS/WSS
- [ ] Bảo vệ Stringee credentials (không commit vào Git)
- [ ] Sử dụng environment variables hoặc secret manager
- [ ] Implement CORS policy phù hợp
- [ ] Monitoring và alerting cho cuộc gọi

## 📚 API Reference

### GET /api/stringee/access-token

**Mô tả:** Tạo và trả về Client Access Token để kết nối Stringee

**Query Parameters:**
- `userId` (required): ID của người dùng cần kết nối (ví dụ: `user1`, `test`)


**Request Example:**
```bash
GET http://localhost:8080/api/stringee/access-token?userId=user1
```

**Response Success (200):**
```json
{
  "userId": "user1",
  "token": "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYiLCJraWQiOiJTSy4wLjRVUlMxQnpt..."
}
```

**Response Error (500):**
```json
{
  "error": "Error creating Client Access Token"
}
```

**Curl Example:**
```bash
curl "http://localhost:8080/api/stringee/access-token?userId=user1"
```

## 🔗 Related Files

### Backend
- [StringeeService.java](../src/main/java/com/g4/capstoneproject/service/StringeeService.java) - Service tạo JWT token
- [StringeeController.java](../src/main/java/com/g4/capstoneproject/controller/StringeeController.java) - REST API endpoints
- [PageController.java](../src/main/java/com/g4/capstoneproject/controller/PageController.java) - Mapping `/ai/web-call`

### Frontend
- [web-call.html](../src/main/resources/templates/ai/web-call.html) - Web call interface
- [latest.sdk.bundle.min.js](../src/main/resources/static/js/latest.sdk.bundle.min.js) - Stringee SDK

### Configuration
- [application.properties](../src/main/resources/application.properties) - Spring config
- [application-local.properties](../src/main/resources/application-local.properties) - Local environment config

## 🎓 Next Steps

Sau khi test thành công Web-to-Web Call, bạn có thể:

1. **Tích hợp vào UI chính**: 
   - Embed web-call vào dashboard
   - Thêm vào profile page
   - Tích hợp với user management

2. **Thêm Video Call**: 
   ```javascript
   // Sửa isVideoCall = true
   currentCall = new StringeeCall(stringeeClient, callerId, calleeId, true);
   ```

3. **Lưu lịch sử cuộc gọi**: 
   - Tạo entity CallLog
   - Lưu thông tin: caller, callee, duration, timestamp
   - API để xem lịch sử

4. **Tích hợp AI**: 
   - Speech-to-text để ghi chép cuộc gọi
   - AI assistant trong cuộc gọi
   - Sentiment analysis

5. **Thêm features nâng cao**:
   - Screen sharing
   - Call recording
   - Call transfer
   - Group call / Conference
   - Call analytics & reporting

## 📝 Best Practices

### Frontend
- ✅ Luôn kiểm tra kết nối trước khi gọi
- ✅ Xử lý tất cả các event callbacks
- ✅ Hiển thị trạng thái rõ ràng cho user
- ✅ Cleanup resources khi kết thúc cuộc gọi
- ✅ Xử lý lỗi gracefully

### Backend
- ✅ Validate userId trước khi tạo token
- ✅ Sử dụng logging để debug
- ✅ Implement rate limiting
- ✅ Cache token nếu cần (cẩn thận với expiry)
- ✅ Không expose credentials trong response

## 📞 Support & Troubleshooting

### Nếu gặp vấn đề:

1. **Kiểm tra Backend logs:**
   ```bash
   # Xem trong terminal hoặc console
   INFO  c.g.c.controller.StringeeController
   ```

2. **Kiểm tra Browser console (F12):**
   - Tab Console: Xem errors và logs
   - Tab Network: Kiểm tra API calls
   - Tab Application: Xem permissions

3. **Kiểm tra Stringee Dashboard:**
   - Login vào [Stringee Dashboard](https://developer.stringee.com)
   - Xem usage, call logs
   - Kiểm tra credits còn lại

4. **Common Issues:**
   - Port 8080 đã được sử dụng → Đổi port
   - Token hết hạn → Kết nối lại
   - Microphone không hoạt động → Kiểm tra permissions
   - CORS error → Kiểm tra Spring Security config

### Resources
- [Stringee Documentation](https://developer.stringee.com/docs)
- [Stringee API Reference](https://developer.stringee.com/docs/api)
- [Web SDK Guide](https://developer.stringee.com/docs/web-sdk)

---
**Last Updated**: January 22, 2026  
**Version**: 2.0.0  
**Author**: G4 Team
