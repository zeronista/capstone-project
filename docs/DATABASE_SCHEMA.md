# TÀI LIỆU CƠ SỞ DỮ LIỆU - HỆ THỐNG AI CALLBOT PHÒNG KHÁM

## TỔNG QUAN HỆ THỐNG

Hệ thống quản lý phòng khám với tính năng AI Callbot tự động gọi điện cho bệnh nhân để khảo sát, tư vấn và hỗ trợ. Khi AI không thể giải quyết vấn đề, lễ tân sẽ can thiệp và chuyển cho bác sĩ xử lý. Mọi cuộc hội thoại được ghi lại để huấn luyện AI ngày càng thông minh hơn.

---

## 1. QUẢN LÝ NGƯỜI DÙNG

### 📋 Bảng `users` - Thông tin tài khoản và bảo mật

**Mục đích**: Lưu trữ thông tin đăng nhập, phân quyền và trạng thái bảo mật của tất cả người dùng trong hệ thống.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ | Bắt buộc |
|------------|---------|-------|----------|
| `id` | Mã số định danh duy nhất | 1, 2, 3... | ✅ |
| `email` | Địa chỉ email (dùng để đăng nhập) | nguyen.van.a@gmail.com | ⚠️ Ít nhất 1 trong email/phone |
| `phone` | Số điện thoại (dùng để đăng nhập) | 0912345678 | ⚠️ Ít nhất 1 trong email/phone |
| `password_hash` | Mật khẩu đã mã hóa | (được bảo mật) | ✅ (trừ OAuth) |
| `google_id` | ID Google (nếu đăng ký bằng Google) | 123456789... | ❌ |
| `role` | Vai trò trong hệ thống | PATIENT, DOCTOR, RECEPTIONIST, ADMIN | ✅ |
| `is_active` | Tài khoản có đang hoạt động không | true/false | ✅ (default: true) |
| `email_verified` | Email đã xác thực chưa | true/false | ✅ (default: false) |
| `phone_verified` | SĐT đã xác thực chưa | true/false | ✅ (default: false) |
| `created_at` | Ngày tạo tài khoản | 26/01/2026 10:30 | ✅ |
| `updated_at` | Ngày cập nhật gần nhất | 26/01/2026 15:45 | ✅ |
| `last_login` | Lần đăng nhập cuối | 26/01/2026 14:20 | ❌ |

**Các vai trò (role)**:
- **PATIENT**: Bệnh nhân - người sử dụng dịch vụ phòng khám
- **RECEPTIONIST**: Lễ tân - quản lý cuộc gọi AI, tạo ticket
- **DOCTOR**: Bác sĩ - tư vấn, điều trị, kê đơn
- **ADMIN**: Quản trị viên - quản lý toàn bộ hệ thống

**Quy tắc đặc biệt**:
- Bắt buộc phải có email HOẶC số điện thoại (ít nhất 1)
- Có thể đăng ký/đăng nhập bằng Google
- Email và số điện thoại phải duy nhất (không trùng lặp)

---

### 👤 Bảng `user_info` - Thông tin cá nhân người dùng

**Mục đích**: Lưu trữ thông tin cá nhân của người dùng, tách riêng khỏi thông tin bảo mật để dễ quản lý và tuân thủ quy định bảo vệ dữ liệu.

**Quan hệ**: OneToOne với bảng `users` (mỗi user có một record user_info)

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ | Bắt buộc |
|------------|---------|-------|----------|
| `id` | Mã số định danh | 1, 2, 3... | ✅ |
| `user_id` | Liên kết đến bảng users (unique) | 5 (tham chiếu users.id) | ✅ |
| `full_name` | Họ và tên đầy đủ | Nguyễn Văn A | ❌ |
| `date_of_birth` | Ngày sinh | 15/03/1990 | ❌ |
| `gender` | Giới tính | MALE, FEMALE, OTHER | ❌ |
| `address` | Địa chỉ | 123 Đường ABC, Quận 1, TP.HCM | ❌ |
| `avatar_url` | Link ảnh đại diện | https://... | ❌ |
| `created_at` | Ngày tạo | 26/01/2026 10:30 | ✅ |
| `updated_at` | Ngày cập nhật gần nhất | 26/01/2026 15:45 | ✅ |

**Các giá trị giới tính (gender)**:
- **MALE**: Nam
- **FEMALE**: Nữ  
- **OTHER**: Khác

**Ví dụ thực tế**:
```
- User ID: 1
  + Họ tên: Nguyễn Văn A
  + Ngày sinh: 15/03/1990
  + Giới tính: MALE
  + Địa chỉ: 123 Đường ABC, Quận 1, TP.HCM
```

