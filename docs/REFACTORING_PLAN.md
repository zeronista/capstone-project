# 📁 Kế hoạch Refactor Resources - ABClinic

> **Version:** 1.0.0  
> **Date:** February 1, 2026  
> **Project:** ABClinic Capstone Project

---

## 📊 PHẦN 1: PHÂN TÍCH CODEBASE HIỆN TẠI

### 1.1 Cấu trúc hiện tại

```
src/main/resources/
├── application.properties
├── application-local.properties
├── application-local.properties.example
├── logback-spring.xml
├── static/
│   ├── css/
│   │   └── app.css                          # 577 lines - Unified CSS
│   ├── images/
│   │   └── Logo.jpg
│   └── js/
│       ├── admin-accounts.js                # 489 lines - Admin module
│       ├── api-client.js                    # 499 lines - Core/Shared
│       ├── app.js                           # 329 lines - Core/Shared utilities
│       ├── dashboard-charts.js              # 133 lines - Charts module
│       ├── doctor-health-forecast.js        # Doctor module
│       ├── doctor-prescription-create.js    # Doctor module
│       ├── doctor-prescriptions.js          # 314 lines - Doctor module
│       ├── doctor-sidebar-controller.js     # Doctor module
│       ├── doctor-tickets.js                # Doctor module
│       ├── doctor-treatments.js             # Doctor module
│       ├── latest.sdk.bundle.min.js         # External SDK (Stringee)
│       ├── patient.js                       # 834 lines - Patient module
│       ├── profile-patient.js               # 497 lines - Profile module
│       ├── profile-staff.js                 # 478 lines - Profile module
│       ├── receptionist-dashboard.js        # 477 lines - Receptionist module
│       ├── reports-detail-charts.js         # Reports module
│       ├── sidebar-controller.js            # 178 lines - Core/Shared
│       └── tailwind-config.js               # 211 lines - Config
└── templates/
    ├── index.html                           # Landing page
    ├── admin/
    │   ├── accounts.html                    # 1083 lines
    │   ├── profile.html
    │   └── users.html
    ├── ai/
    │   ├── call-detail.html
    │   ├── calls.html
    │   ├── config.html
    │   ├── voice.html
    │   └── web-call.html
    ├── auth/
    │   ├── forgot-password.html
    │   ├── login.html
    │   ├── register.html
    │   ├── resend-verification.html
    │   ├── reset-password.html
    │   └── verification-error.html
    ├── call/
    │   ├── history.html
    │   └── index.html
    ├── crm/
    │   ├── notifications.html
    │   ├── social.html
    │   ├── surveys.html
    │   └── tickets.html
    ├── doctor/
    │   ├── dashboard.html
    │   ├── health-forecast.html
    │   ├── knowledge.html
    │   ├── patient-detail.html
    │   ├── prescriptions/
    │   │   ├── create.html
    │   │   └── edit.html
    │   ├── prescriptions.html
    │   ├── profile.html
    │   ├── tickets.html
    │   └── treatments.html
    ├── email/
    │   ├── password-reset.html
    │   ├── verification.html
    │   └── welcome.html
    ├── error/
    │   ├── 403.html
    │   └── 404.html
    ├── fragments/
    │   ├── components.html                  # Shared components
    │   ├── doctor-layout.html               # Doctor-specific layout
    │   └── layout.html                      # 787 lines - Main layout
    ├── patient/
    │   ├── appointments.html
    │   ├── call.html
    │   ├── documents.html
    │   ├── index.html
    │   ├── prescriptions.html
    │   ├── tickets.html
    │   └── treatments.html
    ├── profile/
    │   ├── index.html
    │   └── patient.html
    ├── receptionist/
    │   ├── callbot.html
    │   ├── dashboard.html
    │   ├── profile.html
    │   └── tickets.html
    ├── reports/
    │   ├── detail.html
    │   └── index.html
    └── test/
        └── s3-upload.html
```

### 1.2 Xác định các Role/Module chính

