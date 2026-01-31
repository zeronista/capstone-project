package com.g4.capstoneproject.dto.Ticket;

import com.g4.capstoneproject.entity.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Ticket Create/Update Request from Receptionist
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCreateRequest {

    @NotNull(message = "Vui lòng chọn bệnh nhân")
    private Long patientId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    private Ticket.Priority priority = Ticket.Priority.MEDIUM;

    private Ticket.Category category = Ticket.Category.OTHER;

    // Optional: Assign to a doctor immediately
    private Long assignedToId;
}
