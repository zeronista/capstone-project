/**
 * Doctor Health Forecast Management Module
 * Handles health risk forecasting, patient analytics, and trend predictions
 */

let allForecasts = [];
let filteredForecasts = [];

/**
 * Initialize the health forecast module
 */
export function init() {
    loadForecasts();
    checkHighRiskAlerts();
}

/**
 * Load forecasts from API
 */
async function loadForecasts() {
    try {
        const response = await fetch("/api/doctor/forecasts");
        const data = await response.json();

        allForecasts = data.forecasts || [];
        filteredForecasts = allForecasts;

        updateStatistics();
        renderForecasts();
    } catch (error) {
        console.error("Error loading forecasts:", error);
        showError("Không thể tải danh sách dự báo");
    }
}

/**
 * Check for high-risk alerts
 */
async function checkHighRiskAlerts() {
    try {
        const response = await fetch("/api/doctor/forecasts/alerts");
        const data = await response.json();

        if (data.count > 0) {
            document
                .getElementById("highRiskBanner")
                .classList.remove("hidden");
            document.getElementById("highRiskMessage").textContent =
                `Có ${data.count} bệnh nhân với mức độ rủi ro cao cần theo dõi sát. Vui lòng kiểm tra ngay.`;
        }
    } catch (error) {
        console.error("Error checking alerts:", error);
    }
}

/**
 * Update statistics display
 */
function updateStatistics() {
    document.getElementById("totalForecasts").textContent =
        allForecasts.length;

    const highRisk = allForecasts.filter(
        (f) => f.overallRisk === "HIGH" || f.overallRisk === "VERY_HIGH",
    ).length;
    document.getElementById("highRiskCount").textContent = highRisk;

    const activeCount = allForecasts.filter(
        (f) => f.status === "ACTIVE",
    ).length;
    document.getElementById("activeForecasts").textContent = activeCount;

    const cvRisks = allForecasts
        .filter((f) => f.cardiovascularRisk != null)
        .map((f) => f.cardiovascularRisk);
    const avgCV =
        cvRisks.length > 0
            ? (cvRisks.reduce((a, b) => a + b, 0) / cvRisks.length).toFixed(1)
            : "0";
    document.getElementById("avgCVRisk").textContent = avgCV + "%";
}

/**
 * Render forecasts table
 */
function renderForecasts() {
    const tbody = document.getElementById("forecastsTable");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");

    loadingState.classList.add("hidden");

    if (filteredForecasts.length === 0) {
        tbody.innerHTML = "";
        emptyState.classList.remove("hidden");
        return;
    }

    emptyState.classList.add("hidden");

    tbody.innerHTML = filteredForecasts
        .map(
            (forecast) => `
          <tr class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4">
              <div>
                <div class="font-medium text-gray-900">${forecast.patientName}</div>
                <div class="text-sm text-gray-500">${forecast.patientPhone || ""}</div>
              </div>
            </td>
            <td class="px-6 py-4 text-sm text-gray-700">
              ${formatDate(forecast.forecastDate)}
            </td>
            <td class="px-6 py-4">
              <div class="flex items-center gap-2">
                <span class="text-lg font-bold ${getCVRiskColor(forecast.cardiovascularRisk)}">
                  ${forecast.cardiovascularRisk != null ? forecast.cardiovascularRisk.toFixed(1) + "%" : "N/A"}
                </span>
              </div>
            </td>
            <td class="px-6 py-4">
              ${getRiskBadge(forecast.overallRisk)}
            </td>
            <td class="px-6 py-4">
              ${getStatusBadge(forecast.status)}
            </td>
            <td class="px-6 py-4">
              ${forecast.hasRiskAlerts ? '<span class="inline-flex items-center gap-1 px-2 py-1 bg-red-100 text-red-700 rounded-full text-xs"><span class="material-icons text-sm">warning</span>Có</span>' : '<span class="text-gray-400 text-sm">Không</span>'}
            </td>
            <td class="px-6 py-4">
              <button onclick="window.doctorHealthForecast.viewForecast(${forecast.id})" class="text-blue-600 hover:text-blue-800" title="Xem chi tiết">
                <span class="material-icons text-xl">visibility</span>
              </button>
            </td>
          </tr>
        `,
        )
        .join("");
}

