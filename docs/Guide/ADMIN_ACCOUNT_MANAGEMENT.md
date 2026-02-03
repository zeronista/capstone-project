# Tài liệu: Màn hình Admin - Quản lý tài khoản

## Tổng quan

Màn hình admin cho phép quản lý tài khoản người dùng, với tính năng chính là **assign role cho các tài khoản không phải bệnh nhân** (ADMIN, DOCTOR, NURSE, STAFF).

## Các tính năng đã triển khai

### 1. Backend API

#### DTOs
- **AssignRoleRequest**: DTO cho yêu cầu phân quyền
  - `userId`: ID của user cần phân quyền
  - `role`: Role mới (ADMIN, DOCTOR, NURSE, STAFF)

- **AccountResponse**: DTO cho thông tin tài khoản
  - Chứa đầy đủ thông tin user: id, fullName, email, phone, role, enabled, createdAt, etc.

#### AdminService
Xử lý business logic cho quản lý tài khoản:

- `getAllNonPatientAccounts()`: Lấy danh sách tất cả accounts không phải bệnh nhân
- `getAllAccounts(Pageable)`: Lấy danh sách tất cả accounts có phân trang
- `searchAccounts(keyword)`: Tìm kiếm theo tên, email, số điện thoại
- `filterAccountsByRole(role)`: Lọc theo vai trò
- `assignRole(request)`: Phân quyền cho user (chỉ non-patient roles)
- `getAccountById(id)`: Lấy chi tiết một account
- `toggleAccountStatus(id)`: Kích hoạt/vô hiệu hóa tài khoản
- `getAccountStatistics()`: Lấy thống kê số lượng accounts theo role

#### AdminController
REST API endpoints:

```
GET    /api/admin/accounts                    - Lấy tất cả accounts (có phân trang)
GET    /api/admin/accounts/non-patient        - Lấy accounts không phải bệnh nhân
GET    /api/admin/accounts/search?keyword=... - Tìm kiếm accounts
GET    /api/admin/accounts/filter?role=...    - Lọc theo role
GET    /api/admin/accounts/{id}               - Chi tiết một account
PUT    /api/admin/accounts/assign-role        - Phân quyền cho user
PUT    /api/admin/accounts/{id}/toggle-status - Kích hoạt/vô hiệu hóa
GET    /api/admin/accounts/statistics         - Thống kê accounts
```

### 2. Frontend

#### Giao diện
- **Trang**: `/admin/accounts`
- **URL**: `http://localhost:8080/admin/accounts`

#### Các tính năng UI:
1. **Danh sách tài khoản động**: Tự động load từ API khi trang được mở
2. **Tìm kiếm real-time**: Tìm theo tên, email, số điện thoại
3. **Lọc theo vai trò**: Dropdown filter cho từng role
4. **Phân quyền**: Modal popup để assign role mới
5. **Toggle trạng thái**: Kích hoạt/vô hiệu hóa tài khoản
6. **Hiển thị thông tin**: Avatar, role badge, trạng thái, ngày tạo

#### Modal Assign Role
- Hiển thị tên người dùng
- Dropdown chọn role mới (chỉ ADMIN, DOCTOR, NURSE, STAFF)
- Nút xác nhận và hủy
- Tự động refresh danh sách sau khi cập nhật

## Hướng dẫn sử dụng

### 1. Khởi động ứng dụng

```bash
# Chạy Spring Boot application
mvn spring-boot:run

# Hoặc chạy từ IDE (Run CapstoneProjectApplication)
```

### 2. Truy cập màn hình admin

1. Mở trình duyệt và truy cập: `http://localhost:8080/admin/accounts`
2. Đăng nhập (nếu cần) với tài khoản admin

### 3. Sử dụng các tính năng

#### Xem danh sách tài khoản
- Danh sách tự động load khi vào trang
- Hiển thị: Avatar, tên, email, role, trạng thái, ngày tạo

#### Tìm kiếm tài khoản
- Nhập từ khóa vào ô tìm kiếm (tối thiểu 2 ký tự)
- Hệ thống tìm trong: tên, email, số điện thoại
- Kết quả hiển thị real-time

#### Lọc theo vai trò
- Chọn role từ dropdown "Tất cả vai trò"
- Chọn "Tất cả vai trò" để hiển thị lại toàn bộ

#### Phân quyền tài khoản
1. Click icon phân quyền (icon chìa khóa) ở cột "Thao tác"
2. Modal popup hiển thị
3. Chọn vai trò mới từ dropdown (ADMIN, DOCTOR, NURSE, STAFF)
4. Click "Xác nhận" để lưu
5. Danh sách tự động refresh

> **Lưu ý**: Không thể assign role PATIENT qua tính năng này

#### Kích hoạt/Vô hiệu hóa tài khoản
1. Click icon toggle status (icon check/ban)
2. Xác nhận trong dialog
3. Trạng thái tài khoản được cập nhật
4. Badge trạng thái thay đổi: "Hoạt động" ↔ "Vô hiệu hóa"

## API Request/Response Examples

