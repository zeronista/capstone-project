/**
 * Patient Notifications Page Module
 * 
 * Chức năng: Quản lý trang thông báo của bệnh nhân
 * Cách hoạt động:
 * - Tải danh sách thông báo từ API với phân trang
 * - Hiển thị thông báo với icon và màu sắc theo loại
 * - Lọc thông báo (tất cả / chưa đọc)
 * - Đánh dấu đã đọc khi click vào thông báo
 * - Đánh dấu tất cả đã đọc
 * - Phân trang
 * 
 * @version 1.0.0
 * @date February 13, 2026
 */

(function () {
  'use strict';

  // ============================================================================
  // Configuration
  // ============================================================================

  const CONFIG = {
    pageSize: 20,
  };

  // Notification type icons and colors
  const NOTIFICATION_ICONS = {
    TICKET: { icon: 'confirmation_number', color: 'text-blue-500' },
    REMINDER: { icon: 'alarm', color: 'text-orange-500' },
    MESSAGE: { icon: 'mail', color: 'text-green-500' },
    SYSTEM: { icon: 'info', color: 'text-slate-500' },
    CALL: { icon: 'call', color: 'text-purple-500' },
  };

  // ============================================================================
  // State Management
  // ============================================================================

  const state = {
    notifications: [],
    currentPage: 0,
    totalPages: 0,
    totalItems: 0,
    filter: 'all', // 'all' or 'unread'
    isLoading: false,
  };

  // ============================================================================
  // DOM Elements
  // ============================================================================

  const elements = {
    notificationsList: document.getElementById('notificationsList'),
    loadingState: document.getElementById('loadingState'),
    emptyState: document.getElementById('emptyState'),
    filterAll: document.getElementById('filterAll'),
    filterUnread: document.getElementById('filterUnread'),
    markAllReadBtn: document.getElementById('markAllReadBtn'),
    paginationContainer: document.getElementById('paginationContainer'),
    pageInfo: document.getElementById('pageInfo'),
    prevPageBtn: document.getElementById('prevPageBtn'),
    nextPageBtn: document.getElementById('nextPageBtn'),
  };

  // ============================================================================
  // API Functions
  // ============================================================================

  /**
   * Load notifications from API
   * Chức năng: Tải danh sách thông báo từ server
   * Cách hoạt động: Gọi API với tham số phân trang, xử lý response và render
   */
  async function loadNotifications(page = 0) {
    try {
      state.isLoading = true;
      showLoadingState();

      let endpoint = `/notifications?page=${page}&size=${CONFIG.pageSize}`;

      const response = await apiClient.get(endpoint);

      state.notifications = response.notifications || [];
      state.currentPage = response.currentPage || 0;
      state.totalPages = response.totalPages || 0;
      state.totalItems = response.totalItems || 0;

      renderNotifications();
      updatePagination();
    } catch (error) {
      console.error('Error loading notifications:', error);
      showError('Không thể tải thông báo. Vui lòng thử lại.');
    } finally {
      state.isLoading = false;
      hideLoadingState();
    }
  }

  /**
   * Load unread notifications
   * Chức năng: Tải danh sách thông báo chưa đọc
   * Cách hoạt động: Gọi API endpoint /unread để lấy chỉ thông báo chưa đọc
   */
  async function loadUnreadNotifications() {
    try {
      state.isLoading = true;
      showLoadingState();

      const response = await apiClient.get('/notifications/unread');

      state.notifications = response || [];
      state.currentPage = 0;
      state.totalPages = 1;
      state.totalItems = response.length;

      renderNotifications();
      updatePagination();
    } catch (error) {
      console.error('Error loading unread notifications:', error);
      showError('Không thể tải thông báo. Vui lòng thử lại.');
    } finally {
      state.isLoading = false;
      hideLoadingState();
    }
  }

  /**
   * Mark notification as read
   * Chức năng: Đánh dấu một thông báo là đã đọc
   * Cách hoạt động: Gọi API PUT /notifications/{id}/read, cập nhật state và UI
   */
  async function markAsRead(notificationId) {
    try {
      const response = await apiClient.put(
        `/notifications/${notificationId}/read`
      );

      if (response.success) {
        // Update local state
        const notification = state.notifications.find(
          (n) => n.id === notificationId
        );
        if (notification) {
          notification.isRead = true;
        }

        // Re-render
        renderNotifications();

        // Update badge count in header
        if (window.NotificationDropdown) {
          window.NotificationDropdown.loadUnreadCount();
        }
      }
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  }

  /**
   * Mark all notifications as read
   * Chức năng: Đánh dấu tất cả thông báo là đã đọc
   * Cách hoạt động: Gọi API PUT /notifications/read-all, cập nhật tất cả thông báo trong state
   */
  async function markAllAsRead() {
    try {
      const response = await apiClient.put('/notifications/read-all');

      if (response.success) {
        // Update local state
        state.notifications.forEach((n) => (n.isRead = true));

        // Re-render
        renderNotifications();

        // Update badge count in header
        if (window.NotificationDropdown) {
          window.NotificationDropdown.loadUnreadCount();
        }

        if (window.showToast) {
          showToast('Đã đánh dấu tất cả đã đọc', 'success');
        }
      }
    } catch (error) {
      console.error('Error marking all as read:', error);
      if (window.showToast) {
        showToast('Không thể đánh dấu đã đọc', 'error');
      }
    }
  }

  // ============================================================================
  // Rendering Functions
  // ============================================================================

  /**
   * Render notifications list
   * Chức năng: Hiển thị danh sách thông báo
   * Cách hoạt động: Tạo HTML cho mỗi thông báo và thêm vào DOM
   */
  function renderNotifications() {
    // Clear existing notifications
    const existingItems = elements.notificationsList.querySelectorAll(
      '.notification-item'
    );
    existingItems.forEach((item) => item.remove());

    if (state.notifications.length === 0) {
      showEmptyState();
      return;
    }

    hideEmptyState();

    // Render each notification
    state.notifications.forEach((notification) => {
      const itemElement = createNotificationElement(notification);
      elements.notificationsList.appendChild(itemElement);
    });
  }

  /**
   * Create notification element
   * Chức năng: Tạo HTML element cho một thông báo
   * Cách hoạt động: Tạo div với icon, title, content, time và trạng thái đọc/chưa đọc
   */
  function createNotificationElement(notification) {
    const div = document.createElement('div');
    div.className = `notification-item bg-white dark:bg-[#1a2634] rounded-xl border border-slate-200 dark:border-slate-800 p-4 hover:shadow-md transition-all cursor-pointer ${
      !notification.isRead
        ? 'border-l-4 border-l-primary bg-blue-50/30 dark:bg-blue-900/10'
        : ''
    }`;
    div.setAttribute('data-notification-id', notification.id);
    div.setAttribute('data-notification-read', notification.isRead);

    const iconInfo =
      NOTIFICATION_ICONS[notification.notificationType] ||
      NOTIFICATION_ICONS.SYSTEM;

    div.innerHTML = `
      <div class="flex gap-4">
        <div class="flex-shrink-0">
          <div class="w-12 h-12 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
            <span class="material-symbols-outlined text-2xl ${iconInfo.color}">
              ${iconInfo.icon}
            </span>
          </div>
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-start justify-between gap-3 mb-1">
            <h3 class="text-base font-semibold text-slate-900 dark:text-white">
              ${escapeHtml(notification.title)}
            </h3>
            ${
              !notification.isRead
                ? '<span class="flex-shrink-0 w-2 h-2 bg-primary rounded-full mt-2"></span>'
                : ''
            }
          </div>
          <p class="text-sm text-slate-600 dark:text-slate-400 mb-2">
            ${escapeHtml(notification.content)}
          </p>
          <div class="flex items-center gap-2 text-xs text-slate-400">
            <span class="material-symbols-outlined text-[16px]">schedule</span>
            <span>${notification.timeAgo}</span>
          </div>
        </div>
      </div>
    `;

    // Add click handler
    div.addEventListener('click', () => handleNotificationClick(notification));

    return div;
  }

  /**
   * Handle notification click
   * Chức năng: Xử lý khi người dùng click vào thông báo
   * Cách hoạt động: Đánh dấu đã đọc nếu chưa đọc, điều hướng nếu có reference
   */
  function handleNotificationClick(notification) {
    // Mark as read if unread
    if (!notification.isRead) {
      markAsRead(notification.id);
    }

    // Navigate to reference if available
    if (notification.referenceId && notification.referenceType) {
      navigateToReference(notification);
    }
  }

  /**
   * Navigate to notification reference
   * Chức năng: Điều hướng đến trang liên quan của thông báo
   * Cách hoạt động: Dựa vào referenceType để xác định URL cần chuyển đến
   */
  function navigateToReference(notification) {
    const routes = {
      TICKET: `/patient/tickets?id=${notification.referenceId}`,
      CALL: `/call/history?id=${notification.referenceId}`,
      APPOINTMENT: `/patient/appointments?id=${notification.referenceId}`,
      PRESCRIPTION: `/patient/prescriptions?id=${notification.referenceId}`,
      TREATMENT: `/patient/treatments?id=${notification.referenceId}`,
    };

    const route = routes[notification.referenceType];
    if (route) {
      window.location.href = route;
    }
  }

  /**
   * Update pagination controls
   * Chức năng: Cập nhật trạng thái các nút phân trang
   * Cách hoạt động: Enable/disable nút prev/next dựa vào trang hiện tại
   */
  function updatePagination() {
    if (state.totalPages <= 1) {
      elements.paginationContainer.classList.add('hidden');
      return;
    }

    elements.paginationContainer.classList.remove('hidden');

    // Update page info
    const start = state.currentPage * CONFIG.pageSize + 1;
    const end = Math.min(
      (state.currentPage + 1) * CONFIG.pageSize,
      state.totalItems
    );
    elements.pageInfo.textContent = `${start}-${end} / ${state.totalItems}`;

    // Update buttons
    elements.prevPageBtn.disabled = state.currentPage === 0;
    elements.nextPageBtn.disabled =
      state.currentPage >= state.totalPages - 1;
  }

  // ============================================================================
  // UI State Functions
  // ============================================================================

  function showLoadingState() {
    elements.loadingState.classList.remove('hidden');
    elements.emptyState.classList.add('hidden');
  }

  function hideLoadingState() {
    elements.loadingState.classList.add('hidden');
  }

  function showEmptyState() {
    elements.emptyState.classList.remove('hidden');
  }

  function hideEmptyState() {
    elements.emptyState.classList.add('hidden');
  }

  function showError(message) {
    if (window.showToast) {
      showToast(message, 'error');
    }
  }

  // ============================================================================
  // Event Handlers
  // ============================================================================

  /**
   * Initialize event listeners
   * Chức năng: Khởi tạo các event listener cho các nút và filter
   * Cách hoạt động: Gắn click handler cho các nút filter, mark all read, pagination
   */
  function initEventListeners() {
    // Filter buttons
    elements.filterAll.addEventListener('click', () => {
      setFilter('all');
    });

    elements.filterUnread.addEventListener('click', () => {
      setFilter('unread');
    });

    // Mark all read button
    elements.markAllReadBtn.addEventListener('click', () => {
      markAllAsRead();
    });

    // Pagination buttons
    elements.prevPageBtn.addEventListener('click', () => {
      if (state.currentPage > 0) {
        if (state.filter === 'all') {
          loadNotifications(state.currentPage - 1);
        }
      }
    });

    elements.nextPageBtn.addEventListener('click', () => {
      if (state.currentPage < state.totalPages - 1) {
        if (state.filter === 'all') {
          loadNotifications(state.currentPage + 1);
        }
      }
    });
  }

  /**
   * Set filter
   * Chức năng: Thay đổi bộ lọc thông báo
   * Cách hoạt động: Cập nhật UI filter buttons và tải lại danh sách theo filter
   */
  function setFilter(filter) {
    state.filter = filter;

    // Update button styles
    if (filter === 'all') {
      elements.filterAll.classList.add('active', 'bg-primary', 'text-white');
      elements.filterAll.classList.remove(
        'bg-slate-100',
        'dark:bg-slate-800',
        'text-slate-700',
        'dark:text-slate-300'
      );
      elements.filterUnread.classList.remove(
        'active',
        'bg-primary',
        'text-white'
      );
      elements.filterUnread.classList.add(
        'bg-slate-100',
        'dark:bg-slate-800',
        'text-slate-700',
        'dark:text-slate-300'
      );
      loadNotifications(0);
    } else {
      elements.filterUnread.classList.add(
        'active',
        'bg-primary',
        'text-white'
      );
      elements.filterUnread.classList.remove(
        'bg-slate-100',
        'dark:bg-slate-800',
        'text-slate-700',
        'dark:text-slate-300'
      );
      elements.filterAll.classList.remove('active', 'bg-primary', 'text-white');
      elements.filterAll.classList.add(
        'bg-slate-100',
        'dark:bg-slate-800',
        'text-slate-700',
        'dark:text-slate-300'
      );
      loadUnreadNotifications();
    }
  }

  // ============================================================================
  // Utility Functions
  // ============================================================================

  /**
   * Escape HTML to prevent XSS
   * Chức năng: Escape các ký tự HTML để tránh XSS
   * Cách hoạt động: Sử dụng textContent để escape HTML
   */
  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  // ============================================================================
  // Initialization
  // ============================================================================

  /**
   * Initialize the module
   * Chức năng: Khởi tạo module
   * Cách hoạt động: Đợi DOM ready, khởi tạo event listeners và tải dữ liệu ban đầu
   */
  function init() {
    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', init);
      return;
    }

    // Initialize event listeners
    initEventListeners();

    // Load initial data
    loadNotifications(0);

    console.log('Patient Notifications module initialized');
  }

  // Start initialization
  init();
})();