---

### 👔 Bảng `staff_info` - Thông tin chi tiết nhân viên

**Mục đích**: Lưu thông tin bổ sung cho nhân viên (lễ tân, bác sĩ).

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã số định danh | 1, 2, 3... |
| `user_id` | Liên kết đến bảng users | 5 (tham chiếu users.id) |
| `employee_code` | Mã nhân viên | NV001, BS002 |
| `department` | Phòng ban | Khoa Nội, Khoa Ngoại |
| `specialization` | Chuyên khoa (cho bác sĩ) | Tim mạch, Da liễu |
| `license_number` | Số giấy phép hành nghề | BS12345 |
| `hire_date` | Ngày vào làm | 01/01/2024 |
| `status` | Trạng thái | ACTIVE, INACTIVE, ON_LEAVE |

**Ví dụ thực tế**:
```
- Bác sĩ Nguyễn Văn B (user_id=5)
  + Mã NV: BS001
  + Chuyên khoa: Tim mạch
  + Giấy phép: BS12345
  + Trạng thái: ACTIVE (đang làm việc)
```

---

## 2. QUẢN LÝ TÀI LIỆU BỆNH NHÂN

### 📁 Bảng `patient_documents` - Tài liệu của bệnh nhân

**Mục đích**: Bệnh nhân có thể upload các tài liệu y tế như lịch sử khám bệnh, đơn thuốc cũ, kết quả xét nghiệm.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã tài liệu | 1, 2, 3... |
| `patient_id` | ID bệnh nhân | 10 (tham chiếu users.id) |
| `document_type` | Loại tài liệu | MEDICAL_HISTORY, PRESCRIPTION, TEST_RESULT |
| `file_name` | Tên file | ket_qua_xet_nghiem_mau.pdf |
| `file_url` | Link file trên server | https://storage.../file.pdf |
| `file_size` | Kích thước file | 2048000 (bytes) |
| `description` | Mô tả | Kết quả xét nghiệm máu ngày 15/01/2026 |
| `is_verified` | Đã được xác minh chưa | true/false |
| `verified_by` | Người xác minh | 5 (ID bác sĩ) |
| `verified_at` | Thời gian xác minh | 26/01/2026 10:00 |
| `upload_date` | Ngày upload | 25/01/2026 14:30 |

**Các loại tài liệu**:
- **MEDICAL_HISTORY**: Lịch sử khám bệnh, bệnh án
- **PRESCRIPTION**: Đơn thuốc (cũ hoặc từ nơi khác)
- **TEST_RESULT**: Kết quả xét nghiệm, chẩn đoán hình ảnh
- **OTHER**: Loại khác

**Quy trình thực tế**:
1. Bệnh nhân upload tài liệu lên hệ thống
2. Bác sĩ/nhân viên xem xét và xác minh (verified)
3. Tài liệu được sử dụng để tham khảo khi khám bệnh

---

## 3. AI CALLBOT & CHIẾN DỊCH GỌI ĐIỆN

### 📝 Bảng `survey_templates` - Mẫu kịch bản khảo sát

**Mục đích**: Lưu các kịch bản/mẫu câu hỏi mà AI Callbot sẽ sử dụng khi gọi cho bệnh nhân.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã mẫu | 1, 2, 3... |
| `template_name` | Tên mẫu | Khảo sát sau khám bệnh |
| `description` | Mô tả | Khảo sát mức độ hài lòng của bệnh nhân |
| `questions_json` | Danh sách câu hỏi (dạng JSON) | {"questions": [...]} |
| `is_active` | Đang được sử dụng không | true/false |
| `created_by` | Người tạo | 3 (ID lễ tân) |
| `created_at` | Ngày tạo | 20/01/2026 |

**Ví dụ mẫu kịch bản**:
```
Tên: "Khảo sát sau khám"
Câu hỏi:
1. Anh/chị có hài lòng với dịch vụ không?
2. Tình trạng sức khỏe hiện tại như thế nào?
3. Anh/chị có tuân thủ uống thuốc theo đơn không?
```

---

### 📞 Bảng `call_campaigns` - Chiến dịch gọi điện

