/**
 * Doctor Treatment Plans Management Module
 * Handles treatment plan listing, filtering, viewing, and checkup management
 */

// State management
let allTreatments = [];
let filteredTreatments = [];

/**
 * Initialize treatment plans page
 */
export function init() {
    loadTreatmentPlans();
}

/**
 * Load treatment plans from API
 */
async function loadTreatmentPlans() {
    try {
        const response = await fetch("/api/doctor/treatments");
        const data = await response.json();

        allTreatments = data.treatments;
        filteredTreatments = allTreatments;

        updateStatistics();
        renderTreatmentPlans();
    } catch (error) {
        console.error("Error loading treatment plans:", error);
        showError("Không thể tải danh sách kế hoạch điều trị");
    }
}

/**
 * Update statistics dashboard
 */
function updateStatistics() {
    document.getElementById("totalCount").textContent = allTreatments.length;

    const activeCount = allTreatments.filter(t => t.status === "ACTIVE").length;
    document.getElementById("activeCount").textContent = activeCount;

    const completedCount = allTreatments.filter(t => t.status === "COMPLETED").length;
    document.getElementById("completedCount").textContent = completedCount;

    const draftCount = allTreatments.filter(t => t.status === "DRAFT").length;
    document.getElementById("draftCount").textContent = draftCount;
}

/**
 * Render treatment plans table
 */
function renderTreatmentPlans() {
    const tbody = document.getElementById("treatmentsTable");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");

    loadingState.classList.add("hidden");

    if (filteredTreatments.length === 0) {
        tbody.innerHTML = "";
        emptyState.classList.remove("hidden");
        return;
    }

    emptyState.classList.add("hidden");

    tbody.innerHTML = filteredTreatments.map(treatment => `
        <tr class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4">
                <span class="font-medium text-gray-900">#${treatment.id}</span>
            </td>
            <td class="px-6 py-4">
                <div>
                    <div class="font-medium text-gray-900">${treatment.patientName}</div>
                    <div class="text-sm text-gray-500">${treatment.patientPhone || ""}</div>
                </div>
            </td>
            <td class="px-6 py-4">
                <span class="text-sm text-gray-700">${treatment.diagnosis || "Không có"}</span>
            </td>
            <td class="px-6 py-4">
                <span class="text-sm text-gray-700 line-clamp-2">${treatment.treatmentGoal || "Không có"}</span>
            </td>
            <td class="px-6 py-4">
                <div class="text-sm">
                    <div class="text-gray-700">Bắt đầu: ${formatDate(treatment.startDate)}</div>
                    <div class="text-gray-500">Dự kiến: ${formatDate(treatment.expectedEndDate)}</div>
                </div>
            </td>
            <td class="px-6 py-4">
                ${getStatusBadge(treatment.status)}
            </td>
            <td class="px-6 py-4">
                <span class="inline-flex items-center gap-1 px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-sm font-medium">
                    <span class="material-icons text-base">event</span>
                    ${treatment.checkupCount} lịch
                </span>
            </td>
            <td class="px-6 py-4">
                <div class="flex items-center gap-2">
                    <button onclick="window.doctorTreatments.viewTreatment(${treatment.id})" class="text-blue-600 hover:text-blue-800" title="Xem chi tiết">
                        <span class="material-icons text-xl">visibility</span>
                    </button>
                    <button onclick="window.doctorTreatments.managementCheckups(${treatment.id})" class="text-purple-600 hover:text-purple-800" title="Quản lý lịch tái khám">
                        <span class="material-icons text-xl">event_note</span>
                    </button>
                </div>
            </td>
        </tr>
    `).join("");
}

/**
 * Apply filters to treatment list
 */
export function applyFilters() {
    const statusFilter = document.getElementById("statusFilter").value;
    const patientSearch = document.getElementById("patientSearch").value.toLowerCase();

    filteredTreatments = allTreatments.filter(treatment => {
        if (statusFilter && treatment.status !== statusFilter) {
            return false;
        }

        if (patientSearch) {
            const patientName = treatment.patientName.toLowerCase();
            if (!patientName.includes(patientSearch)) {
                return false;
            }
        }

        return true;
    });

    renderTreatmentPlans();
}

