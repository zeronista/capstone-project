/**
 * Doctor Tickets Management Module
 * Handles ticket listing, filtering, viewing, and status updates
 */

// State management
let currentTicketId = null;
let allTickets = [];

/**
 * Initialize tickets page
 */
export function init() {
    loadTickets();

    // Add event listeners for filters
    document.getElementById("statusFilter").addEventListener("change", filterTickets);
    document.getElementById("priorityFilter").addEventListener("change", filterTickets);
}

/**
 * Load tickets from API
 */
async function loadTickets() {
    try {
        const response = await fetch("/api/doctor/tickets");
        const data = await response.json();

        allTickets = data.tickets || [];
        renderTickets(allTickets);
        updateStatistics(allTickets);
    } catch (error) {
        console.error("Error loading tickets:", error);
        showError("Không thể tải danh sách ticket");
    }
}

/**
 * Render tickets to table
 */
function renderTickets(tickets) {
    const tbody = document.getElementById("ticketsTableBody");

    if (tickets.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="px-6 py-8 text-center text-slate-500">Không có ticket nào</td></tr>';
        document.getElementById("resultsInfo").textContent = "Không có ticket";
        return;
    }

    tbody.innerHTML = tickets.map(ticket => `
        <tr class="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
            <td class="px-6 py-4 text-sm font-medium text-slate-900 dark:text-white">#${ticket.id}</td>
            <td class="px-6 py-4">
                <div class="text-sm font-medium text-slate-900 dark:text-white">${escapeHtml(ticket.title)}</div>
                <div class="text-xs text-slate-500 dark:text-slate-400 truncate max-w-xs">${escapeHtml(ticket.description || "")}</div>
            </td>
            <td class="px-6 py-4">
                <div class="text-sm text-slate-900 dark:text-white">${escapeHtml(ticket.patientName || "N/A")}</div>
                <div class="text-xs text-slate-500 dark:text-slate-400">${escapeHtml(ticket.patientPhone || "")}</div>
            </td>
            <td class="px-6 py-4">
                ${getPriorityBadge(ticket.priority)}
            </td>
            <td class="px-6 py-4">
                ${getStatusBadge(ticket.status)}
            </td>
            <td class="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
                ${formatDateTime(ticket.createdAt)}
            </td>
            <td class="px-6 py-4 text-right">
                <button onclick="window.doctorTickets.viewTicketDetail(${ticket.id})" class="text-primary hover:text-primary-dark transition-colors">
                    <span class="material-symbols-outlined">visibility</span>
                </button>
            </td>
        </tr>
    `).join("");

    document.getElementById("resultsInfo").textContent = `Hiển thị ${tickets.length} ticket`;
}

/**
 * Filter tickets
 */
function filterTickets() {
    const statusFilter = document.getElementById("statusFilter").value;
    const priorityFilter = document.getElementById("priorityFilter").value;

    let filtered = allTickets;

    if (statusFilter) {
        filtered = filtered.filter(t => t.status === statusFilter);
    }

    if (priorityFilter) {
        filtered = filtered.filter(t => t.priority === priorityFilter);
    }

    renderTickets(filtered);
}

/**
 * Update statistics
 */
function updateStatistics(tickets) {
    document.getElementById("totalTickets").textContent = tickets.length;
    document.getElementById("inProgressTickets").textContent = tickets.filter(t => t.status === "IN_PROGRESS").length;
    document.getElementById("highPriorityTickets").textContent = tickets.filter(t => t.priority === "HIGH" || t.priority === "URGENT").length;
    document.getElementById("resolvedTickets").textContent = tickets.filter(t => t.status === "RESOLVED" || t.status === "CLOSED").length;
}

/**
 * View ticket detail
 */
export async function viewTicketDetail(ticketId) {
    currentTicketId = ticketId;

    try {
        const response = await fetch(`/api/doctor/tickets/${ticketId}`);
        const ticket = await response.json();

        renderTicketDetail(ticket);
        openModal();
    } catch (error) {
        console.error("Error loading ticket detail:", error);
        showError("Không thể tải chi tiết ticket");
    }
}

/**
 * Render ticket detail
 */
