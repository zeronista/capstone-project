package com.g4.capstoneproject.service;

import com.g4.capstoneproject.controller.QueueWebSocketController;
import com.g4.capstoneproject.dto.QueueUpdateMessage;
import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TicketMessage;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.TicketRepository;
import com.g4.capstoneproject.repository.TicketMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Ticket chuyên khoa
 * Refactored to use database in Phase 2
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private QueueWebSocketController webSocketController;

    /**
     * Lấy tất cả tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Lấy ticket theo ID
     */
    @Transactional(readOnly = true)
    public Ticket getTicketById(String id) {
        return ticketRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /**
     * Lấy tickets theo người tạo
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByCreatedByUserId(Long userId) {
        return ticketRepository.findByCreatedById(userId);
    }

    /**
     * Lấy tickets theo người được assign
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByAssignedUserId(Long userId) {
        return ticketRepository.findByAssignedToId(userId);
    }

    /**
     * Lọc theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByStatus(Ticket.Status status) {
        return ticketRepository.findByStatus(status);
    }

    /**
     * Lọc theo priority
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByPriority(Ticket.Priority priority) {
        return ticketRepository.findByPriority(priority);
    }

    /**
     * Tạo ticket mới
     */
    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Cập nhật ticket
     */
    public Ticket updateTicket(String id, Ticket updatedTicket) {
        return ticketRepository.findById(Long.parseLong(id))
                .map(existing -> {
                    existing.setTitle(updatedTicket.getTitle());
                    existing.setDescription(updatedTicket.getDescription());
                    existing.setStatus(updatedTicket.getStatus());
                    existing.setPriority(updatedTicket.getPriority());
                    return ticketRepository.save(existing);
                })
                .orElse(null);
    }

    /**
     * Cập nhật trạng thái ticket
     */
    public Ticket updateTicketStatus(Long ticketId, Ticket.Status newStatus, User resolvedBy) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> {
                    ticket.setStatus(newStatus);

                    // Update resolved info if status is RESOLVED or CLOSED
                    if (newStatus == Ticket.Status.RESOLVED || newStatus == Ticket.Status.CLOSED) {
                        ticket.setResolvedBy(resolvedBy);
                        if (ticket.getResolvedAt() == null) {
                            ticket.setResolvedAt(LocalDateTime.now());
                        }
                    }

                    return ticketRepository.save(ticket);
                })
                .orElse(null);
    }

    /**
     * Thống kê
     */
    @Transactional(readOnly = true)
    public long getOpenCount() {
        return ticketRepository.findByStatus(Ticket.Status.OPEN).size();
    }

    @Transactional(readOnly = true)
    public long getInProgressCount() {
        return ticketRepository.findByStatus(Ticket.Status.IN_PROGRESS).size();
    }

    @Transactional(readOnly = true)
    public long getHighPriorityCount() {
        return ticketRepository.findByPriority(Ticket.Priority.HIGH).size();
    }

    /**
     * Thêm message vào ticket
     */
    public TicketMessage addMessage(Long ticketId, TicketMessage message) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> {
                    message.setTicket(ticket);
                    return ticketMessageRepository.save(message);
                })
                .orElse(null);
    }

    /**
     * Lấy messages của ticket
     */
    @Transactional(readOnly = true)
    public List<TicketMessage> getTicketMessages(Long ticketId) {
        return ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    /**
     * Set WebSocket controller (to avoid circular dependency)
     */
    public void setWebSocketController(QueueWebSocketController controller) {
        this.webSocketController = controller;
    }

    // ==================== QUEUE MANAGEMENT METHODS ====================

    /**
     * Get all tickets in main queue (OPEN status, not retrying)
     * Ordered by priority (HIGH > MEDIUM > LOW) and creation time
     */
    @Transactional(readOnly = true)
    public List<Ticket> getQueuedTickets() {
        return ticketRepository.findByStatus(Ticket.Status.OPEN)
                .stream()
                .filter(t -> t.getRetryCount() == null || t.getRetryCount() == 0)
                .sorted(Comparator
                        .comparing(Ticket::getPriority, Comparator.reverseOrder())
                        .thenComparing(Ticket::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Get all tickets in retry queue (has retry_count > 0)
     * Ordered by next retry time
     */
    @Transactional(readOnly = true)
    public List<Ticket> getRetryTickets() {
        return ticketRepository.findAll()
                .stream()
                .filter(t -> t.getRetryCount() != null && t.getRetryCount() > 0)
                .filter(t -> t.getStatus() == Ticket.Status.OPEN)
                .sorted(Comparator.comparing(Ticket::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get queue position for a ticket (1-based index)
     */
    @Transactional(readOnly = true)
    public Integer getQueuePosition(String ticketId) {
        List<Ticket> queue = getQueuedTickets();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().toString().equals(ticketId)) {
                return i + 1; // 1-based position
            }
        }
        return null;
    }

    /**
     * Create ticket with WebSocket notification
     */
    public Ticket createTicketWithNotification(Ticket ticket) {
        Ticket created = ticketRepository.save(ticket);

        // Broadcast to WebSocket subscribers
        if (webSocketController != null) {
            Integer position = getQueuePosition(created.getId().toString());
            String patientName = created.getCreatedBy() != null ? created.getCreatedBy().getFullName() : "Unknown";

            webSocketController.broadcastTicketAdded(
                    created.getId().toString(),
                    QueueUpdateMessage.QueueType.QUEUE,
                    patientName,
                    created.getPriority().toString(),
                    position);
        }

        return created;
    }

    /**
     * Update ticket status with WebSocket notification
     */
    public Ticket updateTicketStatus(String ticketId, Ticket.Status newStatus) {
        return ticketRepository.findById(Long.parseLong(ticketId))
                .map(ticket -> {
                    ticket.setStatus(newStatus);
                    Ticket updated = ticketRepository.save(ticket);

                    // Broadcast status change
                    if (webSocketController != null) {
                        if (newStatus == Ticket.Status.CLOSED) {
                            webSocketController.broadcastTicketCompleted(ticketId);
                        } else {
                            QueueUpdateMessage.QueueType queueType = (ticket.getRetryCount() != null
                                    && ticket.getRetryCount() > 0) ? QueueUpdateMessage.QueueType.RETRY
                                            : QueueUpdateMessage.QueueType.QUEUE;

                            Integer position = getQueuePosition(ticketId);
                            webSocketController.broadcastTicketUpdated(
                                    ticketId,
                                    queueType,
                                    newStatus.toString(),
                                    ticket.getRetryCount(),
                                    position);
                        }
                    }

                    return updated;
                })
                .orElse(null);
    }

    /**
     * Call ticket (receptionist calls patient)
     */
    public Ticket callTicket(String ticketId) {
        return ticketRepository.findById(Long.parseLong(ticketId))
                .map(ticket -> {
                    ticket.setStatus(Ticket.Status.IN_PROGRESS);
                    Ticket updated = ticketRepository.save(ticket);

                    // Broadcast call notification
                    if (webSocketController != null) {
                        String patientName = ticket.getCreatedBy() != null ? ticket.getCreatedBy().getFullName()
                                : "Unknown";
                        webSocketController.broadcastTicketCall(ticketId, patientName);
                    }

                    return updated;
                })
                .orElse(null);
    }

    /**
     * Move ticket to retry queue
     */
    public Ticket moveToRetry(String ticketId) {
        return ticketRepository.findById(Long.parseLong(ticketId))
                .map(ticket -> {
                    // Increment retry count
                    int retryCount = (ticket.getRetryCount() != null) ? ticket.getRetryCount() : 0;
                    ticket.setRetryCount(retryCount + 1);
                    ticket.setStatus(Ticket.Status.OPEN);

                    Ticket updated = ticketRepository.save(ticket);

                    // Broadcast move to retry
                    if (webSocketController != null) {
                        // Remove from main queue
                        webSocketController.broadcastTicketRemoved(
                                ticketId,
                                QueueUpdateMessage.QueueType.QUEUE);

                        // Add to retry queue
                        String patientName = ticket.getCreatedBy() != null ? ticket.getCreatedBy().getFullName()
                                : "Unknown";
                        webSocketController.broadcastTicketAdded(
                                ticketId,
                                QueueUpdateMessage.QueueType.RETRY,
                                patientName,
                                ticket.getPriority().toString(),
                                null);
                    }

                    return updated;
                })
                .orElse(null);
    }

    /**
     * Move ticket from retry back to main queue
     */
    public Ticket moveToMainQueue(String ticketId) {
        return ticketRepository.findById(Long.parseLong(ticketId))
                .map(ticket -> {
                    // Keep retry count for history
                    ticket.setStatus(Ticket.Status.OPEN);

                    Ticket updated = ticketRepository.save(ticket);

                    // Broadcast move to main queue
                    if (webSocketController != null) {
                        // Remove from retry queue
                        webSocketController.broadcastTicketRemoved(
                                ticketId,
                                QueueUpdateMessage.QueueType.RETRY);

                        // Add to main queue
                        String patientName = ticket.getCreatedBy() != null ? ticket.getCreatedBy().getFullName()
                                : "Unknown";
                        Integer position = getQueuePosition(ticketId);
                        webSocketController.broadcastTicketAdded(
                                ticketId,
                                QueueUpdateMessage.QueueType.QUEUE,
                                patientName,
                                ticket.getPriority().toString(),
                                position);
                    }

                    return updated;
                })
                .orElse(null);
    }

    /**
     * Get total queue count
     */
    @Transactional(readOnly = true)
    public long getQueueCount() {
        return getQueuedTickets().size();
    }

    /**
     * Get retry queue count
     */
    @Transactional(readOnly = true)
    public long getRetryCount() {
        return getRetryTickets().size();
    }
}
