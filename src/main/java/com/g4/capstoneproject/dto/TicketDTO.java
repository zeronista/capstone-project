package com.g4.capstoneproject.dto;

import com.g4.capstoneproject.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho Ticket
 * Chức năng: Truyền dữ liệu ticket mà không gây lazy loading exception
 * Cách hoạt động: Chỉ chứa các trường cần thiết, không có quan hệ entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    private String ticketNumber;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private Long assignedToId;
    private String assignedToName;
    private String subject;
    private String description;
    private String status;
    private String priority;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    /**
     * Chuyển đổi từ Entity sang DTO
     * Chức năng: Tạo DTO từ Ticket entity
     * Cách hoạt động: Lấy các trường cần thiết từ entity, tránh lazy loading
     */
    public static TicketDTO fromEntity(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .ticketNumber("TK-" + ticket.getId())
                .patientId(ticket.getPatient() != null ? ticket.getPatient().getId() : null)
                .patientName(ticket.getPatient() != null && ticket.getPatient().getUserInfo() != null 
                    ? ticket.getPatient().getUserInfo().getFullName() : null)
                .patientEmail(ticket.getPatient() != null ? ticket.getPatient().getEmail() : null)
                .patientPhone(ticket.getPatient() != null ? ticket.getPatient().getPhoneNumber() : null)
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null && ticket.getAssignedTo().getUserInfo() != null 
                    ? ticket.getAssignedTo().getUserInfo().getFullName() : null)
                .subject(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : null)
                .priority(ticket.getPriority() != null ? ticket.getPriority().name() : null)
                .category(ticket.getCategory() != null ? ticket.getCategory().name() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }
}
