package com.g4.capstoneproject.dto.Ticket;

import com.g4.capstoneproject.entity.Ticket;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating Ticket status
 * Used by doctors to change ticket status (IN_PROGRESS, RESOLVED, CLOSED)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketStatusUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private Ticket.Status status;

    private String note; // Optional note when changing status
}
