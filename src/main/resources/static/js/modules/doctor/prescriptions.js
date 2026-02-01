/**
 * Doctor Prescriptions Management Module
 * Handles prescription listing, filtering, viewing, and actions
 */

// State management
let allPrescriptions = [];
let filteredPrescriptions = [];

/**
 * Initialize prescriptions page
 */
export function init() {
    loadPrescriptions();
}

/**
 * Load prescriptions from API
 */
async function loadPrescriptions() {
    try {
        const response = await fetch("/api/doctor/prescriptions");
        const data = await response.json();

        allPrescriptions = data.prescriptions;
        filteredPrescriptions = allPrescriptions;

        updateStatistics();
        renderPrescriptions();
    } catch (error) {
        console.error("Error loading prescriptions:", error);
        showError("Không thể tải danh sách đơn thuốc");
    }
}

/**
 * Update statistics dashboard
 */
function updateStatistics() {
    document.getElementById("totalCount").textContent = allPrescriptions.length;

    const activeCount = allPrescriptions.filter(p => p.status === "ACTIVE").length;
    document.getElementById("activeCount").textContent = activeCount;

    const completedCount = allPrescriptions.filter(p => p.status === "COMPLETED").length;
    document.getElementById("completedCount").textContent = completedCount;

    const cancelledCount = allPrescriptions.filter(p => p.status === "CANCELLED").length;
    document.getElementById("cancelledCount").textContent = cancelledCount;
}

/**
 * Render prescriptions table
 */
function renderPrescriptions() {
    const tbody = document.getElementById("prescriptionsTable");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");

    loadingState.classList.add("hidden");

    if (filteredPrescriptions.length === 0) {
        tbody.innerHTML = "";
        emptyState.classList.remove("hidden");
        return;
    }

    emptyState.classList.add("hidden");

    tbody.innerHTML = filteredPrescriptions.map(prescription => `
        <tr class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4">
                <span class="font-medium text-gray-900">#${prescription.id}</span>
            </td>
            <td class="px-6 py-4">
                <div>
                    <div class="font-medium text-gray-900">${prescription.patientName}</div>
                    <div class="text-sm text-gray-500">${prescription.patientPhone || ""}</div>
                </div>
            </td>
            <td class="px-6 py-4">
                <span class="text-sm text-gray-700">${prescription.diagnosis || "Không có"}</span>
            </td>
            <td class="px-6 py-4">
                <span class="inline-flex items-center gap-1 px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm font-medium">
                    <span class="material-icons text-base">medication</span>
                    ${prescription.medicationCount} loại
                </span>
            </td>
            <td class="px-6 py-4">
                <span class="text-sm text-gray-700">${formatDate(prescription.prescriptionDate)}</span>
            </td>
            <td class="px-6 py-4">
                ${getStatusBadge(prescription.status)}
            </td>
            <td class="px-6 py-4">
                <div class="flex items-center gap-2">
                    <button onclick="window.doctorPrescriptions.viewPrescription(${prescription.id})" class="text-blue-600 hover:text-blue-800" title="Xem chi tiết">
                        <span class="material-icons text-xl">visibility</span>
                    </button>
                    <button onclick="window.doctorPrescriptions.editPrescription(${prescription.id})" class="text-green-600 hover:text-green-800" title="Chỉnh sửa">
                        <span class="material-icons text-xl">edit</span>
                    </button>
                    <button onclick="window.doctorPrescriptions.printPrescription(${prescription.id})" class="text-purple-600 hover:text-purple-800" title="In đơn thuốc">
                        <span class="material-icons text-xl">print</span>
                    </button>
                </div>
            </td>
        </tr>
    `).join("");
}

/**
 * Apply filters to prescription list
 */
export function applyFilters() {
    const statusFilter = document.getElementById("statusFilter").value;
    const patientSearch = document.getElementById("patientSearch").value.toLowerCase();
    const startDate = document.getElementById("startDate").value;
    const endDate = document.getElementById("endDate").value;

    filteredPrescriptions = allPrescriptions.filter(prescription => {
        if (statusFilter && prescription.status !== statusFilter) {
            return false;
        }

        if (patientSearch) {
            const patientName = prescription.patientName.toLowerCase();
            if (!patientName.includes(patientSearch)) {
                return false;
            }
        }

        if (startDate && prescription.prescriptionDate < startDate) {
            return false;
        }
        if (endDate && prescription.prescriptionDate > endDate) {
            return false;
        }

        return true;
    });

    renderPrescriptions();
}

/**
 * Clear all filters
 */
export function clearFilters() {
    document.getElementById("statusFilter").value = "";
    document.getElementById("patientSearch").value = "";
    document.getElementById("startDate").value = "";
    document.getElementById("endDate").value = "";

    filteredPrescriptions = allPrescriptions;
    renderPrescriptions();
}

/**
 * View prescription detail
 */