**Mục đích**: Quản lý các chiến dịch gọi điện tự động (ví dụ: khảo sát hàng loạt, nhắc lịch hẹn).

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã chiến dịch | 1, 2, 3... |
| `campaign_name` | Tên chiến dịch | Khảo sát tháng 1/2026 |
| `campaign_type` | Loại chiến dịch | FOLLOW_UP, SURVEY, APPOINTMENT_REMINDER |
| `target_audience` | Đối tượng mục tiêu | EXISTING_PATIENTS, NEW_PATIENTS, ALL |
| `script_template` | Kịch bản cuộc gọi | Xin chào, tôi là trợ lý ảo... |
| `survey_template_id` | Liên kết đến mẫu khảo sát | 1 (tham chiếu survey_templates) |
| `start_date` | Ngày bắt đầu | 01/02/2026 |
| `end_date` | Ngày kết thúc | 28/02/2026 |
| `status` | Trạng thái | DRAFT, ACTIVE, PAUSED, COMPLETED |
| `created_by` | Người tạo | 3 (ID lễ tân) |

**Các loại chiến dịch**:
- **FOLLOW_UP**: Theo dõi sau khi khám bệnh
- **SURVEY**: Khảo sát ý kiến
- **APPOINTMENT_REMINDER**: Nhắc lịch hẹn tái khám
- **HEALTH_CHECK**: Kiểm tra tình trạng sức khỏe

**Đối tượng mục tiêu**:
- **EXISTING_PATIENTS**: Bệnh nhân cũ (đã khám)
- **NEW_PATIENTS**: Bệnh nhân mới (chưa khám)
- **ALL**: Tất cả

---

### 📱 Bảng `call_logs` - Lịch sử cuộc gọi

**Mục đích**: Ghi lại TẤT CẢ cuộc gọi (AI hoặc người thật) với đầy đủ thông tin.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã cuộc gọi | 1, 2, 3... |
| `campaign_id` | Thuộc chiến dịch nào | 1 (nếu có) |
| `patient_id` | Bệnh nhân được gọi | 10 |
| `phone_number` | Số điện thoại gọi | 0912345678 |
| `call_type` | Loại cuộc gọi | AI_BOT, HUMAN_TAKEOVER, MANUAL |
| `call_status` | Trạng thái | COMPLETED, FAILED, NO_ANSWER |
| `start_time` | Thời gian bắt đầu | 26/01/2026 09:00:00 |
| `end_time` | Thời gian kết thúc | 26/01/2026 09:05:30 |
| `duration` | Thời lượng (giây) | 330 |
| `recording_url` | Link file ghi âm | https://storage.../call123.mp3 |
| `transcript_text` | Nội dung cuộc gọi (văn bản) | AI: Xin chào... BN: Tôi muốn hỏi... |
| `ai_confidence_score` | Độ tin cậy AI (0-1) | 0.85 (AI tự tin 85%) |
| `is_escalated` | Đã chuyển cho người không | true/false |
| `escalation_reason` | Lý do chuyển | Câu hỏi vượt kiến thức AI |
| `handled_by` | Người xử lý (lễ tân/bác sĩ) | 5 |
| `survey_responses` | Câu trả lời khảo sát (JSON) | {"q1": "Hài lòng", "q2": "Tốt"} |
| `created_at` | Thời gian tạo | 26/01/2026 09:00 |

**Quy trình cuộc gọi**:

```
┌─────────────────┐
│ AI Bot gọi      │ ← AI_BOT
│ cho bệnh nhân   │
└────────┬────────┘
         │
    ✅ Trả lời OK?
         │
    ┌────┴────┐
    │         │
   YES       NO (câu hỏi khó)
    │         │
 [Hoàn  │  is_escalated = true
  thành]│  escalation_reason = "..."
    │         │
    │    ┌────▼────────┐
    │    │ Lễ tân nhận │ ← HUMAN_TAKEOVER
    │    │ thông báo   │
    │    └────┬────────┘
    │         │
    │    Tạo Ticket → Bác sĩ xử lý
    │
    └─────► Lưu transcript → Huấn luyện AI
```

---

## 4. HỆ THỐNG TICKET & HỖ TRỢ

### 🎫 Bảng `tickets` - Phiếu yêu cầu hỗ trợ

**Mục đích**: Khi AI không thể giải quyết, lễ tân tạo ticket để bác sĩ xử lý trực tiếp.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã ticket | 1, 2, 3... |
| `call_id` | Từ cuộc gọi nào | 100 (tham chiếu call_logs) |
| `patient_id` | Bệnh nhân | 10 |
| `title` | Tiêu đề ngắn gọn | Tư vấn đơn thuốc điều trị đau dạ dày |
| `description` | Mô tả chi tiết | Bệnh nhân hỏi về tác dụng phụ... |
| `priority` | Mức độ ưu tiên | LOW, MEDIUM, HIGH, URGENT |
| `category` | Danh mục | MEDICAL_QUERY, APPOINTMENT, PRESCRIPTION |
| `status` | Trạng thái | OPEN, ASSIGNED, IN_PROGRESS, RESOLVED |
| `created_by` | Lễ tân tạo | 3 |
| `assigned_to` | Bác sĩ được giao | 5 |
| `resolved_by` | Người giải quyết | 5 |
| `created_at` | Ngày tạo | 26/01/2026 09:10 |
| `resolved_at` | Ngày giải quyết | 26/01/2026 10:30 |

