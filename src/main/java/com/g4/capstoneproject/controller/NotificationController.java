package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.NotificationDTO;
import com.g4.capstoneproject.entity.Notification;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API Controller cho Notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Lấy danh sách notifications của user hiện tại (paginated)
     * GET /api/notifications?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notificationsPage = notificationService.getNotificationsByUser(user, pageable);
            
            List<NotificationDTO> notifications = notificationsPage.getContent()
                    .stream()
                    .map(NotificationDTO::fromEntity)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("currentPage", notificationsPage.getNumber());
            response.put("totalItems", notificationsPage.getTotalElements());
            response.put("totalPages", notificationsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting notifications for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy danh sách notifications mới nhất (không phân trang, dùng cho dropdown)
     * GET /api/notifications/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NotificationDTO>> getRecentNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<Notification> notificationsPage = notificationService.getNotificationsByUser(user, pageable);
            
            List<NotificationDTO> notifications = notificationsPage.getContent()
                    .stream()
                    .map(NotificationDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting recent notifications for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Đếm số notifications chưa đọc
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal User user) {
        try {
            long count = notificationService.getUnreadCount(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("hasUnread", count > 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting unread count for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Đánh dấu một notification đã đọc
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        try {
            boolean success = notificationService.markAsRead(id, user);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Đã đánh dấu đã đọc");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông báo hoặc bạn không có quyền");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Đánh dấu tất cả notifications đã đọc
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@AuthenticationPrincipal User user) {
        try {
            notificationService.markAllAsRead(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã đánh dấu tất cả thông báo đã đọc");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xóa một notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        try {
            boolean success = notificationService.deleteNotification(id, user);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Đã xóa thông báo");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông báo hoặc bạn không có quyền");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error deleting notification: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy notifications chưa đọc
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@AuthenticationPrincipal User user) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(user.getId());
            
            List<NotificationDTO> dtos = notifications.stream()
                    .map(NotificationDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting unread notifications for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