| Module           | Mô tả                                 | Files liên quan                                                                         |
| ---------------- | ------------------------------------- | --------------------------------------------------------------------------------------- |
| **Core/Shared**  | Utilities, API client, Layout chung   | `app.js`, `api-client.js`, `sidebar-controller.js`, `tailwind-config.js`, `layout.html` |
| **Admin**        | Quản lý tài khoản, users, hệ thống    | `admin-accounts.js`, `admin/*.html`                                                     |
| **Doctor**       | Dashboard bác sĩ, đơn thuốc, điều trị | `doctor-*.js`, `doctor/*.html`                                                          |
| **Patient**      | Portal bệnh nhân                      | `patient.js`, `patient/*.html`                                                          |
| **Receptionist** | Lễ tân, quản lý queue                 | `receptionist-dashboard.js`, `receptionist/*.html`                                      |
| **Profile**      | Quản lý profile chung                 | `profile-*.js`, `profile/*.html`                                                        |
| **Auth**         | Xác thực, đăng nhập                   | `auth/*.html`                                                                           |
| **CRM**          | Customer relationship                 | `crm/*.html`                                                                            |
| **Reports**      | Báo cáo, thống kê                     | `dashboard-charts.js`, `reports-detail-charts.js`, `reports/*.html`                     |
| **AI/Call**      | AI Assistant, gọi điện                | `latest.sdk.bundle.min.js`, `ai/*.html`, `call/*.html`                                  |
| **Email**        | Templates email                       | `email/*.html`                                                                          |

