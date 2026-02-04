# Hướng dẫn cấu hình Backend đơn giản

## 📦 Cấu trúc đã tạo

### 1. **Models** (src/main/java/com/g4/capstoneproject/model/)
- `Prescription.java` - Đơn thuốc
- `TreatmentPlan.java` - Lộ trình điều trị
- `Ticket.java` - Ticket chuyên khoa

### 2. **Services** (src/main/java/com/g4/capstoneproject/service/)
- `PrescriptionService.java` - Quản lý đơn thuốc (có dữ liệu mock)
- `TreatmentPlanService.java` - Quản lý lộ trình điều trị (có dữ liệu mock)
- `TicketService.java` - Quản lý tickets (có dữ liệu mock)

### 3. **Controllers**
- `PageController.java` - Render HTML pages (đã có sẵn)
- `ApiController.java` - REST API endpoints (mới tạo)

---

## 🚀 Cách chạy ứng dụng

### Bước 1: Build và chạy Spring Boot

```bash
# Trên Windows (PowerShell/CMD)
mvnw.cmd spring-boot:run

# Hoặc trên Linux/Mac
./mvnw spring-boot:run
```

### Bước 2: Mở trình duyệt

Truy cập: **http://localhost:8080**

---

## 📍 Danh sách URLs có thể truy cập

### **Trang chủ & Dashboard**
- `http://localhost:8080/` - Trang chủ
- `http://localhost:8080/dashboard` - Dashboard bác sĩ

### **Các màn hình chính (Doctor Role)**
1. **Quản lý đơn thuốc**
   - URL: `http://localhost:8080/medical/prescriptions`
   - Hiển thị danh sách đơn thuốc với dữ liệu mock

2. **Quản lý lộ trình điều trị**
   - URL: `http://localhost:8080/medical/treatments`
   - Hiển thị các kế hoạch điều trị với dữ liệu mock

3. **Dự báo sức khỏe**
   - URL: `http://localhost:8080/medical/forecast`
   - Hiển thị dự báo rủi ro sức khỏe

4. **Cơ sở tri thức y tế**
   - URL: `http://localhost:8080/medical/knowledge`
   - Hiển thị tài liệu y khoa

5. **Quản lý Ticket chuyên khoa**
   - URL: `http://localhost:8080/crm/tickets`
   - Hiển thị tickets với dữ liệu mock

---

## 🔌 REST API Endpoints (có thể test bằng Postman/curl)

### **Prescription APIs**

**Lấy tất cả đơn thuốc:**
```bash
GET http://localhost:8080/api/prescriptions
```

**Lấy đơn thuốc theo ID:**
```bash
GET http://localhost:8080/api/prescriptions/RX-2026-0089
```

**Tạo đơn thuốc mới:**
```bash
POST http://localhost:8080/api/prescriptions
Content-Type: application/json

{
  "id": "RX-2026-0090",
  "patientId": "BN-2026-0150",
  "patientName": "Nguyễn Văn Test",
  "doctorName": "BS. Test Doctor",
  "medicationCount": 3,
  "prescriptionDate": "2026-01-22",
  "status": "Chờ xử lý",
  "hasDrugInteraction": false
}
```

**Cập nhật đơn thuốc:**
```bash
PUT http://localhost:8080/api/prescriptions/RX-2026-0089
Content-Type: application/json

{
  "status": "Đã xác nhận"
}
```

**Xóa đơn thuốc:**
```bash
DELETE http://localhost:8080/api/prescriptions/RX-2026-0089
```

**Thống kê:**
```bash
GET http://localhost:8080/api/prescriptions/stats
```

---

### **Treatment Plan APIs**

**Lấy tất cả lộ trình:**
```bash
GET http://localhost:8080/api/treatment-plans
```

**Lấy lộ trình theo ID:**
```bash
GET http://localhost:8080/api/treatment-plans/TP-2026-001
```

**Tạo lộ trình mới:**
```bash
POST http://localhost:8080/api/treatment-plans
Content-Type: application/json

{
  "id": "TP-2026-004",
  "patientId": "BN-2026-0150",
  "patientName": "Nguyễn Văn Test",
  "patientAge": 50,
  "patientGender": "Nam",
  "diagnosis": "Test diagnosis",
  "goals": "Test goals",
  "progress": 30,
  "status": "Đang thực hiện",
  "followUpPeriod": "Mỗi 3 tháng",
  "doctorName": "BS. Test",
  "priority": "Bình thường"
}
```

**Thống kê:**
```bash
GET http://localhost:8080/api/treatment-plans/stats
```

---

### **Ticket APIs**

**Lấy tất cả tickets:**
```bash
GET http://localhost:8080/api/tickets
```

**Lấy ticket theo ID:**
```bash
GET http://localhost:8080/api/tickets/TK-2026-0042
```

**Tạo ticket mới:**
```bash
POST http://localhost:8080/api/tickets
Content-Type: application/json

{
  "id": "TK-2026-0043",
  "title": "Test ticket",
  "description": "Test description",
  "patientId": "BN-2026-0150",
  "patientName": "Nguyễn Văn Test",
  "creatorName": "NV. Test",
  "creatorRole": "Y tá",
  "priority": "Ưu tiên trung bình",
  "status": "Chờ phản hồi",
  "category": "Tư vấn chuyên khoa"
}
```