function renderTicketDetail(ticket) {
    const content = document.getElementById("ticketDetailContent");

    content.innerHTML = `
        <div class="space-y-6">
            <!-- Ticket Info -->
            <div class="bg-slate-50 dark:bg-slate-800 rounded-lg p-4">
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <p class="text-xs text-slate-500 dark:text-slate-400">Bệnh nhân</p>
                        <p class="text-sm font-medium text-slate-900 dark:text-white">${escapeHtml(ticket.patientName || "N/A")}</p>
                    </div>
                    <div>
                        <p class="text-xs text-slate-500 dark:text-slate-400">Danh mục</p>
                        <p class="text-sm font-medium text-slate-900 dark:text-white">${ticket.category || "N/A"}</p>
                    </div>
                    <div>
                        <p class="text-xs text-slate-500 dark:text-slate-400">Mức độ</p>
                        ${getPriorityBadge(ticket.priority)}
                    </div>
                    <div>
                        <p class="text-xs text-slate-500 dark:text-slate-400">Trạng thái</p>
                        ${getStatusBadge(ticket.status)}
                    </div>
                </div>
            </div>

            <!-- Description -->
            <div>
                <h4 class="text-sm font-bold text-slate-900 dark:text-white mb-2">Mô tả</h4>
                <p class="text-sm text-slate-600 dark:text-slate-400">${escapeHtml(ticket.description || "Không có mô tả")}</p>
            </div>

            <!-- Messages -->
            <div>
                <h4 class="text-sm font-bold text-slate-900 dark:text-white mb-3">Lịch sử tin nhắn</h4>
                <div class="space-y-3">
                    ${ticket.messages && ticket.messages.length > 0 ? ticket.messages.map(msg => `
                        <div class="bg-slate-50 dark:bg-slate-800 rounded-lg p-3">
                            <div class="flex items-start gap-3">
                                <div class="size-8 rounded-full bg-primary text-white flex items-center justify-center text-xs font-bold">
                                    ${msg.senderName ? msg.senderName.charAt(0).toUpperCase() : "?"}
                                </div>
                                <div class="flex-1">
                                    <div class="flex items-center gap-2 mb-1">
                                        <p class="text-sm font-medium text-slate-900 dark:text-white">${escapeHtml(msg.senderName || "Unknown")}</p>
                                        <span class="text-xs text-slate-500">${formatDateTime(msg.createdAt)}</span>
                                    </div>
                                    <p class="text-sm text-slate-600 dark:text-slate-400">${escapeHtml(msg.messageText)}</p>
                                </div>
                            </div>
                        </div>
                    `).join("") : '<p class="text-sm text-slate-500 text-center py-4">Chưa có tin nhắn</p>'}
                </div>
            </div>
        </div>
    `;
}

/**
 * Update status
 */
export async function updateStatus() {
    const newStatus = document.getElementById("newStatus").value;

    if (!newStatus) {
        alert("Vui lòng chọn trạng thái");
        return;
    }

    try {
        const response = await fetch(`/api/doctor/tickets/${currentTicketId}/status`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                status: newStatus,
                note: "",
            }),
        });

        const result = await response.json();

        if (result.success) {
            alert("Cập nhật trạng thái thành công");
            closeTicketDetail();
            loadTickets();
        }
    } catch (error) {
        console.error("Error updating status:", error);
        showError("Không thể cập nhật trạng thái");
    }
}

/**
 * Send reply
 */
export async function sendReply() {
    const message = document.getElementById("replyMessage").value.trim();

    if (!message) {
        alert("Vui lòng nhập nội dung phản hồi");
        return;
    }

    try {
        const response = await fetch(`/api/doctor/tickets/${currentTicketId}/messages`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                messageText: message,
                messageType: "TEXT",
                isInternalNote: false,
            }),
        });

        const result = await response.json();

        if (result.success) {
            document.getElementById("replyMessage").value = "";
            viewTicketDetail(currentTicketId); // Reload detail
        }
    } catch (error) {
        console.error("Error sending reply:", error);
        showError("Không thể gửi phản hồi");
    }
}

/**
 * Refresh tickets
 */
export function refreshTickets() {
    loadTickets();
}

/**
 * Open modal
 */
function openModal() {
    document.getElementById("ticketDetailModal").classList.add("show");
    document.getElementById("modalBackdrop").classList.add("show");
    document.body.style.overflow = "hidden";
}

/**
 * Close ticket detail modal
 */
export function closeTicketDetail() {
    document.getElementById("ticketDetailModal").classList.remove("show");
    document.getElementById("modalBackdrop").classList.remove("show");
    document.body.style.overflow = "";
    currentTicketId = null;
}

// Helper functions
function getPriorityBadge(priority) {
    const badges = {
        URGENT: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-700 border border-red-200">Khẩn cấp</span>',
        HIGH: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-orange-100 text-orange-700 border border-orange-200">Cao</span>',
        MEDIUM: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-yellow-100 text-yellow-700 border border-yellow-200">Trung bình</span>',
        LOW: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-100 text-slate-700 border border-slate-200">Thấp</span>',
    };
    return badges[priority] || badges["MEDIUM"];
}

function getStatusBadge(status) {
    const badges = {
        OPEN: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-100 text-blue-700">Mới tạo</span>',
        ASSIGNED: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-purple-100 text-purple-700">Đã gán</span>',
        IN_PROGRESS: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-100 text-amber-700">Đang xử lý</span>',
        RESOLVED: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-green-100 text-green-700">Đã giải quyết</span>',
        CLOSED: '<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-100 text-slate-700">Đóng</span>',
    };
    return badges[status] || badges["OPEN"];
}

function formatDateTime(dateString) {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleString("vi-VN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

function showError(message) {
    alert(message);
}