/**
 * View treatment detail
 */
export async function viewTreatment(treatmentId) {
    try {
        const response = await fetch(`/api/doctor/treatments/${treatmentId}`);
        const treatment = await response.json();

        document.getElementById("treatmentDetailContent").innerHTML = `
            <div class="space-y-6">
                <!-- Patient Info -->
                <div class="border-b pb-4">
                    <h4 class="font-semibold text-gray-900 mb-3">Thông tin bệnh nhân</h4>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <span class="text-sm text-gray-500">Họ tên:</span>
                            <p class="font-medium text-gray-900">${treatment.patientName}</p>
                        </div>
                        <div>
                            <span class="text-sm text-gray-500">Số điện thoại:</span>
                            <p class="font-medium text-gray-900">${treatment.patientPhone || "N/A"}</p>
                        </div>
                    </div>
                </div>
                
                <!-- Treatment Plan Info -->
                <div class="border-b pb-4">
                    <h4 class="font-semibold text-gray-900 mb-3">Thông tin kế hoạch</h4>
                    <div class="space-y-3">
                        <div>
                            <span class="text-sm text-gray-500">Chẩn đoán:</span>
                            <p class="font-medium text-gray-900 mt-1">${treatment.diagnosis || "Không có"}</p>
                        </div>
                        <div>
                            <span class="text-sm text-gray-500">Mục tiêu điều trị:</span>
                            <p class="font-medium text-gray-900 mt-1">${treatment.treatmentGoal || "Không có"}</p>
                        </div>
                        <div class="grid grid-cols-3 gap-4">
                            <div>
                                <span class="text-sm text-gray-500">Ngày bắt đầu:</span>
                                <p class="font-medium text-gray-900">${formatDate(treatment.startDate)}</p>
                            </div>
                            <div>
                                <span class="text-sm text-gray-500">Dự kiến kết thúc:</span>
                                <p class="font-medium text-gray-900">${formatDate(treatment.expectedEndDate)}</p>
                            </div>
                            <div>
                                <span class="text-sm text-gray-500">Trạng thái:</span>
                                <div class="mt-1">${getStatusBadge(treatment.status)}</div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Treatment Items -->
                <div class="border-b pb-4">
                    <h4 class="font-semibold text-gray-900 mb-3">Các hạng mục điều trị</h4>
                    ${treatment.items && treatment.items.length > 0 ? `
                        <div class="space-y-3">
                            ${treatment.items.map((item, index) => `
                                <div class="border border-gray-200 rounded-lg p-4">
                                    <div class="flex items-start justify-between">
                                        <div class="flex-1">
                                            <div class="flex items-center gap-2 mb-2">
                                                <span class="font-medium text-gray-900">${index + 1}. ${item.treatmentType}</span>
                                                ${getItemStatusBadge(item.status)}
                                            </div>
                                            <p class="text-sm text-gray-700 mb-2">${item.description}</p>
                                            <div class="grid grid-cols-2 gap-2 text-sm">
                                                <div><span class="text-gray-500">Tần suất:</span> ${item.frequency || "N/A"}</div>
                                                <div><span class="text-gray-500">Thời gian:</span> ${item.duration || "N/A"}</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            `).join("")}
                        </div>
                    ` : '<p class="text-gray-500 text-center py-4">Chưa có hạng mục điều trị</p>'}
                </div>
                
                <!-- Checkup Schedules -->
                <div>
                    <h4 class="font-semibold text-gray-900 mb-3">Lịch tái khám</h4>
                    ${treatment.checkups && treatment.checkups.length > 0 ? `
                        <div class="space-y-3">
                            ${treatment.checkups.map(checkup => `
                                <div class="border border-gray-200 rounded-lg p-4">
                                    <div class="flex items-center justify-between mb-2">
                                        <div class="flex items-center gap-2">
                                            <span class="material-icons text-blue-600">event</span>
                                            <span class="font-medium text-gray-900">${formatDate(checkup.scheduledDate)}</span>
                                        </div>
                                        ${getCheckupStatusBadge(checkup.status)}
                                    </div>
                                    <div class="text-sm space-y-1">
                                        <div><span class="text-gray-500">Loại:</span> ${checkup.checkupType}</div>
                                        ${checkup.notes ? `<div><span class="text-gray-500">Ghi chú:</span> ${checkup.notes}</div>` : ""}
                                        ${checkup.completedDate ? `<div><span class="text-gray-500">Hoàn thành:</span> ${formatDate(checkup.completedDate)}</div>` : ""}
                                        ${checkup.resultSummary ? `<div><span class="text-gray-500">Kết quả:</span> ${checkup.resultSummary}</div>` : ""}
                                    </div>
                                </div>
                            `).join("")}
                        </div>
                    ` : '<p class="text-gray-500 text-center py-4">Chưa có lịch tái khám</p>'}
                </div>
            </div>
        `;

        document.getElementById("treatmentDetailModal").classList.remove("hidden");
        document.getElementById("treatmentDetailModal").classList.add("flex");
    } catch (error) {
        console.error("Error loading treatment detail:", error);
        showError("Không thể tải chi tiết kế hoạch điều trị");
    }
}

