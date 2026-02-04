# HƯỚNG DẪN DEMO CHỨC NĂNG BỆNH NHÂN

## Tổng quan
Màn hình bệnh nhân đã được hoàn thiện với đầy đủ chức năng để demo. Bệnh nhân có thể xem và quản lý thông tin sức khỏe cá nhân của mình.

## Các chức năng đã hoàn thiện

### 1. **Dashboard Thống kê**
- Số lượng đơn thuốc
- Số lượng kế hoạch điều trị
- Tổng số yêu cầu hỗ trợ
- Số yêu cầu đang xử lý

### 2. **Quản lý Đơn thuốc** 
Đường dẫn: `/patient` → Tab "Đơn thuốc"

**Tính năng:**
- Xem danh sách tất cả đơn thuốc của bản thân
- Hiển thị trạng thái: Đang sử dụng / Hoàn thành / Đã hủy
- Xem thông tin bác sĩ kê đơn
- Xem chi tiết từng đơn thuốc bao gồm:
  - Chẩn đoán
  - Danh sách thuốc
  - Liều lượng, tần suất sử dụng
  - Ghi chú của bác sĩ

**API Endpoints:**
- `GET /api/patient/prescriptions` - Lấy danh sách đơn thuốc
- `GET /api/patient/prescriptions/{id}` - Xem chi tiết đơn thuốc

### 3. **Kế hoạch Điều trị**
Đường dẫn: `/patient` → Tab "Kế hoạch điều trị"

**Tính năng:**
- Xem danh sách kế hoạch điều trị
- Hiển thị trạng thái: Đang điều trị / Hoàn thành / Tạm dừng / Đã hủy
- Badge "AI Gợi ý" cho các kế hoạch do AI đề xuất
- Thông tin bác sĩ phụ trách
- Ngày bắt đầu và dự kiến kết thúc
- Ghi chú điều trị

**API Endpoints:**
- `GET /api/patient/treatments` - Lấy danh sách kế hoạch điều trị

### 4. **Yêu cầu Hỗ trợ (Tickets)**
Đường dẫn: `/patient` → Tab "Yêu cầu hỗ trợ"

**Tính năng:**
- Xem danh sách yêu cầu hỗ trợ của bản thân
- Hiển thị trạng thái: Mở / Đang xử lý / Chờ bác sĩ / Đã giải quyết / Đã đóng
- Hiển thị mức độ ưu tiên: Khẩn cấp / Cao / Trung bình / Thấp
- Xem người phụ trách xử lý
- Thời gian tạo yêu cầu

**API Endpoints:**
- `GET /api/patient/tickets` - Lấy danh sách tickets

### 5. **Thông tin Cá nhân**
Đường dẫn: `/patient` → Tab "Thông tin cá nhân"

**Tính năng:**
- Xem thông tin cá nhân đầy đủ:
  - Họ tên
  - Email (với trạng thái xác thực)
  - Số điện thoại
  - Ngày sinh
  - Giới tính
  - Địa chỉ
  - Ngày tạo tài khoản
- Nút chuyển đến trang chỉnh sửa thông tin

**API Endpoints:**
- `GET /api/patient/profile` - Lấy thông tin cá nhân

### 6. **API Thống kê**
**API Endpoints:**
- `GET /api/patient/stats` - Lấy thống kê tổng quan

## Cấu trúc Files

### Backend
```
src/main/java/com/g4/capstoneproject/
├── controller/
│   └── PatientController.java          # REST API cho bệnh nhân
├── repository/
│   ├── PrescriptionRepository.java     # Repository đơn thuốc (đã cập nhật)
│   ├── TreatmentPlanRepository.java    # Repository kế hoạch điều trị (đã cập nhật)
│   └── TicketRepository.java           # Repository tickets (đã cập nhật)
```

### Frontend
```
src/main/resources/
├── templates/patient/
│   └── index.html                       # Giao diện chính bệnh nhân
└── static/js/
    └── patient.js                       # Logic JavaScript
```

## Hướng dẫn Demo

### Bước 1: Tạo dữ liệu mẫu
Trước khi demo, cần có dữ liệu mẫu trong database:

1. **Tạo tài khoản bệnh nhân** (hoặc dùng tài khoản đã có)
2. **Tạo đơn thuốc mẫu** cho bệnh nhân (bởi bác sĩ)
3. **Tạo kế hoạch điều trị** cho bệnh nhân
4. **Tạo tickets** từ bệnh nhân

### Bước 2: Đăng nhập
```
URL: http://localhost:8080/auth/login
Email/Phone: [email của bệnh nhân]
Password: [mật khẩu]
```

Sau khi đăng nhập thành công với role PATIENT, hệ thống sẽ tự động redirect đến `/patient`

### Bước 3: Demo từng chức năng

#### 3.1 Dashboard
- Kiểm tra các số liệu thống kê trên 4 card
- Các số liệu tự động load từ API

#### 3.2 Đơn thuốc
- Click tab "Đơn thuốc"
- Xem danh sách đơn thuốc
- Click "Xem chi tiết" để xem modal chi tiết
- Kiểm tra trạng thái, bác sĩ, chẩn đoán
- Xem danh sách thuốc, liều lượng

#### 3.3 Kế hoạch điều trị
- Click tab "Kế hoạch điều trị"
- Xem danh sách kế hoạch
- Kiểm tra badge "AI Gợi ý" (nếu có)
- Xem thông tin bác sĩ, ngày bắt đầu/kết thúc