**Mức độ ưu tiên**:
- **URGENT**: Khẩn cấp (phản ứng thuốc, cấp cứu)
- **HIGH**: Cao (cần giải quyết trong ngày)
- **MEDIUM**: Trung bình (1-2 ngày)
- **LOW**: Thấp (không gấp)

**Các danh mục**:
- **MEDICAL_QUERY**: Câu hỏi về y tế
- **APPOINTMENT**: Vấn đề về lịch hẹn
- **PRESCRIPTION**: Vấn đề về đơn thuốc
- **TECHNICAL**: Vấn đề kỹ thuật
- **OTHER**: Khác

**Chu trình ticket**:
```
OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
 ↑        ↓          ↓             ↓
Lễ tân  Giao cho  Bác sĩ đang  Đã giải
 tạo     bác sĩ    xử lý        quyết
```

---

### 💬 Bảng `ticket_messages` - Tin nhắn trong ticket

**Mục đích**: Lưu toàn bộ cuộc hội thoại giữa bệnh nhân - bác sĩ - lễ tân trong ticket.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã tin nhắn | 1, 2, 3... |
| `ticket_id` | Thuộc ticket nào | 5 |
| `sender_id` | Người gửi | 10 (bệnh nhân) hoặc 5 (bác sĩ) |
| `message_text` | Nội dung tin nhắn | Tôi bị đau bụng sau khi uống thuốc |
| `message_type` | Loại tin nhắn | TEXT, FILE, SYSTEM |
| `attachment_url` | File đính kèm | https://.../image.jpg |
| `is_internal_note` | Ghi chú nội bộ không | true/false |
| `created_at` | Thời gian gửi | 26/01/2026 09:15 |

**Loại tin nhắn**:
- **TEXT**: Tin nhắn văn bản thông thường
- **FILE**: Có file đính kèm (ảnh, PDF...)
- **SYSTEM**: Tin nhắn tự động (vd: "Ticket đã được giao cho Bác sĩ X")

**Ghi chú nội bộ** (`is_internal_note = true`):
- Chỉ nhìn thấy bởi nhân viên (lễ tân, bác sĩ)
- Bệnh nhân KHÔNG thấy
- Ví dụ: "Bệnh nhân này có tiền sử dị ứng thuốc A"

---

## 5. TRÍ THỨC & HUẤN LUYỆN AI

### 🧠 Bảng `knowledge_base` - Cơ sở tri thức

**Mục đích**: Lưu trữ kiến thức để AI học tập và trả lời câu hỏi. Càng nhiều tri thức, AI càng thông minh.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã tri thức | 1, 2, 3... |
| `category` | Danh mục | Thuốc, Bệnh lý, Chế độ ăn |
| `question` | Câu hỏi | Paracetamol có tác dụng gì? |
| `answer` | Câu trả lời | Paracetamol giúp hạ sốt, giảm đau... |
| `context` | Ngữ cảnh bổ sung | Liều dùng: 500mg mỗi 6 giờ |
| `source_type` | Nguồn tri thức từ đâu | MANUAL, CALL_TRANSCRIPT, DOCTOR_INPUT |
| `source_id` | ID nguồn | 100 (nếu từ call_id) |
| `confidence_score` | Độ tin cậy (0-1) | 0.95 |
| `usage_count` | Số lần được sử dụng | 150 |
| `is_approved` | Đã được duyệt chưa | true/false |
| `approved_by` | Bác sĩ phê duyệt | 5 |
| `approved_at` | Ngày phê duyệt | 26/01/2026 |
| `last_used_at` | Lần dùng gần nhất | 26/01/2026 14:00 |

**Nguồn tri thức**:
- **MANUAL**: Bác sĩ/nhân viên nhập tay
- **CALL_TRANSCRIPT**: Học từ cuộc gọi thực tế
- **DOCTOR_INPUT**: Bác sĩ cung cấp sau khi giải quyết ticket

