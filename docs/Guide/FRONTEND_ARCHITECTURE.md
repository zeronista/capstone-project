# Frontend Architecture Documentation

**Project:** Capstone Healthcare Management System  
**Stack:** Spring Boot + Thymeleaf + Tailwind CSS  
**Date:** January 28, 2026

---

## Overview

This document provides a comprehensive overview of the frontend architecture, current structure, identified issues, and improvement roadmap.

---

## Current State

### Technology Stack

| Technology       | Version/Source                     | Purpose                                          |
| ---------------- | ---------------------------------- | ------------------------------------------------ |
| **Thymeleaf**    | Spring Boot integrated             | Server-side template engine                      |
| **Tailwind CSS** | CDN (latest)                       | Utility-first CSS framework                      |
| **Chart.js**     | CDN                                | Dashboard visualizations                         |
| **Stringee SDK** | Bundled (latest.sdk.bundle.min.js) | Voice/video calling                              |
| **Google Fonts** | CDN                                | Typography (Open Sans, Poppins, Figtree, Roboto) |
| **Heroicons**    | unpkg CDN                          | Icon library                                     |

### Project Statistics

- **Total Templates:** 26 HTML files
- **Thymeleaf Fragments:** 4 (head, sidebar, navbar, content-wrapper)
- **JavaScript Files:** 3 (patient.js [513 lines], profile-patient.js [273 lines], SDK bundle)
- **CSS Files:** 0 (all styles inline or CDN)
- **Design References:** 31 PNG mockups in `stitch_frontend_n/`

### Directory Structure

```
src/main/resources/
├── templates/
│   ├── index.html                    # Landing page
│   ├── fragments/
│   │   └── layout.html               # Shared layout fragments
│   ├── admin/                        # Admin module (2 files)
│   │   ├── accounts.html
│   │   └── users.html
│   ├── auth/                         # Authentication (3 files)
│   │   ├── login.html
│   │   ├── register.html
│   │   └── forgot-password.html
│   ├── dashboard/                    # Dashboard (1 file)
│   │   └── index.html
│   ├── medical/                      # Medical records (4 files)
│   │   ├── prescriptions.html
│   │   ├── treatments.html
│   │   ├── forecast.html
│   │   └── knowledge.html
│   ├── ai/                          # AI features (4 files)
│   │   ├── calls.html
│   │   ├── voice.html
│   │   ├── config.html
│   │   └── web-call.html
│   ├── crm/                         # CRM module (4 files)
│   │   ├── tickets.html
│   │   ├── surveys.html
│   │   ├── social.html
│   │   └── notifications.html
│   ├── patient/                     # Patient portal (1 file)
│   │   └── index.html
│   ├── profile/                     # User profiles (2 files)
│   │   ├── index.html
│   │   └── patient.html
│   ├── reports/                     # Reporting (2 files)
│   │   ├── index.html
│   │   └── detail.html
│   └── test/                        # Testing utilities (1 file)
│       └── s3-upload.html
├── static/
│   ├── js/
│   │   ├── patient.js               # Patient portal logic (513 lines)
│   │   ├── profile-patient.js       # Patient profile management (273 lines)
│   │   └── latest.sdk.bundle.min.js # Stringee SDK
│   └── (CSS directory - empty)
└── stitch_frontend_n/               # Design reference files (31 folders)
    ├── doctor_dashboard_overview_[1-14]/
    ├── doctor_dashboard_-_priority_view_[1-10]/
    └── [other design mockups]/
```

---

## Critical Issues Identified

### 1. Code Duplication

#### Tailwind Configuration Duplication

- **Problem:** 22+ templates contain identical inline `tailwind.config` definitions
- **Impact:** ~50-100 lines duplicated per template = ~1,100-2,200 lines of duplicate code
- **Example locations:** `dashboard/index.html:18`, `admin/accounts.html:10`, `medical/prescriptions.html:19`

#### CSS Duplication

- **Problem:** 15+ templates contain identical `<style>` blocks
- **Common duplicates:**
  - Font family definitions (body, headings)
  - `.sidebar-link.active` styles
  - `@media (prefers-reduced-motion)` rules
  - Scrollbar custom styling
  - Card hover effects

### 2. Design System Inconsistency

#### Color Palette Conflicts

**Main Application Palette (Cyan-based):**

```javascript
primary: {
  50: '#ECFEFF',
  100: '#CFFAFE',
  500: '#0891B2',  // Cyan-600
  600: '#0E7490',
  // ... 50-900 range
}
```

**Patient Portal Palette (Sky Blue-based):**