**Thay đổi trạng thái:**
```bash
PUT http://localhost:8080/api/tickets/TK-2026-0042/status
Content-Type: application/json

{
  "status": "Đang xử lý"
}
```

**Thống kê:**
```bash
GET http://localhost:8080/api/tickets/stats
```

---

### **AI APIs (Mock)**

**Gợi ý thuốc thay thế:**
```bash
POST http://localhost:8080/api/ai/suggest-medication
Content-Type: application/json

{
  "currentMedication": "Enalapril",
  "patientCondition": "Hypertension"
}
```

**Kiểm tra tương tác thuốc:**
```bash
POST http://localhost:8080/api/ai/check-interactions
Content-Type: application/json

["Warfarin", "Aspirin", "Metformin", "Enalapril", "Atorvastatin", "Omeprazole"]
```

**Tính toán rủi ro sức khỏe:**
```bash
POST http://localhost:8080/api/ai/calculate-risk
Content-Type: application/json

{
  "age": 45,
  "gender": "Male",
  "bloodPressure": "145/92",
  "cholesterol": 245,
  "smoking": true,
  "diabetes": true
}
```

**Trạng thái huấn luyện AI:**
```bash
GET http://localhost:8080/api/ai/training-status
```

---

## 🧪 Test với curl (Windows PowerShell)

### Test GET request:
```powershell
curl http://localhost:8080/api/prescriptions
```

### Test POST request:
```powershell
$body = @{
    id = "RX-2026-0090"
    patientName = "Test Patient"
    doctorName = "BS. Test"
    status = "Chờ xử lý"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions" -Method Post -Body $body -ContentType "application/json"
```

---

## 📝 Dữ liệu Mock có sẵn

### **Prescriptions (3 đơn thuốc)**
1. RX-2026-0089 - Trần Văn Hùng
2. RX-2026-0088 - Lê Thị Mai (có tương tác thuốc)
3. RX-2026-0087 - Phạm Văn Đức

### **Treatment Plans (3 lộ trình)**
1. TP-2026-001 - Trần Văn Hùng (Tăng huyết áp, ĐTĐ)
2. TP-2026-002 - Võ Thị Thanh (Suy tim, Rung nhĩ) - Ưu tiên cao
3. TP-2026-003 - Lê Thị Mai (Rối loạn tuyến giáp)

### **Tickets (2 tickets)**
1. TK-2026-0042 - Tư vấn suy tim (Ưu tiên cao)
2. TK-2026-0041 - Hội chẩn ĐTĐ (Đang xử lý)

---

## 🎨 Tích hợp với Frontend

### Ví dụ gọi API từ JavaScript trong HTML:

```javascript
// Lấy danh sách đơn thuốc
async function loadPrescriptions() {
    try {
        const response = await fetch('/api/prescriptions');
        const prescriptions = await response.json();
        console.log('Prescriptions:', prescriptions);
        // Xử lý hiển thị dữ liệu
    } catch (error) {
        console.error('Error loading prescriptions:', error);
    }
}

// Tạo đơn thuốc mới
async function createPrescription(data) {
    try {
        const response = await fetch('/api/prescriptions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        const created = await response.json();
        console.log('Created prescription:', created);
        alert('Tạo đơn thuốc thành công!');
    } catch (error) {
        console.error('Error creating prescription:', error);
        alert('Lỗi khi tạo đơn thuốc!');
    }
}

// Kiểm tra tương tác thuốc
async function checkDrugInteractions(medications) {
    try {
        const response = await fetch('/api/ai/check-interactions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(medications)
        });
        const result = await response.json();
        if (result.hasInteraction) {
            alert(`Cảnh báo: ${result.warning}`);
        }
    } catch (error) {
        console.error('Error checking interactions:', error);
    }
}
```

---

## 🔧 Mở rộng và tùy chỉnh

### Thêm dữ liệu mock:
- Mở file `Service` tương ứng (ví dụ: `PrescriptionService.java`)
- Thêm dữ liệu trong constructor

### Tạo API endpoint mới:
- Mở `ApiController.java`
- Thêm method với annotation `@GetMapping`, `@PostMapping`, etc.

### Kết nối database thật:
1. Thêm dependency vào `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
   </dependency>
   ```

2. Cấu hình `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/medical_db
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

3. Thêm annotation `@Entity` vào models
4. Tạo Repository interfaces

---

## ✅ Checklist để bắt đầu

- [ ] Chạy `mvnw.cmd spring-boot:run`
- [ ] Mở http://localhost:8080/dashboard
- [ ] Test các màn hình:
  - [ ] /medical/prescriptions
  - [ ] /medical/treatments
  - [ ] /medical/forecast
  - [ ] /medical/knowledge
  - [ ] /crm/tickets
- [ ] Test API với Postman:
  - [ ] GET /api/prescriptions
  - [ ] GET /api/treatment-plans
  - [ ] GET /api/tickets
- [ ] Xem console logs để kiểm tra lỗi

---

## 🐛 Troubleshooting

### Lỗi "Port 8080 already in use":
```bash
# Tìm và kill process đang dùng port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Lỗi compile:
```bash
# Clean và build lại
mvnw.cmd clean install
```

### Lỗi 404 Not Found:
- Kiểm tra URL có đúng không
- Kiểm tra application đã chạy chưa
- Xem logs trong console

---

**🎉 Hoàn tất! Backend đã sẵn sàng để tương tác với giao diện!**