**Quy trình học tập của AI**:
```
1. Cuộc gọi/Ticket được giải quyết
   ↓
2. Nội dung được trích xuất → Knowledge Base (chưa duyệt)
   ↓
3. Bác sĩ xem xét và phê duyệt (is_approved = true)
   ↓
4. AI sử dụng tri thức này cho lần gọi sau
   ↓
5. usage_count tăng lên mỗi lần sử dụng
```

---

### 📊 Bảng `ai_training_data` - Dữ liệu huấn luyện AI

**Mục đích**: Lưu dữ liệu thô từ cuộc gọi/ticket để huấn luyện và cải thiện AI.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã dữ liệu | 1, 2, 3... |
| `call_id` | Từ cuộc gọi | 100 |
| `ticket_id` | Từ ticket | 5 |
| `input_text` | Câu hỏi của bệnh nhân | Tôi bị đau đầu nên uống thuốc gì? |
| `expected_output` | Câu trả lời mong muốn | Nên uống Paracetamol 500mg... |
| `actual_output` | Câu trả lời AI đã đưa ra | Bạn nên uống thuốc giảm đau... |
| `feedback_score` | Đánh giá (1-5) | 4 (tốt) |
| `is_used_for_training` | Đã dùng để huấn luyện chưa | true/false |
| `training_batch_id` | Lô huấn luyện | BATCH_2026_01 |
| `created_at` | Ngày tạo | 26/01/2026 |

**Quy trình huấn luyện**:
```
1. Thu thập dữ liệu từ call_logs và tickets
   ↓
2. Đánh giá chất lượng (feedback_score >= 4)
   ↓
3. Gộp thành lô huấn luyện (training_batch_id)
   ↓
4. Huấn luyện model AI
   ↓
5. Đánh dấu is_used_for_training = true
```

---

## 6. KẾ HOẠCH ĐIỀU TRỊ

### 💊 Bảng `treatment_plans` - Kế hoạch điều trị

**Mục đích**: Bác sĩ lập kế hoạch điều trị cho bệnh nhân. AI có thể gợi ý nhưng quyết định cuối cùng thuộc về bác sĩ.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã kế hoạch | 1, 2, 3... |
| `patient_id` | Bệnh nhân | 10 |
| `doctor_id` | Bác sĩ phụ trách | 5 |
| `diagnosis` | Chẩn đoán | Viêm loét dạ dày - tá tràng |
| `treatment_goal` | Mục tiêu điều trị | Giảm triệu chứng đau, chữa lành vết loét |
| `start_date` | Ngày bắt đầu | 26/01/2026 |
| `expected_end_date` | Dự kiến kết thúc | 26/02/2026 |
| `status` | Trạng thái | DRAFT, ACTIVE, COMPLETED, CANCELLED |
| `ai_suggested` | AI có gợi ý không | true/false |
| `ai_suggestion_data` | Dữ liệu gợi ý AI (JSON) | {"drugs": [...], "lifestyle": [...]} |
| `created_at` | Ngày tạo | 26/01/2026 |

**Ví dụ kế hoạch điều trị**:
```
Bệnh nhân: Nguyễn Văn A (30 tuổi)
Chẩn đoán: Viêm loét dạ dày
Mục tiêu: Giảm đau, chữa lành vết loét trong 4 tuần
Thời gian: 26/01/2026 - 26/02/2026
Trạng thái: ACTIVE (đang thực hiện)

AI gợi ý: Thuốc kháng acid, chế độ ăn kiêng
Bác sĩ quyết định: Chấp nhận + bổ sung thuốc kháng sinh
```

---

### 📝 Bảng `treatment_plan_items` - Chi tiết kế hoạch

**Mục đích**: Các bước cụ thể trong kế hoạch điều trị.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã chi tiết | 1, 2, 3... |
| `plan_id` | Thuộc kế hoạch nào | 1 |
| `item_type` | Loại mục | MEDICATION, THERAPY, LIFESTYLE, CHECKUP |
| `description` | Mô tả | Uống thuốc Omeprazole 20mg |
| `frequency` | Tần suất | 2 lần/ngày (sáng - tối) |
| `duration` | Thời gian | 4 tuần |
| `notes` | Ghi chú | Uống trước bữa ăn 30 phút |
| `status` | Trạng thái | PENDING, ONGOING, COMPLETED, SKIPPED |

**Các loại mục điều trị**:
- **MEDICATION**: Dùng thuốc
- **THERAPY**: Liệu pháp (vật lý trị liệu, tâm lý...)
- **LIFESTYLE**: Thay đổi lối sống (ăn uống, tập thể dục)
- **CHECKUP**: Tái khám, xét nghiệm

