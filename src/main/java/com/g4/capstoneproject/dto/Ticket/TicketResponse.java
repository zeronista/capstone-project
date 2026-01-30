package com.g4.capstoneproject.dto.Ticket;

import com.g4.capstoneproject.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Ticket List Response
 * Lightweight response for listing tickets in table view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

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

    // Assignment info
    private Long assignedToId;
    private String assignedToName;

    private Long createdById;
    private String createdByName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    // Additional info
    private Integer retryCount;
    private Long callLogId;

    /**
     * Convert Ticket entity to TicketResponse DTO
     */
    public static TicketResponse fromEntity(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .status(ticket.getStatus())
                .patientId(ticket.getPatient() != null ? ticket.getPatient().getId() : null)
                .patientName(ticket.getPatient() != null ? ticket.getPatient().getUserInfo().getFullName() : null)
                .patientEmail(ticket.getPatient() != null ? ticket.getPatient().getEmail() : null)
                .patientPhone(ticket.getPatient() != null ? ticket.getPatient().getPhoneNumber() : null)
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(
                        ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUserInfo().getFullName() : null)
                .createdById(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null)
                .createdByName(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getUserInfo().getFullName() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .retryCount(ticket.getRetryCount())
                .callLogId(ticket.getCallLog() != null ? ticket.getCallLog().getId() : null)
                .build();
    }
}
