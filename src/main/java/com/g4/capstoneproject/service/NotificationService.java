package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.Notification;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý Notifications
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Lấy danh sách notifications của user (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByUser(User user, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    /**
     * Lấy danh sách notifications của user (không phân trang)
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Lấy danh sách notifications chưa đọc
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Đếm số notifications chưa đọc
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    /**
     * Đếm số notifications chưa đọc theo userId
     */
    @Transactional(readOnly = true)
    public long getUnreadCountByUserId(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Lấy notification theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    /**
     * Đánh dấu notification đã đọc
     * Chức năng: Đánh dấu một thông báo cụ thể là đã đọc
     * Cách hoạt động: Tìm notification theo ID, kiểm tra quyền sở hữu, sau đó cập nhật trạng thái isRead và thời gian đọc
     */
    public boolean markAsRead(Long notificationId, User user) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            // Check ownership
            if (notification.getUser().getId().equals(user.getId())) {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
                return true;
            }
        }
        return false;
    }

    /**
     * Đánh dấu tất cả notifications đã đọc
     * Chức năng: Đánh dấu tất cả thông báo chưa đọc của user là đã đọc
     * Cách hoạt động: Sử dụng query UPDATE để cập nhật tất cả notifications chưa đọc của user
     */
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user.getId());
    }

    /**
     * Đánh dấu tất cả notifications đã đọc theo userId
     */
    public void markAllAsReadByUserId(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * Xóa notification
     */
    public boolean deleteNotification(Long notificationId, User user) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            // Check ownership
            if (notification.getUser().getId().equals(user.getId())) {
                notificationRepository.delete(notification);
                return true;
            }
        }
        return false;
    }

    /**
     * Tạo notification mới
     */
    public Notification createNotification(
            User user,
            Notification.NotificationType notificationType,
            String title,
            String content,
            Long referenceId,
            String referenceType) {
        
        Notification notification = Notification.builder()
                .user(user)
                .notificationType(notificationType)
                .title(title)
                .content(content)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .build();
        
        return notificationRepository.save(notification);
    }

    /**
     * Tạo notification với builder pattern
     */
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * Xóa notifications cũ hơn số ngày chỉ định
     */
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldNotifications(beforeDate);
    }

    /**
     * Lấy notifications theo loại
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByType(Long userId, Notification.NotificationType type) {
        return notificationRepository.findByUserIdAndNotificationTypeOrderByCreatedAtDesc(userId, type);
    }
}