**Ví dụ kế hoạch hoàn chỉnh**:
```
Kế hoạch điều trị #1: Viêm loét dạ dày

Chi tiết:
1. [MEDICATION] Omeprazole 20mg - 2 lần/ngày - 4 tuần
2. [MEDICATION] Clarithromycin 500mg - 2 lần/ngày - 2 tuần  
3. [LIFESTYLE] Kiêng cay, chua, rượu bia - Liên tục
4. [CHECKUP] Tái khám sau 2 tuần - 1 lần
```

---

## 7. ĐƠN THUỐC

### 💊 Bảng `prescriptions` - Đơn thuốc

**Mục đích**: Bác sĩ kê đơn thuốc cho bệnh nhân.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã đơn thuốc | 1, 2, 3... |
| `patient_id` | Bệnh nhân | 10 |
| `doctor_id` | Bác sĩ kê đơn | 5 |
| `prescription_date` | Ngày kê đơn | 26/01/2026 |
| `diagnosis` | Chẩn đoán | Viêm họng cấp |
| `notes` | Ghi chú | Uống đủ nước, nghỉ ngơi |
| `status` | Trạng thái | ACTIVE, COMPLETED, CANCELLED |
| `created_at` | Ngày tạo | 26/01/2026 10:00 |

---

### 💊 Bảng `prescription_details` - Chi tiết đơn thuốc

**Mục đích**: Liệt kê các loại thuốc trong đơn.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã chi tiết | 1, 2, 3... |
| `prescription_id` | Thuộc đơn thuốc nào | 1 |
| `medicine_name` | Tên thuốc | Amoxicillin |
| `dosage` | Liều lượng | 500mg |
| `frequency` | Tần suất dùng | 3 lần/ngày |
| `duration` | Thời gian dùng | 7 ngày |
| `instructions` | Hướng dẫn | Uống sau ăn, uống đủ liều |
| `quantity` | Số lượng | 21 viên |

**Ví dụ đơn thuốc hoàn chỉnh**:
```
Đơn thuốc #1 - Ngày 26/01/2026
Bệnh nhân: Nguyễn Văn A
Chẩn đoán: Viêm họng cấp

Chi tiết:
1. Amoxicillin 500mg
   - Liều dùng: 1 viên x 3 lần/ngày
   - Thời gian: 7 ngày
   - Số lượng: 21 viên
   - Cách dùng: Uống sau ăn

2. Paracetamol 500mg
   - Liều dùng: 1-2 viên khi sốt/đau
   - Thời gian: Theo nhu cầu
   - Số lượng: 10 viên
   - Cách dùng: Không quá 8 viên/ngày

Ghi chú: Uống đủ nước, nghỉ ngơi
```

---

## 8. THÔNG BÁO & PHẢN HỒI

### 🔔 Bảng `notifications` - Thông báo

**Mục đích**: Gửi thông báo cho người dùng về các sự kiện quan trọng.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã thông báo | 1, 2, 3... |
| `user_id` | Người nhận | 10 |
| `notification_type` | Loại thông báo | TICKET, REMINDER, MESSAGE, SYSTEM, CALL |
| `title` | Tiêu đề | Bác sĩ đã trả lời câu hỏi của bạn |
| `content` | Nội dung | Bác sĩ Nguyễn Văn B đã trả lời... |
| `reference_id` | Liên quan đến | 5 (ticket_id) |
| `reference_type` | Loại tham chiếu | TICKET, CALL, REMINDER |
| `is_read` | Đã đọc chưa | true/false |
| `created_at` | Thời gian tạo | 26/01/2026 10:30 |
| `read_at` | Thời gian đọc | 26/01/2026 11:00 |

**Các loại thông báo**:
- **TICKET**: Liên quan đến ticket (có câu trả lời mới, ticket được giao...)
- **REMINDER**: Nhắc lịch hẹn, nhắc uống thuốc
- **MESSAGE**: Tin nhắn mới
- **SYSTEM**: Thông báo hệ thống (bảo trì, cập nhật...)
- **CALL**: Liên quan đến cuộc gọi

---

### ⭐ Bảng `feedbacks` - Phản hồi đánh giá

**Mục đích**: Thu thập ý kiến của bệnh nhân về dịch vụ, cuộc gọi AI, chất lượng tư vấn.

**Các thông tin chính**:

