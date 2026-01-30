package com.g4.capstoneproject.dto.Ticket;

import com.g4.capstoneproject.entity.TicketMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating new Ticket Message
 * Used by doctors to reply to tickets
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessageRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(min = 1, max = 5000, message = "Nội dung tin nhắn phải từ 1 đến 5000 ký tự")
    private String messageText;

    @Builder.Default
    private TicketMessage.MessageType messageType = TicketMessage.MessageType.TEXT;

    private String attachmentUrl; // Optional file attachment

    @Builder.Default
    private Boolean isInternalNote = false; // True if this is internal note between staff
}
