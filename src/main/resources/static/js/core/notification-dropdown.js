/**
 * Notification Dropdown Module
 * 
 * Manages the notifications dropdown in the unified header:
 * - Load notifications from API
 * - Display notifications list with icons and formatting
 * - Mark notifications as read
 * - Real-time updates via WebSocket
 * - Badge count management
 * 
 * @version 1.0.0
 * @date February 12, 2026
 */

(function () {
  'use strict';

  // ============================================================================
  // Configuration
  // ============================================================================

  const CONFIG = {
    maxNotifications: 10, // Max notifications to show in dropdown
    autoCloseDelay: 300, // Delay before closing dropdown (ms)
    pollInterval: 60000, // Fallback polling interval if WebSocket fails (ms)
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
  // DOM Elements
  // ============================================================================

  let elements = {};

  function initializeElements() {
    elements = {
      dropdown: document.querySelector('[data-notification-dropdown]'),
      trigger: document.querySelector('[data-notification-trigger]'),
      menu: document.querySelector('[data-notification-menu]'),
      list: document.querySelector('[data-notifications-list]'),
      badge: document.querySelector('[data-notification-badge]'),
      count: document.querySelector('[data-notification-count]'),
      markAllRead: document.querySelector('[data-mark-all-read]'),
      loadingState: document.querySelector('[data-loading-state]'),
      emptyState: document.querySelector('[data-empty-state]'),
    };

    return Object.values(elements).every((el) => el !== null);
  }

  // ============================================================================
  // State Management
  // ============================================================================

  const state = {
    isOpen: false,
    notifications: [],
    unreadCount: 0,
    isLoading: false,
    hasLoaded: false,
  };

  // ============================================================================
  // API Functions
  // ============================================================================

  /**
   * Load recent notifications from API
   */
  async function loadNotifications() {
    try {
      state.isLoading = true;
      showLoadingState();

      const notifications = await apiClient.get(
        `/notifications/recent?limit=${CONFIG.maxNotifications}`
      );

      state.notifications = notifications;
      state.hasLoaded = true;
      renderNotifications();
    } catch (error) {
      console.error('Error loading notifications:', error);
      showError('Không thể tải thông báo');
    } finally {
      state.isLoading = false;
      hideLoadingState();
    }
  }

  /**
   * Load unread count
   */
  async function loadUnreadCount() {
    try {
      const response = await apiClient.get('/notifications/unread-count');
      updateBadgeCount(response.count);
    } catch (error) {
      console.error('Error loading unread count:', error);
    }
  }

  /**
   * Mark notification as read
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
          state.unreadCount = Math.max(0, state.unreadCount - 1);
          updateBadgeCount(state.unreadCount);
          renderNotifications();
        }
      }
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  }

  /**
   * Mark all notifications as read
   */
  async function markAllAsRead() {
    try {
      const response = await apiClient.put('/notifications/read-all');

      if (response.success) {
        // Update local state
        state.notifications.forEach((n) => (n.isRead = true));
        state.unreadCount = 0;
        updateBadgeCount(0);
        renderNotifications();

        // Show success message
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
   */
  function renderNotifications() {
    if (!elements.list) return;

    // Clear existing notifications (except loading/empty states)
    const items = elements.list.querySelectorAll('.notification-item');
    items.forEach((item) => item.remove());

    if (state.notifications.length === 0) {
      showEmptyState();
      return;
    }

    hideEmptyState();

    // Render each notification
    state.notifications.forEach((notification) => {
      const itemElement = createNotificationElement(notification);
      elements.list.appendChild(itemElement);
    });
  }

  /**
   * Create notification element
   */
  function createNotificationElement(notification) {
    const div = document.createElement('div');
    div.className = `notification-item p-4 hover:bg-slate-50 dark:hover:bg-slate-700 cursor-pointer transition-colors ${
      !notification.isRead ? 'bg-blue-50 dark:bg-blue-900/20' : ''
    }`;
    div.setAttribute('data-notification-id', notification.id);
    div.setAttribute('data-notification-read', notification.isRead);

    const iconInfo =
      NOTIFICATION_ICONS[notification.notificationType] ||
      NOTIFICATION_ICONS.SYSTEM;

    div.innerHTML = `
      <div class="flex gap-3">
        <div class="flex-shrink-0">
          <span class="material-symbols-outlined text-2xl ${iconInfo.color}">
            ${iconInfo.icon}
          </span>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-slate-900 dark:text-white">${escapeHtml(
            notification.title
          )}</p>
          <p class="text-sm text-slate-600 dark:text-slate-400 line-clamp-2">${escapeHtml(
            notification.content
          )}</p>
          <p class="text-xs text-slate-400 mt-1">${notification.timeAgo}</p>
        </div>
        ${
          !notification.isRead
            ? '<div class="w-2 h-2 bg-primary rounded-full mt-2 flex-shrink-0"></div>'
            : ''
        }
      </div>
    `;

    // Add click handler
    div.addEventListener('click', () =>
      handleNotificationClick(notification)
    );

    return div;
  }

  /**
   * Handle notification click
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

    // Close dropdown
    closeDropdown();
  }

  /**
   * Navigate to notification reference
   */
  function navigateToReference(notification) {
    const routes = {
      TICKET: `/crm/tickets?id=${notification.referenceId}`,
      CALL: `/call/history?id=${notification.referenceId}`,
      // Add more route mappings as needed
    };

    const route = routes[notification.referenceType];
    if (route) {
      window.location.href = route;
    }
  }

  /**
   * Update badge count
   */
  function updateBadgeCount(count) {
    state.unreadCount = count;

    if (!elements.badge || !elements.count) return;

    if (count > 0) {
      elements.count.textContent = count > 99 ? '99+' : count;
      elements.badge.classList.remove('hidden');
    } else {
      elements.badge.classList.add('hidden');
    }
  }

  /**
   * Show loading state
   */
  function showLoadingState() {
    if (elements.loadingState) {
      elements.loadingState.classList.remove('hidden');
    }
    if (elements.emptyState) {
      elements.emptyState.classList.add('hidden');
    }
  }

  /**
   * Hide loading state
   */
  function hideLoadingState() {
    if (elements.loadingState) {
      elements.loadingState.classList.add('hidden');
    }
  }

  /**
   * Show empty state
   */
  function showEmptyState() {
    if (elements.emptyState) {
      elements.emptyState.classList.remove('hidden');
    }
  }

  /**
   * Hide empty state
   */
  function hideEmptyState() {
    if (elements.emptyState) {
      elements.emptyState.classList.add('hidden');
    }
  }

  /**
   * Show error message
   */
  function showError(message) {
    if (window.showToast) {
      showToast(message, 'error');
    }
  }

  // ============================================================================
  // Dropdown Toggle
  // ============================================================================

  /**
   * Open dropdown
   */
  function openDropdown() {
    if (!elements.menu) return;

    state.isOpen = true;
    elements.menu.classList.remove('hidden');

    // Load notifications if not loaded yet
    if (!state.hasLoaded) {
      loadNotifications();
    }

    // Add click-outside listener
    setTimeout(() => {
      document.addEventListener('click', handleClickOutside);
    }, 0);
  }

  /**
   * Close dropdown
   */
  function closeDropdown() {
    if (!elements.menu) return;

    state.isOpen = false;
    elements.menu.classList.add('hidden');

    // Remove click-outside listener
    document.removeEventListener('click', handleClickOutside);
  }

  /**
   * Toggle dropdown
   */
  function toggleDropdown() {
    if (state.isOpen) {
      closeDropdown();
    } else {
      openDropdown();
    }
  }

  /**
   * Handle click outside dropdown
   */
  function handleClickOutside(event) {
    if (!elements.dropdown) return;

    if (!elements.dropdown.contains(event.target)) {
      closeDropdown();
    }
  }

  // ============================================================================
  // WebSocket Integration
  // ============================================================================

  let stompClient = null;

  /**
   * Initialize WebSocket connection for real-time notifications
   */
  function initWebSocket() {
    // Check if SockJS and Stomp are available
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
      console.warn(
        'SockJS or Stomp not available, falling back to polling'
      );
      startPolling();
      return;
    }

    try {
      const socket = new SockJS('/ws');
      stompClient = Stomp.over(socket);

      // Disable debug logging in production
      stompClient.debug = null;

      stompClient.connect(
        {},
        () => {
          console.log('WebSocket connected for notifications');

          // Subscribe to user-specific notifications
          stompClient.subscribe('/user/queue/notifications', (message) => {
            handleWebSocketNotification(JSON.parse(message.body));
          });
        },
        (error) => {
          console.error('WebSocket connection error:', error);
          startPolling();
        }
      );
    } catch (error) {
      console.error('Error initializing WebSocket:', error);
      startPolling();
    }
  }

  /**
   * Handle WebSocket notification
   */
  function handleWebSocketNotification(notification) {
    // Add to local state
    state.notifications.unshift(notification);

    // Limit to max notifications
    if (state.notifications.length > CONFIG.maxNotifications) {
      state.notifications.pop();
    }

    // Update unread count
    if (!notification.isRead) {
      state.unreadCount++;
      updateBadgeCount(state.unreadCount);
    }

    // Re-render if dropdown is open
    if (state.isOpen) {
      renderNotifications();
    }

    // Show toast notification
    if (window.showToast) {
      showToast(notification.title, 'info', 3000);
    }
  }

  /**
   * Start polling for notifications (fallback)
   */
  function startPolling() {
    setInterval(() => {
      if (!state.isOpen) {
        loadUnreadCount();
      }
    }, CONFIG.pollInterval);
  }

  // ============================================================================
  // Event Listeners
  // ============================================================================

  /**
   * Initialize event listeners
   */
  function initEventListeners() {
    // Dropdown trigger
    if (elements.trigger) {
      elements.trigger.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleDropdown();
      });
    }

    // Mark all as read button
    if (elements.markAllRead) {
      elements.markAllRead.addEventListener('click', (e) => {
        e.stopPropagation();
        markAllAsRead();
      });
    }
  }

  // ============================================================================
  // Utility Functions
  // ============================================================================

  /**
   * Escape HTML to prevent XSS
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
   * Initialize notification dropdown
   */
  function init() {
    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', init);
      return;
    }

    // Initialize elements
    if (!initializeElements()) {
      console.warn('Notification dropdown elements not found');
      return;
    }

    // Initialize event listeners
    initEventListeners();

    // Load initial unread count
    loadUnreadCount();

    // Initialize WebSocket
    initWebSocket();

    console.log('Notification dropdown initialized');
  }

  // Start initialization
  init();

  // Expose API for external use
  window.NotificationDropdown = {
    loadNotifications,
    loadUnreadCount,
    openDropdown,
    closeDropdown,
  };
})();
