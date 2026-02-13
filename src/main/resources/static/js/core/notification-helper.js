/**
 * Notification Helper Module
 * 
 * Chức năng: Cung cấp các hàm helper cho thông báo
 * Cách hoạt động:
 * - Xác định URL trang thông báo dựa vào role của user
 * - Lấy thông tin user từ session/localStorage
 * 
 * @version 1.0.0
 * @date February 13, 2026
 */

(function () {
  'use strict';

  /**
   * Get notifications page URL based on user role
   * Chức năng: Lấy URL trang thông báo dựa vào role
   * Cách hoạt động: Kiểm tra role trong sessionStorage hoặc từ API, trả về URL phù hợp
   */
  async function getNotificationsPageUrl() {
    try {
      // Try to get user info from session storage first
      let userRole = sessionStorage.getItem('userRole');

      // If not in session, fetch from API
      if (!userRole) {
        const response = await apiClient.get('/profile/info');
        if (response && response.role) {
          userRole = response.role;
          sessionStorage.setItem('userRole', userRole);
        }
      }

      // Return URL based on role
      switch (userRole) {
        case 'PATIENT':
          return '/patient/notifications';
        case 'DOCTOR':
        case 'RECEPTIONIST':
        case 'ADMIN':
          return '/crm/notifications';
        default:
          return '/patient/notifications'; // Default to patient
      }
    } catch (error) {
      console.error('Error getting notifications page URL:', error);
      return '/patient/notifications'; // Default fallback
    }
  }

  // Expose to global scope
  window.NotificationHelper = {
    getNotificationsPageUrl,
  };
})();