| Tên trường | Ý nghĩa | Ví dụ |
|------------|---------|-------|
| `id` | Mã phản hồi | 1, 2, 3... |
| `call_id` | Đánh giá cuộc gọi | 100 |
| `ticket_id` | Đánh giá ticket | 5 |
| `user_id` | Người đánh giá | 10 (bệnh nhân) |
| `rating` | Số sao (1-5) | 4 sao |
| `feedback_text` | Nội dung | AI rất lịch sự nhưng chưa trả lời được câu hỏi |
| `feedback_type` | Loại đánh giá | CALL_QUALITY, SERVICE, AI_PERFORMANCE |
| `is_reviewed` | Đã xem xét chưa | true/false |
| `reviewed_by` | Người xem xét | 3 (lễ tân) |
| `created_at` | Ngày gửi | 26/01/2026 09:30 |

**Mục đích sử dụng**:
- Đánh giá chất lượng AI (để cải thiện)
- Đánh giá dịch vụ phòng khám
- Tìm điểm yếu cần khắc phục
- Thống kê mức độ hài lòng

---

## 9. SƠ ĐỒ QUAN HỆ TỔNG QUAN

```
┌──────────────┐
│    USERS     │ ◄────┐
│ (Người dùng) │      │
└──────┬───────┘      │
       │              │
       ├──────────────┼─► STAFF_INFO (Thông tin nhân viên)
       │              │
       ├──────────────┼─► PATIENT_DOCUMENTS (Tài liệu BN)
       │              │
       ├──────────────┼─► CALL_CAMPAIGNS (Chiến dịch gọi)
       │              │   └─► CALL_LOGS (Lịch sử cuộc gọi)
       │              │       └─► TICKETS (Phiếu hỗ trợ)
       │              │           └─► TICKET_MESSAGES (Tin nhắn)
       │              │
       ├──────────────┼─► KNOWLEDGE_BASE (Tri thức AI)
       │              │
       ├──────────────┼─► AI_TRAINING_DATA (Dữ liệu huấn luyện)
       │              │
       ├──────────────┼─► TREATMENT_PLANS (Kế hoạch điều trị)
       │              │   └─► TREATMENT_PLAN_ITEMS (Chi tiết)
       │              │
       ├──────────────┼─► PRESCRIPTIONS (Đơn thuốc)
       │              │   └─► PRESCRIPTION_DETAILS (Chi tiết)
       │              │
       ├──────────────┼─► NOTIFICATIONS (Thông báo)
       │              │
       └──────────────┼─► FEEDBACKS (Phản hồi)
                      │
                      └─► SURVEY_TEMPLATES (Mẫu khảo sát)
```

---

## 10. QUY TRÌNH NGHIỆP VỤ TỔNG THỂ

### 📞 Quy trình AI Callbot gọi điện

```
BƯỚC 1: TẠO CHIẾN DỊCH
├─ Lễ tân tạo chiến dịch gọi (call_campaigns)
├─ Chọn mẫu kịch bản (survey_templates)
├─ Chọn đối tượng: Bệnh nhân cũ/mới/tất cả
└─ Kích hoạt chiến dịch (status = ACTIVE)

BƯỚC 2: AI GỌI ĐIỆN TỰ ĐỘNG
├─ Hệ thống AI gọi theo danh sách
├─ Ghi âm cuộc gọi
├─ Chuyển đổi giọng nói thành văn bản (transcript_text)
└─ Lưu vào call_logs

BƯỚC 3A: AI TRẢ LỜI THÀNH CÔNG ✅
├─ AI confidence_score cao (>= 0.8)
├─ Bệnh nhân hài lòng
├─ Lưu câu hỏi-trả lời vào knowledge_base
└─ Hoàn thành (call_status = COMPLETED)

BƯỚC 3B: AI KHÔNG TRẢ LỜI ĐƯỢC ❌
├─ AI confidence_score thấp (< 0.8)
├─ Câu hỏi nằm ngoài kiến thức
├─ Đánh dấu is_escalated = true
└─ Chuyển sang BƯỚC 4

BƯỚC 4: LỄ TÂN NHẬN THÔNG BÁO
├─ Lễ tân xem cuộc gọi cần hỗ trợ
├─ Đọc transcript_text
├─ Tạo ticket (tickets)
└─ Giao cho bác sĩ phù hợp

BƯỚC 5: BÁC SĨ XỬ LÝ TICKET
├─ Bác sĩ nhận ticket (status = ASSIGNED)
├─ Trao đổi qua ticket_messages
├─ Tư vấn, kê đơn (nếu cần)
├─ Giải quyết xong (status = RESOLVED)
└─ Nội dung lưu vào knowledge_base

BƯỚC 6: AI HỌC TẬP
├─ Trích xuất Q&A từ ticket đã giải quyết
├─ Bác sĩ phê duyệt (knowledge_base.is_approved)
├─ Lưu vào ai_training_data
├─ Huấn luyện model AI
└─ AI thông minh hơn cho lần sau!
```

