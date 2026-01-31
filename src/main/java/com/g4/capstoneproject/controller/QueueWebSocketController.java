package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.QueueUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebSocket Controller for Queue Management
 * Handles real-time queue updates via WebSocket/STOMP protocol
 */
@Slf4j
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast queue update to all connected clients
     * Endpoint: POST /api/queue/broadcast
     */
    @PostMapping("/broadcast")
    public void broadcastQueueUpdate(@RequestBody QueueUpdateMessage message) {
        log.info("Broadcasting queue update: action={}, ticketId={}, queueType={}",
                message.getAction(), message.getTicketId(), message.getQueueType());

        // Send to all subscribers of /topic/queue
        messagingTemplate.convertAndSend("/topic/queue", message);
    }

    /**
     * Handle incoming queue update messages from clients
     * Client sends to: /app/queue/update
     * Server broadcasts to: /topic/queue
     */
    @MessageMapping("/queue/update")
    @SendTo("/topic/queue")
    public QueueUpdateMessage handleQueueUpdate(QueueUpdateMessage message) {
        log.info("Received queue update from client: action={}, ticketId={}",
                message.getAction(), message.getTicketId());

        // Add server timestamp
        message.setTimestamp(System.currentTimeMillis());

        // Broadcast to all connected clients
        return message;
    }

    /**
     * Notify specific user (for private messages)
     * Sends to: /user/{username}/queue/private
     */
    public void notifyUser(String username, QueueUpdateMessage message) {
        log.info("Sending private message to user: {}, ticketId={}", username, message.getTicketId());
        messagingTemplate.convertAndSendToUser(username, "/queue/private", message);
    }

    /**
     * Broadcast ticket call notification
     * Used when receptionist calls a patient
     */
    public void broadcastTicketCall(String ticketId, String patientName) {
        QueueUpdateMessage message = QueueUpdateMessage.called(ticketId, patientName);
        log.info("Broadcasting ticket call: ticketId={}, patient={}", ticketId, patientName);
        messagingTemplate.convertAndSend("/topic/queue", message);
    }

    /**
     * Broadcast ticket addition
     */
    public void broadcastTicketAdded(String ticketId, QueueUpdateMessage.QueueType queueType,
            String patientName, String priority, Integer position) {
        QueueUpdateMessage message = QueueUpdateMessage.added(ticketId, queueType, patientName, priority, position);
        log.info("Broadcasting ticket added: ticketId={}, queueType={}, position={}",
                ticketId, queueType, position);
        messagingTemplate.convertAndSend("/topic/queue", message);
    }

    /**
     * Broadcast ticket removal
     */
    public void broadcastTicketRemoved(String ticketId, QueueUpdateMessage.QueueType queueType) {
        QueueUpdateMessage message = QueueUpdateMessage.removed(ticketId, queueType);
        log.info("Broadcasting ticket removed: ticketId={}, queueType={}", ticketId, queueType);
        messagingTemplate.convertAndSend("/topic/queue", message);
    }

    /**
     * Broadcast ticket update
     */
    public void broadcastTicketUpdated(String ticketId, QueueUpdateMessage.QueueType queueType,
            String status, Integer retryCount, Integer position) {
        QueueUpdateMessage message = QueueUpdateMessage.updated(ticketId, queueType, status, retryCount, position);
        log.info("Broadcasting ticket updated: ticketId={}, status={}, position={}",
                ticketId, status, position);
        messagingTemplate.convertAndSend("/topic/queue", message);
    }

    /**
     * Broadcast ticket completion
     */
    public void broadcastTicketCompleted(String ticketId) {
        QueueUpdateMessage message = QueueUpdateMessage.completed(ticketId);
        log.info("Broadcasting ticket completed: ticketId={}", ticketId);
        messagingTemplate.convertAndSend("/topic/queue", message);
    }
}
