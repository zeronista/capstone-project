# Tính năng tự động nhận diện tài khoản đăng nhập

## Tổng quan
Tính năng này cho phép màn hình Web Call tự động nhận diện tài khoản đang đăng nhập trong hệ thống và kết nối Stringee mà không cần nhập thủ công User ID.

## Luồng hoạt động

### 1. Khi truy cập trang web-call.html
```
1. Trang tự động gọi loadCurrentUser()
2. Gửi request GET đến /api/web-call/current-user
3. Backend kiểm tra session/authentication
4. Trả về thông tin user hoặc trạng thái chưa đăng nhập
```

### 2. Nếu đã đăng nhập
```
1. Lưu thông tin user vào biến currentUser
2. Hiển thị "Chào {fullName}! Đang kết nối..."
3. Tự động gọi connectStringee()
4. Lấy token từ /api/web-call/token (không cần userId parameter)
5. Kết nối Stringee với stringeeUserId = "user_{userId}"
6. Hiển thị màn hình cuộc gọi với tên đầy đủ
```

### 3. Nếu chưa đăng nhập
```
1. Hiển thị thông báo "⚠️ Chưa đăng nhập"
2. Hiển thị nút "Đăng nhập ngay" redirect đến /login
3. Không cho phép sử dụng tính năng cuộc gọi
```

## API Endpoints

### 1. GET /api/web-call/current-user
Lấy thông tin user hiện đang đăng nhập

**Request:** Không cần parameters (sử dụng session)

**Response (Đã đăng nhập):**
```json
{
  "authenticated": true,
  "userId": 123,
  "stringeeUserId": "user_123",
  "email": "doctor@example.com",
  "fullName": "Dr. Nguyễn Văn A",
  "role": "DOCTOR"
}
```

**Response (Chưa đăng nhập):**
```json
{
  "authenticated": false
}
```

### 2. GET /api/web-call/token
Lấy Stringee access token cho user hiện tại

**Request:** Không cần parameters

**Response:**
```json
{
  "userId": 123,
  "stringeeUserId": "user_123",
  "fullName": "Dr. Nguyễn Văn A",
  "role": "DOCTOR",
  "token": "eyJjdHkiOiJzdHJpbmdlZS1hcGk7..."
}
```

## Thay đổi trong Code

### Backend - WebCallApiController.java

#### Method mới: getCurrentUser()
```java
@GetMapping("/current-user")
public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("authenticated", false));
    }
    
    User user = getUserFromPrincipal(userDetails);
    String stringeeUserId = "user_" + user.getId();
    
    return ResponseEntity.ok(Map.of(
        "authenticated", true,
        "userId", user.getId(),
        "stringeeUserId", stringeeUserId,
        "email", user.getEmail(),
        "fullName", user.getFullName() != null ? user.getFullName() : "User " + user.getId(),
        "role", user.getRole().name()
    ));
}
```

#### Cập nhật getAccessToken()
- Không cần userId parameter nữa
- Tự động lấy từ @AuthenticationPrincipal

### Frontend - web-call.html

#### Biến global mới
```javascript
var currentUser = null; // Lưu thông tin user hiện tại
```

#### Function mới: loadCurrentUser()
```javascript
async function loadCurrentUser() {
    const response = await fetch('/api/web-call/current-user');
    const data = await response.json();
    
    if (data.authenticated) {
        currentUser = data;
        await connectStringee();
    } else {
        // Hiển thị form đăng nhập
    }
}
```

#### Cập nhật connectStringee()
```javascript
async function connectStringee() {
    if (!currentUser) {
        alert('Vui lòng đăng nhập trước');
        return;
    }
    
    const userId = currentUser.stringeeUserId;
    const userName = currentUser.fullName;
    
    // Lấy token từ /api/web-call/token (không cần userId)
    const res = await fetch('/api/web-call/token');
    // ...
}
```

#### Cập nhật makeCall()
```javascript
function makeCall() {
    if (!currentUser) {
        alert('Vui lòng đăng nhập trước');
        return;
    }
    
    const callerId = currentUser.stringeeUserId;
    // ...
}
```

## UI Changes

