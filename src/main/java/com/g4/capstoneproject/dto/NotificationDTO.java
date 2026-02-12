package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * DTO for Notification entity
 * Used for API responses with additional computed fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    
    private Long id;
    private String notificationType;
    private String title;
    private String content;
    private Long referenceId;
    private String referenceType;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String timeAgo; // Computed field: "5 phút trước", "2 giờ trước", etc.
    
    /**
     * Factory method to create DTO from Entity
     */
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType().name())
                .title(notification.getTitle())
                .content(notification.getContent())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .timeAgo(calculateTimeAgo(notification.getCreatedAt()))
                .build();
    }
    
    /**
     * Calculate "time ago" string from LocalDateTime
     */
    private static String calculateTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);
        
        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        
        if (seconds < 60) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days == 1) {
            return "Hôm qua";
        } else if (days < 7) {
            return days + " ngày trước";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + (weeks == 1 ? " tuần trước" : " tuần trước");
        } else if (days < 365) {
            long months = days / 30;
            return months + " tháng trước";
        } else {
            long years = days / 365;
            return years + " năm trước";
        }
    }
}