```javascript
primary: {
  50: '#f0f9ff',
  100: '#e0f2fe',
  500: '#0ea5e9',  // Sky-500
  600: '#0284c7',
  // ... different values
}
```

**Impact:** Brand inconsistency, different button colors, user confusion

### 3. Responsive Design Gaps

- **Sidebar:** Fixed `w-64` width, not responsive for mobile devices
- **No mobile navigation:** Missing hamburger menu/drawer pattern
- **Tables:** Some tables don't have horizontal scroll on mobile
- **Forms:** Input fields don't adapt well to small screens

### 4. JavaScript Organization

#### Lack of Modularity

- **patient.js:** 513 lines mixing API calls, UI logic, event handlers
- **Inline scripts:** 700+ lines in `admin/accounts.html` alone
- **No code reuse:** Each template implements its own fetch logic

#### API Integration Pattern

```javascript
// Repeated in multiple files - no abstraction
const response = await fetch("/api/endpoint");
if (!response.ok) throw new Error("Failed");
const data = await response.json();
```

### 5. Missing Fragment Usage

**Standalone Templates (not using shared fragments):**

- `patient/index.html` - Completely custom layout
- `profile/patient.html` - Different header/navigation
- Auth pages (by design - expected)

---

## API Endpoints Inventory

| Endpoint                         | Used In                        | Method |
| -------------------------------- | ------------------------------ | ------ |
| `/api/patient/stats`             | patient.js                     | GET    |
| `/api/patient/prescriptions`     | patient.js                     | GET    |
| `/api/patient/treatments`        | patient.js                     | GET    |
| `/api/patient/tickets`           | patient.js                     | GET    |
| `/api/patient/profile`           | patient.js, profile-patient.js | GET    |
| `/api/profile/update`            | profile-patient.js             | PUT    |
| `/api/profile/upload-avatar`     | profile-patient.js             | POST   |
| `/api/admin/accounts`            | admin/accounts.html            | GET    |
| `/api/admin/accounts/search`     | admin/accounts.html            | GET    |
| `/api/admin/accounts/filter`     | admin/accounts.html            | GET    |
| `/api/stringee/upload-recording` | ai/voice.html                  | POST   |

---

## Reusable Component Patterns

### Components Identified for Extraction

1. **Stats Card** (20+ occurrences)

   ```html
   <div class="bg-white rounded-2xl border p-6">
     <div class="flex items-center justify-between">
       <div>
         <p class="text-sm text-surface-500">Label</p>
         <p class="text-3xl font-bold">Value</p>
       </div>
       <div class="w-14 h-14 bg-{color}-100 rounded-2xl">
         <!-- Icon -->
       </div>
     </div>
   </div>
   ```

2. **Content Card** (30+ occurrences)

   ```html
   <div class="bg-white rounded-2xl border border-surface-200 p-6">
     <!-- Content -->
   </div>
   ```

3. **Primary Button** (50+ occurrences)

   ```html
   <button
     class="px-5 py-2.5 bg-primary-500 text-white rounded-xl hover:bg-primary-600"
   ></button>
   ```

4. **Form Input** (40+ occurrences)

   ```html
   <input
     class="w-full px-4 py-2.5 bg-surface-50 border rounded-xl focus:ring-2"
   />
   ```

5. **Modal** (10+ occurrences)
   - Used in: accounts, prescriptions, tickets
   - Backdrop + centered dialog pattern

6. **Data Table** (15+ occurrences)
   - Consistent styling needed across admin, medical, CRM modules

---

## Improvement Roadmap

### Phase 1: Code Consolidation ✅ IN PROGRESS

1. ✅ **Centralize Tailwind Configuration**
   - Create: `static/js/tailwind-config.js`
   - Unified color palette (resolve Cyan vs Sky Blue)
   - Single source of truth

2. ✅ **Consolidate CSS**
   - Create: `static/css/app.css`
   - Font definitions
   - Scrollbar styling
   - Common animation/transitions
   - Reduced motion support

3. ✅ **Update Layout Fragment**
   - Enhance `fragments/layout.html`
   - Include centralized config/CSS in head
   - Standardize structure

### Phase 2: Component Library

4. **Create Reusable Fragments**
   - Create: `templates/fragments/components.html`
   - Fragments: card, stats-card, button, input, modal, table
   - Use `th:fragment` for reusability

5. **Responsive Navigation**
   - Mobile hamburger menu
   - Off-canvas sidebar
   - Breakpoint-aware navigation

### Phase 3: JavaScript Modernization

6. **API Client Layer**
   - Create: `static/js/api-client.js`
   - Centralized fetch wrapper
   - Standardized error handling
   - Request/response interceptors
   - JSDoc type annotations

