# Test API nhanh với PowerShell (Windows)

## 1. Khởi động ứng dụng

```powershell
# Chạy Spring Boot
.\mvnw.cmd spring-boot:run
```

Chờ đến khi thấy:
```
Started CapstoneProjectApplication in X seconds
```

---

## 2. Test các trang HTML (mở trình duyệt)

### Dashboard
```
http://localhost:8080/dashboard
```

### Quản lý đơn thuốc
```
http://localhost:8080/medical/prescriptions
```

### Quản lý lộ trình điều trị
```
http://localhost:8080/medical/treatments
```

### Dự báo sức khỏe
```
http://localhost:8080/medical/forecast
```

### Cơ sở tri thức y tế
```
http://localhost:8080/medical/knowledge
```

### Quản lý Ticket
```
http://localhost:8080/crm/tickets
```

---

## 3. Test REST APIs với PowerShell

### A. Lấy tất cả đơn thuốc
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions" -Method Get | ConvertTo-Json -Depth 10
```

### B. Lấy đơn thuốc theo ID
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions/RX-2026-0089" -Method Get | ConvertTo-Json -Depth 10
```

### C. Thống kê đơn thuốc
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions/stats" -Method Get | ConvertTo-Json -Depth 10
```

### D. Lấy tất cả lộ trình điều trị
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/treatment-plans" -Method Get | ConvertTo-Json -Depth 10
```

### E. Thống kê lộ trình điều trị
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/treatment-plans/stats" -Method Get | ConvertTo-Json -Depth 10
```

### F. Lấy tất cả tickets
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tickets" -Method Get | ConvertTo-Json -Depth 10
```

