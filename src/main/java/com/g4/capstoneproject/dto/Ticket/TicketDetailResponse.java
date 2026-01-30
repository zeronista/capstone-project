package com.g4.capstoneproject.dto.Ticket;

import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TicketMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Ticket Detail Response
 * Full response including messages for detail view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailResponse {

        private Long id;
        private String title;
        private String description;
        private Ticket.Priority priority;
        private Ticket.Category category;
        private Ticket.Status status;

        // Patient info
        private Long patientId;
        private String patientName;
        private String patientEmail;
        private String patientPhone;
        private String patientAvatar;

        // Assignment info
        private Long assignedToId;
        private String assignedToName;

        private Long createdById;
        private String createdByName;

        private Long resolvedById;
        private String resolvedByName;

        // Timestamps
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;

        // Additional info
        private Integer retryCount;
        private Long callLogId;

        // Messages
        private List<TicketMessageDTO> messages;

        /**
         * Inner DTO for Ticket Message
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class TicketMessageDTO {
                private Long id;
                private Long senderId;
                private String senderName;
                private String senderAvatar;
                private String messageText;
                private TicketMessage.MessageType messageType;
                private String attachmentUrl;
                private Boolean isInternalNote;
                private LocalDateTime createdAt;

                public static TicketMessageDTO fromEntity(TicketMessage message) {
                        return TicketMessageDTO.builder()
                                        .id(message.getId())
                                        .senderId(message.getSender() != null ? message.getSender().getId() : null)
                                        .senderName(message.getSender() != null
                                                        && message.getSender().getUserInfo() != null
                                                                        ? message.getSender().getUserInfo()
                                                                                        .getFullName()
                                                                        : "Unknown")
                                        .senderAvatar(message.getSender() != null
                                                        && message.getSender().getUserInfo() != null
                                                                        ? message.getSender().getUserInfo()
                                                                                        .getAvatarUrl()
                                                                        : null)
                                        .messageText(message.getMessageText())
                                        .messageType(message.getMessageType())
                                        .attachmentUrl(message.getAttachmentUrl())
                                        .isInternalNote(message.getIsInternalNote())
                                        .createdAt(message.getCreatedAt())
                                        .build();
                }
        }

        /**
         * Convert Ticket entity to TicketDetailResponse DTO
         */
        public static TicketDetailResponse fromEntity(Ticket ticket, List<TicketMessage> messages) {
                return TicketDetailResponse.builder()
                                .id(ticket.getId())
                                .title(ticket.getTitle())
                                .description(ticket.getDescription())
                                .priority(ticket.getPriority())
                                .category(ticket.getCategory())
                                .status(ticket.getStatus())
                                .patientId(ticket.getPatient() != null ? ticket.getPatient().getId() : null)
                                .patientName(ticket.getPatient() != null && ticket.getPatient().getUserInfo() != null
                                                ? ticket.getPatient().getUserInfo().getFullName()
                                                : null)
                                .patientEmail(ticket.getPatient() != null ? ticket.getPatient().getEmail() : null)
                                .patientPhone(ticket.getPatient() != null ? ticket.getPatient().getPhoneNumber() : null)
                                .patientAvatar(ticket.getPatient() != null && ticket.getPatient().getUserInfo() != null
                                                ? ticket.getPatient().getUserInfo().getAvatarUrl()
                                                : null)
                                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                                .assignedToName(ticket.getAssignedTo() != null
                                                && ticket.getAssignedTo().getUserInfo() != null
                                                                ? ticket.getAssignedTo().getUserInfo().getFullName()
                                                                : null)
                                .createdById(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null)
                                .createdByName(ticket.getCreatedBy() != null
                                                && ticket.getCreatedBy().getUserInfo() != null
                                                                ? ticket.getCreatedBy().getUserInfo().getFullName()
                                                                : null)
                                .resolvedById(ticket.getResolvedBy() != null ? ticket.getResolvedBy().getId() : null)
                                .resolvedByName(ticket.getResolvedBy() != null
                                                && ticket.getResolvedBy().getUserInfo() != null
                                                                ? ticket.getResolvedBy().getUserInfo().getFullName()
                                                                : null)
                                .createdAt(ticket.getCreatedAt())
                                .updatedAt(ticket.getUpdatedAt())
                                .resolvedAt(ticket.getResolvedAt())
                                .retryCount(ticket.getRetryCount())
                                .callLogId(ticket.getCallLog() != null ? ticket.getCallLog().getId() : null)
                                .messages(messages != null ? messages.stream()
                                                .map(TicketMessageDTO::fromEntity)
                                                .collect(Collectors.toList()) : null)
                                .build();
        }
}
