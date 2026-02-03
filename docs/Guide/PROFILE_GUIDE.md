# Profile Page - Trang Thông Tin Cá Nhân

## Tổng quan
Hệ thống profile đã được phân quyền theo role. Mỗi role có trang profile riêng với giao diện và chức năng phù hợp.

## Phân quyền theo Role

### 1. PATIENT (Bệnh nhân)
- **URL**: `/profile` → tự động chuyển đến `profile/patient.html`
- **Chức năng**:
  - Xem và cập nhật thông tin cá nhân
  - Đổi mật khẩu
  - Giao diện đơn giản, tập trung vào thông tin cơ bản

### 2. DOCTOR, RECEPTIONIST, ADMIN (Nhân viên y tế)
- **URL**: `/profile` → tự động chuyển đến `profile/index.html`
- **Chức năng**:
  - Xem và cập nhật thông tin nhân viên
  - Quản lý chức vụ, khoa/phòng
  - Đổi mật khẩu

## Cấu trúc Files

### Backend
```
src/main/java/com/g4/capstoneproject/controller/
├── ProfileController.java          # Controller chính xử lý phân quyền
└── PatientController.java          # API endpoints cho bệnh nhân
```

### Frontend
```
src/main/resources/
├── templates/profile/
│   ├── patient.html               # Trang profile cho bệnh nhân
│   └── index.html                 # Trang profile cho nhân viên
└── static/js/
    └── profile-patient.js         # JavaScript cho bệnh nhân
```

## API Endpoints

### 1. GET /profile
- **Mô tả**: Hiển thị trang profile (phân quyền tự động)
- **Authentication**: Required (Session)
- **Response**:
  - Role PATIENT → `profile/patient.html`
  - Role khác → `profile/index.html`

### 2. GET /api/patient/profile
- **Mô tả**: Lấy thông tin profile của bệnh nhân (JSON)
- **Authentication**: Required (Session)
- **Response**:
```json
{
    "id": 123,
    "email": "patient@example.com",
    "phoneNumber": "0912345678",
    "fullName": "Nguyễn Văn A",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "address": "123 Đường ABC, Quận 1, TP.HCM",
    "createdAt": "2024-01-01T10:00:00"
}
```

### 3. POST /api/profile/update
- **Mô tả**: Cập nhật thông tin cá nhân
- **Authentication**: Required (Session)
- **Request Body**:
```json
{
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0912345678",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "address": "123 Đường ABC"
}
```
- **Response**:
```json
{
    "success": true,
    "message": "Cập nhật thông tin thành công"
}
```

### 4. POST /api/profile/change-password
- **Mô tả**: Đổi mật khẩu
- **Authentication**: Required (Session)
- **Request Body**:
```json
{
    "currentPassword": "old123",
    "newPassword": "new456",
    "confirmPassword": "new456"
}
```
- **Response**:
```json
{
    "success": true,
    "message": "Đổi mật khẩu thành công"
}
```

## Tính năng Profile của Bệnh nhân

### 1. Xem thông tin
- Tên đầy đủ
- Email (không thể sửa)
- Số điện thoại
- Ngày sinh
- Giới tính
- Địa chỉ
- Ngày đăng ký

### 2. Cập nhật thông tin
- Click nút "Chỉnh sửa"
- Thay đổi thông tin
- Click "Lưu thay đổi" hoặc "Hủy"

### 3. Đổi mật khẩu
- Nhập mật khẩu hiện tại
- Nhập mật khẩu mới (tối thiểu 6 ký tự)
- Xác nhận mật khẩu mới

### 4. Navigation
- Link "Quay lại" về trang `/patient`
- Có sẵn trong header của trang patient portal

## Cách sử dụng

### Từ Patient Portal
1. Đăng nhập với tài khoản bệnh nhân
2. Vào trang `/patient`
3. Click nút "Hồ sơ" trên header
4. Xem và cập nhật thông tin

### Truy cập trực tiếp
1. Truy cập `/profile`
2. Hệ thống tự động nhận diện role
3. Hiển thị trang profile phù hợp

## Database Schema

### Bảng users
```sql
- id (PK)
- email (unique)
- phoneNumber
- password
- role (PATIENT, DOCTOR, RECEPTIONIST, ADMIN)
- createdAt
- updatedAt
```

### Bảng user_info
```sql
- id (PK)
- userId (FK to users.id)
- fullName
- dateOfBirth
- gender (MALE, FEMALE, OTHER)
- address
```

## Validation Rules

### Frontend
- Họ tên: bắt buộc
- Số điện thoại: bắt buộc
- Email: không thể thay đổi
- Mật khẩu mới: tối thiểu 6 ký tự
- Xác nhận mật khẩu: phải khớp với mật khẩu mới

### Backend
- Session validation
- User ownership validation
- Field validation (required, format)

## Troubleshooting

### Lỗi: URL mapping conflict
- **Nguyên nhân**: Nhiều controller map cùng một URL
- **Giải pháp**: ProfileController đã xử lý `/profile`, các controller khác dùng `/api/patient/profile`

### Lỗi: 401 Unauthorized
- **Nguyên nhân**: Chưa đăng nhập
- **Giải pháp**: Redirect về `/auth/login`

### Lỗi: Profile không load
- **Nguyên nhân**: API endpoint sai hoặc session expired
- **Giải pháp**: Kiểm tra console log và network tab

## TODO
- [ ] Implement BCrypt password verification trong changePassword
- [ ] Add avatar upload functionality
- [ ] Add profile completion percentage
- [ ] Add notification preferences
- [ ] Add activity history

## Testing

### Test Case 1: Patient Profile Access
1. Login với role PATIENT
2. Truy cập `/profile`
3. Verify: hiển thị `profile/patient.html`

### Test Case 2: Profile Update
1. Click "Chỉnh sửa"
2. Thay đổi họ tên
3. Click "Lưu thay đổi"
4. Verify: thông tin được cập nhật

### Test Case 3: Password Change
1. Nhập mật khẩu cũ, mới, xác nhận
2. Submit form
3. Verify: mật khẩu được thay đổi

## Security Notes
- Session-based authentication
- Owner validation trước khi update
- Password hashing (TODO: implement BCrypt)
- CSRF protection (Spring Security default)