### 1. Lấy danh sách accounts
```http
GET /api/admin/accounts?page=0&size=10&sortBy=createdAt&sortDir=desc
```

**Response:**
```json
{
  "success": true,
  "accounts": [
    {
      "id": 1,
      "fullName": "Nguyễn Văn A",
      "email": "admin@hospital.vn",
      "phone": "0123456789",
      "role": "ADMIN",
      "provider": "LOCAL",
      "enabled": true,
      "accountNonLocked": true,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "currentPage": 0,
  "totalItems": 50,
  "totalPages": 5
}
```

### 2. Assign role
```http
PUT /api/admin/accounts/assign-role
Content-Type: application/json

{
  "userId": 5,
  "role": "DOCTOR"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Cập nhật vai trò thành công",
  "account": {
    "id": 5,
    "fullName": "Trần Thị B",
    "role": "DOCTOR",
    ...
  }
}
```

### 3. Toggle account status
```http
PUT /api/admin/accounts/5/toggle-status
```

**Response:**
```json
{
  "success": true,
  "message": "Vô hiệu hóa tài khoản thành công",
  "account": {
    "id": 5,
    "enabled": false,
    ...
  }
}
```

## Security & Validation

### Backend Validation
- **Role validation**: Chỉ cho phép assign các role: ADMIN, DOCTOR, NURSE, STAFF
- **User existence**: Kiểm tra user có tồn tại trước khi cập nhật
- **Transaction safety**: Sử dụng `@Transactional` để đảm bảo data integrity

### Frontend Validation
- **Role selection**: Bắt buộc chọn role trước khi submit
- **User confirmation**: Xác nhận trước khi toggle status
- **Error handling**: Hiển thị thông báo lỗi rõ ràng

## Cấu trúc File

```
src/
├── main/
│   ├── java/com/g4/capstoneproject/
│   │   ├── controller/
│   │   │   └── AdminController.java          # REST API endpoints
│   │   ├── service/
│   │   │   └── AdminService.java             # Business logic
│   │   ├── dto/
│   │   │   ├── AssignRoleRequest.java        # Request DTO
│   │   │   └── AccountResponse.java          # Response DTO
│   │   ├── entity/
│   │   │   └── User.java                     # User entity (đã có)
│   │   └── repository/
│   │       └── UserRepository.java           # Data access (đã có)
│   └── resources/
│       └── templates/
│           └── admin/
│               └── accounts.html             # Frontend UI
```

## Testing

### Manual Testing
1. Tạo một vài user với role khác nhau (có thể dùng API register hoặc trực tiếp DB)
2. Truy cập `/admin/accounts`
3. Test các tính năng:
   - Load danh sách
   - Tìm kiếm
   - Lọc theo role
   - Assign role
   - Toggle status

### Test Data
Bạn có thể tạo test users bằng cách chạy SQL:

```sql
INSERT INTO users (full_name, email, password, role, provider, enabled, account_non_locked, created_at, updated_at)
VALUES 
('Admin User', 'admin@test.com', '$2a$10$...', 'ADMIN', 'LOCAL', true, true, NOW(), NOW()),
('Doctor User', 'doctor@test.com', '$2a$10$...', 'DOCTOR', 'LOCAL', true, true, NOW(), NOW()),
('Nurse User', 'nurse@test.com', '$2a$10$...', 'NURSE', 'LOCAL', true, true, NOW(), NOW());
```

## Mở rộng trong tương lai

### Gợi ý tính năng có thể thêm:
1. **Tạo tài khoản mới**: Form tạo account trực tiếp từ admin panel
2. **Xóa tài khoản**: Soft delete hoặc hard delete
3. **Chỉnh sửa thông tin**: Cập nhật email, phone, tên
4. **Phân trang**: Implement pagination UI
5. **Export**: Xuất danh sách ra Excel/CSV
6. **Activity log**: Lưu lại lịch sử thay đổi role
7. **Bulk actions**: Chọn nhiều tài khoản để thao tác cùng lúc
8. **Advanced filters**: Lọc theo ngày tạo, trạng thái, provider
9. **Role permissions**: Quản lý chi tiết quyền hạn của từng role
10. **Notification**: Gửi email thông báo khi role thay đổi

## Troubleshooting

### Lỗi thường gặp:

1. **Không load được danh sách**
   - Kiểm tra backend có chạy không
   - Check console browser để xem lỗi API
   - Verify database connection

2. **Không assign được role**
   - Kiểm tra role có hợp lệ không (phải là ADMIN, DOCTOR, NURSE, STAFF)
   - Verify userId tồn tại trong database
   - Check logs trong console

3. **UI không cập nhật**
   - Clear browser cache
   - Hard refresh (Ctrl + Shift + R)
   - Check JavaScript console for errors

## Liên hệ & Hỗ trợ

Nếu có vấn đề hoặc câu hỏi, vui lòng:
1. Check logs trong console và browser
2. Review code trong các file đã tạo
3. Test API trực tiếp bằng Postman/curl
4. Liên hệ team để được hỗ trợ

---

**Version**: 1.0  
**Last Updated**: January 23, 2026  
**Author**: GitHub Copilot