7. **Modularize patient.js**
   - Split into: `api.js`, `ui.js`, `validation.js`
   - Better separation of concerns

### Phase 4: Backend Integration

8. **Spring Boot Configuration**
   - Create: `WebConfig.java`
   - Resource handler optimization
   - Cache control headers
   - Static resource versioning

9. **Custom Thymeleaf Dialect (Advanced)**
   - Create: `UiDialect.java`
   - Custom components: `<ui:card>`, `<ui:button>`
   - Cleaner template syntax

---

## Design System Specifications

### Color Palette (Standardized)

**Decision: Use Cyan-based palette as primary**

```javascript
colors: {
  primary: {
    50: '#ECFEFF',
    100: '#CFFAFE',
    200: '#A5F3FC',
    300: '#67E8F9',
    400: '#22D3EE',
    500: '#0891B2',  // Main brand color
    600: '#0E7490',
    700: '#155E75',
    800: '#164E63',
    900: '#083344',
  },
  secondary: {
    500: '#22C55E',  // Green for success/secondary actions
    600: '#16A34A',
  },
  surface: {
    50: '#F8FAFC',
    100: '#F1F5F9',
    200: '#E2E8F0',
    // ... Slate scale
    900: '#0F172A',
  }
}
```

### Typography

- **Body:** Open Sans (400, 600)
- **Headings:** Poppins (600, 700)
- **Monospace:** (none currently - consider adding for code)

### Spacing Scale

Following Tailwind defaults (4px base unit)

### Border Radius

- **Small:** `rounded-lg` (8px)
- **Medium:** `rounded-xl` (12px)
- **Large:** `rounded-2xl` (16px)

---

## Migration Strategy

### Template Migration Priority

**High Priority (Admin/Medical Core):**

1. `fragments/layout.html` - Shared foundation
2. `dashboard/index.html` - Main entry point
3. `admin/accounts.html` - Heavy inline JS
4. `medical/prescriptions.html` - Complex interactions

**Medium Priority:** 5. CRM module templates 6. AI module templates 7. Reports module

**Low Priority:** 8. Patient portal (may need separate branding) 9. Auth pages (standalone by design)

### Rollout Plan

1. **Create foundation files** (config, CSS, components)
2. **Update layout fragment** with new includes
3. **Migrate one template** as proof-of-concept
4. **Batch update** remaining templates
5. **Test responsiveness** across devices
6. **Performance audit** and optimization

---

## Metrics & Goals

| Metric                      | Current | Target | Status |
| --------------------------- | ------- | ------ | ------ |
| Duplicated Tailwind configs | 22      | 1      | 🔴     |
| Templates with inline CSS   | 15+     | 0      | 🔴     |
| External CSS files          | 0       | 1      | 🔴     |
| External JS modules         | 3       | 6-8    | 🔴     |
| Reusable fragments          | 4       | 12-15  | 🔴     |
| Color palette consistency   | ~70%    | 100%   | 🟡     |
| Mobile responsive           | Partial | Full   | 🟡     |
| Lighthouse Performance      | Unknown | 90+    | ⚪     |
| Lighthouse Accessibility    | Unknown | 90+    | ⚪     |

---

## Testing Strategy

### Browser Support

- **Chrome/Edge:** Latest 2 versions
- **Firefox:** Latest 2 versions
- **Safari:** Latest 2 versions
- **Mobile:** iOS Safari 14+, Chrome Android

### Responsive Breakpoints

- **Mobile:** 320px - 639px
- **Tablet:** 640px - 1023px
- **Desktop:** 1024px - 1279px
- **Large Desktop:** 1280px+

### Testing Checklist

- [ ] Navigation works on all screen sizes
- [ ] Forms are usable on mobile
- [ ] Tables scroll horizontally when needed
- [ ] Modals center properly on all devices
- [ ] Color contrast meets WCAG AA
- [ ] Keyboard navigation functional
- [ ] Screen reader compatibility

---

## Maintenance Guidelines

### Adding New Templates

1. Use `fragments/layout.html` for consistent structure
2. Include centralized `tailwind-config.js` and `app.css`
3. Leverage `fragments/components.html` for UI elements
4. Use `api-client.js` for API calls
5. Follow established naming conventions

### Updating Components

1. Modify fragment definition in `components.html`
2. Changes propagate to all templates using the fragment
3. Test across multiple templates

### Performance Considerations

- Minimize inline JavaScript
- Use CDN for third-party libraries
- Leverage browser caching for static assets
- Consider lazy loading for images/charts

---

## References

- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)
- [Spring Boot Static Resources](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.static-content)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

---

**Last Updated:** January 28, 2026  
**Maintained By:** Development Team
