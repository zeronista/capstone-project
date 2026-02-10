let patientsData = [];
let filteredData = [];

document.addEventListener("DOMContentLoaded", () => {
  bindFilters();
  loadGoogleFormPatients();
});

function bindFilters() {
  const searchInput = document.getElementById("searchInput");
  const dateFromInput = document.getElementById("dateFromInput");
  const dateToInput = document.getElementById("dateToInput");
  const callStatusFilter = document.getElementById("callStatusFilter");

  if (searchInput) searchInput.addEventListener("input", applyFilters);
  if (dateFromInput) dateFromInput.addEventListener("change", applyFilters);
  if (dateToInput) dateToInput.addEventListener("change", applyFilters);
  if (callStatusFilter) callStatusFilter.addEventListener("change", applyFilters);
}

async function loadGoogleFormPatients() {
  setLoading(true);
  try {
    const response = await fetch("/api/receptionist/google-form-patients?limit=200", {
      headers: { Accept: "application/json" },
    });

    if (!response.ok) {
      throw new Error("Không thể tải dữ liệu bệnh nhân từ Google Forms");
    }

    const result = await response.json();
    patientsData = Array.isArray(result.patients) ? result.patients : [];
    applyFilters();
  } catch (error) {
    showToast(error.message || "Có lỗi xảy ra khi tải dữ liệu", false);
  } finally {
    setLoading(false);
  }
}

async function syncGoogleForms() {
  const syncBtn = document.getElementById("syncBtn");
  syncBtn.disabled = true;
  syncBtn.classList.add("opacity-60", "cursor-not-allowed");
  setLoading(true);

  try {
    const response = await fetch("/api/receptionist/google-form-patients/sync", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    });
    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Đồng bộ thất bại");
    }

    const message =
      `Đồng bộ thành công. Mới: ${result.syncedCount || 0}, Trùng: ${result.skippedDuplicate || 0}, Lỗi: ${result.failedCount || 0}`;
    showToast(message, true);
    await loadGoogleFormPatients();
  } catch (error) {
    showToast(error.message || "Lỗi đồng bộ Google Forms", false);
  } finally {
    syncBtn.disabled = false;
    syncBtn.classList.remove("opacity-60", "cursor-not-allowed");
    setLoading(false);
  }
}

function applyFilters() {
  const searchKeyword = (document.getElementById("searchInput")?.value || "").trim().toLowerCase();
  const dateFrom = document.getElementById("dateFromInput")?.value || "";
  const dateTo = document.getElementById("dateToInput")?.value || "";
  const callStatus = document.getElementById("callStatusFilter")?.value || "ALL";

  filteredData = patientsData.filter((item) => {
    const name = (item.fullName || "").toLowerCase();
    if (searchKeyword && !name.includes(searchKeyword)) {
      return false;
    }

    const itemStatus = (item.callStatus || "NOT_CALLED").toUpperCase();
    if (callStatus !== "ALL" && itemStatus !== callStatus) {
      return false;
    }

    const itemDate = parseDateFromApi(item.submittedAt);
    if (dateFrom && itemDate) {
      const from = new Date(`${dateFrom}T00:00:00`);
      if (itemDate < from) return false;
    }
    if (dateTo && itemDate) {
      const to = new Date(`${dateTo}T23:59:59`);
      if (itemDate > to) return false;
    }

    return true;
  });

  renderPatientsTable(filteredData);
}

function renderPatientsTable(data) {
  const tableBody = document.getElementById("googleFormPatientsTableBody");
  const emptyState = document.getElementById("emptyState");
  const totalCount = document.getElementById("totalCount");

  totalCount.textContent = String(data.length);

  if (!data.length) {
    tableBody.innerHTML = "";
    emptyState.classList.remove("hidden");
    return;
  }

  emptyState.classList.add("hidden");
  tableBody.innerHTML = data
    .map((item, index) => {
      const submittedAt = formatDateTimeGmt7(item.submittedAt);
      const callStatus = item.callStatus || "NOT_CALLED";
      const callStatusBadge = renderCallStatusBadge(callStatus);
      const fullName = escapeHtml(item.fullName || "Chưa có tên");
      const phone = escapeHtml(item.phoneNumber || "");
      const email = escapeHtml(item.email || "");
      const formTitle = escapeHtml(item.formTitle || item.formId || "");
      const syncRecordId = item.syncRecordId;
      const nextStatus = callStatus === "CALLED" ? "NOT_CALLED" : "CALLED";
      const nextStatusLabel = callStatus === "CALLED" ? "Đánh dấu chưa gọi" : "Đánh dấu đã gọi";

      return `
        <tr class="hover:bg-slate-50 dark:hover:bg-slate-700/30">
          <td class="px-4 py-4 text-sm text-slate-700 dark:text-slate-200">${index + 1}</td>
          <td class="px-6 py-4 text-sm font-medium text-slate-900 dark:text-white">${fullName}</td>
          <td class="px-6 py-4 text-sm text-slate-700 dark:text-slate-200">
            <div>${phone || "-"}</div>
            <div class="text-xs text-slate-500 dark:text-slate-400">${email || "-"}</div>
          </td>
          <td class="px-6 py-4 text-sm text-slate-700 dark:text-slate-200">${formTitle}</td>
          <td class="px-6 py-4 text-sm text-slate-700 dark:text-slate-200">${submittedAt}</td>
          <td class="px-6 py-4 text-sm text-slate-700 dark:text-slate-200">${callStatusBadge}</td>
          <td class="px-6 py-4 text-sm text-slate-700 dark:text-slate-200">
            <div class="flex flex-wrap gap-2">
              <button
                class="px-2 py-1 text-xs rounded bg-slate-100 dark:bg-slate-700 hover:bg-slate-200 dark:hover:bg-slate-600"
                onclick="showSurveyDetail(${syncRecordId})"
              >
                Xem chi tiết
              </button>
              <button
                class="px-2 py-1 text-xs rounded bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300 hover:bg-blue-200"
                onclick="callPlaceholder('${phone || ""}')"
              >
                Gọi điện
              </button>
              <button
                class="px-2 py-1 text-xs rounded bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300 hover:bg-emerald-200"
                onclick="updateCallStatus(${syncRecordId}, '${nextStatus}')"
              >
                ${nextStatusLabel}
              </button>
            </div>
          </td>
        </tr>
      `;
    })
    .join("");
}