### Login Screen (Before)
```html
<h3>Bước 1: Kết nối</h3>
<p>Nhập User ID của bạn để kết nối:</p>
<input type="text" id="myUserId" placeholder="Ví dụ: user1">
<button onclick="connectStringee()">Kết nối</button>
```

### Login Screen (After)
```html
<h3>Đang kiểm tra đăng nhập...</h3>
<p id="loginMessage">Vui lòng đợi...</p>
```

Tự động chuyển sang:
- "Chào {fullName}! Đang kết nối..." (nếu đã login)
- "⚠️ Chưa đăng nhập" + nút redirect (nếu chưa login)

### Call Screen Header
```html
<!-- Before -->
<h3>Xin chào: <span id="displayUserId"></span></h3>

<!-- After -->
<h3>Xin chào: <span id="displayUserName"></span> (<span id="displayUserId"></span>)</h3>
```

## Security

### Authentication
- Sử dụng Spring Security @AuthenticationPrincipal
- Kiểm tra session tự động
- Không cần truyền userId qua URL/parameter

### Authorization
- Mỗi user chỉ có thể:
  - Lấy token cho chính mình
  - Gọi điện với stringeeUserId của mình
  - Upload recording với userId của mình

### Session Management
- Session được quản lý bởi Spring Security
- Tự động logout khi session hết hạn
- Redirect đến /login nếu chưa authenticated

## Testing

### Test 1: User đã đăng nhập
1. Đăng nhập vào hệ thống với tài khoản bất kỳ
2. Truy cập /ai/web-call
3. **Expected:** Tự động hiển thị "Chào {tên}! Đang kết nối..."
4. **Expected:** Tự động kết nối Stringee
5. **Expected:** Hiển thị màn hình cuộc gọi với tên user

### Test 2: User chưa đăng nhập
1. Logout khỏi hệ thống (hoặc dùng incognito)
2. Truy cập /ai/web-call
3. **Expected:** Hiển thị "⚠️ Chưa đăng nhập"
4. **Expected:** Hiển thị nút "Đăng nhập ngay"
5. Bấm nút -> redirect đến /login

### Test 3: Session timeout
1. Đăng nhập và truy cập web-call
2. Đợi session hết hạn
3. Thử thực hiện cuộc gọi
4. **Expected:** Lỗi 401 Unauthorized
5. **Expected:** Hiển thị thông báo cần đăng nhập lại

### Test 4: Multiple roles
1. Test với DOCTOR, RECEPTIONIST, ADMIN, PATIENT
2. **Expected:** Tất cả role đều có thể sử dụng
3. **Expected:** stringeeUserId = "user_{id}" cho tất cả

## Logging

### Backend Logs
```
Generated Stringee token for user 123 (Dr. Nguyễn Văn A)
Error getting current user: User not found
```

### Frontend Logs (Console)
```javascript
Current user: {userId: 123, fullName: "Dr. Nguyễn Văn A", ...}
Bắt đầu kết nối với userId: user_123
Đã nhận được token: eyJjdHkiOiJzdHJpbmdlZS1hcGk...
✅ Xác thực thành công! User: Dr. Nguyễn Văn A
```

## Migration Guide

Nếu có code cũ sử dụng manual userId input:

### Old Code
```javascript
const userId = document.getElementById('myUserId').value;
const res = await fetch('/api/stringee/access-token?userId=' + userId);
```

### New Code
```javascript
const userId = currentUser.stringeeUserId;
const res = await fetch('/api/web-call/token');
```

## Benefits

✅ **UX cải thiện:** Không cần nhập ID thủ công
✅ **Security tốt hơn:** Không thể giả mạo user khác
✅ **Tự động hóa:** Kết nối ngay khi vào trang
✅ **Consistent:** Sử dụng authentication có sẵn
✅ **Maintainable:** Tập trung quản lý auth tại một chỗ

## Notes

- Stringee User ID format: `user_{userId}` (ví dụ: `user_123`)
- Tương thích với tất cả roles trong hệ thống
- Không ảnh hưởng đến các tính năng ghi âm hiện có
- Tự động refresh recordings khi upload thành công
