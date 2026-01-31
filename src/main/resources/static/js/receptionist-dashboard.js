/**
 * Receptionist Dashboard Module with Real-time WebSocket Queue Management
 * Handles queue updates, wait times, countdowns, and WebSocket communication
 */

// WebSocket connection state
let stompClient = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 10;
const RECONNECT_DELAY = 3000;

/**
 * Toggle mobile sidebar
 */
export function toggleMobileSidebar() {
    const sidebar = document.getElementById("sidebar");
    sidebar.classList.toggle("hidden");
    sidebar.classList.toggle("flex");
    sidebar.classList.toggle("fixed");
    sidebar.classList.toggle("inset-0");
    sidebar.classList.toggle("z-50");
}

/**
 * Calculate and update wait times for all queue items
 */
function updateWaitTimes() {
    const waitTimeElements = document.querySelectorAll(".wait-time");
    waitTimeElements.forEach((element) => {
        const createdAt = new Date(element.getAttribute("data-created"));
        const now = new Date();
        const diffMinutes = Math.floor((now - createdAt) / (1000 * 60));

        if (diffMinutes < 1) {
            element.textContent = "Vừa xong";
        } else if (diffMinutes < 60) {
            element.textContent = diffMinutes + " phút";
        } else {
            const hours = Math.floor(diffMinutes / 60);
            const minutes = diffMinutes % 60;
            element.textContent = hours + " giờ " + minutes + " phút";
        }
    });
}

/**
 * Calculate and update average wait time
 */
function updateAvgWaitTime() {
    const waitTimeElements = document.querySelectorAll(".wait-time");
    if (waitTimeElements.length === 0) {
        document.getElementById("avgWaitTime").textContent = "--";
        return;
    }

    let totalMinutes = 0;
    waitTimeElements.forEach((element) => {
        const createdAt = new Date(element.getAttribute("data-created"));
        const now = new Date();
        const diffMinutes = Math.floor((now - createdAt) / (1000 * 60));
        totalMinutes += diffMinutes;
    });

    const avgMinutes = Math.floor(totalMinutes / waitTimeElements.length);
    document.getElementById("avgWaitTime").textContent = avgMinutes + " phút";
}

/**
 * Update countdown timers
 */
function updateCountdowns() {
    const countdownElements = document.querySelectorAll(".countdown");
    countdownElements.forEach((element) => {
        const targetTime = new Date(element.getAttribute("data-target"));
        const now = new Date();
        const diff = targetTime - now;

        if (diff <= 0) {
            element.textContent = "00:00";
            element.classList.add("text-red-600", "animate-pulse");
            return;
        }

        const minutes = Math.floor(diff / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        element.textContent = String(minutes).padStart(2, "0") + ":" + String(seconds).padStart(2, "0");
    });
}

/**
 * Refresh queue data (reload page)
 */
export function refreshQueue() {
    console.log("Refreshing queue...");
    location.reload();
}

/**
 * Handle call patient action
 */
export function handleCallPatient(button) {
    const ticketId = button.getAttribute("data-ticket-id");
    console.log("Calling patient for ticket:", ticketId);

    alert(
        "Đang thực hiện cuộc gọi cho ticket #" +
        ticketId +
        "\n\nChức năng này sẽ được tích hợp với Stringee trong phase sau."
    );

    button.closest(".group").style.opacity = "0.5";
    button.disabled = true;
    button.innerHTML = '<span class="material-symbols-outlined text-sm">check</span><span>Đang gọi...</span>';
}

/**
 * Handle retry call action
 */
export function handleRetryCall(button) {
    const ticketId = button.getAttribute("data-ticket-id");
    console.log("Retrying call for ticket:", ticketId);

    alert(
        "Đang thực hiện cuộc gọi lại cho " +
        ticketId +
        "\n\nChức năng này sẽ được tích hợp với Stringee trong phase sau."
    );

    button.disabled = true;
    button.classList.remove("bg-orange-500", "hover:bg-orange-600", "bg-red-500", "hover:bg-red-600");
    button.classList.add("bg-slate-400");
    button.innerHTML = '<span class="material-symbols-outlined">check</span><span>Đang gọi...</span>';
}

// ==================== WEBSOCKET REAL-TIME UPDATES ====================

/**
 * Initialize WebSocket connection
 */
function initWebSocket() {
    console.log("Initializing WebSocket connection...");

    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, onConnected, onError);
}

/**
 * WebSocket connected callback
 */
function onConnected() {
    console.log("WebSocket connected successfully");
    reconnectAttempts = 0;
    updateConnectionStatus(true);

    stompClient.subscribe("/topic/queue", onMessageReceived);
    showNotification("Đã kết nối real-time", "success");
}

/**
 * WebSocket error callback
 */