/**
 * Apply filters to forecast list
 */
export function applyFilters() {
    const riskFilter = document.getElementById("riskFilter").value;
    const statusFilter = document.getElementById("statusFilter").value;

    filteredForecasts = allForecasts.filter((forecast) => {
        if (riskFilter && forecast.overallRisk !== riskFilter) return false;
        if (statusFilter && forecast.status !== statusFilter) return false;
        return true;
    });

    renderForecasts();
}

/**
 * View forecast detail
 * @param {number} forecastId - Forecast ID
 */
export async function viewForecast(forecastId) {
    try {
        const response = await fetch(`/api/doctor/forecasts/${forecastId}`);
        const forecast = await response.json();

        document.getElementById("forecastDetailContent").innerHTML = `
            <div class="space-y-6">
              <!-- Patient Info -->
              <div class="border-b pb-4">
                <h4 class="font-semibold text-gray-900 mb-3">Thông tin bệnh nhân</h4>
                <div class="grid grid-cols-2 gap-4">
                  <div><span class="text-sm text-gray-500">Họ tên:</span><p class="font-medium">${forecast.patientName}</p></div>
                  <div><span class="text-sm text-gray-500">Tuổi:</span><p class="font-medium">${forecast.patientAge || "N/A"}</p></div>
                  <div><span class="text-sm text-gray-500">Giới tính:</span><p class="font-medium">${forecast.patientGender || "N/A"}</p></div>
                  <div><span class="text-sm text-gray-500">SĐT:</span><p class="font-medium">${forecast.patientPhone || "N/A"}</p></div>
                </div>
              </div>
              
              <!-- Risk Scores -->
              <div class="border-b pb-4">
                <h4 class="font-semibold text-gray-900 mb-3">Điểm rủi ro</h4>
                <div class="grid grid-cols-2 gap-4">
                  ${Object.entries(forecast.riskScores || {})
            .map(
                ([key, value]) => `
                    <div class="bg-gray-50 rounded-lg p-3">
                      <span class="text-sm text-gray-600">${formatRiskKey(key)}</span>
                      <p class="text-lg font-bold text-gray-900 mt-1">${formatRiskValue(value)}</p>
                    </div>
                  `,
            )
            .join("")}
                </div>
              </div>
              
              <!-- Vital Signs -->
              <div class="border-b pb-4">
                <h4 class="font-semibold text-gray-900 mb-3">Chỉ số sinh tồn</h4>
                <div class="grid grid-cols-3 gap-3">
                  ${Object.entries(forecast.vitalSignsSnapshot || {})
            .map(
                ([key, value]) => `
                    <div class="text-center bg-blue-50 rounded-lg p-3">
                      <span class="text-xs text-gray-600">${formatVitalKey(key)}</span>
                      <p class="text-sm font-semibold text-gray-900 mt-1">${value || "N/A"}</p>
                    </div>
                  `,
            )
            .join("")}
                </div>
              </div>
              
              <!-- Predictions -->
              <div class="border-b pb-4">
                <h4 class="font-semibold text-gray-900 mb-3">Dự đoán xu hướng</h4>
                <div class="space-y-2">
                  ${Object.entries(forecast.predictions || {})
            .map(
                ([key, value]) => `
                    <div class="flex items-center justify-between">
                      <span class="text-sm text-gray-700">${formatPredictionKey(key)}</span>
                      <span class="font-medium ${getTrendColor(value)}">${formatTrendValue(value)}</span>
                    </div>
                  `,
            )
            .join("")}
                </div>
              </div>
              
              <!-- Recommendations -->
              <div>
                <h4 class="font-semibold text-gray-900 mb-3">Khuyến nghị</h4>
                <div class="bg-amber-50 border border-amber-200 rounded-lg p-4">
                  <pre class="whitespace-pre-wrap text-sm text-gray-700 font-sans">${forecast.recommendations || "Không có khuyến nghị"}</pre>
                </div>
              </div>
            </div>
          `;

        document
            .getElementById("forecastDetailModal")
            .classList.remove("hidden");
        document.getElementById("forecastDetailModal").classList.add("flex");
    } catch (error) {
        console.error("Error loading forecast detail:", error);
        showError("Không thể tải chi tiết dự báo");
    }
}

