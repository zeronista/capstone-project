package com.g4.capstoneproject.config;

import com.g4.capstoneproject.controller.QueueWebSocketController;
import com.g4.capstoneproject.service.TicketService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to wire QueueWebSocketController into TicketService
 * This avoids circular dependency issues
 */
@Configuration
@RequiredArgsConstructor
public class WebSocketServiceConfig {

    private final TicketService ticketService;
    private final QueueWebSocketController webSocketController;

    @PostConstruct
    public void init() {
        // Wire the WebSocket controller into the TicketService
        ticketService.setWebSocketController(webSocketController);
    }
}