function onError(error) {
    console.error("WebSocket connection error:", error);
    updateConnectionStatus(false);

    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++;
        console.log(`Reconnecting... Attempt ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS}`);
        setTimeout(initWebSocket, RECONNECT_DELAY);
    } else {
        showNotification("Không thể kết nối real-time. Vui lòng tải lại trang.", "error");
    }
}

/**
 * Handle incoming WebSocket messages
 */
function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    console.log("Received message:", message);

    switch (message.action) {
        case "ADD":
            handleTicketAdded(message);
            break;
        case "REMOVE":
            handleTicketRemoved(message);
            break;
        case "UPDATE":
            handleTicketUpdated(message);
            break;
        case "CALL":
            handleTicketCalled(message);
            break;
        case "COMPLETE":
            handleTicketCompleted(message);
            break;
        default:
            console.warn("Unknown action:", message.action);
    }
}

/**
 * Handle ticket added to queue
 */
function handleTicketAdded(message) {
    console.log("Ticket added:", message.ticketId);

    const containerId = message.queueType === "RETRY" ? "retryQueueList" : "mainQueueList";
    const container = document.getElementById(containerId);

    if (!container) return;

    const existingCard = container.querySelector(`[data-ticket-id="${message.ticketId}"]`);
    if (existingCard) {
        console.log("Ticket already exists, skipping");
        return;
    }

    const ticketCard = createTicketCard(message);

    if (container.firstChild) {
        container.insertBefore(ticketCard, container.firstChild);
    } else {
        container.appendChild(ticketCard);
    }

    updateQueueCounters();
    showNotification(`Ticket mới: ${message.patientName || "Bệnh nhân"}`, "info");
    playNotificationSound();
}

/**
 * Handle ticket removed from queue
 */
function handleTicketRemoved(message) {
    console.log("Ticket removed:", message.ticketId);

    const ticketCard = document.querySelector(`[data-ticket-id="${message.ticketId}"]`);
    if (ticketCard) {
        ticketCard.classList.add("animate-fade-out");
        setTimeout(() => {
            ticketCard.remove();
            updateQueueCounters();
        }, 300);
    }
}

/**
 * Handle ticket updated
 */
function handleTicketUpdated(message) {
    console.log("Ticket updated:", message.ticketId);

    const ticketCard = document.querySelector(`[data-ticket-id="${message.ticketId}"]`);
    if (ticketCard) {
        const statusBadge = ticketCard.querySelector(".status-badge");
        if (statusBadge && message.status) {
            updateStatusBadge(statusBadge, message.status);
        }

        if (message.retryCount > 0) {
            const retryBadge = ticketCard.querySelector(".retry-badge");
            if (retryBadge) {
                retryBadge.textContent = `Lần ${message.retryCount}`;
            }
        }
    }
}

/**
 * Handle ticket called
 */
function handleTicketCalled(message) {
    console.log("Ticket called:", message.ticketId);

    const ticketCard = document.querySelector(`[data-ticket-id="${message.ticketId}"]`);
    if (ticketCard) {
        ticketCard.classList.add("ring-2", "ring-blue-500", "bg-blue-50");

        const callButton = ticketCard.querySelector(".call-button");
        if (callButton) {
            callButton.disabled = true;
            callButton.classList.add("bg-slate-400");
            callButton.innerHTML = '<span class="material-symbols-outlined">check</span><span>Đang gọi...</span>';
        }
    }

    showNotification(`Đang gọi: ${message.patientName}`, "info");
}

/**
 * Handle ticket completed
 */
function handleTicketCompleted(message) {
    console.log("Ticket completed:", message.ticketId);
    handleTicketRemoved(message);
    showNotification("Ticket đã hoàn thành", "success");
}

/**
 * Create ticket card HTML element
 */
function createTicketCard(message) {
    const card = document.createElement("div");
    card.className = "bg-white rounded-xl border border-slate-200 p-5 hover:border-emerald-300 hover:shadow-md transition-all cursor-pointer animate-slide-in";
    card.setAttribute("data-ticket-id", message.ticketId);

    const priorityColor = {
        HIGH: "text-red-600 bg-red-50",
        MEDIUM: "text-yellow-600 bg-yellow-50",
        LOW: "text-slate-600 bg-slate-50",
    }[message.priority] || "text-slate-600 bg-slate-50";

    card.innerHTML = `
        <div class="flex items-start justify-between mb-3">
            <div>
                <div class="flex items-center gap-2 mb-1">
                    <h3 class="text-base font-semibold text-slate-900">
                        ${message.patientName || "Bệnh nhân"}
                    </h3>
                    <span class="px-2 py-0.5 ${priorityColor} rounded-full text-xs font-medium">
                        ${message.priority || "MEDIUM"}
                    </span>
                </div>
                <p class="text-sm text-slate-500">ID: #${message.ticketId}</p>
            </div>
            <span class="text-xs text-slate-400">Vừa xong</span>
        </div>
        
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-4 text-sm text-slate-500">
                <div class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-lg">schedule</span>
                    <span class="wait-time" data-created="${new Date().toISOString()}">Vừa xong</span>
                </div>
                ${message.queuePosition ? `<span class="text-emerald-600 font-medium">Vị trí: ${message.queuePosition}</span>` : ""}
            </div>
            <button 
                onclick="window.receptionistDashboard.callPatient('${message.ticketId}')" 
                class="call-button flex items-center gap-2 px-4 py-2 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-colors text-sm font-medium">
                <span class="material-symbols-outlined text-lg">call</span>
                <span>Gọi</span>
            </button>
        </div>
    `;

    return card;
}

