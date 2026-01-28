/**
 * ABClinic - Common JavaScript Utilities
 * This file contains reusable utility functions used across the application
 */

/**
 * Debounce function to limit the rate at which a function can fire
 * @param {Function} fn - The function to debounce
 * @param {number} delay - The delay in milliseconds
 * @returns {Function} - The debounced function
 */
function debounce(fn, delay = 300) {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

/**
 * Format date to Vietnamese format
 * @param {Date|string} date - The date to format
 * @param {string} format - Format type: 'short', 'long', 'time', 'datetime'
 * @returns {string} - Formatted date string
 */
function formatDate(date, format = 'short') {
    if (!date) return '';
    
    const d = typeof date === 'string' ? new Date(date) : date;
    
    const options = {
        short: { day: '2-digit', month: '2-digit', year: 'numeric' },
        long: { day: 'numeric', month: 'long', year: 'numeric' },
        time: { hour: '2-digit', minute: '2-digit' },
        datetime: { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }
    };
    
    return d.toLocaleString('vi-VN', options[format] || options.short);
}

/**
 * Show toast notification
 * @param {string} message - The message to display
 * @param {string} type - Type of notification: 'success', 'error', 'warning', 'info'
 * @param {number} duration - Duration in milliseconds (default: 3000)
 */
function showToast(message, type = 'info', duration = 3000) {
    // Remove existing toast if any
    const existingToast = document.getElementById('toast-notification');
    if (existingToast) {
        existingToast.remove();
    }
    
    // Color mappings
    const colors = {
        success: 'bg-emerald-500',
        error: 'bg-red-500',
        warning: 'bg-orange-500',
        info: 'bg-primary-500'
    };
    
    // Icon mappings
    const icons = {
        success: 'check_circle',
        error: 'error',
        warning: 'warning',
        info: 'info'
    };
    
    // Create toast element
    const toast = document.createElement('div');
    toast.id = 'toast-notification';
    toast.className = `fixed top-4 right-4 ${colors[type]} text-white px-6 py-4 rounded-lg shadow-lg flex items-center gap-3 z-50 animate-slide-in`;
    toast.innerHTML = `
        <span class="material-symbols-outlined">${icons[type]}</span>
        <span class="font-medium">${message}</span>
        <button onclick="this.parentElement.remove()" class="ml-4 hover:bg-white/20 p-1 rounded">
            <span class="material-symbols-outlined text-sm">close</span>
        </button>
    `;
    
    document.body.appendChild(toast);
    
    // Auto-remove after duration
    setTimeout(() => {
        if (toast.parentElement) {
            toast.classList.add('animate-slide-out');
            setTimeout(() => toast.remove(), 300);
        }
    }, duration);
}

/**
 * Initialize WebSocket connection
 * @param {string} url - WebSocket URL
 * @param {Object} handlers - Event handlers { onOpen, onMessage, onError, onClose }
 * @returns {WebSocket} - WebSocket instance
 */
function initWebSocket(url, handlers = {}) {
    const ws = new WebSocket(url);
    
    ws.onopen = (event) => {
        console.log('WebSocket connected:', url);
        if (handlers.onOpen) handlers.onOpen(event);
    };
    
    ws.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            if (handlers.onMessage) handlers.onMessage(data);
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
        }
    };
    
    ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        if (handlers.onError) handlers.onError(error);
    };
    
    ws.onclose = (event) => {
        console.log('WebSocket disconnected:', event);
        if (handlers.onClose) handlers.onClose(event);
    };
    
    return ws;
}

/**
 * Calculate time difference from now
 * @param {Date|string} date - The date to compare
 * @returns {string} - Formatted time difference (e.g., "5 phút", "2 giờ")
 */
function timeAgo(date) {
    if (!date) return '';
    
    const now = new Date();
    const past = typeof date === 'string' ? new Date(date) : date;
    const diffMs = now - past;
    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    
    if (diffMinutes < 1) return 'Vừa xong';
    if (diffMinutes < 60) return diffMinutes + ' phút';
    
    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours < 24) {
        const remainingMinutes = diffMinutes % 60;
        return diffHours + ' giờ' + (remainingMinutes > 0 ? ' ' + remainingMinutes + ' phút' : '');
    }
    
    const diffDays = Math.floor(diffHours / 24);
    return diffDays + ' ngày';
}