### G. Thống kê tickets
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/stats" -Method Get | ConvertTo-Json -Depth 10
```

### H. Kiểm tra tương tác thuốc (AI Mock)
```powershell
$medications = @("Warfarin", "Aspirin", "Metformin", "Enalapril", "Atorvastatin", "Omeprazole")
$body = $medications | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/ai/check-interactions" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json -Depth 10
```

### I. Tính toán rủi ro sức khỏe (AI Mock)
```powershell
$patientData = @{
    age = 45
    gender = "Male"
    bloodPressure = "145/92"
    cholesterol = 245
    smoking = $true
    diabetes = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/ai/calculate-risk" -Method Post -Body $patientData -ContentType "application/json" | ConvertTo-Json -Depth 10
```

### J. Trạng thái huấn luyện AI
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/ai/training-status" -Method Get | ConvertTo-Json -Depth 10
```

### K. Gợi ý thuốc thay thế (AI Mock)
```powershell
$request = @{
    currentMedication = "Enalapril"
    patientCondition = "Hypertension"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/ai/suggest-medication" -Method Post -Body $request -ContentType "application/json" | ConvertTo-Json -Depth 10
```

---

## 4. Tạo dữ liệu mới (POST)

### Tạo đơn thuốc mới
```powershell
$newPrescription = @{
    id = "RX-2026-0100"
    patientId = "BN-2026-0200"
    patientName = "Nguyễn Văn Test"
    doctorName = "BS. Test Doctor"
    medicationCount = 5
    prescriptionDate = "2026-01-22"
    status = "Chờ xử lý"
    hasDrugInteraction = $false
    diagnosis = "Tăng huyết áp"
    notes = "Đây là đơn thuốc test"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions" -Method Post -Body $newPrescription -ContentType "application/json" | ConvertTo-Json -Depth 10
```

### Tạo lộ trình điều trị mới
```powershell
$newTreatmentPlan = @{
    id = "TP-2026-100"
    patientId = "BN-2026-0200"
    patientName = "Nguyễn Văn Test"
    patientAge = 50
    patientGender = "Nam"
    diagnosis = "Tăng huyết áp độ II"
    goals = "Kiểm soát huyết áp dưới 140/90"
    progress = 0
    status = "Đang thực hiện"
    followUpPeriod = "Mỗi 3 tháng"
    doctorName = "BS. Test Doctor"
    priority = "Bình thường"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/treatment-plans" -Method Post -Body $newTreatmentPlan -ContentType "application/json" | ConvertTo-Json -Depth 10
```

### Tạo ticket mới
```powershell
$newTicket = @{
    id = "TK-2026-0100"
    title = "Tư vấn test case"
    description = "Đây là ticket test để kiểm tra hệ thống"
    patientId = "BN-2026-0200"
    patientName = "Nguyễn Văn Test"
    creatorName = "NV. Test User"
    creatorRole = "Y tá"
    assignedDoctor = "BS. Nguyễn Văn A"
    priority = "Ưu tiên trung bình"
    status = "Chờ phản hồi"
    category = "Tư vấn chuyên khoa"
    attachmentCount = 0
    commentCount = 0
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/tickets" -Method Post -Body $newTicket -ContentType "application/json" | ConvertTo-Json -Depth 10
```

---

## 5. Cập nhật dữ liệu (PUT)

### Cập nhật trạng thái đơn thuốc
```powershell
$updatedPrescription = @{
    id = "RX-2026-0100"
    patientId = "BN-2026-0200"
    patientName = "Nguyễn Văn Test"
    doctorName = "BS. Test Doctor"
    medicationCount = 5
    prescriptionDate = "2026-01-22"
    status = "Đã xác nhận"
    hasDrugInteraction = $false
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions/RX-2026-0100" -Method Put -Body $updatedPrescription -ContentType "application/json" | ConvertTo-Json -Depth 10
```

### Cập nhật trạng thái ticket
```powershell
$statusUpdate = @{
    status = "Đang xử lý"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/TK-2026-0100/status" -Method Put -Body $statusUpdate -ContentType "application/json" | ConvertTo-Json -Depth 10
```

---

## 6. Xóa dữ liệu (DELETE)

### Xóa đơn thuốc
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions/RX-2026-0100" -Method Delete | ConvertTo-Json -Depth 10
```

### Xóa lộ trình điều trị
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/treatment-plans/TP-2026-100" -Method Delete | ConvertTo-Json -Depth 10
```

---

## 7. Test với curl (nếu có Git Bash hoặc WSL)

### GET request
```bash
curl http://localhost:8080/api/prescriptions | jq
```

### POST request
```bash
curl -X POST http://localhost:8080/api/prescriptions \
  -H "Content-Type: application/json" \
  -d '{
    "id": "RX-2026-0100",
    "patientName": "Test Patient",
    "doctorName": "BS. Test",
    "status": "Chờ xử lý"
  }' | jq
```

---

## 8. Kết quả mong đợi

### Đơn thuốc (3 items):
```json
[
  {
    "id": "RX-2026-0089",
    "patientName": "Trần Văn Hùng",
    "status": "Đã xác nhận",
    "hasDrugInteraction": false
  },
  {
    "id": "RX-2026-0088",
    "patientName": "Lê Thị Mai",
    "status": "Chờ xử lý",
    "hasDrugInteraction": true
  },
  {
    "id": "RX-2026-0087",
    "patientName": "Phạm Văn Đức",
    "status": "Đã phát thuốc",
    "hasDrugInteraction": false
  }
]
```

### Stats:
```json
{
  "total": 3,
  "pending": 1,
  "warnings": 1
}
```

---

## 9. Troubleshooting

### Lỗi kết nối
```powershell
# Kiểm tra app có chạy không
Test-NetConnection -ComputerName localhost -Port 8080
```

### Xem logs
```powershell
# Logs sẽ hiện trong terminal đang chạy Spring Boot
# Tìm dòng có "Started CapstoneProjectApplication"
```

### Clear port 8080
```powershell
# Tìm process
netstat -ano | findstr :8080

# Kill process (thay <PID> bằng số thực tế)
taskkill /PID <PID> /F
```

---

**✅ Tất cả APIs đã sẵn sàng để test!**