/**
 * Close forecast modal
 */
export function closeForecastModal() {
    document.getElementById("forecastDetailModal").classList.add("hidden");
    document.getElementById("forecastDetailModal").classList.remove("flex");
}

/**
 * Open patient search modal (placeholder)
 */
export function openPatientSearchModal() {
    alert(
        "Chức năng tạo dự báo mới sẽ được phát triển trong phiên bản tiếp theo",
    );
}

/**
 * View high risk alerts
 */
export function viewHighRiskAlerts() {
    document.getElementById("riskFilter").value = "HIGH";
    applyFilters();
}

// Helper functions

function getRiskBadge(risk) {
    const badges = {
        LOW: '<span class="px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium">Thấp</span>',
        MODERATE:
            '<span class="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-medium">Trung bình</span>',
        HIGH: '<span class="px-3 py-1 bg-orange-100 text-orange-700 rounded-full text-xs font-medium">Cao</span>',
        VERY_HIGH:
            '<span class="px-3 py-1 bg-red-100 text-red-700 rounded-full text-xs font-medium">Rất cao</span>',
        UNKNOWN:
            '<span class="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-xs font-medium">Chưa xác định</span>',
    };
    return badges[risk] || badges["UNKNOWN"];
}

function getStatusBadge(status) {
    const badges = {
        ACTIVE:
            '<span class="px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium">Hiệu lực</span>',
        OUTDATED:
            '<span class="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-xs font-medium">Lỗi thời</span>',
        DRAFT:
            '<span class="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-medium">Nháp</span>',
        ARCHIVED:
            '<span class="px-3 py-1 bg-gray-100 text-gray-600 rounded-full text-xs font-medium">Lưu trữ</span>',
    };
    return badges[status] || status;
}

function getCVRiskColor(risk) {
    if (risk == null) return "text-gray-400";
    if (risk < 5) return "text-green-600";
    if (risk < 10) return "text-yellow-600";
    if (risk < 20) return "text-orange-600";
    return "text-red-600";
}

function getTrendColor(trend) {
    if (trend === "IMPROVING") return "text-green-600";
    if (trend === "WORSENING") return "text-red-600";
    return "text-gray-600";
}

function formatTrendValue(value) {
    if (value === "IMPROVING") return "↓ Cải thiện";
    if (value === "WORSENING") return "↑ Xấu đi";
    if (value === "STABLE") return "→ Ổn định";
    return value || "N/A";
}

function formatRiskKey(key) {
    const map = {
        cardiovascularRisk: "Rủi ro tim mạch",
        diabetesRisk: "Rủi ro tiểu đường",
        hypertensionRisk: "Rủi ro tăng huyết áp",
        strokeRisk: "Rủi ro đột quỵ",
        overallRisk: "Rủi ro tổng thể",
    };
    return map[key] || key;
}

function formatRiskValue(value) {
    if (typeof value === "number") return value.toFixed(1) + "%";
    return value;
}

function formatVitalKey(key) {
    const map = {
        systolic: "Tâm thu",
        diastolic: "Tâm trương",
        heartRate: "Nhịp tim",
        weight: "Cân nặng",
        bmi: "BMI",
        bloodSugar: "Đường huyết",
    };
    return map[key] || key;
}

function formatPredictionKey(key) {
    const map = {
        bloodPressureTrend: "Xu hướng huyết áp",
        weightTrend: "Xu hướng cân nặng",
        bmiTrend: "Xu hướng BMI",
        bloodSugarTrend: "Xu hướng đường huyết",
        heartRateTrend: "Xu hướng nhịp tim",
    };
    return map[key] || key;
}

function formatDate(dateString) {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
    });
}

function showError(message) {
    alert(message);
}