---

## 11. CÁC CHỈ SỐ & BÁO CÁO QUAN TRỌNG

### 📊 Dashboard Admin cần hiển thị:

**1. Thống kê người dùng**
- Tổng số bệnh nhân: `COUNT(users WHERE role = PATIENT)`
- Tổng số bác sĩ: `COUNT(users WHERE role = DOCTOR)`
- Tài khoản mới hôm nay: `COUNT(users WHERE created_at = TODAY)`

**2. Thống kê cuộc gọi**
- Tổng cuộc gọi hôm nay: `COUNT(call_logs WHERE created_at = TODAY)`
- Tỷ lệ thành công: `(COMPLETED / TOTAL) × 100%`
- Tỷ lệ escalate: `(is_escalated = true / TOTAL) × 100%`
- Điểm AI trung bình: `AVG(ai_confidence_score)`

**3. Thống kê ticket**
- Ticket đang chờ: `COUNT(tickets WHERE status = OPEN)`
- Ticket khẩn cấp: `COUNT(tickets WHERE priority = URGENT AND status != RESOLVED)`
- Thời gian xử lý trung bình: `AVG(resolved_at - created_at)`

**4. Đánh giá dịch vụ**
- Điểm hài lòng trung bình: `AVG(feedbacks.rating)`
- Phản hồi tốt (≥4 sao): `COUNT(feedbacks WHERE rating >= 4)`
- Phản hồi kém (≤2 sao): `COUNT(feedbacks WHERE rating <= 2)`

**5. Hiệu suất AI**
- Tri thức được duyệt: `COUNT(knowledge_base WHERE is_approved = true)`
- Tri thức phổ biến nhất: `ORDER BY usage_count DESC LIMIT 10`
- Dữ liệu huấn luyện: `COUNT(ai_training_data WHERE feedback_score >= 4)`

---

## 12. BACKUP & BẢO MẬT

### 🔒 Dữ liệu nhạy cảm cần bảo vệ:

1. **Thông tin cá nhân**
   - `users.email`, `users.phone_number`
   - `users.password_hash` (đã mã hóa)
   - `users.date_of_birth`, `users.address`

2. **Thông tin y tế**
   - `patient_documents.*` (tất cả tài liệu)
   - `treatment_plans.diagnosis`
   - `prescriptions.*` (đơn thuốc)
   - `call_logs.transcript_text` (cuộc hội thoại)

3. **File lưu trữ**
   - `patient_documents.file_url`
   - `call_logs.recording_url`

### 💾 Chiến lược backup:

- **Hàng ngày**: Backup incremental (dữ liệu thay đổi)
- **Hàng tuần**: Full backup toàn bộ
- **Lưu trữ**: Tối thiểu 6 tháng
- **Encryption**: Mã hóa dữ liệu nhạy cảm

---

## 13. KẾT LUẬN

### Tóm tắt hệ thống:

Hệ thống được thiết kế với **16 bảng dữ liệu** phục vụ cho:

1. **Quản lý người dùng** (2 bảng)
   - Users, Staff Info

2. **Quản lý tài liệu bệnh nhân** (1 bảng)
   - Patient Documents

3. **AI Callbot & Cuộc gọi** (3 bảng)
   - Survey Templates, Call Campaigns, Call Logs

4. **Hỗ trợ & Ticket** (2 bảng)
   - Tickets, Ticket Messages

5. **Tri thức & Huấn luyện AI** (2 bảng)
   - Knowledge Base, AI Training Data

6. **Kế hoạch điều trị** (2 bảng)
   - Treatment Plans, Treatment Plan Items

7. **Đơn thuốc** (2 bảng)
   - Prescriptions, Prescription Details

8. **Thông báo & Phản hồi** (2 bảng)
   - Notifications, Feedbacks

### Ưu điểm thiết kế:

✅ **Linh hoạt**: Hỗ trợ nhiều kịch bản nghiệp vụ
✅ **Mở rộng**: Dễ thêm tính năng mới
✅ **Thông minh**: AI học từ dữ liệu thực tế
✅ **An toàn**: Phân quyền rõ ràng, dữ liệu được bảo vệ
✅ **Truy vết**: Lưu đầy đủ lịch sử thao tác

---

**Tài liệu được tạo ngày**: 26/01/2026
**Phiên bản**: 1.0
**Người tạo**: Hệ thống AI Assistant