/**
 * Validate email format
 * @param {string} email - Email address to validate
 * @returns {boolean} - True if valid
 */
function isValidEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

/**
 * Validate phone number (Vietnamese format)
 * @param {string} phone - Phone number to validate
 * @returns {boolean} - True if valid
 */
function isValidPhone(phone) {
    const regex = /^(0|\+84)[0-9]{9}$/;
    return regex.test(phone.replace(/\s/g, ''));
}

/**
 * Format number with thousand separators
 * @param {number} num - Number to format
 * @returns {string} - Formatted number
 */
function formatNumber(num) {
    return new Intl.NumberFormat('vi-VN').format(num);
}

/**
 * Confirm dialog with custom message
 * @param {string} message - Confirmation message
 * @returns {Promise<boolean>} - True if confirmed
 */
function confirmDialog(message) {
    return new Promise((resolve) => {
        const confirmed = window.confirm(message);
        resolve(confirmed);
    });
}

/**
 * Copy text to clipboard
 * @param {string} text - Text to copy
 * @returns {Promise<void>}
 */
async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text);
        showToast('Đã sao chép vào clipboard', 'success');
    } catch (error) {
        console.error('Failed to copy:', error);
        showToast('Không thể sao chép', 'error');
    }
}

/**
 * Scroll to element smoothly
 * @param {string|Element} element - Element ID or element
 * @param {number} offset - Offset from top (default: 0)
 */
function scrollToElement(element, offset = 0) {
    const el = typeof element === 'string' ? document.getElementById(element) : element;
    if (!el) return;
    
    const top = el.getBoundingClientRect().top + window.pageYOffset - offset;
    window.scrollTo({ top, behavior: 'smooth' });
}

/**
 * Toggle element visibility
 * @param {string|Element} element - Element ID or element
 */
function toggleVisibility(element) {
    const el = typeof element === 'string' ? document.getElementById(element) : element;
    if (!el) return;
    
    el.classList.toggle('hidden');
}

/**
 * Fetch API with error handling
 * @param {string} url - URL to fetch
 * @param {Object} options - Fetch options
 * @returns {Promise<any>} - Response data
 */
async function fetchAPI(url, options = {}) {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Fetch error:', error);
        showToast('Có lỗi xảy ra khi tải dữ liệu', 'error');
        throw error;
    }
}

/**
 * Initialize tooltips for elements with data-tooltip attribute
 */
function initTooltips() {
    const tooltipElements = document.querySelectorAll('[data-tooltip]');
    tooltipElements.forEach(el => {
        el.addEventListener('mouseenter', function() {
            const text = this.getAttribute('data-tooltip');
            const tooltip = document.createElement('div');
            tooltip.className = 'fixed bg-slate-900 text-white text-xs px-3 py-2 rounded shadow-lg z-50';
            tooltip.textContent = text;
            tooltip.id = 'tooltip-' + Date.now();
            
            document.body.appendChild(tooltip);
            
            const rect = this.getBoundingClientRect();
            tooltip.style.left = (rect.left + rect.width / 2 - tooltip.offsetWidth / 2) + 'px';
            tooltip.style.top = (rect.top - tooltip.offsetHeight - 8) + 'px';
        });
        
        el.addEventListener('mouseleave', function() {
            const tooltips = document.querySelectorAll('[id^="tooltip-"]');
            tooltips.forEach(t => t.remove());
        });
    });
}

// Initialize common features on page load
document.addEventListener('DOMContentLoaded', function() {
    initTooltips();
    
    // Add animation classes
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slide-in {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slide-out {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(100%); opacity: 0; }
        }
        .animate-slide-in { animation: slide-in 0.3s ease-out; }
        .animate-slide-out { animation: slide-out 0.3s ease-in; }
    `;
    document.head.appendChild(style);
});

// Export for use in modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        debounce,
        formatDate,
        showToast,
        initWebSocket,
        timeAgo,
        isValidEmail,
        isValidPhone,
        formatNumber,
        confirmDialog,
        copyToClipboard,
        scrollToElement,
        toggleVisibility,
        fetchAPI
    };
}