/**
 * Close treatment modal
 */
export function closeTreatmentModal() {
    document.getElementById("treatmentDetailModal").classList.add("hidden");
    document.getElementById("treatmentDetailModal").classList.remove("flex");
}

/**
 * Management checkups (placeholder)
 */
export function managementCheckups(treatmentId) {
    alert("Chức năng quản lý lịch tái khám sẽ được phát triển trong phiên bản tiếp theo");
}

/**
 * Open create treatment modal (placeholder)
 */
export function openCreateTreatmentModal() {
    alert("Chức năng tạo kế hoạch điều trị sẽ được phát triển trong phiên bản tiếp theo");
}

// Helper functions
function getStatusBadge(status) {
    const badges = {
        DRAFT: '<span class="inline-flex items-center gap-1 px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-medium"><span class="material-icons text-sm">draft</span>Nháp</span>',
        ACTIVE: '<span class="inline-flex items-center gap-1 px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium"><span class="material-icons text-sm">play_circle</span>Đang thực hiện</span>',
        COMPLETED: '<span class="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-xs font-medium"><span class="material-icons text-sm">check_circle</span>Hoàn thành</span>',
        CANCELLED: '<span class="inline-flex items-center gap-1 px-3 py-1 bg-red-100 text-red-700 rounded-full text-xs font-medium"><span class="material-icons text-sm">cancel</span>Đã hủy</span>',
    };
    return badges[status] || status;
}

function getItemStatusBadge(status) {
    const badges = {
        PENDING: '<span class="px-2 py-1 bg-yellow-100 text-yellow-700 rounded text-xs">Chờ</span>',
        ONGOING: '<span class="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs">Đang thực hiện</span>',
        COMPLETED: '<span class="px-2 py-1 bg-green-100 text-green-700 rounded text-xs">Hoàn thành</span>',
        SKIPPED: '<span class="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs">Bỏ qua</span>',
    };
    return badges[status] || status;
}

function getCheckupStatusBadge(status) {
    const badges = {
        SCHEDULED: '<span class="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs">Đã lên lịch</span>',
        CONFIRMED: '<span class="px-2 py-1 bg-green-100 text-green-700 rounded text-xs">Đã xác nhận</span>',
        COMPLETED: '<span class="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs">Hoàn thành</span>',
        CANCELLED: '<span class="px-2 py-1 bg-red-100 text-red-700 rounded text-xs">Đã hủy</span>',
        NO_SHOW: '<span class="px-2 py-1 bg-orange-100 text-orange-700 rounded text-xs">Không đến</span>',
    };
    return badges[status] || status;
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