export async function viewPrescription(prescriptionId) {
    try {
        const response = await fetch(`/api/doctor/prescriptions/${prescriptionId}`);
        const prescription = await response.json();

        document.getElementById("prescriptionDetailContent").innerHTML = `
            <div class="space-y-6">
                <!-- Patient Info -->
                <div class="border-b pb-4">
                    <h4 class="font-semibold text-gray-900 mb-3">Thông tin bệnh nhân</h4>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <span class="text-sm text-gray-500">Họ tên:</span>
                            <p class="font-medium text-gray-900">${prescription.patientName}</p>
                        </div>
                        <div>
                            <span class="text-sm text-gray-500">Số điện thoại:</span>
                            <p class="font-medium text-gray-900">${prescription.patientPhone || "N/A"}</p>
                        </div>
                    </div>
                </div>
                
                <!-- Prescription Info -->
                <div class="border-b pb-4">
                    <h4 class="font-semibold text-gray-900 mb-3">Thông tin đơn thuốc</h4>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <span class="text-sm text-gray-500">Mã đơn:</span>
                            <p class="font-medium text-gray-900">#${prescription.id}</p>
                        </div>
                        <div>
                            <span class="text-sm text-gray-500">Ngày kê đơn:</span>
                            <p class="font-medium text-gray-900">${formatDate(prescription.prescriptionDate)}</p>
                        </div>
                        <div>
                            <span class="text-sm text-gray-500">Bác sĩ:</span>
                            <p class="font-medium text-gray-900">${prescription.doctorName}</p>
                        </div>
                        <div>
                            <span class="text-sm text-gray-500">Trạng thái:</span>
                            <div class="mt-1">${getStatusBadge(prescription.status)}</div>
                        </div>
                    </div>
                    <div class="mt-4">
                        <span class="text-sm text-gray-500">Chẩn đoán:</span>
                        <p class="font-medium text-gray-900 mt-1">${prescription.diagnosis || "Không có"}</p>
                    </div>
                </div>
                
                <!-- Medications -->
                <div>
                    <h4 class="font-semibold text-gray-900 mb-3">Danh sách thuốc</h4>
                    <div class="space-y-4">
                        ${prescription.medications.map((med, index) => `
                            <div class="border border-gray-200 rounded-lg p-4">
                                <div class="flex items-start justify-between">
                                    <div class="flex-1">
                                        <h5 class="font-medium text-gray-900">${index + 1}. ${med.medicineName}</h5>
                                        <div class="grid grid-cols-2 gap-2 mt-2">
                                            <div class="text-sm">
                                                <span class="text-gray-500">Liều dùng:</span>
                                                <span class="text-gray-900 ml-2">${med.dosage}</span>
                                            </div>
                                            <div class="text-sm">
                                                <span class="text-gray-500">Tần suất:</span>
                                                <span class="text-gray-900 ml-2">${med.frequency}</span>
                                            </div>
                                            <div class="text-sm">
                                                <span class="text-gray-500">Thời gian:</span>
                                                <span class="text-gray-900 ml-2">${med.duration}</span>
                                            </div>
                                            <div class="text-sm">
                                                <span class="text-gray-500">Số lượng:</span>
                                                <span class="text-gray-900 ml-2">${med.quantity}</span>
                                            </div>
                                        </div>
                                        ${med.instructions ? `
                                            <div class="mt-2 text-sm">
                                                <span class="text-gray-500">Hướng dẫn:</span>
                                                <p class="text-gray-700 mt-1">${med.instructions}</p>
                                            </div>
                                        ` : ""}
                                    </div>
                                </div>
                            </div>
                        `).join("")}
                    </div>
                </div>
                
                ${prescription.notes ? `
                    <div class="border-t pt-4">
                        <h4 class="font-semibold text-gray-900 mb-2">Ghi chú</h4>
                        <p class="text-gray-700">${prescription.notes}</p>
                    </div>
                ` : ""}
            </div>
        `;

        document.getElementById("prescriptionDetailModal").classList.remove("hidden");
        document.getElementById("prescriptionDetailModal").classList.add("flex");
    } catch (error) {
        console.error("Error loading prescription detail:", error);
        showError("Không thể tải chi tiết đơn thuốc");
    }
}

/**
 * Close prescription modal
 */
export function closePrescriptionModal() {
    document.getElementById("prescriptionDetailModal").classList.add("hidden");
    document.getElementById("prescriptionDetailModal").classList.remove("flex");
}

/**
 * Edit prescription
 */
export function editPrescription(prescriptionId) {
    window.location.href = `/doctor/prescriptions/edit/${prescriptionId}`;
}

/**
 * Print prescription
 */
export function printPrescription(prescriptionId) {
    window.open(`/doctor/prescriptions/${prescriptionId}/print`, "_blank");
}

// Helper functions
function getStatusBadge(status) {
    const badges = {
        ACTIVE: '<span class="inline-flex items-center gap-1 px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium"><span class="material-icons text-sm">check_circle</span>Đang hoạt động</span>',
        COMPLETED: '<span class="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-xs font-medium"><span class="material-icons text-sm">done_all</span>Hoàn thành</span>',
        CANCELLED: '<span class="inline-flex items-center gap-1 px-3 py-1 bg-red-100 text-red-700 rounded-full text-xs font-medium"><span class="material-icons text-sm">cancel</span>Đã hủy</span>',
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