### 1.3 Dependencies giữa các file

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CORE/SHARED LAYER                          │
├─────────────────────────────────────────────────────────────────────┤
│  tailwind-config.js ────► app.css ────► layout.html                 │
│        │                                    │                       │
│        ▼                                    ▼                       │
│  app.js (utilities) ◄──── api-client.js   fragments/*.html          │
│        │                      │                                     │
│        ▼                      ▼                                     │
│  sidebar-controller.js    All API calls                             │
└─────────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
    ┌──────────┐        ┌──────────┐        ┌──────────┐
    │  ADMIN   │        │  DOCTOR  │        │ PATIENT  │
    │ accounts │        │ prescr.  │        │ portal   │
    │          │        │ treat.   │        │          │
    └──────────┘        │ tickets  │        └──────────┘
                        │ forecast │
                        │ knowledge│
                        └──────────┘
```

---

## 📐 PHẦN 2: CẤU TRÚC MỚI ĐỀ XUẤT

### 2.1 Cấu trúc folder mới

```
src/main/resources/
├── application.properties
├── application-local.properties
├── application-local.properties.example
├── logback-spring.xml
│
├── static/
│   ├── css/
│   │   ├── core/
│   │   │   └── app.css                      # Main stylesheet
│   │   ├── components/
│   │   │   ├── _buttons.css                 # (Future: extracted components)
│   │   │   ├── _cards.css
│   │   │   ├── _forms.css
│   │   │   └── _modals.css
│   │   └── themes/
│   │       ├── _dark.css                    # (Future: dark mode specifics)
│   │       └── _light.css
│   │
│   ├── images/
│   │   ├── branding/
│   │   │   └── Logo.jpg
│   │   ├── icons/                           # (Future: custom icons)
│   │   └── illustrations/                   # (Future: illustrations)
│   │
│   └── js/
│       ├── config/
│       │   └── tailwind-config.js           # Tailwind configuration
│       │
│       ├── core/
│       │   ├── app.js                       # Common utilities (debounce, formatDate, showToast, etc.)
│       │   ├── api-client.js                # Centralized API client
│       │   └── sidebar-controller.js        # Universal sidebar controller
│       │
│       ├── modules/
│       │   ├── admin/
│       │   │   └── accounts.js              # admin-accounts.js
│       │   │
│       │   ├── doctor/
│       │   │   ├── dashboard.js             # (extracted from inline)
│       │   │   ├── health-forecast.js       # doctor-health-forecast.js
│       │   │   ├── prescriptions.js         # doctor-prescriptions.js
│       │   │   ├── prescription-create.js   # doctor-prescription-create.js
│       │   │   ├── sidebar-controller.js    # doctor-sidebar-controller.js
│       │   │   ├── tickets.js               # doctor-tickets.js
│       │   │   └── treatments.js            # doctor-treatments.js
│       │   │
│       │   ├── patient/
│       │   │   └── portal.js                # patient.js
│       │   │
│       │   ├── profile/
│       │   │   ├── patient.js               # profile-patient.js
│       │   │   └── staff.js                 # profile-staff.js
│       │   │
│       │   ├── receptionist/
│       │   │   └── dashboard.js             # receptionist-dashboard.js
│       │   │
│       │   └── reports/
│       │       ├── dashboard-charts.js      # dashboard-charts.js
│       │       └── detail-charts.js         # reports-detail-charts.js
│       │
│       └── vendor/
│           └── stringee/
│               └── sdk.bundle.min.js        # latest.sdk.bundle.min.js
│
└── templates/
    ├── index.html                           # Landing page
    │
    ├── layouts/                             # NEW: Extracted layouts
    │   ├── base.html                        # Base HTML structure
    │   ├── admin-layout.html                # Admin-specific layout
    │   ├── doctor-layout.html               # Doctor-specific layout
    │   ├── patient-layout.html              # Patient-specific layout
    │   └── receptionist-layout.html         # Receptionist-specific layout
    │
    ├── fragments/                           # Reusable components
    │   ├── head.html                        # Common <head> elements
    │   ├── scripts.html                     # Common script includes
    │   ├── sidebar/
    │   │   ├── admin-sidebar.html
    │   │   ├── doctor-sidebar.html
    │   │   ├── patient-sidebar.html
    │   │   └── receptionist-sidebar.html
    │   ├── header/
    │   │   └── main-header.html
    │   ├── modals/
    │   │   ├── confirm-modal.html
    │   │   └── form-modal.html
    │   └── components/
    │       ├── alert.html
    │       ├── badge.html
    │       ├── card.html
    │       ├── pagination.html
    │       └── table.html
    │
    ├── pages/
    │   ├── admin/
    │   │   ├── accounts.html
    │   │   ├── profile.html
    │   │   └── users.html
    │   │
    │   ├── ai/
    │   │   ├── call-detail.html
    │   │   ├── calls.html
    │   │   ├── config.html
    │   │   ├── voice.html
    │   │   └── web-call.html
    │   │
    │   ├── auth/
    │   │   ├── forgot-password.html
    │   │   ├── login.html
    │   │   ├── register.html
    │   │   ├── resend-verification.html
    │   │   ├── reset-password.html
    │   │   └── verification-error.html
    │   │
    │   ├── call/
    │   │   ├── history.html
    │   │   └── index.html
    │   │
    │   ├── crm/
    │   │   ├── notifications.html
    │   │   ├── social.html
    │   │   ├── surveys.html
    │   │   └── tickets.html
    │   │
    │   ├── doctor/
    │   │   ├── dashboard.html
    │   │   ├── health-forecast.html
    │   │   ├── knowledge.html
    │   │   ├── patient-detail.html
    │   │   ├── prescriptions/
    │   │   │   ├── create.html
    │   │   │   ├── edit.html
    │   │   │   └── index.html              # Renamed from prescriptions.html
    │   │   ├── profile.html
    │   │   ├── tickets.html
    │   │   └── treatments.html
    │   │
    │   ├── patient/
    │   │   ├── appointments.html
    │   │   ├── call.html
    │   │   ├── documents.html
    │   │   ├── index.html
    │   │   ├── prescriptions.html
    │   │   ├── tickets.html
    │   │   └── treatments.html
    │   │
    │   ├── profile/
    │   │   ├── index.html
    │   │   └── patient.html
    │   │
    │   ├── receptionist/
    │   │   ├── callbot.html
    │   │   ├── dashboard.html
    │   │   ├── profile.html
    │   │   └── tickets.html
    │   │
    │   └── reports/
    │       ├── detail.html
    │       └── index.html
    │
    ├── email/                               # Email templates (giữ nguyên vị trí)
    │   ├── password-reset.html
    │   ├── verification.html
    │   └── welcome.html
    │
    └── error/                               # Error pages (giữ nguyên vị trí)
        ├── 403.html
        └── 404.html
```

### 2.2 Lý do cho từng quyết định thiết kế

#### **JavaScript Organization:**

| Thay đổi             | Lý do                                                   |
| -------------------- | ------------------------------------------------------- |
| `js/config/`         | Tách riêng các file cấu hình (Tailwind, future configs) |
| `js/core/`           | Chứa các module core được sử dụng khắp nơi              |
| `js/modules/{role}/` | Tổ chức theo domain/module giúp dễ tìm và maintain      |
| `js/vendor/`         | Tách riêng third-party libraries, dễ quản lý version    |

#### **CSS Organization:**

| Thay đổi          | Lý do                                    |
| ----------------- | ---------------------------------------- |
| `css/core/`       | File CSS chính của ứng dụng              |
| `css/components/` | (Future) Tách các component CSS để reuse |
| `css/themes/`     | (Future) Hỗ trợ nhiều themes             |

#### **Templates Organization:**

| Thay đổi                    | Lý do                                                                  |
| --------------------------- | ---------------------------------------------------------------------- |
| `templates/layouts/`        | Tách layout templates riêng biệt                                       |
| `templates/fragments/`      | Tổ chức fragments theo chức năng (sidebar, header, modals, components) |
| `templates/pages/{module}/` | Các page templates theo module                                         |
| `templates/email/`          | Email templates riêng (không đổi)                                      |
| `templates/error/`          | Error pages riêng (không đổi)                                          |

---

## 🔄 PHẦN 3: KẾ HOẠCH MIGRATION

### Phase 1: Chuẩn bị (Không breaking changes)

1. Tạo cấu trúc folder mới
2. Copy files đến vị trí mới (giữ nguyên files cũ)
3. Test cấu trúc mới hoạt động

### Phase 2: Migrate JavaScript

1. Di chuyển core files: `app.js`, `api-client.js`, `sidebar-controller.js`
2. Di chuyển config: `tailwind-config.js`
3. Di chuyển vendor: `latest.sdk.bundle.min.js`
4. Di chuyển modules theo từng role
5. Cập nhật tất cả import paths trong templates

### Phase 3: Migrate CSS

1. Di chuyển `app.css` sang `css/core/`
2. Cập nhật import paths trong templates

### Phase 4: Migrate Templates

1. Tách fragments từ `layout.html`
2. Di chuyển templates vào `pages/`
3. Cập nhật tất cả `th:replace` và `th:insert` paths

### Phase 5: Cleanup

1. Xóa files cũ
2. Test toàn bộ chức năng
3. Cập nhật documentation

---

## 📋 PHẦN 4: MAPPING FILES CŨ → MỚI

### JavaScript Files

| File cũ                             | File mới                                    |
| ----------------------------------- | ------------------------------------------- |
| `/js/tailwind-config.js`            | `/js/config/tailwind-config.js`             |
| `/js/app.js`                        | `/js/core/app.js`                           |
| `/js/api-client.js`                 | `/js/core/api-client.js`                    |
| `/js/sidebar-controller.js`         | `/js/core/sidebar-controller.js`            |
| `/js/admin-accounts.js`             | `/js/modules/admin/accounts.js`             |
| `/js/doctor-health-forecast.js`     | `/js/modules/doctor/health-forecast.js`     |
| `/js/doctor-prescription-create.js` | `/js/modules/doctor/prescription-create.js` |
| `/js/doctor-prescriptions.js`       | `/js/modules/doctor/prescriptions.js`       |
| `/js/doctor-sidebar-controller.js`  | `/js/modules/doctor/sidebar-controller.js`  |
| `/js/doctor-tickets.js`             | `/js/modules/doctor/tickets.js`             |
| `/js/doctor-treatments.js`          | `/js/modules/doctor/treatments.js`          |
| `/js/patient.js`                    | `/js/modules/patient/portal.js`             |
| `/js/profile-patient.js`            | `/js/modules/profile/patient.js`            |
| `/js/profile-staff.js`              | `/js/modules/profile/staff.js`              |
| `/js/receptionist-dashboard.js`     | `/js/modules/receptionist/dashboard.js`     |
| `/js/dashboard-charts.js`           | `/js/modules/reports/dashboard-charts.js`   |
| `/js/reports-detail-charts.js`      | `/js/modules/reports/detail-charts.js`      |
| `/js/latest.sdk.bundle.min.js`      | `/js/vendor/stringee/sdk.bundle.min.js`     |

### CSS Files

| File cũ        | File mới            |
| -------------- | ------------------- |
| `/css/app.css` | `/css/core/app.css` |

### Template Path Changes

| Template cũ             | Template mới                        |
| ----------------------- | ----------------------------------- |
| `admin/*.html`          | `pages/admin/*.html`                |
| `ai/*.html`             | `pages/ai/*.html`                   |
| `auth/*.html`           | `pages/auth/*.html`                 |
| `call/*.html`           | `pages/call/*.html`                 |
| `crm/*.html`            | `pages/crm/*.html`                  |
| `doctor/*.html`         | `pages/doctor/*.html`               |
| `patient/*.html`        | `pages/patient/*.html`              |
| `profile/*.html`        | `pages/profile/*.html`              |
| `receptionist/*.html`   | `pages/receptionist/*.html`         |
| `reports/*.html`        | `pages/reports/*.html`              |
| `fragments/layout.html` | `layouts/base.html` + `fragments/*` |

---

## ⚠️ PHẦN 5: BREAKING CHANGES

### 5.1 Controller URL Mappings

**CẦN CẬP NHẬT** các Spring Controllers để phù hợp với template paths mới:

```java
// Trước:
return "admin/accounts";

// Sau:
return "pages/admin/accounts";
```

### 5.2 Thymeleaf Fragment References

**CẦN CẬP NHẬT** tất cả các fragment references:

```html
<!-- Trước -->
<head th:replace="~{fragments/layout :: head(title='...')}"></head>

<!-- Sau -->
<head th:replace="~{fragments/head :: head(title='...')}"></head>
```

### 5.3 Static Resource Paths

**CẦN CẬP NHẬT** tất cả các resource paths:

```html
<!-- Trước -->
<script th:src="@{/js/tailwind-config.js}"></script>
<script th:src="@{/js/app.js}"></script>

<!-- Sau -->
<script th:src="@{/js/config/tailwind-config.js}"></script>
<script th:src="@{/js/core/app.js}"></script>
```

---

## ✅ PHẦN 6: MIGRATION CHECKLIST

### Pre-Migration

- [ ] Backup toàn bộ resources folder
- [ ] Tạo branch mới cho refactoring
- [ ] Kiểm tra tất cả tests pass

### JavaScript Migration

- [ ] Tạo folder structure: `js/config/`, `js/core/`, `js/modules/`, `js/vendor/`
- [ ] Di chuyển `tailwind-config.js` → `js/config/`
- [ ] Di chuyển core files → `js/core/`
- [ ] Di chuyển vendor files → `js/vendor/stringee/`
- [ ] Di chuyển module files → `js/modules/{role}/`
- [ ] Cập nhật tất cả script imports trong templates

### CSS Migration

- [ ] Tạo folder: `css/core/`, `css/components/`, `css/themes/`
- [ ] Di chuyển `app.css` → `css/core/`
- [ ] Cập nhật CSS imports trong templates

### Template Migration

- [ ] Tạo folder: `templates/layouts/`, `templates/pages/`
- [ ] Tách fragments từ `layout.html`
- [ ] Di chuyển page templates → `pages/{module}/`
- [ ] Cập nhật tất cả fragment references
- [ ] Cập nhật Controller return values

### Testing

- [ ] Test tất cả pages render đúng
- [ ] Test tất cả JS functionality
- [ ] Test CSS styling
- [ ] Test responsive design
- [ ] Test dark mode

### Cleanup

- [ ] Xóa files cũ
- [ ] Update README
- [ ] Commit và push changes

---

## 📝 PHẦN 7: QUY TẮC ĐẶT TÊN

### JavaScript Files

- **Core modules**: `{function-name}.js` (lowercase, kebab-case)
- **Module files**: `{feature-name}.js` (lowercase, kebab-case)
- **Example**: `api-client.js`, `sidebar-controller.js`, `prescriptions.js`

### CSS Files

- **Core**: `app.css`
- **Components**: `_{component-name}.css` (underscore prefix cho partials)
- **Example**: `_buttons.css`, `_modals.css`

### HTML Templates

- **Layouts**: `{role}-layout.html` hoặc `base.html`
- **Pages**: `{feature-name}.html` (lowercase, kebab-case)
- **Fragments**: `{component-name}.html`
- **Example**: `doctor-layout.html`, `accounts.html`, `confirm-modal.html`

### Folder Names

- Lowercase
- Kebab-case cho multi-word: `health-forecast/`
- Singular form cho module names: `admin/`, `doctor/`

---

**Bước tiếp theo:** Bắt đầu thực hiện migration từ Phase 1. Bạn muốn tôi tiến hành ngay?
