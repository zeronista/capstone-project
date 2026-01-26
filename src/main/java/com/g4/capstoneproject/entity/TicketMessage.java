package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity TicketMessage - Tin nhắn trong ticket (hội thoại bệnh nhân - bác sĩ)
 */
@Entity
@Table(name = "ticket_messages", indexes = {
    @Index(name = "idx_msg_ticket", columnList = "ticket_id"),
    @Index(name = "idx_msg_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private MessageType messageType = MessageType.TEXT;
    
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
    
    @Builder.Default
    @Column(name = "is_internal_note")
    private Boolean isInternalNote = false; // Ghi chú nội bộ (bác sĩ - lễ tân)
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Enum loại tin nhắn
     */
    public enum MessageType {
        TEXT,       // Văn bản
        FILE,       // File đính kèm
        SYSTEM      // Tin nhắn hệ thống
    }
}