#### 3.4 Yêu cầu hỗ trợ
- Click tab "Yêu cầu hỗ trợ"
- Xem danh sách tickets
- Kiểm tra trạng thái và mức độ ưu tiên
- Xem người phụ trách

#### 3.5 Thông tin cá nhân
- Click tab "Thông tin cá nhân"
- Xem thông tin đầy đủ
- Click "Chỉnh sửa thông tin" để đến trang profile

## Tính năng Bảo mật

### Authentication & Authorization
- **Kiểm tra đăng nhập**: Tất cả API đều kiểm tra session userId
- **Phân quyền dữ liệu**: Bệnh nhân chỉ xem được dữ liệu của chính mình
- **Validation**: Kiểm tra ownership khi xem chi tiết (ví dụ: prescription detail)

### Error Handling
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy dữ liệu
- 500: Lỗi hệ thống

## UI/UX Features

### Design
- **Tailwind CSS**: Modern, responsive design
- **Font Awesome Icons**: Icons đẹp mắt
- **Color Coding**: 
  - Blue: Đơn thuốc
  - Green: Kế hoạch điều trị
  - Purple: Tickets
  - Gray: Thông tin cá nhân

### Interactions
- **Tabs**: Chuyển đổi mượt mà giữa các chức năng
- **Loading States**: Spinner khi đang tải dữ liệu
- **Empty States**: Hiển thị thông báo khi chưa có dữ liệu
- **Modal**: Xem chi tiết đơn thuốc
- **Hover Effects**: Các card có hiệu ứng hover
- **Badges**: Trạng thái với màu sắc rõ ràng

### Responsive
- Desktop: Hiển thị grid 4 cột cho stats
- Tablet: Grid 2 cột
- Mobile: Grid 1 cột

## Điểm nổi bật

### 1. **Lazy Loading**
- Dữ liệu chỉ được load khi user click vào tab đó
- Giảm số lượng API calls không cần thiết
- Cải thiện performance

### 2. **Caching**
- Dữ liệu được cache trong biến JavaScript
- Không reload lại khi switch tabs
- Load lại khi refresh page

### 3. **User Experience**
- Auto-update header với tên bệnh nhân
- Thông báo rõ ràng khi không có dữ liệu
- Loading indicators
- Error messages thân thiện

### 4. **Clean Code**
- Tách riêng logic JavaScript vào file patient.js
- Sử dụng async/await
- Utility functions để tái sử dụng code
- Comments đầy đủ

## Testing Checklist

### Functional Testing
- [ ] Stats cards hiển thị đúng số liệu
- [ ] Tab switching hoạt động mượt mà
- [ ] Prescriptions list load và hiển thị đúng
- [ ] Prescription detail modal mở/đóng đúng
- [ ] Treatments list hiển thị đầy đủ thông tin
- [ ] Tickets list hiển thị trạng thái và priority đúng
- [ ] Profile hiển thị thông tin cá nhân đúng
- [ ] Link "Chỉnh sửa thông tin" hoạt động

### Security Testing
- [ ] Không thể xem dữ liệu của bệnh nhân khác
- [ ] API trả 401 khi chưa đăng nhập
- [ ] API trả 403 khi truy cập dữ liệu không phải của mình

### UI/UX Testing
- [ ] Responsive trên mobile
- [ ] Icons hiển thị đúng
- [ ] Colors và badges hiển thị đúng
- [ ] Loading states hoạt động
- [ ] Empty states hiển thị

### Performance Testing
- [ ] Lazy loading hoạt động đúng
- [ ] No unnecessary API calls
- [ ] Caching data hiệu quả

## Troubleshooting

### Lỗi "Chưa đăng nhập"
- Kiểm tra session có userId không
- Đăng nhập lại

### Lỗi "Không có quyền truy cập"
- Kiểm tra role của user có phải PATIENT không
- Kiểm tra dữ liệu có thuộc về user này không

### Không hiển thị dữ liệu
- Kiểm tra database có dữ liệu không
- Check console log để xem API response
- Verify repository methods

### Modal không mở
- Check JavaScript console cho errors
- Verify prescription ID tồn tại
- Check API response

## Mở rộng trong tương lai

### Tính năng có thể thêm:
1. **Đặt lịch khám**: Booking appointments
2. **Chat với bác sĩ**: Real-time messaging
3. **Upload tài liệu**: Medical documents upload
4. **Nhắc nhở uống thuốc**: Medication reminders
5. **Theo dõi sức khỏe**: Health tracking (BP, glucose, etc.)
6. **Video call**: Telemedicine consultation
7. **Payment**: Thanh toán online
8. **Rating & Review**: Đánh giá bác sĩ
9. **Export PDF**: Xuất đơn thuốc ra PDF
10. **Notifications**: Real-time notifications

## Kết luận

Màn hình bệnh nhân đã được hoàn thiện với đầy đủ chức năng cơ bản để demo:
- ✅ Xem đơn thuốc với chi tiết đầy đủ
- ✅ Xem kế hoạch điều trị
- ✅ Xem yêu cầu hỗ trợ
- ✅ Quản lý thông tin cá nhân
- ✅ Dashboard thống kê
- ✅ UI/UX hiện đại, responsive
- ✅ Bảo mật tốt với authentication & authorization
- ✅ Performance tối ưu với lazy loading

Hệ thống sẵn sàng để demo cho khách hàng! 🎉