function setLoading(isLoading) {
  const loadingState = document.getElementById("loadingState");
  if (!loadingState) return;
  loadingState.classList.toggle("hidden", !isLoading);
}

function formatDateTimeGmt7(value) {
  if (!value) return "-";
  const date = parseDateFromApi(value);
  if (Number.isNaN(date.getTime())) return "-";
  return new Intl.DateTimeFormat("vi-VN", {
    timeZone: "Asia/Ho_Chi_Minh",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  }).format(date);
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function parseDateFromApi(value) {
  if (!value) return new Date(NaN);
  if (typeof value === "string" && value.includes("T") && !value.endsWith("Z") && !value.includes("+")) {
    return new Date(`${value}+07:00`);
  }
  return new Date(value);
}

function renderCallStatusBadge(status) {
  if (status === "CALLED") {
    return '<span class="inline-flex items-center px-2 py-0.5 rounded text-xs bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300">Đã gọi</span>';
  }
  return '<span class="inline-flex items-center px-2 py-0.5 rounded text-xs bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300">Chưa gọi</span>';
}

async function showSurveyDetail(syncRecordId) {
  try {
    const response = await fetch(`/api/receptionist/google-form-patients/${syncRecordId}`, {
      headers: { Accept: "application/json" },
    });
    const result = await response.json();
    if (!response.ok || !result.success) {
      throw new Error(result.message || "Không thể tải chi tiết khảo sát");
    }

    const data = result.data || {};
    document.getElementById("detailFullName").textContent = data.fullName || "-";
    document.getElementById("detailPhone").textContent = data.phoneNumber || "-";
    document.getElementById("detailFormTitle").textContent = data.formTitle || "-";
    document.getElementById("detailSubmittedAt").textContent = formatDateTimeGmt7(data.submittedAt);
    document.getElementById("detailSurveyContent").textContent = data.surveyContent || "Chưa có nội dung khảo sát.";
    document.getElementById("detailModal").classList.remove("hidden");
  } catch (error) {
    showToast(error.message || "Lỗi tải chi tiết khảo sát", false);
  }
}

function closeDetailModal() {
  document.getElementById("detailModal").classList.add("hidden");
}

function callPlaceholder(phoneNumber) {
  const text = phoneNumber
    ? `Placeholder gọi điện cho số ${phoneNumber}. Chức năng gọi điện chưa triển khai.`
    : "Placeholder gọi điện. Chức năng gọi điện chưa triển khai.";
  showToast(text, true);
}

async function updateCallStatus(syncRecordId, status) {
  try {
    const response = await fetch(
      `/api/receptionist/google-form-patients/${syncRecordId}/call-status?status=${status}`,
      { method: "PUT", headers: { "Content-Type": "application/json" } }
    );
    const result = await response.json();
    if (!response.ok || !result.success) {
      throw new Error(result.message || "Không thể cập nhật trạng thái gọi");
    }
    showToast("Đã cập nhật trạng thái gọi", true);
    await loadGoogleFormPatients();
  } catch (error) {
    showToast(error.message || "Lỗi cập nhật trạng thái gọi", false);
  }
}

function showToast(message, isSuccess) {
  const toast = document.getElementById("toast");
  const toastIcon = document.getElementById("toastIcon");
  const toastMessage = document.getElementById("toastMessage");

  toastMessage.textContent = message;
  toastIcon.innerHTML = isSuccess
    ? '<span class="material-symbols-outlined text-emerald-600">check_circle</span>'
    : '<span class="material-symbols-outlined text-red-600">error</span>';

  toast.classList.remove("hidden", "translate-y-full", "opacity-0");
  setTimeout(() => {
    toast.classList.add("translate-y-full", "opacity-0");
    setTimeout(() => toast.classList.add("hidden"), 300);
  }, 3000);
}
