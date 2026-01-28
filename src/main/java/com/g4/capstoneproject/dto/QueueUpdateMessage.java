package com.g4.capstoneproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Queue Update Messages via WebSocket
 * Used to broadcast real-time queue changes to connected clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueUpdateMessage {

    /**
     * Action types for queue updates
     */
    public enum Action {
        ADD, // New ticket added to queue
        REMOVE, // Ticket removed from queue
        UPDATE, // Ticket information updated
        CALL, // Ticket called by receptionist
        COMPLETE // Ticket completed
    }

    /**
     * Queue types
     */
    public enum QueueType {
        QUEUE, // Main waiting queue
        RETRY // Retry queue (after failed attempts)
    }

    private String ticketId;
    private Action action;
    private QueueType queueType;
    private Long timestamp;

    // Patient information
    private String patientName;
    private String patientPhone;

    // Ticket details
    private String priority; // HIGH, MEDIUM, LOW
    private String status; // OPEN, IN_PROGRESS, CLOSED
    private Integer retryCount;
    private Integer queuePosition;

    // Additional metadata
    private String message; // Optional message for the update

    /**
     * Factory method for ADD action
     */
    public static QueueUpdateMessage added(String ticketId, QueueType queueType, String patientName,
            String priority, Integer position) {
        return QueueUpdateMessage.builder()
                .ticketId(ticketId)
                .action(Action.ADD)
                .queueType(queueType)
                .patientName(patientName)
                .priority(priority)
                .queuePosition(position)
                .timestamp(System.currentTimeMillis())
                .message("Ticket mới được thêm vào hàng đợi")
                .build();
    }

    /**
     * Factory method for REMOVE action
     */
    public static QueueUpdateMessage removed(String ticketId, QueueType queueType) {
        return QueueUpdateMessage.builder()
                .ticketId(ticketId)
                .action(Action.REMOVE)
                .queueType(queueType)
                .timestamp(System.currentTimeMillis())
                .message("Ticket đã được xóa khỏi hàng đợi")
                .build();
    }

    /**
     * Factory method for CALL action
     */
    public static QueueUpdateMessage called(String ticketId, String patientName) {
        return QueueUpdateMessage.builder()
                .ticketId(ticketId)
                .action(Action.CALL)
                .queueType(QueueType.QUEUE)
                .patientName(patientName)
                .timestamp(System.currentTimeMillis())
                .message("Đang gọi bệnh nhân")
                .build();
    }

    /**
     * Factory method for UPDATE action
     */
    public static QueueUpdateMessage updated(String ticketId, QueueType queueType, String status,
            Integer retryCount, Integer position) {
        return QueueUpdateMessage.builder()
                .ticketId(ticketId)
                .action(Action.UPDATE)
                .queueType(queueType)
                .status(status)
                .retryCount(retryCount)
                .queuePosition(position)
                .timestamp(System.currentTimeMillis())
                .message("Thông tin ticket đã được cập nhật")
                .build();
    }

    /**
     * Factory method for COMPLETE action
     */
    public static QueueUpdateMessage completed(String ticketId) {
        return QueueUpdateMessage.builder()
                .ticketId(ticketId)
                .action(Action.COMPLETE)
                .queueType(QueueType.QUEUE)
                .status("CLOSED")
                .timestamp(System.currentTimeMillis())
                .message("Ticket đã hoàn thành")
                .build();
    }
}
