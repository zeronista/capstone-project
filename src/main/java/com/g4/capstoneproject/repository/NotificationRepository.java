package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.Notification;
import com.g4.capstoneproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Tìm thông báo theo user
     */
    List<Notification> findByUser(User user);
    
    /**
     * Tìm thông báo theo user ID
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Tìm thông báo chưa đọc của user
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    /**
     * Tìm thông báo theo loại
     */
    List<Notification> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(Long userId, Notification.NotificationType notificationType);
    
    /**
     * Phân trang thông báo của user
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Đánh dấu tất cả đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
    
    /**
     * Đánh dấu đã đọc theo ID
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
    
    /**
     * Đếm thông báo chưa đọc
     */
    long countByUserIdAndIsReadFalse(Long userId);
    
    /**
     * Xóa thông báo cũ
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :beforeDate")
    void deleteOldNotifications(@Param("beforeDate") java.time.LocalDateTime beforeDate);
}