/**
 * Update connection status indicator
 */
function updateConnectionStatus(connected) {
    let statusElement = document.getElementById("wsConnectionStatus");
    
    if (!statusElement) {
        statusElement = document.createElement("div");
        statusElement.id = "wsConnectionStatus";
        document.body.appendChild(statusElement);
    }

    if (connected) {
        statusElement.className = "fixed bottom-4 right-4 px-4 py-2 bg-green-500 rounded-lg shadow-lg text-white text-sm font-medium z-50 flex items-center gap-2";
        statusElement.innerHTML = '<span class="w-2 h-2 bg-white rounded-full animate-pulse"></span><span>Kết nối real-time</span>';
        setTimeout(() => (statusElement.style.display = "none"), 3000);
    } else {
        statusElement.className = "fixed bottom-4 right-4 px-4 py-2 bg-red-500 rounded-lg shadow-lg text-white text-sm font-medium z-50 flex items-center gap-2";
        statusElement.innerHTML = '<span class="w-2 h-2 bg-white rounded-full"></span><span>Mất kết nối</span>';
        statusElement.style.display = "flex";
    }
}

/**
 * Update queue counters
 */
function updateQueueCounters() {
    const mainQueueCount = document.querySelectorAll("#mainQueueList > div").length;
    const retryQueueCount = document.querySelectorAll("#retryQueueList > div").length;

    const queueBadge = document.querySelector('[href="/receptionist/dashboard"] .badge');
    if (queueBadge) {
        queueBadge.textContent = mainQueueCount;
    }

    const openTicketsElement = document.querySelector(".stat-open-tickets");
    if (openTicketsElement) {
        openTicketsElement.textContent = mainQueueCount;
    }

    const retryTicketsElement = document.querySelector(".stat-retry-tickets");
    if (retryTicketsElement) {
        retryTicketsElement.textContent = retryQueueCount;
    }
}

/**
 * Show toast notification
 */
function showNotification(message, type = "info") {
    const toast = document.createElement("div");
    const colors = {
        success: "bg-green-500",
        error: "bg-red-500",
        info: "bg-blue-500",
        warning: "bg-yellow-500",
    };

    toast.className = `fixed top-4 right-4 px-6 py-3 ${colors[type]} text-white rounded-lg shadow-lg z-50 animate-slide-in flex items-center gap-2`;
    toast.innerHTML = `
        <span class="material-symbols-outlined">notifications</span>
        <span>${message}</span>
    `;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add("animate-fade-out");
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

/**
 * Update status badge appearance
 */
function updateStatusBadge(badge, status) {
    const statusClasses = {
        OPEN: "bg-blue-50 text-blue-600",
        IN_PROGRESS: "bg-yellow-50 text-yellow-600",
        CLOSED: "bg-green-50 text-green-600",
        RESOLVED: "bg-emerald-50 text-emerald-600",
    };

    badge.className = `px-2 py-0.5 rounded-full text-xs font-medium ${statusClasses[status] || "bg-slate-50 text-slate-600"}`;
    badge.textContent = status;
}

/**
 * Play notification sound
 */
function playNotificationSound() {
    // Optional: Add audio notification
    // const audio = new Audio('/sounds/notification.mp3');
    // audio.play().catch(e => console.log('Audio play failed:', e));
}

/**
 * Send message via WebSocket
 */
export function sendMessage(destination, message) {
    if (stompClient && stompClient.connected) {
        stompClient.send(destination, {}, JSON.stringify(message));
    } else {
        console.error("WebSocket not connected");
    }
}

/**
 * Initialize the receptionist dashboard
 */
export function init() {
    updateWaitTimes();
    updateAvgWaitTime();
    updateCountdowns();

    setInterval(() => {
        updateWaitTimes();
        updateAvgWaitTime();
        updateCountdowns();
    }, 1000);

    initWebSocket();
}
